package com.mercado_solidario.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.mercado_solidario.api.entity.Fornecedor;

@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, Long>, JpaSpecificationExecutor<Fornecedor> {

}
