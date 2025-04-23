package com.dario.ast.view.component.common.reactive;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import java.lang.reflect.Method;
import java.util.ArrayList;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

// TODO improve / re-design
public interface ReactiveBinderSupport extends HasElement {

  default void bindReactiveSubscriptions(Object target, UI ui) {
    var subscriptions = new ArrayList<Disposable>();

    for (var method : target.getClass().getDeclaredMethods()) {
      if (method.isAnnotationPresent(ReactiveSubscription.class)) {
        method.setAccessible(true);
        try {
          var result = method.invoke(target);
          if (result instanceof Flux<?> flux) {
            var disposable = flux.subscribe(event -> {
              if (ui.isAttached()) {
                ui.access(() -> {
                  try {
                    var handler = findHandlerFor(target, event.getClass());
                    if (handler != null) {
                      handler.setAccessible(true);
                      handler.invoke(target, event);
                    }
                  } catch (Exception e) {
                    e.printStackTrace();
                  }
                });
              }
            });
            subscriptions.add(disposable);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    // Clean up on detach
    this.getElement().addDetachListener(e -> subscriptions.forEach(Disposable::dispose));
  }

  private Method findHandlerFor(Object target, Class<?> eventType) {
    for (var method : target.getClass().getDeclaredMethods()) {
      if (method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom(eventType)) {
        return method;
      }
    }

    return null;
  }

}
