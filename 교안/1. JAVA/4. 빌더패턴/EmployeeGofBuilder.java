/**
 * 빌더 패턴을 GoF 원조 스타일(인터페이스 + 구현체 + Director)로 구현한 예제.
 * Employee.java(이너클래스 방식)와 결과는 비슷하지만 구조가 다르다.
 * - Product: Employee
 * - Builder(인터페이스): EmployeeBuilder
 * - ConcreteBuilder: DefaultEmployeeBuilder
 * - Director: EmployeeDirector (조립 절차 = 레시피를 캡슐화)
 */
public class EmployeeGofBuilder {

    // ---- 직접 실행해서 동작을 확인해보는 메인 메서드 ----
    public static void main(String[] args) {

        EmployeeBuilder builder = new DefaultEmployeeBuilder();
        EmployeeDirector director = new EmployeeDirector(builder);

        // 레시피 1: 정규직 사원 만들기 (jobCode="J1"이 Director 안에 고정되어 있음)
        Employee regular = director.constructRegularEmployee("223", "홍길동", "D1", 3000000);
        System.out.println("정규직: " + regular);

        // 레시피 2: 인턴 사원 만들기 (jobCode="J9", salary=null이 Director 안에 고정되어 있음)
        Employee intern = director.constructInternEmployee("310", "김인턴", "D2");
        System.out.println("인턴: " + intern);

        // 실패 케이스: 필수값(empName)을 빼고 build() 호출 -> 예외 발생
        try {
            Employee invalid = builder.empId("999").deptCode("D1").build();
        } catch (IllegalStateException e) {
            System.out.println("예상된 예외 발생: " + e.getMessage());
        }
    }
}

/**
 * 1) Builder 인터페이스 - "무엇을 조립할 수 있는지"만 선언한다.
 *    이너클래스 방식과 달리, 이 인터페이스를 구현하는 클래스는 여러 개 있을 수 있다.
 */
interface EmployeeBuilder {
    EmployeeBuilder empId(String empId);
    EmployeeBuilder empName(String empName);
    EmployeeBuilder deptCode(String deptCode);
    EmployeeBuilder jobCode(String jobCode);
    EmployeeBuilder salary(Integer salary);
    Employee build();
}

/**
 * 2) ConcreteBuilder - 인터페이스를 구현(implements)해서 실제로 값을 채운다.
 */
class DefaultEmployeeBuilder implements EmployeeBuilder {
    private String empId;
    private String empName;
    private String deptCode;
    private String jobCode;
    private Integer salary;

    @Override
    public EmployeeBuilder empId(String empId) {
        this.empId = empId;
        return this;
    }

    @Override
    public EmployeeBuilder empName(String empName) {
        this.empName = empName;
        return this;
    }

    @Override
    public EmployeeBuilder deptCode(String deptCode) {
        this.deptCode = deptCode;
        return this;
    }

    @Override
    public EmployeeBuilder jobCode(String jobCode) {
        this.jobCode = jobCode;
        return this;
    }

    @Override
    public EmployeeBuilder salary(Integer salary) {
        this.salary = salary;
        return this;
    }

    @Override
    public Employee build() {
        if (empId == null || empName == null) {
            throw new IllegalStateException("empId와 empName은 필수입니다.");
        }
        return new Employee(empId, empName, deptCode, jobCode, salary);
    }
}

/**
 * 3) Director - Builder의 메서드를 "어떤 순서로, 어떤 값으로" 호출할지 아는 지휘자.
 *    조립 절차(레시피) 자체를 캡슐화하므로, 클라이언트는 레시피의 세부 규칙을 몰라도 된다.
 */
class EmployeeDirector {
    private final EmployeeBuilder builder;

    public EmployeeDirector(EmployeeBuilder builder) {
        this.builder = builder;
    }

    public Employee constructRegularEmployee(String empId, String empName, String deptCode, Integer salary) {
        return builder.empId(empId)
                .empName(empName)
                .deptCode(deptCode)
                .jobCode("J1")
                .salary(salary)
                .build();
    }

    public Employee constructInternEmployee(String empId, String empName, String deptCode) {
        return builder.empId(empId)
                .empName(empName)
                .deptCode(deptCode)
                .jobCode("J9")
                .salary(null)
                .build();
    }
}

/**
 * 4) Product - 최종적으로 만들어지는 불변 객체.
 *    이너클래스 방식과 달리 생성자를 private으로 막을 필요가 없다.
 *    (ConcreteBuilder가 이 클래스와 같은 파일/패키지에 있어야만 접근 가능하게 하려면
 *     생성자를 package-private으로 둘 수도 있다.)
 */
class Employee {
    private final String empId;
    private final String empName;
    private final String deptCode;
    private final String jobCode;
    private final Integer salary;

    Employee(String empId, String empName, String deptCode, String jobCode, Integer salary) {
        this.empId = empId;
        this.empName = empName;
        this.deptCode = deptCode;
        this.jobCode = jobCode;
        this.salary = salary;
    }

    public String getEmpId() { return empId; }
    public String getEmpName() { return empName; }
    public String getDeptCode() { return deptCode; }
    public String getJobCode() { return jobCode; }
    public Integer getSalary() { return salary; }

    @Override
    public String toString() {
        return String.format(
                "사번:%-4s 이름:%-5s 부서:%-3s 직급:%-3s 급여:%s",
                empId, empName,
                deptCode == null ? "-" : deptCode,
                jobCode,
                salary == null ? "-" : salary
        );
    }
}
