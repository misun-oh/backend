package ex05.dto;

import ex06.필수입력;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmpDTO {

    @필수입력
    String empId;
    String empName;
    int salary;

    @Override
    @Deprecated
    public String toString() {
        return super.toString();
    }

    

    
}
