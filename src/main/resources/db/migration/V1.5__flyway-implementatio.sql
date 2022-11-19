CREATE TABLE `estado` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nome` varchar(255) NOT NULL,
  `sigla` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `cidade` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nome` varchar(255) NOT NULL,
  `estado_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkworrwk40xj58kevvh3evi500` (`estado_id`),
  CONSTRAINT `FKkworrwk40xj58kevvh3evi500` FOREIGN KEY (`estado_id`) REFERENCES `estado` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `formas_de_pagamento` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `descricao` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `grupo` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nome` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `permissao` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `descricao` varchar(255) NOT NULL,
  `nome` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `grupo_permissao` (
  `grupo_id` bigint NOT NULL,
  `permissao_id` bigint NOT NULL,
  KEY `FKh21kiw0y0hxg6birmdf2ef6vy` (`permissao_id`),
  KEY `FKta4si8vh3f4jo3bsslvkscc2m` (`grupo_id`),
  CONSTRAINT `FKh21kiw0y0hxg6birmdf2ef6vy` FOREIGN KEY (`permissao_id`) REFERENCES `permissao` (`id`),
  CONSTRAINT `FKta4si8vh3f4jo3bsslvkscc2m` FOREIGN KEY (`grupo_id`) REFERENCES `grupo` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `market_place` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `aberto` bit(1) NOT NULL,
  `ativo` bit(1) NOT NULL,
  `classificacao` varchar(255) DEFAULT NULL,
  `data_atualizacao` datetime(6) NOT NULL,
  `data_cadastro` datetime(6) NOT NULL,
  `endereco_bairro` varchar(255) NOT NULL,
  `endereco_cep` varchar(255) NOT NULL,
  `endereco_complemento` varchar(255) NOT NULL,
  `endereco_logadouro` varchar(255) DEFAULT NULL,
  `endereco_numero` int NOT NULL,
  `nome` varchar(255) NOT NULL,
  `taxa_frete` decimal(19,2) NOT NULL,
  `endereco_cidade` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbd421apdswihh7fk4rwgxe2r2` (`endereco_cidade`),
  CONSTRAINT `FKbd421apdswihh7fk4rwgxe2r2` FOREIGN KEY (`endereco_cidade`) REFERENCES `cidade` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `market_place_formas_de_pagamento` (
  `market_place_id` bigint NOT NULL,
  `formas_de_pagamento_id` bigint NOT NULL,
  KEY `FK8iqm6itwkls9k0ddqdqhtxd6h` (`formas_de_pagamento_id`),
  KEY `FK3w38h5p119avqum7ntc3kt6el` (`market_place_id`),
  CONSTRAINT `FK3w38h5p119avqum7ntc3kt6el` FOREIGN KEY (`market_place_id`) REFERENCES `market_place` (`id`),
  CONSTRAINT `FK8iqm6itwkls9k0ddqdqhtxd6h` FOREIGN KEY (`formas_de_pagamento_id`) REFERENCES `formas_de_pagamento` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `usuario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `data_cadastro` datetime(6) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `endereco_bairro` varchar(255) NOT NULL,
  `endereco_cep` varchar(255) NOT NULL,
  `endereco_complemento` varchar(255) NOT NULL,
  `endereco_logadouro` varchar(255) DEFAULT NULL,
  `endereco_numero` int NOT NULL,
  `nome` varchar(255) NOT NULL,
  `senha` varchar(255) NOT NULL,
  `endereco_cidade` bigint NOT NULL,
  `grupo_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdub5p0jq4sjxmnyubvipuj3ib` (`endereco_cidade`),
  KEY `FKsb0gm9wgivb6lcp3sm5gh2vt7` (`grupo_id`),
  CONSTRAINT `FKdub5p0jq4sjxmnyubvipuj3ib` FOREIGN KEY (`endereco_cidade`) REFERENCES `cidade` (`id`),
  CONSTRAINT `FKsb0gm9wgivb6lcp3sm5gh2vt7` FOREIGN KEY (`grupo_id`) REFERENCES `grupo` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `pedido` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `codigo` varchar(255) NOT NULL,
  `data_cançelamento` datetime(6) DEFAULT NULL,
  `data_confirmação` datetime(6) DEFAULT NULL,
  `data_criacao` datetime(6) NOT NULL,
  `data_entrega` datetime(6) DEFAULT NULL,
  `endereco_bairro` varchar(255) NOT NULL,
  `endereco_cep` varchar(255) NOT NULL,
  `endereco_complemento` varchar(255) NOT NULL,
  `endereco_logadouro` varchar(255) DEFAULT NULL,
  `endereco_numero` int NOT NULL,
  `status` varchar(255) NOT NULL,
  `sub_total` decimal(19,2) NOT NULL,
  `taxa_frete` decimal(19,2) NOT NULL,
  `valor_total` decimal(19,2) NOT NULL,
  `endereco_cidade` bigint NOT NULL,
  `market_place_id` bigint DEFAULT NULL,
  `usuario_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKikshlbu2ogysi8rufcv9qbhn1` (`endereco_cidade`),
  KEY `FK94c6e8vcrck9eijexmihluq2o` (`market_place_id`),
  KEY `FK6uxomgomm93vg965o8brugt00` (`usuario_id`),
  CONSTRAINT `FK6uxomgomm93vg965o8brugt00` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FK94c6e8vcrck9eijexmihluq2o` FOREIGN KEY (`market_place_id`) REFERENCES `market_place` (`id`),
  CONSTRAINT `FKikshlbu2ogysi8rufcv9qbhn1` FOREIGN KEY (`endereco_cidade`) REFERENCES `cidade` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `fornecedor` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `endereco_bairro` varchar(255) NOT NULL,
  `endereco_cep` varchar(255) NOT NULL,
  `endereco_complemento` varchar(255) NOT NULL,
  `endereco_logadouro` varchar(255) DEFAULT NULL,
  `endereco_numero` int NOT NULL,
  `nome` varchar(255) NOT NULL,
  `endereco_cidade` bigint NOT NULL,
  `market_place_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKms4g68iiooykwl383vhrwuqm4` (`endereco_cidade`),
  KEY `FK57qlx0yqdgepyo5qq2ct07qv6` (`market_place_id`),
  CONSTRAINT `FK57qlx0yqdgepyo5qq2ct07qv6` FOREIGN KEY (`market_place_id`) REFERENCES `market_place` (`id`),
  CONSTRAINT `FKms4g68iiooykwl383vhrwuqm4` FOREIGN KEY (`endereco_cidade`) REFERENCES `cidade` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `produto` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `categoria` varchar(255) NOT NULL,
  `descricao` varchar(255) DEFAULT NULL,
  `disponivel` bit(1) NOT NULL,
  `foto_data` varchar(255) DEFAULT NULL,
  `frequencia_disponibilidade` varchar(255) DEFAULT NULL,
  `manuseio` varchar(255) DEFAULT NULL,
  `natureza` varchar(255) NOT NULL,
  `nome` varchar(255) NOT NULL,
  `origem` varchar(255) NOT NULL,
  `preco` decimal(19,2) NOT NULL,
  `quantidade` int DEFAULT NULL,
  `unidade` varchar(255) DEFAULT NULL,
  `fornecedor_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKo6c1dbi17sempey5dpnx6ovrj` (`fornecedor_id`),
  CONSTRAINT `FKo6c1dbi17sempey5dpnx6ovrj` FOREIGN KEY (`fornecedor_id`) REFERENCES `fornecedor` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `pedido_produto` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `observacao` varchar(255) DEFAULT NULL,
  `preco_total` decimal(19,2) NOT NULL,
  `quantidade` int NOT NULL,
  `pedido_id` bigint DEFAULT NULL,
  `produto_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcsbxw0y9i3wfmiupq9eqfpdtc` (`pedido_id`),
  KEY `FKf8l3k06bmjhdwd79t0ndcw7tt` (`produto_id`),
  CONSTRAINT `FKcsbxw0y9i3wfmiupq9eqfpdtc` FOREIGN KEY (`pedido_id`) REFERENCES `pedido` (`id`),
  CONSTRAINT `FKf8l3k06bmjhdwd79t0ndcw7tt` FOREIGN KEY (`produto_id`) REFERENCES `produto` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;