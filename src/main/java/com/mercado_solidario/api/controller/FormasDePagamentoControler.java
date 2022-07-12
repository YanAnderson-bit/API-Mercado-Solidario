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
import com.mercado_solidario.api.entity.FormasDePagamento;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.FormasDePagamentoRepository;
import com.mercado_solidario.api.service.FormasDePagamentoServices;

@RestController 
@RequestMapping(value = "/formasDePagamento")
public class FormasDePagamentoControler {

	@Autowired
	private FormasDePagamentoRepository formasDePagamentoRepository;
	
	@Autowired
	private FormasDePagamentoServices formasDePagamentoServices;
	
	//Comando GET
	@GetMapping
	public List<FormasDePagamento> listar() {
		return formasDePagamentoRepository.findAll();
	}
	
	@GetMapping("/{formasDePagamentoId}") // -> /formasDePagamento/formasDePagamentoId 
	public ResponseEntity<FormasDePagamento> buscar(@PathVariable("formasDePagamentoId") Long Id) {
		Optional<FormasDePagamento> formasDePagamento = formasDePagamentoRepository.findById(Id);
		
		if(formasDePagamento.isPresent()) {
			return ResponseEntity.ok(formasDePagamento.get());
		}
		
		return ResponseEntity.notFound().build();
	}
	
	//Busca por componenetes
	//  -> /formasDePagamento/por-descricao?descrica = descricao_buscado
	@GetMapping("/por-descricao")
	public List<FormasDePagamento> FormasDePagamentoPorDescricao(String descricao) { 
		return formasDePagamentoRepository.findAllByDescricaoContains(descricao);
	}
	
	//Comando POST
	@PostMapping 
	@ResponseStatus(HttpStatus.CREATED)
	public FormasDePagamento adicionar(@RequestBody FormasDePagamento formasDePagamento) { 
		return formasDePagamentoServices.salvar(formasDePagamento);
	}
	
	//Comandos PUT
	@PutMapping("/{formasDePagamentoId}") 
	public ResponseEntity<?> atualizar(@PathVariable("formasDePagamentoId") Long Id, @RequestBody FormasDePagamento formasDePagamento) {
		try {
			Optional<FormasDePagamento> formasDePagamentoAtual = formasDePagamentoRepository.findById(Id);
	
			if(formasDePagamentoAtual.isPresent()) {
				BeanUtils.copyProperties(formasDePagamento, formasDePagamentoAtual.get(), "id");
				FormasDePagamento formasDePagamentoSalvo = formasDePagamentoServices.salvar(formasDePagamentoAtual.get());
				
				return ResponseEntity.ok(formasDePagamentoSalvo);
			}
			
			return ResponseEntity.notFound().build();
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} 
		
	}
	
	
	//Comando PATCH
		@PatchMapping("/{formasDePagamentoId}") 
		public ResponseEntity<?> atualizaParcial(@PathVariable("formasDePagamentoId") Long Id, @RequestBody Map<String, Object> campos) {
			Optional<FormasDePagamento> marketPlace = formasDePagamentoRepository.findById(Id);
		
			if(marketPlace.isEmpty()) {
				return ResponseEntity.notFound().build();
			}
			
			merge(campos, marketPlace.get());		
			return atualizar(Id,marketPlace.get());
		}
		
		
		private void merge(Map<String, Object> camposOrigem, FormasDePagamento formasDePagamentoDestino) {
			ObjectMapper objectMapper = new ObjectMapper();
			FormasDePagamento formasDePagamentoOrigem = objectMapper.convertValue(camposOrigem, FormasDePagamento.class);
			
			camposOrigem.forEach((nomePropiedade, valorPropiedade) -> {
				Field field = ReflectionUtils.findField(FormasDePagamento.class, nomePropiedade);
				field.setAccessible(true); 
				Object novoValor = ReflectionUtils.getField(field,formasDePagamentoOrigem);
				
				ReflectionUtils.setField(field, formasDePagamentoDestino, novoValor);
			});
		}
	
	
	//Comandos DELET
	@DeleteMapping("/{formasDePagamentoId}") 
	public ResponseEntity<FormasDePagamento> remover(@PathVariable("formasDePagamentoId") Long Id) {
		try {
			
			formasDePagamentoServices.excluir(Id); 
			return ResponseEntity.noContent().build();

		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.notFound().build();
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		
	}
}
