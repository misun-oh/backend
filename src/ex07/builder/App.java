package ex07.builder;

public class App {
    public static void main(String[] args) {
        DeptDto dept = new DeptDto("D1", "홍보부", "L1");
        DeptDto dept1 = new DeptDto("홍보부", "D2", "L1");

        DeptDto dept2 = new DeptDto().builder()
                            .deptId("D1")
                            .deptCode("홍보부")
                            .locationId("L1")
                            .build();

        System.out.println(dept);
        System.out.println(dept1);
        System.out.println(dept2);

    }
}
