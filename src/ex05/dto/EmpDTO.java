package ex05.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
//@NoArgsConstructor
public class EmpDTO {

    String empId;
    String empName;
    int salary;
    String empNo;
  
    public EmpDTO() {
    }

    


    

    
}
