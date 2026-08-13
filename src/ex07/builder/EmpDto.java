package ex07.builder;

import lombok.Data;

@Data
public class EmpDto {
    private String empId;        // 사원번호 (PK)
    private String empName;      // 직원명
    private String empNo;        // 주민등록번호


    // builder에 있는 필드값을 이용해서 객체를 생성하고 반환
    // private 생성자를 막아놈 -> 외부에서 생성 할 수 없음!
    public EmpDto(Builder builder){


        // 빌더에 있는 값을 EmpDto에 세팅
        empId = builder.empId;
        empName = builder.empName;
        empNo = builder.empNo;
    }

    // 빌더를 통해서 객체를 생성
    public static Builder builder(){
        return new Builder();
    }

    // 내부 정적 클래스 
    public static class Builder{
        private String empId;        // 사원번호 (PK)
        private String empName;      // 직원명
        private String empNo;        // 주민등록번호

        public Builder setEmpId(String empId) {
            this.empId = empId;
            return this;
        }
        public Builder setEmpName(String empName) {
            this.empName = empName;
            return this;
        }
        public Builder setEmpNo(String empNo) {
            this.empNo = empNo;
            return this;
        }

        public EmpDto build(){

            // 필수 체크 - emp_no 필수값인데 값이 입력되지 않은 경우
            if(empNo == null){
                throw new NullPointerException("emp_no는 필수 값입니다.");
            }

            return new EmpDto(this);
        }
    }
    
}
