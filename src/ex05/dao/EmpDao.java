package ex05.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import ex05.dto.EmpDTO;
import ex05.util.DBUtil;

// 데이터베이스에 접근해서 쿼리 질의결과를 반환
public class EmpDao {

    // 사원의 정보를 조회 하고 리스트를 반환
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
                
                // dto생성및 리스트에 담기
                list.add(new EmpDTO(empId, empName, salary));
            }
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        
        return list;
    }

}
