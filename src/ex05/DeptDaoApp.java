package ex05;

import java.util.List;

import ex05.dao.DeptDao;
import ex05.dto.DeptDTO;

public class DeptDaoApp {
    public static void main(String[] args) {
        //DeptDTO deptDto = new DeptDTO("D1", "총무부", "L1");
        // toString 메서드 재정의
        //System.out.println(deptDto);

        DeptDao dao = new DeptDao();
        
        List<DeptDTO> list = dao.findAll();
        System.out.println(list);

        

        
    }
}
