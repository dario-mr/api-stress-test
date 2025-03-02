package com.dario.ast.view.component.save;

import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.core.service.AstStorageService;
import com.dario.ast.view.component.notification.ErrorNotification;
import com.dario.ast.view.component.notification.SuccessNotification;
import com.vaadin.flow.component.button.Button;

import java.util.function.Supplier;

import static com.dario.ast.util.EventUtil.updateRequestName;

public class SaveButton extends Button {

    private final AstStorageService astStorageService;
    private final Supplier<ConfigParams> configParamsSupplier;
    private final Supplier<RunParams> runParamsSupplier;

    public SaveButton(AstStorageService astStorageService,
                      Supplier<ConfigParams> configParamsSupplier,
                      Supplier<RunParams> runParamsSupplier) {
        this.astStorageService = astStorageService;
        this.configParamsSupplier = configParamsSupplier;
        this.runParamsSupplier = runParamsSupplier;

        setIcon(new SaveIcon());
        addClickListener(event -> save());
    }

    private void save() {
        try {
            var configParams = configParamsSupplier.get();
            var runParams = runParamsSupplier.get();

            astStorageService.updateAstRequest(new AstRequest(configParams, runParams));
            updateRequestName(configParams.getRequestName());

            SuccessNotification.show("Parameters saved");
        } catch (Exception e) {
            ErrorNotification.show("Error saving parameters");
            throw new RuntimeException(e);
        }
    }
}
