package ex09.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class EmpFileStreamExample {
    public static void main(String[] args) throws IOException {
        // EmpFileStore.save()를 그대로 재사용해서 데이터 파일을 준비
        List<Emp> employees = List.of(
                new Emp("223", "홍길동", 3000000),
                new Emp("310", "김철수", 2800000),
                new Emp("402", "이영희", 3500000)
        );
        EmpFileStore.save(employees, "employees.csv");

        try (BufferedReader reader = new BufferedReader(new FileReader("employees.csv", StandardCharsets.UTF_8))) {
            // lines(): 파일의 각 줄을 Stream<String>으로 반환
            List<String> highEarners = reader.lines()
                    .map(line -> line.split(","))                                          // 한 줄 -> 필드 배열
                    .map(fields -> new Emp(fields[0], fields[1], Integer.parseInt(fields[2]))) // 필드 배열 -> Emp 객체
                    .filter(emp -> emp.getSalary() >= 3000000)                               // 급여 300만원 이상만
                    .map(emp -> emp.getEmpName() + ": " + emp.getSalary() + "원")            // 출력용 문자열로 변환
                    .collect(Collectors.toList());

            highEarners.forEach(System.out::println);
        }
    }
}
