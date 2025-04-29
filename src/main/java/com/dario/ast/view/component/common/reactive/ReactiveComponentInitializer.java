package com.dario.ast.view.component.common.reactive;

import com.dario.ast.core.domain.AppState;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.Resource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

/**
 * Automatically binds reactive stream handlers to Vaadin components annotated with {@link ReactiveComponent}, using
 * methods marked with {@link ReactiveHandler}.
 */
@SpringComponent
public class ReactiveComponentInitializer implements VaadinServiceInitListener {

  @Resource
  private AppState appState;

  /**
   * Called once on application startup. Hooks into Vaadin's UI lifecycle.
   */
  @Override
  public void serviceInit(ServiceInitEvent event) {
    event.getSource().addUIInitListener(uiEvent -> {
      var ui = uiEvent.getUI();
      ui.addAfterNavigationListener(nav ->
          ui.getChildren().forEach(child -> bindHandlersRecursively(child, ui)));
    });
  }

  /**
   * Recursively traverses the component tree to bind reactive handlers.
   *
   * @param component The root component.
   * @param ui        The Vaadin UI instance.
   */
  private void bindHandlersRecursively(Component component, UI ui) {
    if (component.getClass().isAnnotationPresent(ReactiveComponent.class)) {
      bindReactiveHandlers(component, ui);
    }
    component.getChildren().forEach(child -> bindHandlersRecursively(child, ui));
  }

  /**
   * Binds all {@link ReactiveHandler}-annotated methods on the component to their corresponding Flux streams in
   * {@link AppState}.
   *
   * @param target The component with handler methods.
   * @param ui     The Vaadin UI context.
   */
  private void bindReactiveHandlers(Object target, UI ui) {
    var subscriptions = new ArrayList<Disposable>();
    var clazz = target.getClass();

    for (Method method : clazz.getDeclaredMethods()) {
      var annotation = method.getAnnotation(ReactiveHandler.class);
      if (annotation != null) {
        var type = annotation.value();
        method.setAccessible(true);
        var stream = getStreamForType(type);

        var disposable = stream.subscribe(event -> {
          if (ui.isAttached()) {
            ui.access(() -> {
              try {
                method.invoke(target, event);
              } catch (Exception e) {
                throw new RuntimeException("Reactive handler failed", e);
              }
            });
          }
        });
        subscriptions.add(disposable);
      }
    }

    // Dispose subscriptions on component detach
    if (target instanceof HasElement hasElement) {
      hasElement.getElement().addDetachListener(e -> subscriptions.forEach(Disposable::dispose));
    }
  }

  private Flux<?> getStreamForType(ReactiveType type) {
    var stream = resolveStream(type);
    if (stream == null) {
      throw new IllegalArgumentException("No stream defined for type: " + type);
    }
    return stream;
  }

  /**
   * Maps {@link ReactiveType} to AppState's reactive stream.
   *
   * @param type The enum value representing the stream.
   * @return A Flux for the given type.
   */
  private Flux<?> resolveStream(ReactiveType type) {
    return switch (type) {
      case REQUEST -> appState.getSelectedRequestStream();
      case ENVIRONMENT -> appState.getSelectedEnvironmentStream();
      case CONFIG_PARAMS_LIST -> appState.getPreRequestsParamsStream();
      case ENVIRONMENTS_LIST -> appState.getEnvironmentsStream();
    };
  }

}
