import java.sql.*;
import java.util.List;
import java.util.ArrayList;

import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;



public class DBManager {
    private static final String URL = "jdbc:mysql://localhost:3306/vending_machine?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true";

    private static final String USER = "root";
    private static final String PASSWORD = "0710";

    // 1. 판매 기록 INSERT
    public static void insertSale(long inventoryId, String beverageName, int price) {
        String sql = "INSERT INTO sales (inventory_id, beverage_name, price, timestamp) VALUES (?, ?, ?, NOW())";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, inventoryId);
            pstmt.setString(2, beverageName);
            pstmt.setInt(3, price);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    // 2. 판매 이력 SELECT
    public static ResultSet getSales() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            Statement stmt = conn.createStatement();
            return stmt.executeQuery("SELECT * FROM sales ORDER BY timestamp DESC");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void insertCollectHistory(int denomination, int amount) {
        String sql = "INSERT INTO collect_history (denomination, amount) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, denomination);
            pstmt.setInt(2, amount);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 수금 내역을 조회하는 메서드 (collections 테이블 기준)
    // SELECT 수금 이력 조회
    public static ResultSet getCollectHistory() {
        String sql = "SELECT denomination, amount, collected_at FROM collect_history ORDER BY collected_at DESC";

        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void updateInventoryQuantity(String beverageName, int quantity) {
        String sql = "UPDATE inventory SET quantity = ? WHERE beverage_name = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setString(2, beverageName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    // 음료 이름 변경
    public static void updateInventoryName(Long id, String newName) {
        String sql = "UPDATE inventory SET beverage_name = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setLong(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateInventoryPrice(Long id, int newPrice) {
        String sql = "UPDATE inventory SET price = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newPrice);
            pstmt.setLong(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static List<Inventory> getInventoryList() {
        List<Inventory> list = new ArrayList<>();
        String sql = "SELECT id, beverage_name, price, quantity FROM inventory";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                long id = rs.getLong("id");
                String name = rs.getString("beverage_name");
                int price = rs.getInt("price");
                int qty = rs.getInt("quantity");
                list.add(new Inventory(id, name, price, qty));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }


    public static Map<Integer, Money> getMoneyMapFromDB() {
        Map<Integer, Money> map = new TreeMap<>(Collections.reverseOrder());
        String sql = "SELECT denomination, quantity FROM coin";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int denom = rs.getInt("denomination");
                int qty = rs.getInt("quantity");
                map.put(denom, new Money(denom, qty));
            }
            System.out.println("[화폐 로딩 성공] 불러온 개수: " + map.size());
        } catch (SQLException e) {
            System.err.println("[DB 오류] 화폐 정보를 불러올 수 없습니다.");
            e.printStackTrace();
        }

        return map;
    }


    public static void updateMoneyQuantity(int denomination, int quantity) {
        String sql = "UPDATE coin SET quantity = ? WHERE denomination = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, denomination);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
