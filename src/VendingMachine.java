import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.ArrayList;

public class VendingMachine extends JFrame {
    private static VendingMachine instance;
    private static Map<String, Inventory> inventories = new LinkedHashMap<>();
    private static Map<Integer, Money> moneyMap = new TreeMap<>(Collections.reverseOrder());

    private Integer inputMoney = null;
    private int count1000 = 0;
    private JLabel moneyLabel;
    private final Map<String, JButton> purchaseButtons = new HashMap<>();
    private final Map<String, JLabel> priceLabels = new HashMap<>();

    private final String[] displayNames = {"믹스커피", "고급믹스커피", "물", "캔커피", "이온음료", "고급캔커피", "탄산음료", "특화음료"};
    private final String[] keys = {"mixCoffee", "premiumMix", "water", "canCoffee", "sport", "premiumCan", "coke", "specialDrink"};
    private final long[] ids = {1, 2, 3, 4, 5, 6, 7, 8}; // DB 내 inventory ID와 매칭
    private final int[] prices = {200, 300, 450, 500, 550, 700, 750, 800};
    private final String[] imageFiles = {"믹스커피.png", "고급커피.png", "물.png", "커피.png", "이온음료.png", "고급 캔 커피.png", "탄산음료.png", "특화음료.png"};

    private static boolean isAdminActive = false; // 관리자 스레드 동기화용

    public VendingMachine() {
        instance = this;

        List<Inventory> inventoryList = DBManager.getInventoryList();
        Map<Long, Inventory> inventoryById = new HashMap<>();
        for (Inventory inv : inventoryList) {
            inventoryById.put(inv.getId(), inv);
        }

        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            long id = ids[i];
            Inventory inv = inventoryById.get(id);
            if (inv == null) {
                System.err.println("[오류] DB에서 ID=" + id + " 음료를 찾을 수 없습니다.");
                continue;
            }
            inventories.put(key, inv);
        }

        moneyMap = DBManager.getMoneyMapFromDB();
        if (moneyMap.isEmpty()) {
            for (int denom : new int[]{10, 50, 100, 500, 1000}) {
                moneyMap.put(denom, new Money(denom, 10));
            }
        }

        inputMoney = 0;

        setTitle("20204026 정명훈 자판기");
        Container c = getContentPane();
        c.setLayout(null);

        int x = 50, y = 30;
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            Inventory inv = inventories.get(key);

            JLabel imgLabel = new JLabel();
            ImageIcon icon = new ImageIcon(imageFiles[i]);
            Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            imgLabel.setIcon(new ImageIcon(img));
            imgLabel.setBounds(x, y, 100, 100);
            c.add(imgLabel);

            JLabel priceLabel = new JLabel("₩" + inv.getPrice());
            priceLabel.setBounds(x + 25, y + 100, 100, 20);
            c.add(priceLabel);
            priceLabels.put(key, priceLabel);

            JButton buyBtn = new JButton("구매");
            buyBtn.setBounds(x + 10, y + 130, 80, 30);
            int finalI = i;
            buyBtn.addActionListener(e -> purchaseBeverage(keys[finalI]));
            c.add(buyBtn);
            purchaseButtons.put(key, buyBtn);

            x += 130;
            if ((i + 1) % 4 == 0) {
                x = 50;
                y += 180;
            }
        }

        int[] units = {10, 50, 100, 500, 1000};
        x = 50; y += 180;
        for (int unit : units) {
            JButton mBtn = new JButton("₩" + unit);
            mBtn.setBounds(x, y, 80, 30);
            final int amount = unit;
            mBtn.addActionListener(e -> insertMoney(amount));
            c.add(mBtn);
            x += 90;
        }

        JButton returnBtn = new JButton("반환");
        returnBtn.setBounds(50, y + 40, 100, 30);
        returnBtn.addActionListener(e -> returnMoney());
        c.add(returnBtn);

        JButton adminBtn = new JButton("관리자 메뉴");
        adminBtn.setBounds(160, y + 40, 130, 30);
        adminBtn.addActionListener(e -> {
            JPasswordField pwd = new JPasswordField();
            int result = JOptionPane.showConfirmDialog(this, pwd, "비밀번호 입력", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION && new String(pwd.getPassword()).equals("@20204026")) {
                this.setVisible(false);                // 사용자 창 숨김
                disableUserControls();                 // 사용자 기능 비활성화
                // 관리자 메뉴 스레드 실행
                SwingUtilities.invokeLater(() -> {
                    new Adminmenu();
                    enableUserControls(); // 관리자 메뉴가 종료되면 사용자 기능을 활성화
                });
            } else {
                JOptionPane.showMessageDialog(this, "비밀번호 불일치");
            }
        });

        c.add(adminBtn);

        moneyLabel = new JLabel("현재 잔액: ₩0");
        moneyLabel.setBounds(310, y + 40, 150, 30);
        c.add(moneyLabel);

        setSize(600, y + 150);
        setVisible(true);
    }

    private synchronized void openAdminMenuThread() {
        if (isAdminActive) {
            JOptionPane.showMessageDialog(this, "이미 관리자 메뉴가 열려 있습니다.");
            return;
        }
        JPasswordField pwd = new JPasswordField();
        int result = JOptionPane.showConfirmDialog(this, pwd, "비밀번호 입력", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION && new String(pwd.getPassword()).equals("@20204026")) {
            isAdminActive = true;
            new Thread(() -> {
                new Adminmenu();
                isAdminActive = false;
                SwingUtilities.invokeLater(() -> enableUserControls()); // 관리자 메뉴 종료 후 사용자 기능 활성화
            }).start();
        } else {
            JOptionPane.showMessageDialog(this, "비밀번호 불일치");
        }
    }

    private void insertMoney(int amount) {
        if (inputMoney + amount > 7000) {
            JOptionPane.showMessageDialog(this, "총 7000원을 초과할 수 없습니다.");
            return;
        }

        if (amount == 1000 && count1000 >= 5) {
            JOptionPane.showMessageDialog(this, "₩1000 지폐는 최대 5장까지만 넣을 수 있습니다.");
            return;
        }

        Money money = moneyMap.get(amount);
        if (money == null) {
            JOptionPane.showMessageDialog(this, "[오류] 화폐 정보가 없습니다: " + amount + "원");
            return;
        }

        money.increase();
        DBManager.updateMoneyQuantity(amount, money.getCount());  // 💾 DB 반영
        inputMoney += amount;

        if (amount == 1000) count1000++;

        updateMoneyLabel();
        updateButtonStatus();
    }


    private void returnMoney() {
        if (inputMoney == null || inputMoney == 0) return;

        int change = inputMoney;
        Map<Integer, Integer> plan = new LinkedHashMap<>();

        // 시뮬레이션: 반환 가능한지 먼저 확인
        for (int denom : moneyMap.keySet()) {
            Money money = moneyMap.get(denom);
            int available = money.getCount();
            int needed = change / denom;
            int used = Math.min(available, needed);
            if (used > 0) {
                plan.put(denom, used);
                change -= denom * used;
            }
        }

        if (change > 0) {
            JOptionPane.showMessageDialog(this, "거스름돈이 부족하여 반환할 수 없습니다.");
            return;
        }

        // 가능하면 실제로 화폐 차감
        for (Map.Entry<Integer, Integer> entry : plan.entrySet()) {
            moneyMap.get(entry.getKey()).decrease(entry.getValue());
            DBManager.updateMoneyQuantity(entry.getKey(), moneyMap.get(entry.getKey()).getCount());
        }

        inputMoney = null;
        count1000 = 0;
        JOptionPane.showMessageDialog(this, "반환된 화폐: " + plan);
        inputMoney = 0;
        updateMoneyLabel();
        updateButtonStatus();
    }


    private void updateMoneyLabel() {
        moneyLabel.setText("현재 잔액: ₩" + (inputMoney != null ? inputMoney : 0));
    }

    public void updateButtonStatus() {
        for (String key : inventories.keySet()) {
            Inventory inv = inventories.get(key);
            JButton btn = purchaseButtons.get(key);
            if (inputMoney >= inv.getPrice() && !inv.isOutOfStock()) {
                btn.setBackground(Color.GREEN);
                btn.setEnabled(true);
            } else {
                btn.setBackground(Color.RED);
                btn.setEnabled(false);
            }
        }
    }

    private void purchaseBeverage(String key) {
        Inventory inv = inventories.get(key);
        if (inv.isOutOfStock()) {
            JOptionPane.showMessageDialog(this, inv.getName() + " 품절");
            return;
        }
        if (inputMoney < inv.getPrice()) {
            JOptionPane.showMessageDialog(this, "잔액 부족");
            return;
        }

        inv.sell(); // 음료 판매
        inputMoney -= inv.getPrice(); // 잔액 차감
        updateMoneyLabel(); // 잔액 업데이트
        updateButtonStatus(); // 버튼 상태 업데이트
        JOptionPane.showMessageDialog(this, inv.getName() + " 나왔습니다.");

        // 판매 기록을 추가하는 작업을 멀티스레드로 처리
        new SaleThread(inv).start(); // 판매 내역을 스레드로 처리

        // 재고가 0이면 품절 처리
        if (inv.getStockCount() == 0) {
            Adminmenu.logSoldOut(inv.getName());
        }
    }


    public static Map<String, Inventory> getInventoryMap() {
        return inventories;
    }

    public static Map<Integer, Money> getMoneyMap() {
        return moneyMap;
    }

    public static void restockInventory(String name, int count) {
        for (Inventory inv : inventories.values()) {
            if (inv.getName().equals(name)) {
                inv.restock(count);
                JOptionPane.showMessageDialog(null, name + "의 재고가 " + count + "개 추가되었습니다.");
                return;
            }
        }
        JOptionPane.showMessageDialog(null, name + "이라는 이름의 음료를 찾을 수 없습니다.");
    }

    public static void renameInventory(String oldName, String newName) {
        for (Map.Entry<String, Inventory> entry : inventories.entrySet()) {
            Inventory inv = entry.getValue();
            if (inv.getName().equals(oldName)) {
                inv.setName(newName);
                JLabel priceLabel = getInstance().priceLabels.get(entry.getKey());
                if (priceLabel != null) {
                    priceLabel.setText("₩" + inv.getPrice());
                }
                break;
            }
        }
    }

    public static void changeInventoryPrice(String name, int newPrice) {
        for (Map.Entry<String, Inventory> entry : inventories.entrySet()) {
            Inventory inv = entry.getValue();
            if (inv.getName().equals(name)) {
                inv.setPrice(newPrice);
                JLabel priceLabel = getInstance().priceLabels.get(entry.getKey());
                if (priceLabel != null) {
                    priceLabel.setText("₩" + newPrice);
                }
                break;
            }
        }
    }

    public void disableUserControls() {
        for (JButton btn : purchaseButtons.values()) {
            btn.setEnabled(false); // 구매 버튼 비활성화
        }
        // 화폐 입력 버튼도 비활성화
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JButton) {
                JButton b = (JButton) comp;
                if (b.getText().startsWith("₩") || b.getText().equals("반환")) {
                    b.setEnabled(false); // 화폐 버튼도 비활성화
                }
            }
        }
    }

    // enableUserControls() 메서드 수정
    public void enableUserControls() {
        updateButtonStatus(); // 구매 버튼은 잔액/재고 조건 반영
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JButton) {
                JButton b = (JButton) comp;
                if (b.getText().startsWith("₩") || b.getText().equals("반환")) {
                    b.setEnabled(true); // 화폐 입력 버튼 활성화
                }
            }
        }
    }

    public static VendingMachine getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        new VendingMachine();
    }
}

class SaleThread extends Thread {
    private final Inventory inventory;

    public SaleThread(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public void run() {
        // 큐에 추가 (화면용 이름, 가격)
        Adminmenu.enqueueSale(inventory.getName(), String.valueOf(inventory.getPrice()));

        // DB에 저장할 때는 id를 기반으로 저장
        Adminmenu.logSale(inventory.getId(), inventory.getName(), inventory.getPrice());

    }
}

