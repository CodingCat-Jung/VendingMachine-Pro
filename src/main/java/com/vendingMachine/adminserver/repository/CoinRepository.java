package com.vendingMachine.adminserver.repository;

import com.vendingMachine.adminserver.entity.Coin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoinRepository extends JpaRepository<Coin, Integer> {
}
