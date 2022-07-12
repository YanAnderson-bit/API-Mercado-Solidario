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
import com.mercado_solidario.api.entity.Fornecedor;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.FornecedorRepository;
import com.mercado_solidario.api.service.FornecedorServices;

@RestController 
@RequestMapping(value = "/fornecedores")
public class FornecedorControler {

	@Autowired 
	private FornecedorRepository fornecedorRepository;
	
	@Autowired 
	private FornecedorServices fornecedorServices;
	
	@GetMapping
	public List<Fornecedor> listar() {
		return fornecedorRepository.findAll();
	}
	
	@GetMapping("/{fornecedorId}") // -> /fornecedores/fornecedoreId 
	public ResponseEntity<Fornecedor> buscar(@PathVariable("fornecedorId") Long Id) {
		Optional<Fornecedor> fornecedor = fornecedorRepository.findById(Id);
		
		if(fornecedor.isPresent()) {
			return ResponseEntity.ok(fornecedor.get());
		}
		
		return ResponseEntity.notFound().build();
	}
	
	//Busca por componenetes
	//  -> /fornecedores/por-nome?nome = nome_buscado
	@GetMapping("/por-nome")
	public List<Fornecedor> FornecedoesPorNome(String nome) { 
		return fornecedorRepository.findAllByNomeContains(nome);
	}
	@GetMapping("/por-cidade")
	public List<Fornecedor> FornecedoesPorCidade(String cidade) { 
		return fornecedorRepository.findAllByEndereçoCidadeNomeContains(cidade);
	}
	@GetMapping("/por-estado")
	public List<Fornecedor> FornecedoesPorEstado(String estado) { 
		return fornecedorRepository.findAllByEndereçoCidadeEstadoNomeContains(estado);
	}
	
	//Comando POST
	@PostMapping 
	@ResponseStatus(HttpStatus.CREATED)
	public Fornecedor adicionar(@RequestBody Fornecedor fornecedor) { 
		/*Modelo:
		{
		    "nome":"nome",
		    "endereço": {
		    	"id" = id
		    }
		}
		*/
		return fornecedorServices.salvar(fornecedor);
	}
		
	//Comandos PUT
	@PutMapping("/{fornecedorId}") 
	public ResponseEntity<?> atualizar(@PathVariable("fornecedorId") Long Id, @RequestBody Fornecedor fornecedor) {
		try {
			Optional<Fornecedor> fornecedorAtual = fornecedorRepository.findById(Id);
	
			if(fornecedorAtual.isPresent()) {
				BeanUtils.copyProperties(fornecedor, fornecedorAtual.get(), "id");
				Fornecedor fornecedorSalvo = fornecedorServices.salvar(fornecedorAtual.get());
				
				return ResponseEntity.ok(fornecedorSalvo);
			}
			
			return ResponseEntity.notFound().build();
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} 
		
	}
	
	@PatchMapping("/{fornecedorId}") 
	public ResponseEntity<?> atualizaParcial(@PathVariable("fornecedorId") Long Id, @RequestBody Map<String, Object> campos) {
		Optional<Fornecedor> fornecedor = fornecedorRepository.findById(Id);
	
		if(fornecedor.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		
		merge(campos, fornecedor.get());		
	
		return atualizar(Id,fornecedor.get());
	}
		
		
	private void merge(Map<String, Object> camposOrigem, Fornecedor fornecedorDestino) {
		ObjectMapper objectMapper = new ObjectMapper();
		Fornecedor fornecedorOrigem = objectMapper.convertValue(camposOrigem, Fornecedor.class);
		
		camposOrigem.forEach((nomePropiedade, valorPropiedade) -> {
			Field field = ReflectionUtils.findField(Fornecedor.class, nomePropiedade);
			field.setAccessible(true); 
			Object novoValor = ReflectionUtils.getField(field,fornecedorOrigem);
			
			ReflectionUtils.setField(field, fornecedorDestino, novoValor);
		});
	}
	
	//Comandos DELET
	@DeleteMapping("/{fornecedorId}") 
	public ResponseEntity<Fornecedor> remover(@PathVariable("fornecedorId") Long Id) {
		try {
			
			fornecedorServices.excluir(Id); 
			return ResponseEntity.noContent().build();
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.notFound().build();
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		
	}
	
}
