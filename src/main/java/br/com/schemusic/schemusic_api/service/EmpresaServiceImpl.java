package br.com.schemusic.schemusic_api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.schemusic.schemusic_api.dto.EmpresaRequest;
import br.com.schemusic.schemusic_api.dto.EmpresaResponse;
import br.com.schemusic.schemusic_api.entity.Empresa;
import br.com.schemusic.schemusic_api.exception.ResourceNotFoundException;
import br.com.schemusic.schemusic_api.repository.EmpresaRepository;

@Service
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository repo;

    public EmpresaServiceImpl(EmpresaRepository repo) {
        this.repo = repo;
    }

    @Override
    public EmpresaResponse create(EmpresaRequest request) {
        if (request.getCnpj() != null && repo.existsByCnpj(request.getCnpj())) {
            throw new IllegalArgumentException("CNPJ já cadastrado");
        }
        Empresa e = Empresa.builder()
                .nome(request.getNome())
                .cnpj(request.getCnpj())
                .contato(request.getContato())
                .endereco(request.getEndereco())
                .build();
        Empresa saved = repo.save(e);
        return toResponse(saved);
    }

    @Override
    public EmpresaResponse getById(Long id) {
        Empresa e = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + id));
        return toResponse(e);
    }

    @Override
    public Page<EmpresaResponse> list(Pageable pageable) {
        Page<Empresa> page = repo.findAll(pageable);
        return page.map(this::toResponse);
    }

    @Override
    public EmpresaResponse update(Long id, EmpresaRequest request) {
        Empresa e = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + id));
        e.setNome(request.getNome());
        e.setCnpj(request.getCnpj());
        e.setContato(request.getContato());
        e.setEndereco(request.getEndereco());
        Empresa updated = repo.save(e);
        return toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("Empresa não encontrada: " + id);
        repo.deleteById(id);
    }

    private EmpresaResponse toResponse(Empresa e) {
        return EmpresaResponse.builder()
                .id(e.getId())
                .nome(e.getNome())
                .cnpj(e.getCnpj())
                .contato(e.getContato())
                .endereco(e.getEndereco())
                .build();
    }
}
