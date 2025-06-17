const {app, BrowserWindow} = require('electron');
const path = require('path');
const {spawn} = require('child_process');
const http = require('http');
const fs = require('fs');

const CONFIG = {
  appUrl: 'http://localhost:8083',
  javaPath: 'electron/jre/bin/java',
  jarPath: 'electron/app.jar',
  iconPath: 'electron/icon.icns',
  backendLogName: 'backend.log',
  frontendLogName: 'frontend.log',
  envFileName: '.api-stress-test.env',
  splashScreenPath: 'electron/splash.html'
};

const ROOT = app.isPackaged
    ? process.resourcesPath
    : path.dirname(__dirname);
const JAVA_PATH = path.join(ROOT, CONFIG.javaPath);
const JAR_PATH = path.join(ROOT, CONFIG.jarPath);
const ICON_PATH = path.join(ROOT, CONFIG.iconPath);
const BACKEND_LOG_FILE = fs.openSync(
    path.join(
        app.isPackaged ? app.getPath('logs') : path.join(ROOT, 'electron'),
        CONFIG.backendLogName
    ), 'a'
);
const FRONTEND_LOG_PATH = path.join(
    app.isPackaged ? app.getPath('logs') : path.join(ROOT, 'electron'),
    CONFIG.frontendLogName
);
const ENV = parseEnvFile(path.join(app.getPath('home'), CONFIG.envFileName));

let splashWindow, backendProcess;

app.on('ready', () => {
  redirectConsoleToFile();

  splashWindow = createSplashWindow();
  backendProcess = startBackend();

  waitForBackendReady(CONFIG.appUrl + "/actuator/health")
  .then(() => {
    splashWindow.close();
    createMainWindow();
  })
  .catch(err => {
    console.error('Backend failed to startup: ', err);
    app.quit();
  });
});

app.on('window-all-closed', () => {
  if (!backendProcess) {
    app.quit();
    return;
  }

  console.log('Sending SIGINT to backend...');
  backendProcess.kill('SIGINT');

  backendProcess.once('close', (code, sig) => {
    console.log(`Backend process closed → code=${code} sig=${sig}`);
    app.quit();
  });
});

function startBackend() {
  const javaArgs = [...toSystemProps(ENV), '-jar', JAR_PATH];
  console.log('Java command:', JAVA_PATH, maskSecrets(javaArgs).join(' '));

  return spawn(JAVA_PATH, javaArgs, {
    stdio: ['ignore', BACKEND_LOG_FILE, BACKEND_LOG_FILE]
  });
}

function createSplashWindow() {
  const window = new BrowserWindow({
    width: 400, height: 300,
    icon: ICON_PATH,
    frame: false, transparent: true, resizable: false, center: true, show: false
  });

  window.loadFile(path.join(ROOT, CONFIG.splashScreenPath));
  window.once('ready-to-show', () => window.show());

  return window;
}

function createMainWindow() {
  const window = new BrowserWindow({
    width: 1200, height: 800,
    icon: ICON_PATH,
    webPreferences: {
      contextIsolation: true,
      sandbox: true
    }
  });

  window.loadURL(CONFIG.appUrl);
  window.on('closed', () => console.log('User closed the main window'));
}

function waitForBackendReady(url, timeoutMs = CONFIG.waitTimeoutMs, intervalMs = CONFIG.waitIntervalMs) {
  return new Promise((resolve, reject) => {
    const start = Date.now();

    const check = () => {
      http.get(url, res => {
        res.statusCode === 200 ? resolve() : retry();
      }).on('error', retry);
    };

    const retry = () => {
      if (Date.now() - start > timeoutMs) {
        reject(new Error('Backend did not start in time.'));
      } else {
        setTimeout(check, intervalMs);
      }
    };

    check();
  });
}

function toSystemProps(obj) {
  return Object.entries(obj).map(([k, v]) => `-D${k}=${v}`);
}

function parseEnvFile(filePath) {
  if (!fs.existsSync(filePath)) {
    console.error(`Env file not found: ${filePath}`);
    return {};
  }

  return Object.fromEntries(
      fs.readFileSync(filePath, 'utf8')
      .split(/\r?\n/)
      .filter(line => line && !line.trim().startsWith('#') && line.includes('='))
      .map(line => {
        const [key, ...rest] = line.split('=');
        return [key.trim(), rest.join('=').trim()];
      })
  );
}

function redirectConsoleToFile() {
  const FRONTEND_LOG_STREAM = fs.createWriteStream(FRONTEND_LOG_PATH, {flags: 'a'});

  const write = stream => (...args) => {
    const timestamp = new Date().toISOString();
    const message = args.map(arg => typeof arg === 'string' ? arg : JSON.stringify(arg)).join(' ');
    FRONTEND_LOG_STREAM.write(`[${timestamp}] ${message}\n`);
    stream.apply(console, args);
  };

  console.log = write(console.log);
  console.error = write(console.error);
  console.warn = write(console.warn);
  console.info = write(console.info);
}

function maskSecrets(args) {
  return args.map(arg => {
    const match = arg.match(/^-D([^=]+)=(.+)$/);
    if (!match) {
      return arg;
    }

    const [, key, value] = match;
    return `-D${key}=${value.slice(0, 3)}***`;
  });
}
