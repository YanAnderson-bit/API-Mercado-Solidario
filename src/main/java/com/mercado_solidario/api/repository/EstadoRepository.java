package com.mercado_solidario.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.mercado_solidario.api.entity.Estado;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Long>, JpaSpecificationExecutor<Estado> {

}