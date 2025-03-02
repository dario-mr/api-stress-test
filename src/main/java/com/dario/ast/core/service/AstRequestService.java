package com.dario.ast.core.service;

import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.repository.AstRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AstRequestService {

    private final AstRequestRepository astRequestRepository;

    public List<AstRequest> getAstRequestsByEmail(String email) {
        return astRequestRepository.findByUserEmail(email);
    }

    public void updateAstRequest(AstRequest astRequest) {
        astRequestRepository.update(astRequest);
    }

}
