package com.mercado_solidario.api.controller;

import java.lang.reflect.Field;
import java.math.BigDecimal;
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
import com.mercado_solidario.api.entity.Produto;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.ProdutoRepository;
import com.mercado_solidario.api.service.ProdutoServices;

@RestController 
@RequestMapping(value = "/produtos")
public class ProdutoControler {

	@Autowired
	private ProdutoRepository produtoRepository;
	
	@Autowired
	private ProdutoServices produtoServices;
	
	//Comando GET
	@GetMapping
	public List<Produto> listar() {
		return produtoRepository.findAll();
	}
	
	@GetMapping("/{produtoId}") // -> /produtos/prdutoId 
	public ResponseEntity<Produto> buscar(@PathVariable("produtoId") Long Id) {
		Optional<Produto> produto = produtoRepository.findById(Id);
		
		if(produto.isPresent()) {
			return ResponseEntity.ok(produto.get());
		}
		
		return ResponseEntity.notFound().build();
	}
	
    //  -> /produtos/por-nome?nome=nome_buscado
	@GetMapping("/fornecedores/{produtoId}")
	public ResponseEntity<List<Fornecedor>> FornecedoresPorProduto(@PathVariable("produtoId") Long Id) { 
		Optional<Produto> produto = produtoRepository.findById(Id);
		
		if(produto.isPresent()) {
			return ResponseEntity.ok(produto.get().getFornecedores());
		}
		
		return ResponseEntity.notFound().build();
	}
	
	//  -> /produtos/por-nome?nome=nome_buscado
	@GetMapping("/por-nome")
	public List<Produto> ProdutoPorNome(String nome) { 
		return produtoRepository.findAllByNomeContains(nome);
	}
	
	//  -> /produtos/por-descricao?descricao=descricao_buscado
	@GetMapping("/por-descricao")
	public List<Produto> ProdutoPorDescricao(String descricao) { 
		return produtoRepository.findAllByDescricaoContains(descricao);
	}
	
	//  -> /produtos/por-preco-menor?preco=preco_buscado
	@GetMapping("/por-preco-menor")
	public List<Produto> ProdutoMenoresQuePreco(BigDecimal preco) { 
		return produtoRepository.findAllByPrecoLessThanEqual(preco);
	}
	//  -> /produtos/por-preco-maior?preco=preco_buscado
	@GetMapping("/por-preco-maior")
	public List<Produto> ProdutoMaioresQuePreco(BigDecimal preco) { 
		return produtoRepository.findAllByPrecoGreaterThanEqual(preco);
	}
	
	//  -> /produtos/por-entre-precos?preco=preco_buscado
	@GetMapping("/por-entre-precos")
	public List<Produto> ProdutoEntrePrecos(BigDecimal menorPreco, BigDecimal maiorPreco) { 
		return produtoRepository.findAllByPrecoBetween(menorPreco, maiorPreco);
	}
	
	@GetMapping("/por-disponivel")
	public List<Produto> ProdutosDisponiveis() { 
		return produtoRepository.findAllByDisponivelEquals(true);
	}
	
	@GetMapping("/por-indisponivel")
	public List<Produto> ProdutosIndisponiveis() { 
		return produtoRepository.findAllByDisponivelEquals(false);
	}
	
	//  -> /produtos/por-natureza?natureza=natureza_buscado
	@GetMapping("/por-natureza")
	public List<Produto> ProdutoPorNatureza(String natureza) { 
		return produtoRepository.findAllByNaturezaContains(natureza);
	}
	
//  -> /produtos/por-origem?origem=origem_buscado
	@GetMapping("/por-origem")
	public List<Produto> ProdutoPorOrigem(String origem) { 
		return produtoRepository.findAllByOrigemContains(origem);
	}
	
//  -> /produtos/por-categoria?categoria=categoria_buscado
	@GetMapping("/por-categoria")
	public List<Produto> ProdutoPorCategoria(String categoria) { 
		return produtoRepository.findAllByCategoriaContains(categoria);
	}
	
	//Comando POST
	@PostMapping 
	@ResponseStatus(HttpStatus.CREATED)
	public Produto adicionar(@RequestBody Produto produto) { 
		/*Modelo:
		  {
		    "nome": nome,
		    "descricao": descricao,
		    "preco": preco,
		    "ativo": ativo,
		    "natureza": natureza,
		    "origem": origem,
		    "categoria": categoria 
		}
		*/
		return produtoServices.salvar(produto);
	}

	//Comandos PUT
	@PutMapping("/{produtoId}") 
	public ResponseEntity<?> atualizar(@PathVariable("produtoId") Long Id, @RequestBody Produto produto) {
		/*Modelo:
		  {
		    "nome": nome,
		    "descricao": descricao,
		    "preco": preco,
		    "ativo": ativo,
		    "natureza": natureza,
		    "origem": origem,
		    "categoria": categoria 
		}
		*/
		try {
			Optional<Produto> produtoAtual = produtoRepository.findById(Id);
		
			if(produtoAtual.isPresent()) {
				BeanUtils.copyProperties(produto, produtoAtual.get(), "id");
				Produto produtoSalvo = produtoServices.salvar(produtoAtual.get());
					
				return ResponseEntity.ok(produtoSalvo);
			}
				
			return ResponseEntity.notFound().build();
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} 
			
	}
	
	//Comando PATCH
	@PatchMapping("/{produtoId}") 
	public ResponseEntity<?> atualizaParcial(@PathVariable("produtoId") Long Id, @RequestBody Map<String, Object> campos) {
		Optional<Produto> produto = produtoRepository.findById(Id);
			
		if(produto.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
				
		merge(campos, produto.get());		
		return atualizar(Id,produto.get());
	}
			
	private void merge(Map<String, Object> camposOrigem, Produto produtoDestino) {
		ObjectMapper objectMapper = new ObjectMapper();
		Produto produtoOrigem = objectMapper.convertValue(camposOrigem, Produto.class);
				
		camposOrigem.forEach((nomePropiedade, valorPropiedade) -> {
			Field field = ReflectionUtils.findField(Produto.class, nomePropiedade);
			field.setAccessible(true); 
			Object novoValor = ReflectionUtils.getField(field,produtoOrigem);
					
			ReflectionUtils.setField(field, produtoDestino, novoValor);
		});
	}
	
	//Comandos DELET
	@DeleteMapping("/{produtoId}") 
	public ResponseEntity<Produto> remover(@PathVariable("produtoId") Long Id) {
		try {
				
			produtoServices.excluir(Id); 
			return ResponseEntity.noContent().build();

		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.notFound().build();
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
			
	}
	
}