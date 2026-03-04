package br.com.schemusic.schemusic_api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.schemusic.schemusic_api.dto.EmpresaRequest;
import br.com.schemusic.schemusic_api.dto.EmpresaResponse;

public interface EmpresaService {
    EmpresaResponse create(EmpresaRequest request);
    EmpresaResponse getById(Long id);
    Page<EmpresaResponse> list(Pageable pageable);
    EmpresaResponse update(Long id, EmpresaRequest request);
    void delete(Long id);
}
