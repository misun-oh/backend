import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * EMPLOYEE 테이블에 대한 DB 접근을 담당하는 클래스 (DAO)
 */
public class EmployeeDao {

    /**
     * 사원 목록 전체를 조회한다. (EMPLOYEE 테이블만 조회)
     */
    public List<Employee> findAll() throws SQLException {
        String sql = "SELECT EMP_ID, EMP_NAME, EMP_NO, EMAIL, PHONE, DEPT_CODE, JOB_CODE, " +
                     "       SALARY, BONUS, MANAGER_ID, HIRE_DATE, ENT_DATE, ENT_YN " +
                     "FROM EMPLOYEE " +
                     "ORDER BY EMP_ID";

        List<Employee> employees = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                employees.add(mapRowToEmployee(rs));
            }
        }

        return employees;
    }

    /**
     * 퇴직하지 않은(재직 중) 사원만 조회한다. (심화 예시)
     */
    public List<Employee> findActiveEmployees() throws SQLException {
        String sql = "SELECT EMP_ID, EMP_NAME, EMP_NO, EMAIL, PHONE, DEPT_CODE, JOB_CODE, " +
                     "       SALARY, BONUS, MANAGER_ID, HIRE_DATE, ENT_DATE, ENT_YN " +
                     "FROM EMPLOYEE " +
                     "WHERE ENT_YN = 'N' " +
                     "ORDER BY EMP_ID";

        List<Employee> employees = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                employees.add(mapRowToEmployee(rs));
            }
        }

        return employees;
    }

    /**
     * ResultSet 한 행을 Employee 객체로 변환한다.
     * NULL이 들어올 수 있는 컬럼은 rs.getObject() / rs.getDate()를 사용해 안전하게 처리한다.
     */
    private Employee mapRowToEmployee(ResultSet rs) throws SQLException {
        // getInt()는 값이 NULL이면 0을 반환하므로, NULL 가능 컬럼은 getObject()로 받는다.
        Integer salary = (Integer) rs.getObject("SALARY");
        BigDecimal bonus = rs.getBigDecimal("BONUS"); // NULL이면 그대로 null 반환됨

        Date hireDateSql = rs.getDate("HIRE_DATE");
        Date entDateSql = rs.getDate("ENT_DATE"); // NULL이면 null 반환됨

        return new Employee(
                rs.getString("EMP_ID"),
                rs.getString("EMP_NAME"),
                rs.getString("EMP_NO"),
                rs.getString("EMAIL"),
                rs.getString("PHONE"),
                rs.getString("DEPT_CODE"),   // NULL 가능 (예: 하동운, 이오리)
                rs.getString("JOB_CODE"),
                salary,
                bonus,
                rs.getString("MANAGER_ID"),  // NULL 가능
                hireDateSql == null ? null : hireDateSql.toLocalDate(),
                entDateSql == null ? null : entDateSql.toLocalDate(),
                rs.getString("ENT_YN")
        );
    }
}
