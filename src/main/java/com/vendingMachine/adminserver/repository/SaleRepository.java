package com.vendingMachine.adminserver.repository;

import com.vendingMachine.adminserver.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {
}