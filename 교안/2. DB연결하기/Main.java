import java.sql.SQLException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        EmployeeDao employeeDao = new EmployeeDao();

        try {
            List<Employee> employees = employeeDao.findAll();

            System.out.println("사원 목록 조회 결과 (총 " + employees.size() + "명)");
            System.out.println("--------------------------------------------------------------------------------");
            for (Employee employee : employees) {
                System.out.println(employee);
            }

        } catch (SQLException e) {
            System.out.println("DB 조회 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }
}
