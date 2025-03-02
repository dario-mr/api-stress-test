package com.dario.ast.view.component.sidebar;

import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.core.domain.User;
import com.dario.ast.core.service.AstRequestService;
import com.dario.ast.view.component.notification.ErrorNotification;
import com.vaadin.flow.component.button.Button;
import lombok.extern.slf4j.Slf4j;

import static com.dario.ast.util.EventUtil.asrRequestCreated;
import static org.springframework.http.HttpMethod.GET;

@Slf4j
public class CreateButton extends Button {

    private final AstRequestService astRequestService;

    public CreateButton(AstRequestService astRequestService) {
        this.astRequestService = astRequestService;

        setHeightFull();
        addClassName("create-button");
        setText("+");

        addClickListener(event -> createAsrRequest());
    }

    private void createAsrRequest() {
        var configParams = defaultConfigParams();
        var runParams = defaultRunParams();
        var astRequest = new AstRequest(configParams, runParams);

        try {
            var astRequestId = astRequestService.createAstRequest(astRequest);
            asrRequestCreated(astRequestId);
            // TODO update sidebar and select newly created request, possibly pass entity ID in the event for a light query
        } catch (Exception ex) {
            log.error("Error creating new Request", ex);
            ErrorNotification.show("Error creating Request");
            throw new RuntimeException(ex);
        }
    }

    private static ConfigParams defaultConfigParams() {
        return ConfigParams.builder()
                .requestName("New Request")
                .user(User.builder().id(1L).build()) // TODO use ID of logged user!
                .uri("")
                .method(GET)
                .requestBody("")
                .build();
    }

    private static RunParams defaultRunParams() {
        return RunParams.builder()
                .numRequests(1)
                .threadPoolSize(1)
                .stopOnError(true)
                .build();
    }
}