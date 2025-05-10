package com.vendingMachine.adminserver.repository;

import com.vendingMachine.adminserver.entity.CollectHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectRepository extends JpaRepository<CollectHistory, Long> {
}