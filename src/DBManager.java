import java.sql.*;

public class DBManager {
    private static final String URL = "jdbc:mysql://localhost:3306/vending_machine?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true";

    private static final String USER = "root";
    private static final String PASSWORD = "0710";

    // 1. 판매 기록 INSERT
    public static void insertSale(String beverageName, int price) {
        String sql = "INSERT INTO sales (beverage_name, price, timestamp) VALUES (?, ?, NOW())";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, beverageName);
            pstmt.setInt(2, price);
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



}
