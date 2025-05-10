package com.vendingMachine.adminserver.service;

import com.vendingMachine.adminserver.entity.CollectHistory;
import com.vendingMachine.adminserver.entity.Sale;
import com.vendingMachine.adminserver.entity.Inventory;
import com.vendingMachine.adminserver.entity.Coin;
import com.vendingMachine.adminserver.repository.CollectRepository;
import com.vendingMachine.adminserver.repository.SaleRepository;
import com.vendingMachine.adminserver.repository.InventoryRepository;
import com.vendingMachine.adminserver.repository.CoinRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private CollectRepository collectRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CoinRepository coinRepository;

    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    public List<CollectHistory> getAllCollects() {
        return collectRepository.findAll();
    }

    public int calculateTotalRevenue() {
        return saleRepository.findAll().stream().mapToInt(Sale::getPrice).sum();
    }

    public void saveCollectHistory(CollectHistory history) {
        collectRepository.save(history);
    }

    public void clearSales() {
        saleRepository.deleteAll();
    }

    @Transactional
    public String collectRevenue(int denomination, int amount) {
        if (amount <= 0) {
            return "수금할 금액이 유효하지 않습니다.";
        }

        Coin coin = coinRepository.findById(denomination).orElse(null);

        if (coin == null) {
            return "존재하지 않는 화폐 단위입니다: " + denomination;
        }

        int remainingQuantity = coin.getQuantity() - amount;

        // 화폐 수량이 5개 미만으로 줄어들 경우 수금 불가
        if (remainingQuantity < 5) {
            return "수금할 화폐가 부족하거나 최소 5개 이상을 남겨야 합니다.";
        }

        // 수금 내역 저장
        CollectHistory history = new CollectHistory();
        history.setDenomination(denomination);
        history.setAmount(amount);
        history.setCollectedAt(LocalDateTime.now());
        collectRepository.save(history);

        // 화폐 수량 차감
        coin.setQuantity(remainingQuantity);
        coinRepository.save(coin);

        return denomination + "원 화폐 " + amount + "개 수금 완료.";
    }


    // Inventory Methods
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public Inventory saveInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    public void deleteInventory(Long id) {
        inventoryRepository.deleteById(id);
    }

    // Coin Methods
    public List<Coin> getAllCoins() {
        return coinRepository.findAll();
    }

    public Coin saveCoin(Coin coin) {
        return coinRepository.save(coin);
    }

    public void deleteCoin(int denomination) {
        coinRepository.deleteById(denomination);
    }

    // 화폐 업데이트 메서드
    public String updateCoinQuantity(int denomination, int quantity) {
        if (quantity <= 0) {
            return "유효하지 않은 수량입니다.";
        }

        Coin existingCoin = coinRepository.findById(denomination).orElse(null);

        if (existingCoin != null) {
            existingCoin.setQuantity(existingCoin.getQuantity() + quantity);
            coinRepository.save(existingCoin);
            return denomination + "원 화폐 수량이 " + quantity + "개 추가되었습니다.";
        }

        return "존재하지 않는 화폐 단위입니다: " + denomination + "원";
    }


    // 재고 업데이트 메서드
    public String updateInventoryQuantity(String name, int quantity) {
        if (quantity <= 0) {
            return "유효하지 않은 수량입니다.";
        }

        Inventory existingInventory = inventoryRepository.findByName(name);

        if (existingInventory != null) {
            existingInventory.setQuantity(existingInventory.getQuantity() + quantity);
            inventoryRepository.save(existingInventory);
            return "재고가 성공적으로 추가되었습니다.";
        }

        return "존재하지 않는 음료입니다. 재고를 추가할 수 없습니다.";
    }


}
