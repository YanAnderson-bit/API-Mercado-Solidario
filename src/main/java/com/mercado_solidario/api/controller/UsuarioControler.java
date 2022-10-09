package com.mercado_solidario.api.controller;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercado_solidario.api.entity.Usuario;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.UsuarioRepository;
import com.mercado_solidario.api.service.UsuarioServices;

@RestController 
@RequestMapping(value = "/usuarios")
public class UsuarioControler {
	
	@Autowired 
	private UsuarioRepository usuarioRepository;
	
	@Autowired 
	private UsuarioServices usuarioServices;
	
	@GetMapping
	public List<Usuario> listar() {
		return usuarioRepository.findAll();
	}
	
	@GetMapping("/{usuarioId}") // -> /usuarios/usuarioId 
	public ResponseEntity<Usuario> buscar(@PathVariable("usuarioId") Long Id) {
		Optional<Usuario> usuario = usuarioRepository.findById(Id);
		
		if(usuario.isPresent()) {
			return ResponseEntity.ok(usuario.get());
		}
		
		return ResponseEntity.notFound().build();
	}
	
	//Busca por componenetes
	//  -> /usuarios/por-nome?nome = nome_buscado
	@GetMapping("/por-nome")
	public List<Usuario> UsuariosPorNome(String nome) { 
		return usuarioRepository.findAllByNomeContains(nome);
	}
	@GetMapping("/por-email")
	public List<Usuario> UsuariosPorEmail(String email) { 
		return usuarioRepository.findAllByEmailContains(email);
	}
	@GetMapping("/por-data-cadastro")
	public List<Usuario> UsuariosPorDataCadastro(Date data) { 
		return usuarioRepository.findAllByDataCadastroGreaterThanEqual(data);
	}
	
	@GetMapping("/por-cidade")
	public List<Usuario> UsuariosPorCidade(String cidade) { 
		return usuarioRepository.findAllByEndereçoCidadeNomeContains(cidade);
	}
	@GetMapping("/por-estado")
	public List<Usuario> UsuariosPorEstado(String estado) { 
		return usuarioRepository.findAllByEndereçoCidadeEstadoNomeContains(estado);
	}
	
	//Comando POST
	@PostMapping 
	@ResponseStatus(HttpStatus.CREATED)
	public Usuario adicionar(@RequestBody Usuario usuario) { 
		/*Modelo:
		{
		    "nome":"nome",
		    "email":"mail",
		    "senha":"*****",
		    "endereço": {
		    	"id": id
		    },
		    "grupo": {
		    	"id": id
		    }
		}
		*/
		usuario.setDataCadastro(Date.from(Instant.now()));
		return usuarioServices.salvar(usuario);
	}
	
	//Comandos PUT
	@PutMapping("/{usuarioId}") 
	public ResponseEntity<?> atualizar(@PathVariable("usuarioId") Long Id, @RequestBody Usuario usuario) {
		try {
			Optional<Usuario> usuarioAtual = usuarioRepository.findById(Id);
			
			Date cadastro = usuarioAtual.get().getDataCadastro();
			usuario.setDataCadastro(cadastro);
	
			if(usuarioAtual.isPresent()) {
				BeanUtils.copyProperties(usuario, usuarioAtual.get(), "id", "endereço");
				Usuario usuarioSalvo = usuarioServices.salvar(usuarioAtual.get());
				
				
				return ResponseEntity.ok(usuarioSalvo);
			}
			
			return ResponseEntity.notFound().build();
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} 
		
	}
	//////////////////////////////////

	@PatchMapping("/{usuarioId}") 
	public ResponseEntity<?> atualizaParcial(@PathVariable("usuarioId") Long Id, @RequestBody Map<String, Object> campos) {
		Optional<Usuario> usuario = usuarioRepository.findById(Id);
	
		if(usuario.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		
		merge(campos, usuario.get());		
	
		return atualizar(Id,usuario.get());
	}
		
		
	private void merge(Map<String, Object> camposOrigem, Usuario usuarioDestino) {
		ObjectMapper objectMapper = new ObjectMapper();
		Usuario usuarioOrigem = objectMapper.convertValue(camposOrigem, Usuario.class);
		
		camposOrigem.forEach((nomePropiedade, valorPropiedade) -> {
			Field field = ReflectionUtils.findField(Usuario.class, nomePropiedade);
			field.setAccessible(true); 
			Object novoValor = ReflectionUtils.getField(field,usuarioOrigem);
			
			ReflectionUtils.setField(field, usuarioDestino, novoValor);
		});
	}
	
	//Comandos DELET
	@DeleteMapping("/{usuarioId}") 
	public ResponseEntity<Usuario> remover(@PathVariable("usuarioId") Long Id) {
		try {
			
			usuarioServices.excluir(Id); 
			return ResponseEntity.noContent().build();
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.notFound().build();
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		
	}
}
