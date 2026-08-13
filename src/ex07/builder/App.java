package ex07.builder;

import ex07.builder.DeptDto.DeptDtoBuilder;
import ex07.builder.EmpDto.Builder;

public class App {
    public static void main(String[] args) {
        DeptDto dept = new DeptDto("D1", "홍보부", "L1");
        DeptDto dept1 = new DeptDto("홍보부", "D2", "L1");

        // 생성자를 통해서 생성하는것이 아니라 빌더를 통해서 생성
        DeptDto dept2 = DeptDto.builder()
                            .deptId("D1")
                            .deptCode("홍보부")
                             .locationId("L1")
                            .build(); // locationId 값이 null이면 오류가 발생

        DeptDtoBuilder deptBuilder = DeptDto.builder();
        deptBuilder = deptBuilder.deptId("D2");
        deptBuilder = deptBuilder.deptCode("총무부");
        // non null 컬럼에 값을 넣어야 함!!
        deptBuilder = deptBuilder.locationId("L3");
        DeptDto dept3 = deptBuilder.build();


        System.out.println(dept);
        System.out.println(dept1);
        System.out.println(dept2);
        System.out.println(dept3);
        
        try {

            Builder builder = EmpDto.builder();
            builder.setEmpId("201");
            builder.setEmpName("이미자");
            // builder.setEmpNo("888-888");
            // 오류가 발생 하지 않도록 try-catch로 묶어줌
            EmpDto emp = builder.build();   // 필수체크가 구현 -> emp_no null이면 오류
            System.out.println("emp : " + emp);


            Builder b = EmpDto.builder(); // 내부정적클래스인 빌더가 생성되어져서 반환
            b.setEmpId("300");
            b.setEmpName("강동원");
            b.setEmpNo("888-88888");

            EmpDto empdto = new EmpDto(b); // 매개변수로 builder 객체를 받아서 필드를 초기화 
            System.out.println("empdto : " + empdto);


            EmpDto.builder()                     // Builder 객체를 생성해서 반환
                .setEmpId("202")          // 데이터를 세팅 -> Builder반환 
                .setEmpName("한가인")   // 데이터를 세팅 -> Builder반환 
                .setEmpNo("888-8888")     // 데이터를 세팅 -> Builder반환 
                .build();                       // EmpDto 생성자를 호출 -> EmpDto 생성후 반환

        } catch (Exception e) {
            System.err.println(e.getMessage());
            // e.printStackTrace();
        }

        System.out.println("프로그램 종료");
    }
}
