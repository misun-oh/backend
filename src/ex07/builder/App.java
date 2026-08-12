package ex07.builder;

import ex07.builder.DeptDto.DeptDtoBuilder;
import ex07.builder.EmpDto.Builder;

public class App {
    public static void main(String[] args) {
        DeptDto dept = new DeptDto("D1", "홍보부", "L1");
        DeptDto dept1 = new DeptDto("홍보부", "D2", "L1");

        DeptDto dept2 = new DeptDto().builder()
                            .deptId("D1")
                            .deptCode("홍보부")
                            .locationId("L1")
                            .build();

        DeptDtoBuilder deptBuilder = new DeptDto().builder();
        deptBuilder = deptBuilder.deptId("D2");
        deptBuilder = deptBuilder.deptCode("총무부");
        // non null 컬럼에 값을 넣어야 함!!
        deptBuilder = deptBuilder.locationId("L3");
        DeptDto dept3 = deptBuilder.build();


        System.out.println(dept);
        System.out.println(dept1);
        System.out.println(dept2);
        System.out.println(dept3);


        Builder builder = EmpDto.builder();
        builder.setEmpId("201");
        builder.setEmpName("이미자");
        
        // 오류가 발생 하지 않도록 try-catch로 묶어줌
        try {
            EmpDto emp = builder.build();
            System.out.println("emp : " + emp);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        System.out.println("프로그램 종료");
    }
}
