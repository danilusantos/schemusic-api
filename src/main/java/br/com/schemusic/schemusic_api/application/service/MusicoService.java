package br.com.schemusic.schemusic_api.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.schemusic.schemusic_api.dto.request.MusicoRequest;
import br.com.schemusic.schemusic_api.dto.response.MusicoResponse;

public interface MusicoService {
    MusicoResponse getById(Long id);
    Page<MusicoResponse> list(Pageable pageable);
    MusicoResponse update(Long id, MusicoRequest request);
}
