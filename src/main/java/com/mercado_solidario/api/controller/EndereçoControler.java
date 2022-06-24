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
import com.mercado_solidario.api.entity.Endereço;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.EndereçoRepository;
import com.mercado_solidario.api.service.EndereçoServices;

@RestController 
@RequestMapping(value = "/endereços")
public class EndereçoControler {

	@Autowired
	private EndereçoRepository endereçoRepository;
	
	@Autowired
	private EndereçoServices endereçoServices;
	
	//Comando GET
	@GetMapping
	public List<Endereço> listar() {
		return endereçoRepository.findAll();
	}
	
	@GetMapping("/{endereçoId}") // -> /endereços/endereçoId 
	public ResponseEntity<Endereço> buscar(@PathVariable("endereçoId") Long Id) {
		Optional<Endereço> endereço = endereçoRepository.findById(Id);
		
		if(endereço.isPresent()) {
			return ResponseEntity.ok(endereço.get());
		}
		
		return ResponseEntity.notFound().build();
	}
	
	//  -> /endereços/por-cep?cep=cep_buscado
	@GetMapping("/por-cep")
	public List<Endereço> EndereçosPorCep(String cep) { 
		return endereçoRepository.findAllByCepContains(cep);
	}
	
	//  -> /endereços/por-numero?numero=numero_buscado
	@GetMapping("/por-numero")
	public List<Endereço> EndereçosPorNumero(Integer numero) { 
		return endereçoRepository.findAllByNumero(numero);
	}
	
	//  -> /endereços/por-complemento?complemento=complemento_buscado
	@GetMapping("/por-complemento")
	public List<Endereço> EndereçosPorComplemento(String complemento) { 
		return endereçoRepository.findAllByComplementoContains(complemento);
	}
	
	//  -> /endereços/por-bairro?bairro=bairro_buscado
	@GetMapping("/por-bairro")
	public List<Endereço> EndereçosPorBairro(String bairro) { 
		return endereçoRepository.findAllByBairroContains(bairro);
	}
	
	//  -> /endereços/por-nome-cidade?nome=nome_buscado
	@GetMapping("/por-nome-cidade")
	public List<Endereço> EndereçosPorNomeCidade(String nome) { 
		return endereçoRepository.findAllByCidadeNomeContains(nome);
	}
	
//  -> /endereços/por-nome-estado?nome=nome_buscado
	@GetMapping("/por-nome-estado")
	public List<Endereço> EndereçosPorNomeEstado(String nome) { 
		return endereçoRepository.findAllByCidadeEstadoNomeContains(nome);
	}
	
	//Comando POST
	@PostMapping 
	@ResponseStatus(HttpStatus.CREATED)
	public Endereço adicionar(@RequestBody Endereço endereço) { 
		/*Modelo:
		  {
		    "cep": cep,
		    "logadouro": logadouro,
		    "numero": numero,
		    "complemento": complemento,
		    "bairro": bairro,
		    "cidade": {
		        "id": id,
		    }
		}
		*/
		return endereçoServices.salvar(endereço);
	}

	//Comandos PUT
	@PutMapping("/{endereçoId}") 
	public ResponseEntity<?> atualizar(@PathVariable("endereçoId") Long Id, @RequestBody Endereço endereço) {
		/*Modelo:
		  {
		    "cep": cep,
		    "logadouro": logadouro,
		    "numero": numero,
		    "complemento": complemento,
		    "bairro": bairro,
		    "cidade": {
		        "id": id,
		    }
		}
		*/
		try {
			Optional<Endereço> endereçoAtual = endereçoRepository.findById(Id);
		
			if(endereçoAtual.isPresent()) {
				BeanUtils.copyProperties(endereço, endereçoAtual.get(), "id");
				Endereço endereçoSalvo = endereçoServices.salvar(endereçoAtual.get());
					
				return ResponseEntity.ok(endereçoSalvo);
			}
				
			return ResponseEntity.notFound().build();
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} 
			
	}
	
	//Comando PATCH
	@PatchMapping("/{endereçoId}") 
	public ResponseEntity<?> atualizaParcial(@PathVariable("endereçoId") Long Id, @RequestBody Map<String, Object> campos) {
		Optional<Endereço> endereço = endereçoRepository.findById(Id);
			
		if(endereço.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
				
		merge(campos, endereço.get());		
		return atualizar(Id,endereço.get());
	}
			
	private void merge(Map<String, Object> camposOrigem, Endereço endereçoDestino) {
		ObjectMapper objectMapper = new ObjectMapper();
		Endereço endereçoOrigem = objectMapper.convertValue(camposOrigem, Endereço.class);
				
		camposOrigem.forEach((nomePropiedade, valorPropiedade) -> {
			Field field = ReflectionUtils.findField(Endereço.class, nomePropiedade);
			field.setAccessible(true); 
			Object novoValor = ReflectionUtils.getField(field,endereçoOrigem);
					
			ReflectionUtils.setField(field, endereçoDestino, novoValor);
		});
	}
	
	//Comandos DELET
	@DeleteMapping("/{endereçoId}") 
	public ResponseEntity<Cidade> remover(@PathVariable("endereçoId") Long Id) {
		try {
				
			endereçoServices.excluir(Id); 
			return ResponseEntity.noContent().build();

		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.notFound().build();
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
			
	}
	
}
