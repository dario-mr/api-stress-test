package com.dario.ast.view.component.save;

import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.core.service.AstRequestService;
import com.dario.ast.view.component.notification.ErrorNotification;
import com.dario.ast.view.component.notification.SuccessNotification;
import com.dario.ast.view.component.notification.WarnNotification;
import com.vaadin.flow.component.button.Button;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

import static com.dario.ast.util.EventUtil.asrRequestUpdated;
import static org.springframework.util.StringUtils.hasText;

@Slf4j
public class SaveButton extends Button {

    private final AstRequestService astRequestService;
    private final Supplier<ConfigParams> configParamsSupplier;
    private final Supplier<RunParams> runParamsSupplier;

    public SaveButton(AstRequestService astRequestService,
                      Supplier<ConfigParams> configParamsSupplier,
                      Supplier<RunParams> runParamsSupplier) {
        this.astRequestService = astRequestService;
        this.configParamsSupplier = configParamsSupplier;
        this.runParamsSupplier = runParamsSupplier;

        setIcon(new SaveIcon());
        addClickListener(event -> save());
    }

    private void save() {
        var configParams = configParamsSupplier.get();
        var runParams = runParamsSupplier.get();
        var asrRequest = new AstRequest(configParams, runParams);

        // validate
        if (!hasText(configParams.getRequestName())) {
            WarnNotification.show("Please provide a request name");
            return;
        }
        if (!hasText(configParams.getUri())) {
            WarnNotification.show("Please provide a valid URL");
            return;
        }

        try {
            astRequestService.update(asrRequest);
        } catch (Exception ex) {
            log.error("Error saving parameters", ex);
            ErrorNotification.show("Error saving parameters");
            throw new RuntimeException(ex);
        }

        asrRequestUpdated(asrRequest);
        SuccessNotification.show("Parameters saved");
    }
}
