package ex05.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 테이블에서 조회된 값을 담기 위해 필드를 선언한다

// lombok 라이브 러리를 사용하면 어노테이션(@)을 이용해서 
// set, get, 생성자를 자동으로 생성 할 수 있다!!!

// setter, getter, toString 재정의
@Data
// 매개변수 생성자
@AllArgsConstructor
// 기본생성자
@NoArgsConstructor
public class DeptDTO {
    private String deptId;
    private String deptCode;
    private String locationId;
}
