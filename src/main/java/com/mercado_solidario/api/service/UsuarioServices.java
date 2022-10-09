package com.mercado_solidario.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.mercado_solidario.api.entity.Grupo;
import com.mercado_solidario.api.entity.Usuario;
import com.mercado_solidario.api.execption.EntidadeEmUsoExeption;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.GrupoRepository;
import com.mercado_solidario.api.repository.UsuarioRepository;

@Service
public class UsuarioServices {

	@Autowired 
	private UsuarioRepository usuarioRepository;
	
	@Autowired 
	private GrupoRepository grupoRepository;
	
//	@Autowired 
//	private EndereçoRepository endereçoRepository;
	
	public Usuario salvar(Usuario usuario) {
		Long idGrupo = usuario.getGrupo().getId();	
		Grupo grupo = grupoRepository.findById(idGrupo)
				.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
						String.format("Não existe cadastro de grupo de código %d", idGrupo)));
		
	/*	Long idEndereço = usuario.getEndereço().getId();	
		Endereço endereço = endereçoRepository.findById(idEndereço)
				.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
						String.format("Não existe cadastro de endereço de código %d", idEndereço)));
		
		usuario.setEndereço(endereço);*/
		usuario.setGrupo(grupo);
		
		return usuarioRepository.save(usuario);
	}
	
	public void excluir(Long Id){ 
		try {
			usuarioRepository.deleteById(Id);
		} catch (EmptyResultDataAccessException e) {
			throw new EntidadeNaoEncontradaExeption(
					String.format("Não existe usuario de código %d", Id));
		} catch (DataIntegrityViolationException e) {
			throw new EntidadeEmUsoExeption(
					String.format("Usuario de código %d não pode ser removido", Id));
		}
	}

}