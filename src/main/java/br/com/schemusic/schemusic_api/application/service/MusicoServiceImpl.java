package br.com.schemusic.schemusic_api.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.schemusic.schemusic_api.adapter.out.respository.MusicoRepository;
import br.com.schemusic.schemusic_api.domain.entity.Musico;
import br.com.schemusic.schemusic_api.dto.request.MusicoRequest;
import br.com.schemusic.schemusic_api.dto.response.MusicoResponse;
import br.com.schemusic.schemusic_api.exception.ResourceNotFoundException;

@Service
public class MusicoServiceImpl implements MusicoService {

    private final MusicoRepository repo;

    public MusicoServiceImpl(MusicoRepository repo) {
        this.repo = repo;
    }

    @Override
    public MusicoResponse getById(Long id) {
        Musico m = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Músico não encontrado: " + id));
        return toResponse(m);
    }

    @Override
    public Page<MusicoResponse> list(Pageable pageable) {
        Page<Musico> page = repo.findAll(pageable);
        return page.map(this::toResponse);
    }

    @Override
    public MusicoResponse update(Long id, MusicoRequest request) {
        Musico m = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Músico não encontrado: " + id));
        m.setNome(request.getNome());
        m.setEstilo(request.getEstilo());
        m.setContato(request.getContato());
        m.setDisponibilidade(request.getDisponibilidade());
        Musico updated = repo.save(m);
        return toResponse(updated);
    }

    private MusicoResponse toResponse(Musico m) {
        return MusicoResponse.builder()
                .id(m.getId())
                .nome(m.getNome())
                .estilo(m.getEstilo())
                .contato(m.getContato())
                .disponibilidade(m.getDisponibilidade())
                .build();
    }
}
