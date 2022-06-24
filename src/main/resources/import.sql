insert into estado (id, nome, sigla) values (1, 'Minas Gerais', 'MG');
insert into estado (id, nome, sigla) values (2, 'São Paulo', 'SP');
insert into estado (id, nome, sigla) values (3, 'Ceará', 'CE')

insert into cidade (id, nome, estado_id) values (1, 'Floresta', 1)
insert into cidade (id, nome, estado_id) values (2, 'Petorlina', 1)
insert into cidade (id, nome, estado_id) values (3, 'Juazeiro', 2)

insert into endereço (cep, logadouro, numero, complemento, bairro, cidade_id) values ( '52300-000', null,12,'casa','centro', 1)
insert into endereço (cep, logadouro, numero, complemento, bairro, cidade_id) values ( '52300-000', null,12,'casa','centro', 1)