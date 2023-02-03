insert into estado ( nome, sigla) values ( 'Minas Gerais', 'MG');
insert into estado ( nome, sigla) values ( 'São Paulo', 'SP');
insert into estado (nome, sigla) values ( 'Ceará', 'CE')

insert into cidade (nome, estado_id) values ( 'Floresta', 1)
insert into cidade (nome, estado_id) values ( 'Petorlina', 1)
insert into cidade (nome, estado_id) values ( 'Juazeiro', 2)

insert into permissao (id, nome, descricao) values (1,'GET_ALLOWED', "Permite a obtenção de dados de toda API")
insert into permissao (id, nome, descricao) values (2,'POST_PATCH_ALLOWED', "Permite a adição de novos itens no BD, fora usuario")
insert into permissao (id, nome, descricao) values (3,'ADMIN_USER', "Permissão para adicionar/remover usúarios")
insert into permissao (id, nome, descricao) values (4,'DELETE_ALLOWED', "Permissão para remover intesn do banco de dados")
insert into permissao (id, nome, descricao) values (5,'CREATE_PEDIDO', "Permissão para criar pedidos")

insert into grupo (id, nome) values (1,"Admin")
insert into grupo (id, nome) values (2,"Users")
insert into grupo (id, nome) values (3,"Visitors")

insert into grupo_permissao (grupo_id, permissao_id) values (1,1)
insert into grupo_permissao (grupo_id, permissao_id) values (1,2)
insert into grupo_permissao (grupo_id, permissao_id) values (1,3)
insert into grupo_permissao (grupo_id, permissao_id) values (1,4)
insert into grupo_permissao (grupo_id, permissao_id) values (1,5)
insert into grupo_permissao (grupo_id, permissao_id) values (2,1)
insert into grupo_permissao (grupo_id, permissao_id) values (2,2)
insert into grupo_permissao (grupo_id, permissao_id) values (2,5)
insert into grupo_permissao (grupo_id, permissao_id) values (3,1)
insert into grupo_permissao (grupo_id, permissao_id) values (3,5)

insert into formas_de_pagamento (descricao)  values ("pagamento formato 1")
insert into formas_de_pagamento (descricao)  values ("pasdasdada asasjdh")
insert into formas_de_pagamento (descricao)  values ("lorem ipsum")

insert into  market_place(nome,classificacao, taxa_frete, ativo, aberto, data_cadastro, data_atualizacao, endereco_cep, endereco_logadouro, endereco_numero, endereco_complemento, endereco_bairro, endereco_cidade) values  ("Feira tal 1","Organicos", 0.1, true, false,"2022-02-02","2022-02-02", '52300-000', null,156,'Edificio','centro', 1)

insert into fornecedor (nome, endereco_cep, endereco_logadouro, endereco_numero, endereco_complemento, endereco_bairro, endereco_cidade, market_place_id) values  ("Jorge", '52300-000', null,12,'casa','centro', 1, 1)
insert into fornecedor (nome, endereco_cep, endereco_logadouro, endereco_numero, endereco_complemento, endereco_bairro, endereco_cidade, market_place_id) values ("Lory",'62100-000', null,22,'casa','centro', 2, 1)

insert into usuario (nome, email, senha, data_cadastro, endereco_cep, endereco_logadouro, endereco_numero, endereco_complemento, endereco_bairro, endereco_cidade) values ("Leo","@Uno", "$2y$12$YRzSnhU9Afxu06xd1SxKKOjZqk60XNTBn1enySsoQphwpgak44zHG", "2022-02-02",  '52300-000', null,12,'casa','centro', 1)
insert into usuario (nome, email, senha, data_cadastro, endereco_cep, endereco_logadouro, endereco_numero, endereco_complemento, endereco_bairro, endereco_cidade) values ("Rorona","@Dos", "456", "2022-04-02", '62100-000', null,22,'casa','centro', 2)
insert into usuario (nome, email, senha, data_cadastro, endereco_cep, endereco_logadouro, endereco_numero, endereco_complemento, endereco_bairro, endereco_cidade) values ("Test","Test@test", "123456", "2022-04-02", '62100-000', null,22,'casa','centro', 2)

insert into user_grupo (user_id, grupo_id) values (1,1)
insert into user_grupo (user_id, grupo_id) values (2,2)
insert into user_grupo (user_id, grupo_id) values (3,3)

insert into produto (nome, descricao, preco, disponivel, natureza, origem, categoria, fornecedor_id) values ("Maçã", "Boa maçã", 10, true, "natural", "organico", "categoria 1", 1)
insert into produto (nome, descricao, preco, disponivel, natureza, origem, categoria, fornecedor_id) values ("laranja", "Boa laranja", 12, true, "natural", "organico", "categoria 2", 1)
insert into produto (nome, descricao, preco, disponivel, natureza, origem, categoria, fornecedor_id) values ("carne", "fresco", 14, false, "animal", "organico", "categoria 3", 2)
insert into produto (nome, descricao, preco, disponivel, natureza, origem, categoria, fornecedor_id) values ("Leite", "Leite de gado", 16, true, "natural", "organico", "categoria 4", 1)

insert into pedido (codigo, sub_total, taxa_frete, valor_total, data_criacao, status, usuario_id, market_place_id, endereco_cep, endereco_logadouro, endereco_numero, endereco_complemento, endereco_bairro, endereco_cidade) values ("123", 10, 0.1, 20, "2022-02-02", "Criado", 1, 1, '52300-000', null,12,'casa','centro', 1)
insert into pedido (codigo, sub_total, taxa_frete, valor_total, data_criacao, status, usuario_id, market_place_id, endereco_cep, endereco_logadouro, endereco_numero, endereco_complemento, endereco_bairro, endereco_cidade) values ("456", 20, 0.2, 40, "2022-04-04", "confirmado", 2, 1, '52300-000', null,12,'casa','centro', 1)
insert into pedido (codigo, sub_total, taxa_frete, valor_total, data_criacao, status, usuario_id, market_place_id, endereco_cep, endereco_logadouro, endereco_numero, endereco_complemento, endereco_bairro, endereco_cidade) values ("789", 30, 0.3, 60, "2022-06-06", "cançelado", 2, 1, '62100-000', null,22,'casa','centro', 2)
