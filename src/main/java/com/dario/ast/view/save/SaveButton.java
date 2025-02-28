package com.dario.ast.view.save;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.core.domain.StressTestParams;
import com.dario.ast.core.service.ParamService;
import com.dario.ast.event.ConfigParamsResponseEvent;
import com.dario.ast.event.RunParamsResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;

import java.util.concurrent.CompletableFuture;

import static com.dario.ast.util.EventUtil.requestConfigParams;
import static com.dario.ast.util.EventUtil.requestRunParams;
import static com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER;
import static com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR;
import static com.vaadin.flow.component.notification.NotificationVariant.LUMO_SUCCESS;

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
                Notification.show("Error processing responses", 2_000, TOP_CENTER).addThemeVariants(LUMO_ERROR);
                throw new RuntimeException(e);
            }
        });
    }

    private void save(ConfigParams configParams, RunParams runParams) {
        try {
            paramService.saveParams(new StressTestParams(configParams, runParams));
            Notification.show("Parameters saved", 2_000, TOP_CENTER).addThemeVariants(LUMO_SUCCESS);
        } catch (JsonProcessingException e) {
            Notification.show("Error saving parameters", 2_000, TOP_CENTER).addThemeVariants(LUMO_ERROR);
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
