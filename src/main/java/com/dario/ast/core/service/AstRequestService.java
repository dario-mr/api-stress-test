package com.dario.ast.core.service;

import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.repository.AstRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AstRequestService {

    private final AstRequestRepository astRequestRepository;

    public List<AstRequest> getByEmail(String email) {
        return astRequestRepository.findByUserEmail(email);
    }

    public Optional<AstRequest> getById(Long id) {
        return astRequestRepository.findById(id);
    }

    public void update(AstRequest astRequest) {
        astRequestRepository.update(astRequest);
    }

    public Long create(AstRequest astRequest) {
        return astRequestRepository.create(astRequest);
    }

    public void delete(long id) {
        astRequestRepository.delete(id);
    }

}
