package com.dario.ast.util;

import com.vaadin.flow.server.VaadinServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PathResolver {

  /**
   * Build the external-facing path to redirect to, honoring:
   * <ol> 1. X-Forwarded-Prefix (from gateway) </ol>
   * <ol> 2. servlet context path </ol>
   * <ol> 3. the provided logical path (e.g. "/oauth2/authorization/google") </ol>
   */
  public static String resolve(HttpServletRequest request, String logicalPath) {
    var forwardedPrefix = normalizePrefix(request.getHeader("X-Forwarded-Prefix"));
    var contextPath = normalizePrefix(request.getContextPath());
    var path = normalizePath(logicalPath);

    return concatPaths(forwardedPrefix, contextPath, path);
  }

  /**
   * Vaadin convenience overload to pull the underlying HttpServletRequest.
   */
  public static String resolve(String logicalPath) {
    var request = VaadinServletRequest.getCurrent().getHttpServletRequest();
    return resolve(request, logicalPath);
  }

  // Normalize to ensure leading slash, no trailing slash (except root)
  private static String normalizePrefix(String prefix) {
    if (prefix == null || prefix.isBlank()) {
      return "";
    }

    var prefixTrim = prefix.trim();
    if (!prefixTrim.startsWith("/")) {
      prefixTrim = "/" + prefixTrim;
    }
    if (prefixTrim.endsWith("/")) {
      prefixTrim = prefixTrim.substring(0, prefixTrim.length() - 1);
    }

    return prefixTrim;
  }

  // Ensure path starts with slash, no trailing slash (unless it's just "/")
  private static String normalizePath(String path) {
    if (path == null || path.isBlank()) {
      return "";
    }

    var pathTrim = path.trim();
    if (!pathTrim.startsWith("/")) {
      pathTrim = "/" + pathTrim;
    }
    // keep trailing slash if it's just "/"
    if (pathTrim.length() > 1 && pathTrim.endsWith("/")) {
      pathTrim = pathTrim.substring(0, pathTrim.length() - 1);
    }

    return pathTrim;
  }

  private static String concatPaths(String... parts) {
    var sb = new StringBuilder();
    for (String part : parts) {
      if (part == null || part.isEmpty()) {
        continue;
      }

      if (sb.isEmpty()) {
        sb.append(part);
        continue;
      }

      // ensure single slash between
      if (sb.charAt(sb.length() - 1) == '/') {
        if (part.startsWith("/")) {
          sb.append(part.substring(1));
        } else {
          sb.append(part);
        }
      } else {
        if (part.startsWith("/")) {
          sb.append(part);
        } else {
          sb.append('/').append(part);
        }
      }
    }

    return sb.toString();
  }

}
