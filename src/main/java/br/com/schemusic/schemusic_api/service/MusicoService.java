package br.com.schemusic.schemusic_api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.schemusic.schemusic_api.dto.MusicoRequest;
import br.com.schemusic.schemusic_api.dto.MusicoResponse;

public interface MusicoService {
    MusicoResponse create(MusicoRequest request);
    MusicoResponse getById(Long id);
    Page<MusicoResponse> list(Pageable pageable);
    MusicoResponse update(Long id, MusicoRequest request);
    void delete(Long id);
}
