package ex05.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ex05.dto.DeptDTO;
import ex05.util.DBUtil;

// 자바 애플리케이션에서 데이터베이스(DB)에 접근하여 데이터의 조회, 삽입, 수정, 삭제(CRUD) 
public class DeptDao {
    // 접.제 반환타입 이름 (매.타 매.이) {}
    /*
    
    */
    
    /**
     * DB로 부터 부서정보를 조회후 리스트 반환
     * 
     * @return List<DeptDTO>
     */
    public List<DeptDTO> findAll(){
        List<DeptDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM DEPT";
        try (
            // DB 조회 방법
            // 1. 데이터베이스 연결 
            //          - 접속정보(ip,port,DB이름,user,pw)를 이용해서 connection객체 생성하기
            // 2. 쿼리 질의 - Statement
            // 3. 질의결과를 객체에 담아주기

            Connection con =  DBUtil.getConnection();
            Statement stmt =  con.createStatement();
            // 결과집합을 받아올때 사용
            // select(조회) - 결과집합
            // insert, update, delete - 숫자를 반환 (몇건이 처리 되었는지)
            ResultSet rs = stmt.executeQuery(sql);){
            
            
            //    => 계층간 데이터 이동 
            //         controller - service - dao(mapper)
            
            // 오류가 발생할 소지가 많다
            // 이름으로 접근시 오타 발생, 데이터 누락, 코드의 중복, 자원을 반납하지 않는 경우.... 
            // -> 프레임워크 
            // 쿼리만 작성하면 나머지는 자동으로 처리
            // rs.next()
            // 다음행이 있다면 true, 없으면 false를 반환
            while (rs.next()) {
                // 데이터를 꺼내서 변수에 저장하고 객체를 만들어준다
                String deptId = rs.getString(1);
                String deptTitle = rs.getString("dept_title");
                String locationId = rs.getString(3);

                list.add(new DeptDTO(deptId, deptTitle, locationId));
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return list;
    }
}
