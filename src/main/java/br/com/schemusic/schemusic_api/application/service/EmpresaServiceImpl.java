package br.com.schemusic.schemusic_api.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.schemusic.schemusic_api.adapter.out.respository.EmpresaRepository;
import br.com.schemusic.schemusic_api.domain.entity.Empresa;
import br.com.schemusic.schemusic_api.dto.request.EmpresaRequest;
import br.com.schemusic.schemusic_api.dto.response.EmpresaResponse;
import br.com.schemusic.schemusic_api.exception.ResourceNotFoundException;

@Service
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository repo;

    public EmpresaServiceImpl(EmpresaRepository repo) {
        this.repo = repo;
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
