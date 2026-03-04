package br.com.schemusic.schemusic_api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.schemusic.schemusic_api.dto.MusicoRequest;
import br.com.schemusic.schemusic_api.dto.MusicoResponse;
import br.com.schemusic.schemusic_api.entity.Musico;
import br.com.schemusic.schemusic_api.exception.ResourceNotFoundException;
import br.com.schemusic.schemusic_api.repository.MusicoRepository;

@Service
public class MusicoServiceImpl implements MusicoService {

    private final MusicoRepository repo;

    public MusicoServiceImpl(MusicoRepository repo) {
        this.repo = repo;
    }

    @Override
    public MusicoResponse create(MusicoRequest request) {
        Musico m = Musico.builder()
                .nome(request.getNome())
                .estilo(request.getEstilo())
                .contato(request.getContato())
                .disponibilidade(request.getDisponibilidade())
                .build();
        Musico saved = repo.save(m);
        return toResponse(saved);
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

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("Músico não encontrado: " + id);
        repo.deleteById(id);
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
