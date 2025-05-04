public class Money {
    private int denomination; // 화폐 단위 (예: 10, 50, 100, 500, 1000)
    private int count;        // 해당 화폐 단위의 개수

    // 생성자
    public Money(int denomination, int count) {
        this.denomination = denomination;
        this.count = count;
    }

    // 현재 화폐 단위 반환
    public int getDenomination() {
        return denomination;
    }

    // 현재 개수 반환
    public int getCount() {
        return count;
    }

    // 화폐 개수 증가 (1개 증가)
    public void increase() {
        count++;
    }

    // 화폐 개수 감소 (1개 감소, 0개 이하일 경우는 무시)
    public void decrease() {
        if (count > 0) {
            count--;
        }
    }

    // 🔧 오버로드: 수금할 때 여러 개 감소하는 메서드
    public void decrease(int quantity) {
        if (quantity <= 0) return;
        if (quantity > count) {
            throw new IllegalArgumentException("보유 수량보다 많이 뺄 수 없습니다.");
        }
        count -= quantity;
    }

    // 화폐 개수 증가 (여러 개 추가)
    public void increase(int quantity) {
        if (quantity > 0) {
            count += quantity;
        }
    }
}
