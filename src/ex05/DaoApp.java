package ex05;

import java.util.List;

import ex05.dao.EmpDao;
import ex05.dto.EmpDTO;

public class DaoApp {
    public static void main(String[] args) {
       EmpDao empDao = new EmpDao();
       List<EmpDTO> list = empDao.findAll();

       //System.out.println(list);
       for(EmpDTO emp:list){
            System.out.println(emp);
       }
    }
}
