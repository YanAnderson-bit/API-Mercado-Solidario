package com.mercado_solidario.api.controller;

import java.lang.reflect.Field;
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
import com.mercado_solidario.api.entity.Permissao;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.PermissaoRepository;
import com.mercado_solidario.api.service.PermissaoServices;

@RestController 
@RequestMapping(value = "/permissoes")
public class PermissaoControler {

	@Autowired
	private PermissaoRepository permissaoRepository;
	
	@Autowired
	private PermissaoServices permissaoServices;
	
	//Comando GET
	@GetMapping
	public List<Permissao> listar() {
		return permissaoRepository.findAll();
	}
	
	@GetMapping("/{permissaoId}") // -> /permissoes/permissaoId 
	public ResponseEntity<Permissao> buscar(@PathVariable("permissaoId") Long Id) {
		Optional<Permissao> permissao = permissaoRepository.findById(Id);
		
		if(permissao.isPresent()) {
			return ResponseEntity.ok(permissao.get());
		}
		
		return ResponseEntity.notFound().build();
	}
	
	//Busca por componenetes
	//  -> /permissoes/por-nome?nome = nome_buscado
	@GetMapping("/por-nome")
	public List<Permissao> PermissoesPorNome(String nome) { 
		return permissaoRepository.findAllByNomeContains(nome);
	}
	
//  -> /permissoes/descricao?descricao = descricao_buscada
	@GetMapping("/descricao")
	public List<Permissao> PermissoesPorDescricao(String descricao) { 
		return permissaoRepository.findAllByDescricaoContains(descricao);
	}
	
	//Comando POST
	@PostMapping 
	@ResponseStatus(HttpStatus.CREATED)
	public Permissao adicionar(@RequestBody Permissao permissao) { 
		return permissaoServices.salvar(permissao);
	}
	
	//Comandos PUT
	@PutMapping("/{permissaoId}") 
	public ResponseEntity<?> atualizar(@PathVariable("permissaoId") Long Id, @RequestBody Permissao permissao) {
		try {
			Optional<Permissao> permissaoAtual = permissaoRepository.findById(Id);
	
			if(permissaoAtual.isPresent()) {
				BeanUtils.copyProperties(permissao,permissaoAtual.get(), "id");
				Permissao permissaoSalvo = permissaoServices.salvar(permissaoAtual.get());
				
				return ResponseEntity.ok(permissaoSalvo);
			}
			
			return ResponseEntity.notFound().build();
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} 
		
	}
	
	
	//Comando PATCH
		@PatchMapping("/{permissaoId}") 
		public ResponseEntity<?> atualizaParcial(@PathVariable("permissaoId") Long Id, @RequestBody Map<String, Object> campos) {
			Optional<Permissao> marketPlace = permissaoRepository.findById(Id);
		
			if(marketPlace.isEmpty()) {
				return ResponseEntity.notFound().build();
			}
			
			merge(campos, marketPlace.get());		
			return atualizar(Id,marketPlace.get());
		}
		
		
		private void merge(Map<String, Object> camposOrigem, Permissao permissaoDestino) {
			ObjectMapper objectMapper = new ObjectMapper();
			Permissao permissaoOrigem = objectMapper.convertValue(camposOrigem, Permissao.class);
			
			camposOrigem.forEach((nomePropiedade, valorPropiedade) -> {
				Field field = ReflectionUtils.findField(Permissao.class, nomePropiedade);
				field.setAccessible(true); 
				Object novoValor = ReflectionUtils.getField(field,permissaoOrigem);
				
				ReflectionUtils.setField(field, permissaoDestino, novoValor);
			});
		}
	
	
	//Comandos DELET
	@DeleteMapping("/{permissaoId}") 
	public ResponseEntity<Permissao> remover(@PathVariable("permissaoId") Long Id) {
		try {
			
			permissaoServices.excluir(Id); 
			return ResponseEntity.noContent().build();

		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.notFound().build();
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
	}
}