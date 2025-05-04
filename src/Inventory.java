// Inventory.java - LinkedList 기반 재고 관리

import java.util.LinkedList;

public class Inventory {
    private String name;
    private int price;
    private LinkedList<String> stockList; // LinkedList로 재고 관리

    public Inventory(String name, int price, int initialStock) {
        this.name = name;
        this.price = price;
        stockList = new LinkedList<>();
        for (int i = 0; i < initialStock; i++) {
            stockList.add(name + ":item" + (i + 1));
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int newPrice) {
        this.price = newPrice;
    }

    public int getStockCount() {
        return stockList.size();
    }

    public void restock(int count) {
        for (int i = 0; i < count; i++) {
            stockList.add(name + ":new" + (i + 1));
        }
    }

    public void sell() {
        if (!stockList.isEmpty()) {
            stockList.removeFirst();
        }
    }

    public boolean isOutOfStock() {
        return stockList.isEmpty();
    }
}