package util;

import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientSocketSender {

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 9999;

    public static void sendData(String data) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             OutputStream out = socket.getOutputStream()) {

            // 여기서는 암호화 없이 그대로 전송
            out.write(data.getBytes(StandardCharsets.UTF_8));
            out.flush();

            System.out.println("[✅] 암호화된 데이터 전송 완료: " + data);

        } catch (Exception e) {
            System.err.println("[❌] 서버 전송 실패: " + e.getMessage());
        }
    }
}
