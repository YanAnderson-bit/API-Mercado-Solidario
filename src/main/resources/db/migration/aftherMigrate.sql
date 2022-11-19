insert into estado ( nome, sigla) values ( 'Minas Gerais', 'MG');
insert into estado ( nome, sigla) values ( 'São Paulo', 'SP');
insert into estado (nome, sigla) values ( 'Ceará', 'CE')

insert into cidade (nome, estado_id) values ( 'Floresta', 1)
insert into cidade (nome, estado_id) values ( 'Petorlina', 1)
insert into cidade (nome, estado_id) values ( 'Juazeiro', 2)

insert into permissao (nome, descricao) values ('permissao 1', "permissao teste 1")
insert into permissao (nome, descricao) values ('permissao 2', "permissao teste 2")
insert into permissao (nome, descricao) values ('permissao 3', "permissao teste 3")
insert into permissao (nome, descricao) values ('permissao 4', "permissao teste 4")

insert into grupo (nome) values ("Grupo 1")
insert into grupo (nome) values ("Grupo 2")

insert into grupo_permissao (grupo_id, permissao_id) values (1,1)
insert into grupo_permissao (grupo_id, permissao_id) values (1,2)
insert into grupo_permissao (grupo_id, permissao_id) values (2,2)
insert into grupo_permissao (grupo_id, permissao_id) values (2,4)

insert into formas_de_pagamento (descricao)  values ("pagamento formato 1")
insert into formas_de_pagamento (descricao)  values ("pasdasdada asasjdh")
insert into formas_de_pagamento (descricao)  values ("lorem ipsum")

insert into  market_place(nome,classificacao, taxa_frete, ativo, aberto, data_cadastro, data_atualizacao, endereco_cep, endereco_logadouro, endereco_numero, endereco_complemento, endereco_bairro, endereco_cidade) values  ("Feira tal 1","Organicos", 0.1, true, false,"2022-02-02","2022-02-02", '52300-000', null,156,'Edificio','centro', 1)

insert into fornecedor (nome, endereco_cep, endereco_logadouro, endereco_numero, endereco_complemento, endereco_bairro, endereco_cidade, market_place_id) values  ("Jorge", '52300-000', null,12,'casa','centro', 1, 1)
insert into fornecedor (nome, endereco_cep, endereco_logadouro, endereco_numero, endereco_complemento, endereco_bairro, endereco_cidade, market_place_id) values ("Lory",'62100-000', null,22,'casa','centro', 2, 1)

insert into usuario (nome, email, senha, data_cadastro, endereco_cep, endereco_logadouro, endereco_numero, endereco_complemento, endereco_bairro, endereco_cidade, grupo_id) values ("Leo","@Uno", "123", "2022-02-02",  '52300-000', null,12,'casa','centro', 1, 1)
insert into usuario (nome, email, senha, data_cadastro, endereco_cep, endereco_logadouro, endereco_numero, endereco_complemento, endereco_bairro, endereco_cidade, grupo_id) values ("Rorona","@Dos", "456", "2022-04-02", '62100-000', null,22,'casa','centro', 2, 2)

insert into produto (nome, descricao, preco, disponivel, natureza, origem, categoria, fornecedor_id) values ("Maçã", "Boa maçã", 10, true, "natural", "organico", "categoria 1", 1)
insert into produto (nome, descricao, preco, disponivel, natureza, origem, categoria, fornecedor_id) values ("laranja", "Boa laranja", 12, true, "natural", "organico", "categoria 2", 1)
insert into produto (nome, descricao, preco, disponivel, natureza, origem, categoria, fornecedor_id) values ("carne", "fresco", 14, false, "animal", "organico", "categoria 3", 2)
insert into produto (nome, descricao, preco, disponivel, natureza, origem, categoria, fornecedor_id) values ("Leite", "Leite de gado", 16, true, "natural", "organico", "categoria 4", 1)

insert into pedido (codigo, sub_total, taxa_frete, valor_total, data_criacao, status, usuario_id, market_place_id, endereco_cep, endereco_logadouro, endereco_numero, endereco_complemento, endereco_bairro, endereco_cidade) values ("123", 10, 0.1, 20, "2022-02-02", "Criado", 1, 1, '52300-000', null,12,'casa','centro', 1)
insert into pedido (codigo, sub_total, taxa_frete, valor_total, data_criacao, status, usuario_id, market_place_id, endereco_cep, endereco_logadouro, endereco_numero, endereco_complemento, endereco_bairro, endereco_cidade) values ("456", 20, 0.2, 40, "2022-04-04", "confirmado", 2, 1, '52300-000', null,12,'casa','centro', 1)
insert into pedido (codigo, sub_total, taxa_frete, valor_total, data_criacao, status, usuario_id, market_place_id, endereco_cep, endereco_logadouro, endereco_numero, endereco_complemento, endereco_bairro, endereco_cidade) values ("789", 30, 0.3, 60, "2022-06-06", "cançelado", 2, 1, '62100-000', null,22,'casa','centro', 2)
