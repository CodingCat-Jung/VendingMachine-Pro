package com.vendingMachine.adminserver.repository;

import com.vendingMachine.adminserver.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Inventory findByName(String name);
}
