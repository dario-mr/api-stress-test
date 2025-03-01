package com.dario.ast.view.component.save;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.core.domain.StressTestParams;
import com.dario.ast.core.service.ParamService;
import com.dario.ast.event.ConfigParamsResponseEvent;
import com.dario.ast.event.RunParamsResponseEvent;
import com.dario.ast.view.component.notification.ErrorNotification;
import com.dario.ast.view.component.notification.SuccessNotification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;

import java.util.concurrent.CompletableFuture;

import static com.dario.ast.util.EventUtil.requestConfigParams;
import static com.dario.ast.util.EventUtil.requestRunParams;

public class SaveButton extends Button {

    private CompletableFuture<ConfigParams> configParamsResponse = new CompletableFuture<>();
    private CompletableFuture<RunParams> runParamsResponse = new CompletableFuture<>();

    private final ParamService paramService;

    public SaveButton(ParamService paramService) {
        this.paramService = paramService;

        setIcon(new SaveIcon());
        addClickListener(event -> requestParamsAndSave());
    }

    private void requestParamsAndSave() {
        // Reset previous responses
        configParamsResponse = new CompletableFuture<>();
        runParamsResponse = new CompletableFuture<>();

        requestConfigParams();
        requestRunParams();

        // Wait for both responses, then save
        CompletableFuture.allOf(configParamsResponse, runParamsResponse).thenRun(() -> {
            try {
                save(configParamsResponse.get(), runParamsResponse.get());
            } catch (Exception e) {
                ErrorNotification.show("Error processing responses");
                throw new RuntimeException(e);
            }
        });
    }

    private void save(ConfigParams configParams, RunParams runParams) {
        try {
            paramService.saveParams(new StressTestParams(configParams, runParams));
            SuccessNotification.show("Parameters saved");
        } catch (JsonProcessingException e) {
            ErrorNotification.show("Error saving parameters");
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // Listen for "config params response" event
        ComponentUtil.addListener(attachEvent.getUI(),
                ConfigParamsResponseEvent.class,
                event -> configParamsResponse.complete(event.getConfigParams())
        );

        // Listen for "run params response" event
        ComponentUtil.addListener(attachEvent.getUI(),
                RunParamsResponseEvent.class,
                event -> runParamsResponse.complete(event.getRunParams())
        );
    }
}
