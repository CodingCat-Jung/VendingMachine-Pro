package com.vendingMachine.adminserver.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String beverageName;
    private int price;
    private LocalDateTime timestamp;

    // Getter 메서드
    public Long getId() {
        return id;
    }

    public String getBeverageName() {
        return beverageName;
    }

    public int getPrice() {
        return price;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // 생성자 (선택사항)
    public Sale() {
    }

    public Sale(String beverageName, int price, LocalDateTime timestamp) {
        this.beverageName = beverageName;
        this.price = price;
        this.timestamp = timestamp;
    }
}
