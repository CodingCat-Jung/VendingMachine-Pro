public class Inventory {
    private Long id; // DB ID
    private String name;
    private int price;
    private int quantity; // 재고 수량

    public Inventory(Long id, String name, int price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void restock(int count) {
        quantity += count;
        DBManager.updateInventoryQuantity(name, quantity); // DB 반영
    }

    public void sell() {
        if (quantity > 0) {
            quantity--;
            DBManager.updateInventoryQuantity(name, quantity); // DB 반영
        }
    }

    public boolean isOutOfStock() {
        return quantity == 0;
    }

    public int getStockCount() {
        return quantity;
    }

    public void setName(String newName) {
        this.name = newName;
        DBManager.updateInventoryName(this.id, newName); // 이름 변경 DB 반영
    }

    public void setPrice(int newPrice) {
        this.price = newPrice;
        DBManager.updateInventoryPrice(this.id, newPrice); // 가격 변경 DB 반영
    }


}
