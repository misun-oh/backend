import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 빌더 패턴을 직접(수동으로) 구현한 예제
 * - 생성자를 private으로 막고, 내부 정적 클래스 Builder를 통해서만 객체를 생성한다.
 * - 필드는 모두 final -> 한 번 만들어지면 값이 바뀌지 않는 불변 객체
 */
public class EmployeeManualBuilder {

    private final String empId;
    private final String empName;
    private final String empNo;
    private final String email;
    private final String phone;
    private final String deptCode;
    private final String jobCode;
    private final Integer salary;
    private final BigDecimal bonus;
    private final String managerId;
    private final LocalDate hireDate;
    private final LocalDate entDate;
    private final String entYn;

    // 1) 생성자를 private으로 막는다 -> new EmployeeManualBuilder(...)로 직접 생성 불가
    private EmployeeManualBuilder(Builder builder) {
        this.empId = builder.empId;
        this.empName = builder.empName;
        this.empNo = builder.empNo;
        this.email = builder.email;
        this.phone = builder.phone;
        this.deptCode = builder.deptCode;
        this.jobCode = builder.jobCode;
        this.salary = builder.salary;
        this.bonus = builder.bonus;
        this.managerId = builder.managerId;
        this.hireDate = builder.hireDate;
        this.entDate = builder.entDate;
        this.entYn = builder.entYn;
    }

    // 2) 빌더를 시작하는 정적 메서드
    public static Builder builder() {
        return new Builder();
    }

    // getter 들 (Lombok의 @Getter가 자동 생성해주는 부분을 수동으로 작성)
    public String getEmpId() { return empId; }
    public String getEmpName() { return empName; }
    public String getEmpNo() { return empNo; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDeptCode() { return deptCode; }
    public String getJobCode() { return jobCode; }
    public Integer getSalary() { return salary; }
    public BigDecimal getBonus() { return bonus; }
    public String getManagerId() { return managerId; }
    public LocalDate getHireDate() { return hireDate; }
    public LocalDate getEntDate() { return entDate; }
    public String getEntYn() { return entYn; }

    @Override
    public String toString() {
        return String.format(
                "사번:%-4s 이름:%-5s 부서:%-3s 직급:%-3s 급여:%-10s 입사일:%s",
                empId, empName,
                deptCode == null ? "-" : deptCode,
                jobCode,
                salary == null ? "-" : salary,
                hireDate
        );
    }

    // 3) 내부 정적 클래스 Builder
    public static class Builder {
        private String empId;
        private String empName;
        private String empNo;
        private String email;
        private String phone;
        private String deptCode;
        private String jobCode;
        private Integer salary;
        private BigDecimal bonus;
        private String managerId;
        private LocalDate hireDate;
        private LocalDate entDate;
        private String entYn;

        public Builder empId(String empId) {
            this.empId = empId;
            return this; // 자기 자신(Builder)을 반환해야 메서드 체이닝이 가능하다
        }

        public Builder empName(String empName) {
            this.empName = empName;
            return this;
        }

        public Builder empNo(String empNo) {
            this.empNo = empNo;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder deptCode(String deptCode) {
            this.deptCode = deptCode;
            return this;
        }

        public Builder jobCode(String jobCode) {
            this.jobCode = jobCode;
            return this;
        }

        public Builder salary(Integer salary) {
            this.salary = salary;
            return this;
        }

        public Builder bonus(BigDecimal bonus) {
            this.bonus = bonus;
            return this;
        }

        public Builder managerId(String managerId) {
            this.managerId = managerId;
            return this;
        }

        public Builder hireDate(LocalDate hireDate) {
            this.hireDate = hireDate;
            return this;
        }

        public Builder entDate(LocalDate entDate) {
            this.entDate = entDate;
            return this;
        }

        public Builder entYn(String entYn) {
            this.entYn = entYn;
            return this;
        }

        // 4) build()에서 최종 검증 후 진짜 객체를 생성
        public EmployeeManualBuilder build() {
            if (empId == null || empName == null) {
                throw new IllegalStateException("empId와 empName은 필수입니다.");
            }
            if (jobCode == null) {
                throw new IllegalStateException("jobCode는 필수입니다.");
            }
            return new EmployeeManualBuilder(this);
        }
    }

    // ---- 직접 실행해서 동작을 확인해보는 메인 메서드 ----
    public static void main(String[] args) {

        // 성공 케이스: 선택적 필드(bonus, entDate)는 생략 가능
        EmployeeManualBuilder emp = EmployeeManualBuilder.builder()
                .empId("223")
                .empName("홍길동")
                .empNo("900101-1234567")
                .email("hong@abc.or.kr")
                .phone("01012345678")
                .deptCode("D1")
                .jobCode("J7")
                .salary(3000000)
                .managerId("200")
                .hireDate(LocalDate.of(2024, 1, 2))
                .entYn("N")
                .build();

        System.out.println("생성 성공: " + emp);

        // 실패 케이스: 필수값(empId)을 빼고 build() 호출 -> 예외 발생
        try {
            EmployeeManualBuilder invalid = EmployeeManualBuilder.builder()
                    .empName("이름만있음")
                    .jobCode("J7")
                    .build();
        } catch (IllegalStateException e) {
            System.out.println("예상된 예외 발생: " + e.getMessage());
        }
    }
}
