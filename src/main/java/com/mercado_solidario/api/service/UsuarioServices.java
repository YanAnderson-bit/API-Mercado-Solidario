package com.mercado_solidario.api.service;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mercado_solidario.api.entity.Grupo;
import com.mercado_solidario.api.entity.Usuario;
import com.mercado_solidario.api.execption.EntidadeEmUsoExeption;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.UsuarioRepository;

@Service
public class UsuarioServices {

	@Autowired 
	private UsuarioRepository usuarioRepository;
	
	//@Autowired 
	//private GrupoRepository grupoRepository;
	
	//@Autowired 
	//private EndereçoServices endereçoServices;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private GrupoServices grupoServices;
	
	public Usuario salvar(Usuario usuario) {
		
		
		Optional<Usuario> ExistingUser = usuarioRepository.findByEmail(usuario.getEmail());
	/*	Set<Grupo> idGrupo = usuario.getGrupo();	
		Grupo grupo = grupoRepository.findById(idGrupo)
				.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
						String.format("Não existe cadastro de grupo de código %d", idGrupo)));
		*/
	/*	Long idEndereço = usuario.getEndereço().getId();	
		Endereço endereço = endereçoRepository.findById(idEndereço)
				.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
						String.format("Não existe cadastro de endereço de código %d", idEndereço)));
		
		usuario.setEndereço(endereço);*/
		//usuario.setGrupo(grupo);
		if(ExistingUser.isPresent()) throw new EntidadeEmUsoExeption(
				String.format("Usuario com e-mail %s já cadastrado", usuario.getEmail())); 
		
		if(usuario.isNew()) {
			usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
			usuario.setNew(false);
		}
		
		return usuarioRepository.save(usuario);
	}
	

	public Usuario buscarOuFalhar(Long usuarioId) {
		return usuarioRepository.findById(usuarioId)
			.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
					String.format("Não existe cadastro de endereço de código %d", usuarioId)));
	}
	
	@Transactional
	public void alterarSenha(Long usuarioId, String senhaAtual, String novaSenha) throws Exception {
		Usuario usuario = buscarOuFalhar(usuarioId);
		
		if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
			throw new Exception("Senha atual informada não coincide com a senha do usuário.");
		}
		
		usuario.setSenha(passwordEncoder.encode(novaSenha));
	}

	@Transactional
	public void desassociarGrupo(Long usuarioId, Long grupoId) {
		Usuario usuario = buscarOuFalhar(usuarioId);
		Grupo grupo = grupoServices.buscarOuFalhar(grupoId);
		
		usuario.removeGrupo(grupo);
	}
	
	@Transactional
	public void associarGrupo(Long usuarioId, Long grupoId) {
		Usuario usuario = buscarOuFalhar(usuarioId);
		Grupo grupo = grupoServices.buscarOuFalhar(grupoId);
		
		usuario.adicionarGrupo(grupo);
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