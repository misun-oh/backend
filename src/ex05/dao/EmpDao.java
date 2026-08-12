package ex05.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ex05.dto.EmpDTO;
import ex05.util.DBUtil;

// 데이터베이스에 접근해서 쿼리 질의결과를 반환
// XXXDao 
// Data Access Object의
// 자바 애플리케이션에서 데이터베이스(DB)에 접근하여 데이터의 조회, 삽입, 수정, 삭제(CRUD) 
// 작업을 전담하는 객체(클래스)를 의미
public class EmpDao {

    // 사원의 정보를 조회 하고 리스트를 반환
    // XXXDto
    // Data Transfer Object
    // 데이터를 계층 간에 전달하기 위해 순수하게 데이터를 담아두는 바구니
    public List<EmpDTO> findAll(){
        List<EmpDTO> list = new ArrayList<>();

        // 사원정보를 조회 하는 쿼리
        // ORDER BY 정렬컬럼 DESC(내림차순)
        String sql = "SELECT * FROM EMP ORDER BY SALARY DESC";

        // 자원 반납 -> try () 안에서 생성하면 구문이 끝나면 자동으로 반납해줘요!!
        try (
            // 1. connection 연결
            Connection con = DBUtil.getConnection();
            // 2. 쿼리 질의 객체 생성
            Statement stmt = con.createStatement();
            // 3. 결과집합을 반환 -> 객체생성 -> 리스트에 담기
            ResultSet rs = stmt.executeQuery(sql);
        ) {

            while (rs.next()) {
                String empId = rs.getString(1);                
                String empName = rs.getString(2);                
                int salary = rs.getInt(8);                
                // 입사일
                LocalDate hireDate = rs.getDate("hire_date").toLocalDate();
                // 퇴사일
                String entDate = rs.getString("ent_Date");
                // dto생성및 리스트에 담기
                list.add(new EmpDTO(empId, empName, salary, "", hireDate, entDate));
            }
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        
        return list;
    }

    // 사원조회 - 사원반환, 사원이 없으면 - null
    public EmpDTO find(String name) {
        EmpDTO emp = null;
        // ;은 삭제해야 합니다!
        String sql = "SELECT * FROM EMP WHERE EMP_NAME = '%s'".formatted(name);
        try (
            // 1. connection 연결
            Connection con = DBUtil.getConnection();
            // 2. 쿼리 질의 객체 생성
            Statement stmt = con.createStatement();
            // 3. 결과집합을 반환 -> 객체생성 -> 리스트에 담기
            ResultSet rs = stmt.executeQuery(sql);
        ) {

            if(rs.next()){
                String empId = rs.getString(1);                
                String empName = rs.getString(2);                
                int salary = rs.getInt(8);                
                
                // 한명의 사원 정보를 반환
                return new EmpDTO(empId, empName, salary, "");
            }

            

        } catch(Exception e){
            e.printStackTrace();
        }
        return emp;
    }

    public int updateEntYn(String empId) {
        int res = 0;
        String sql = "UPDATE EMP SET ENT_YN='Y' WHERE EMP_ID='%s'".formatted(empId);

        try(
            // 1. connection 연결
            Connection con = DBUtil.getConnection();
            // 2. 쿼리 질의 객체 생성
            Statement stmt = con.createStatement();
            
        )
        {
            res = stmt.executeUpdate(sql);
            if(res > 0){
                con.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }




        return res;
    }

    
}
