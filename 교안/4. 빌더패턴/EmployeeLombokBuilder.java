import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Lombok @Builder를 사용한 버전.
 * Employee.java와 동작은 완전히 동일하지만,
 * Builder 내부 클래스 / builder() / 세터들 / build()를 Lombok이 자동 생성해준다.
 *
 * 실행하려면 pom.xml(또는 build.gradle)에 lombok 의존성이 추가되어 있어야 한다.
 */
@Getter
@Builder
@ToString
public class EmployeeLombokBuilder {

    @NonNull
    private final String empId;

    @NonNull
    private final String empName;

    private final String empNo;
    private final String email;
    private final String phone;
    private final String deptCode;

    @NonNull
    private final String jobCode;

    private final Integer salary;
    private final BigDecimal bonus;
    private final String managerId;
    private final LocalDate hireDate;
    private final LocalDate entDate;
    private final String entYn;

    // ---- 직접 실행해서 동작을 확인해보는 메인 메서드 ----
    public static void main(String[] args) {

        // 성공 케이스: 선택적 필드(bonus, entDate)는 생략 가능
        EmployeeLombokBuilder emp = EmployeeLombokBuilder.builder()
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

        // 실패 케이스: @NonNull이 붙은 empId를 빼고 build() 호출 -> NullPointerException 발생
        try {
            EmployeeLombokBuilder invalid = EmployeeLombokBuilder.builder()
                    .empName("이름만있음")
                    .jobCode("J7")
                    .build();
        } catch (NullPointerException e) {
            System.out.println("예상된 예외 발생: " + e.getMessage());
        }
    }
}
