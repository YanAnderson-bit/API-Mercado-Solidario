package com.mercado_solidario.api.service;

//@Service
public class EndereçoServices {
/*
	@Autowired 
	private EndereçoRepository endereçoRepository;
	
	@Autowired 
	private CidadeRepository cidadeRepository;
	
	public Endereço salvar(Endereço endereço) {
		Long Id = endereço.getCidade().getId();	
		Cidade cidade = cidadeRepository.findById(Id)
				.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
						String.format("Não existe cadastro de didade de código %d", Id)));
		
		endereço.setCidade(cidade);
		
		return endereçoRepository.save(endereço);
	}
	
	public void excluir(Long Id){ 
		try {
			endereçoRepository.deleteById(Id);
		} catch (EmptyResultDataAccessException e) {
			throw new EntidadeNaoEncontradaExeption(
					String.format("Não existe cadastro da endereço de código %d", Id));
		} catch (DataIntegrityViolationException e) {
			throw new EntidadeEmUsoExeption(
					String.format("Endereço de código %d não pode ser removida por estar em uso", Id));
		}
	}
*/
}
