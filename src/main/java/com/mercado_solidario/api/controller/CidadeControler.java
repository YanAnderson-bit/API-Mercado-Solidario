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
import com.mercado_solidario.api.entity.Cidade;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.CidadeRepository;
import com.mercado_solidario.api.service.CidadeServices;

@RestController 
@RequestMapping(value = "/cidades")
public class CidadeControler {

	@Autowired
	private CidadeRepository cidadeRepository;
	
	@Autowired
	private CidadeServices cidadeServices;
	
	//Comando GET
	@GetMapping
	public List<Cidade> listar() {
		return cidadeRepository.findAll();
	}
	
	@GetMapping("/{cidadeId}") // -> /cidades/xidadeId 
	public ResponseEntity<Cidade> buscar(@PathVariable("cidadeId") Long Id) {
		Optional<Cidade> cidade = cidadeRepository.findById(Id);
		
		if(cidade.isPresent()) {
			return ResponseEntity.ok(cidade.get());
		}
		
		return ResponseEntity.notFound().build();
	}
	
	//Busca por componenetes
	//  -> /cidades/por-nome?nome = nome_buscado
	@GetMapping("/por-nome")
	public List<Cidade> CidadesPorNome(String nome) { 
		return cidadeRepository.findAllByNomeContains(nome);
	}
	
	//  -> /cidades/estado-nome?nome = nome_buscado
	@GetMapping("/estado-nome")
	public List<Cidade> CidadesPorsEstado(String nome) { 
		return cidadeRepository.findAllByEstadoNomeContains(nome);
	}

	
	//Comando POST
	@PostMapping 
	@ResponseStatus(HttpStatus.CREATED)
	public Cidade adicionar(@RequestBody Cidade cidade) { 
		/*Modelo:
		{
		    "nome":"nome",
		    "estado":{
		    	"id" = id
		    }
		}
		*/
		return cidadeServices.salvar(cidade);
	}
	
	//Comandos PUT
	@PutMapping("/{estadoId}") 
	public ResponseEntity<?> atualizar(@PathVariable("estadoId") Long Id, @RequestBody Cidade cidade) {
		try {
			Optional<Cidade> cidadeAtual = cidadeRepository.findById(Id);
	
			if(cidadeAtual.isPresent()) {
				BeanUtils.copyProperties(cidade, cidadeAtual.get(), "id");
				Cidade cidadeSalvo = cidadeServices.salvar(cidadeAtual.get());
				
				return ResponseEntity.ok(cidadeSalvo);
			}
			
			return ResponseEntity.notFound().build();
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} 
		
	}
	
	//Comando PATCH
		@PatchMapping("/{cidadeId}") 
		public ResponseEntity<?> atualizaParcial(@PathVariable("cidadeId") Long Id, @RequestBody Map<String, Object> campos) {
			Optional<Cidade> cidade = cidadeRepository.findById(Id);
		
			if(cidade.isEmpty()) {
				return ResponseEntity.notFound().build();
			}
			
			merge(campos, cidade.get());		
		
			return atualizar(Id,cidade.get());
		}
		
		
		private void merge(Map<String, Object> camposOrigem, Cidade cidadeDestino) {
			ObjectMapper objectMapper = new ObjectMapper();
			Cidade cidadeOrigem = objectMapper.convertValue(camposOrigem, Cidade.class);
			
			camposOrigem.forEach((nomePropiedade, valorPropiedade) -> {
				Field field = ReflectionUtils.findField(Cidade.class, nomePropiedade);
				field.setAccessible(true); 
				Object novoValor = ReflectionUtils.getField(field,cidadeOrigem);
				
				ReflectionUtils.setField(field, cidadeDestino, novoValor);
			});
		}
	
	
	//Comandos DELET
	@DeleteMapping("/{cidadeId}") 
	public ResponseEntity<Cidade> remover(@PathVariable("cidadeId") Long Id) {
		try {
			
			cidadeServices.excluir(Id); 
			return ResponseEntity.noContent().build();

		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.notFound().build();
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		
	}
}
