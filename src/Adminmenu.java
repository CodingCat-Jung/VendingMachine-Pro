import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.sql.SQLException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import java.util.List;


public class Adminmenu extends JFrame {
    private JTable admTable;
    private static Stack<String> collectHistory = new Stack<>();
    private static final Queue<String> saleQueue = new LinkedList<>();

    public synchronized static void enqueueSale(String item, String price) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        saleQueue.offer(timestamp + " - " + item + " - " + price); // 판매 시간과 항목, 가격을 큐에 추가
    }

    public Adminmenu() {
        setTitle("자판기 관리자 메뉴");
        Container adm = getContentPane();
        adm.setLayout(null);

        admTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(admTable);
        scrollPane.setBounds(160, 30, 500, 600);
        adm.add(scrollPane);

        String[] btnLabels = {
                "이름변경", "가격변경", "재고확인", "재고추가", "일별 매출", "월별 매출", "음료 일별", "음료 월별",
                "재고소진", "화폐현황", "수금", "수금내역", "일별 기록", "월별 기록", "소진 기록",
                "판매 이력", "가격순 정렬", "판매 검색", "닫기"
        };

        ActionListener[] actions = {
                e -> changeBeverageName(),
                e -> changeBeveragePrice(),
                e -> updateStockTable(),
                e -> restockBeverage(),
                e -> loadSalesFile("daily.txt"),
                e -> loadSalesFile("month.txt"),
                e -> loadSalesFile("beverage daily sales.txt"),
                e -> loadSalesFile("beverage month sales.txt"),
                e -> loadQuantity("Quantity.txt"),
                e -> showMoneyStatus(),
                e -> collectMoney(),
                e -> showCollectHistory(),
                e -> {
                    String record = JOptionPane.showInputDialog("기록할 일별 매출 내용 입력 (예: 2025-04-07 총매출: 3400원):");
                    if (record != null && !record.isBlank()) recordDailySale(record);
                },
                e -> {
                    String record = JOptionPane.showInputDialog("기록할 월별 매출 내용 입력 (예: 2025-04 총매출: 3400원):");
                    if (record != null && !record.isBlank()) recordMonthlySale(record);
                },
                e -> {
                    String item = JOptionPane.showInputDialog("재고 소진된 음료 이름 입력:");
                    if (item != null && !item.isBlank()) logSoldOut(item);
                },
                e -> showSaleHistory(),
                e -> sortSalesByPriceFromDB(),
                e -> searchSalesSummary(),
                e -> {
                    VendingMachine.getInstance().setVisible(true);
                    VendingMachine.getInstance().updateButtonStatus();
                    this.dispose();
                }
        };

        for (int i = 0; i < btnLabels.length; i++) {
            JButton btn = new JButton(btnLabels[i]);
            btn.setBounds(30, 10 + i * 40, 100, 30);
            btn.addActionListener(actions[i]);
            adm.add(btn);
        }

        setSize(800, 800);
        setVisible(true);
    }

    public static void logSale(String beverageName, int price) {
        DBManager.insertSale(beverageName, price);
    }



    public static void logSoldOut(String beverageName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Quantity.txt", true))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.write(timestamp + " " + beverageName);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void recordDailySale(String line) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("daily.txt", true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void recordMonthlySale(String line) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("month.txt", true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showMoneyStatus() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("화폐 단위");
        model.addColumn("수량");
        for (Map.Entry<Integer, Money> entry : VendingMachine.getMoneyMap().entrySet()) {
            model.addRow(new Object[]{entry.getKey(), entry.getValue().getCount()});
        }
        admTable.setModel(model);
    }

    private void collectMoney() {
        try {
            int denom = Integer.parseInt(JOptionPane.showInputDialog("수금할 화폐 단위 입력 (10, 50, 100, 500, 1000):"));
            int quantity = Integer.parseInt(JOptionPane.showInputDialog("수금할 수량 입력:"));
            Money money = VendingMachine.getMoneyMap().getOrDefault(denom, null);
            if (money == null) throw new Exception("존재하지 않는 화폐 단위입니다.");
            int current = money.getCount();
            if (current - quantity < 5) throw new Exception("최소 5개의 잔돈은 남겨야 합니다.");
            money.decrease(quantity);
            String log = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +
                    " - 수금: " + denom + "원 x " + quantity + "개";
            collectHistory.push(log);

            // MySQL 병행 저장
            DBManager.insertCollectHistory(denom, quantity);

            JOptionPane.showMessageDialog(null, denom + "원 권 " + quantity + "개 수금 완료.");
            showMoneyStatus();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "오류: " + e.getMessage());
        }
    }

    private void showCollectHistory() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("수금 기록 (Stack)");
        model.addColumn("전체 수금 기록 (DB)");

        // Stack 기반 기록 역순 출력
        List<String> stackLogs = new ArrayList<>();
        for (int i = collectHistory.size() - 1; i >= 0; i--) {
            stackLogs.add(collectHistory.get(i));
        }

        // DB 기반 기록 불러오기
        List<String> dbLogs = new ArrayList<>();
        try (ResultSet rs = DBManager.getCollectHistory()) {
            while (rs != null && rs.next()) {
                String time = rs.getTimestamp("collected_at").toString();
                int denom = rs.getInt("denomination");
                int qty = rs.getInt("amount");
                dbLogs.add(time + " - 수금: " + denom + "원 x " + qty + "개");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Stack/DB 로그를 병렬로 출력
        int maxRows = Math.max(stackLogs.size(), dbLogs.size());
        for (int i = 0; i < maxRows; i++) {
            String stackRow = (i < stackLogs.size()) ? stackLogs.get(i) : "";
            String dbRow = (i < dbLogs.size()) ? dbLogs.get(i) : "";
            model.addRow(new Object[]{stackRow, dbRow});
        }

        admTable.setModel(model);
    }


    private void updateStockTable() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("음료");
        model.addColumn("재고");
        for (Map.Entry<String, Inventory> entry : VendingMachine.getInventoryMap().entrySet()) {
            model.addRow(new Object[]{entry.getValue().getName(), entry.getValue().getStockCount()});
        }
        admTable.setModel(model);
    }

    private void restockBeverage() {
        String name = JOptionPane.showInputDialog("음료 이름 입력:");
        String input = JOptionPane.showInputDialog("추가할 수량 입력:");
        try {
            int count = Integer.parseInt(input);
            VendingMachine.restockInventory(name, count);
            updateStockTable();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "유효한 숫자를 입력하세요.");
        }
    }

    private void changeBeverageName() {
        String oldName = JOptionPane.showInputDialog("기존 음료 이름:");
        String newName = JOptionPane.showInputDialog("새 이름:");
        if (oldName != null && newName != null && !newName.isBlank()) {
            VendingMachine.renameInventory(oldName, newName);
            updateStockTable();
        }
    }

    private void changeBeveragePrice() {
        String name = JOptionPane.showInputDialog("가격 변경할 음료 이름:");
        String input = JOptionPane.showInputDialog("새 가격 입력:");
        try {
            int price = Integer.parseInt(input);
            if (price > 0 && price <= 5000 && price % 10 == 0) {
                VendingMachine.changeInventoryPrice(name, price);
                updateStockTable();
            } else {
                throw new IllegalArgumentException("10원 단위, 최대 5000원까지 입력 가능");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "입력 오류: " + e.getMessage());
        }
    }

    private void loadSalesFile(String filename) {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("매출 내용");
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) model.addRow(new Object[]{line});
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, filename + " 파일을 읽을 수 없습니다.");
        }
        admTable.setModel(model);
    }

    private void loadQuantity(String filename) {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("날짜");
        model.addColumn("시간");
        model.addColumn("음료명");
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length >= 3)
                    model.addRow(new Object[]{parts[0], parts[1], parts[2]});
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, filename + " 파일을 읽을 수 없습니다.");
        }
        admTable.setModel(model);
    }

    private void showSaleHistory() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("시간");
        model.addColumn("음료 이름");
        model.addColumn("가격");

        try {
            ResultSet rs = DBManager.getSales();
            while (rs != null && rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("timestamp"),
                        rs.getString("beverage_name"),
                        rs.getInt("price")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        admTable.setModel(model);
    }


    private void sortSalesByPriceFromDB() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("음료 이름");
        model.addColumn("총 판매 금액");

        String sql = "SELECT beverage_name, SUM(price) AS total_price " +
                "FROM sales " +
                "GROUP BY beverage_name " +
                "ORDER BY total_price DESC";

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/vending_machine?serverTimezone=Asia/Seoul&useSSL=false",
                "root", "0710");
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("beverage_name");
                int total = rs.getInt("total_price");
                model.addRow(new Object[]{name, total});
            }

            admTable.setModel(model);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "DB 정렬 실패: " + e.getMessage());
        }
    }


    private void searchSalesSummary() {
        String keyword = JOptionPane.showInputDialog("검색할 음료 이름 입력:");
        if (keyword == null || keyword.isBlank()) return;

        String sql = "SELECT COUNT(*) AS cnt, SUM(price) AS total FROM sales WHERE beverage_name LIKE ?";

        try (
                Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/vending_machine?serverTimezone=Asia/Seoul&useSSL=false",
                        "root", "0710"
                );
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, "%" + keyword + "%");  // 부분 검색

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("cnt");
                    int total = rs.getInt("total");
                    JOptionPane.showMessageDialog(null,
                            keyword + " 판매량: " + count + "개\n총 판매 금액: " + total + "원");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "검색 오류: " + e.getMessage());
        }
    }

}
