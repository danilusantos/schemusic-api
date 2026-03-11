package br.com.schemusic.schemusic_api.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.schemusic.schemusic_api.dto.request.EmpresaRequest;
import br.com.schemusic.schemusic_api.dto.response.EmpresaResponse;

public interface EmpresaService {
    EmpresaResponse getById(Long id);
    Page<EmpresaResponse> list(Pageable pageable);
    EmpresaResponse update(Long id, EmpresaRequest request);
}
