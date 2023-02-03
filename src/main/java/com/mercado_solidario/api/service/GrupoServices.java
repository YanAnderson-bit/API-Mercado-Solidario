package com.mercado_solidario.api.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.mercado_solidario.api.entity.Grupo;
import com.mercado_solidario.api.entity.Permissao;
import com.mercado_solidario.api.execption.EntidadeEmUsoExeption;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.GrupoRepository;
import com.mercado_solidario.api.repository.PermissaoRepository;

@Service
public class GrupoServices {
	
	@Autowired 
	private GrupoRepository grupoRepository;
	
	@Autowired 
	private PermissaoRepository permissaoRepository;
	
	@Autowired
	private PermissaoServices permissaoServices;
	
	public Grupo salvar(Grupo grupo) { 
		List<Long> Id = new ArrayList<>();
		grupo.getPermissao().forEach(t -> Id.add(t.getId()));
		
		
		Set<Permissao> permissoes = new HashSet<>();
		for(Long id: Id) {
			Permissao permissao = permissaoRepository.findById(id)
					.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
							String.format("Não existe cadastro de permissão de código %d", Id)));
			permissoes.add(permissao);
		}
		grupo.setPermissao(permissoes);
		
		return grupoRepository.save(grupo);
	}
	
	@Transactional
	public void desassociarPermissao(Long grupoId, Long permissaoId) {
		Grupo grupo = buscarOuFalhar(grupoId);
		Permissao permissao = permissaoServices.buscarOuFalhar(permissaoId);
		
		grupo.removePermissao(permissao);
	}
	
	@Transactional
	public void associarPermissao(Long grupoId, Long permissaoId) {
		Grupo grupo = buscarOuFalhar(grupoId);
		Permissao permissao = permissaoServices.buscarOuFalhar(permissaoId);
		
		grupo.adicionarPermissao(permissao);
	}
	
	public Grupo buscarOuFalhar(Long grupoId) {
		return grupoRepository.findById(grupoId)
			.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
					String.format("Não existe cadastro da grupo de código %d", grupoId)));
	}
	
	
	public void excluir(Long Id){ 
		try {
			grupoRepository.deleteById(Id);
		} catch (EmptyResultDataAccessException e) {
			throw new EntidadeNaoEncontradaExeption(
					String.format("Não existe cadastro da grupo de código %d", Id));
		} catch (DataIntegrityViolationException e) {
			throw new EntidadeEmUsoExeption(
					String.format("Grupo de código %d não pode ser removida por estar em uso", Id));
		}
	}

}
