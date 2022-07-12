insert into estado (id, nome, sigla) values (1, 'Minas Gerais', 'MG');
insert into estado (id, nome, sigla) values (2, 'São Paulo', 'SP');
insert into estado (id, nome, sigla) values (3, 'Ceará', 'CE')

insert into cidade (id, nome, estado_id) values (1, 'Floresta', 1)
insert into cidade (id, nome, estado_id) values (2, 'Petorlina', 1)
insert into cidade (id, nome, estado_id) values (3, 'Juazeiro', 2)

insert into endereço (cep, logadouro, numero, complemento, bairro, cidade_id) values ( '52300-000', null,12,'casa','centro', 1)
insert into endereço (cep, logadouro, numero, complemento, bairro, cidade_id) values ( '62100-000', null,22,'casa','centro', 2)

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

insert into fornecedor (nome, endereço_id) values ("Jorge",1)
insert into fornecedor (nome, endereço_id) values ("Lory",2)