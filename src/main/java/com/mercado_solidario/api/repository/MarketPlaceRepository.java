package com.mercado_solidario.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.mercado_solidario.api.entity.MarketPlace;

@Repository
public interface MarketPlaceRepository extends JpaRepository<MarketPlace, Long>, JpaSpecificationExecutor<MarketPlace> {

}