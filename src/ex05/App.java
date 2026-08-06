package ex05;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class App {

    // 접속정보 필드로 정의 -> git에 노출 될 위험이 있다 -> 프로퍼티 파일로 옮겨서 관리
    private static final String URL =
            "jdbc:mysql://43.201.71.21:3306/HR?serverTimezone=Asia/Seoul&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";


    public static void main(String[] args) {
        // 예외를 발생시킬 소지가 있는 메서드인 경우
        // 1. 나도 던진다 - 프로그램의 비정상적인 종료
        // 2. try-catch
        try {
            // 라이브러리가 추가 되었는지 확인 하는 역할 - 클래스가 있는지 없는지 확인을 통해서 
            // 클래스가 없는 경우 ClassNotFoundException 예외가 발생
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("ex05.util.DBUtil 확인");

            // 1. DB Connection 얻어오기 - 네트워크통신및 인증
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            // 2. 쿼리 질의
            Statement stmt = conn.createStatement();
            // 쿼리 실행하고 결과 집합을 받아온다
            ResultSet rs =stmt.executeQuery("SELECT * FROM EMP");
            
            // 3. 결과집합으로 부터 데이터를 꺼내오기
            rs.next();
            // 컬럼이름, 순서
            String empId = rs.getString("EMP_ID");
            String empName = rs.getString(2);
            String salary = rs.getString("SALARY");

            System.out.println(empId);
            System.out.println(empName);
            System.out.println(salary);


        } catch (ClassNotFoundException e) {
            System.out.println("mysql jdbc 라이브러리를 확인해주세요");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Sql을 확인해주세요");
            e.printStackTrace();
        }

        System.out.println("프로그램 종료");
    }
}
