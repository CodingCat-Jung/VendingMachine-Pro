package com.vendingMachine.adminserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coin")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Coin {

    @Id
    @Column(nullable = false)
    private int denomination;

    @Column(nullable = false)
    private int quantity;
}