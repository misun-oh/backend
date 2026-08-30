import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 사원 한 명의 정보를 담는 클래스 (VO / DTO)
 * EMPLOYEE 테이블의 한 행(row)이 이 객체 하나에 대응된다.
 *
 * 주의: 실무 데이터는 NULL 값이 흔하다.
 *      DEPT_CODE, SALARY, BONUS, MANAGER_ID, ENT_DATE 는 NULL일 수 있으므로
 *      기본 타입(int, double) 대신 래퍼 타입(Integer, BigDecimal)을 사용한다.
 *      기본 타입을 쓰면 NULL이 0으로 둔갑해버려서 "급여 없음"과 "급여 0원"을 구분할 수 없다.
 */
public class Employee {

    private String empId;        // 사원번호 (PK)
    private String empName;      // 직원명
    private String empNo;        // 주민등록번호
    private String email;        // 이메일
    private String phone;        // 전화번호
    private String deptCode;     // 부서코드 (NULL 가능)
    private String jobCode;      // 직급코드
    private Integer salary;      // 급여 (NULL 가능)
    private BigDecimal bonus;    // 보너스율 (NULL 가능)
    private String managerId;    // 관리자 사번 (NULL 가능)
    private LocalDate hireDate;  // 입사일
    private LocalDate entDate;   // 퇴사일 (NULL 가능)
    private String entYn;        // 퇴직여부 (Y/N)

    public Employee(String empId, String empName, String empNo, String email, String phone,
                     String deptCode, String jobCode, Integer salary, BigDecimal bonus,
                     String managerId, LocalDate hireDate, LocalDate entDate, String entYn) {
        this.empId = empId;
        this.empName = empName;
        this.empNo = empNo;
        this.email = email;
        this.phone = phone;
        this.deptCode = deptCode;
        this.jobCode = jobCode;
        this.salary = salary;
        this.bonus = bonus;
        this.managerId = managerId;
        this.hireDate = hireDate;
        this.entDate = entDate;
        this.entYn = entYn;
    }

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
                "사번:%-4s 이름:%-5s 부서:%-3s 직급:%-3s 급여:%-10s 보너스:%-6s 입사일:%-10s 퇴직여부:%s",
                empId,
                empName,
                deptCode == null ? "-" : deptCode,
                jobCode,
                salary == null ? "-" : salary,
                bonus == null ? "-" : bonus,
                hireDate,
                entYn
        );
    }
}
