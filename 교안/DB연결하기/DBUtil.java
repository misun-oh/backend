import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DB 연결을 담당하는 유틸 클래스
 * - 이 파일에서만 URL / USER / PASSWORD 를 관리합니다.
 * - 실습 환경에 맞게 아래 세 값만 수정하면 됩니다.
 */
public class DBUtil {

    private static final String URL =
            "jdbc:mysql://localhost:3306/HR?serverTimezone=Asia/Seoul&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASSWORD = "1234"; // 본인 MySQL 비밀번호로 수정

    // 클래스가 처음 로딩될 때 딱 한 번만 드라이버를 등록한다
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC 드라이버를 찾을 수 없습니다. 라이브러리 추가를 확인하세요.", e);
        }
    }

    private DBUtil() {
        // 인스턴스화 방지 (유틸 클래스)
    }

    /**
     * 새 DB 연결을 반환한다.
     * 호출한 쪽에서 사용 후 반드시 close() 해야 한다 (try-with-resources 권장).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
