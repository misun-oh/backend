package ex09.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class EmpFileStore {
    // 사원 목록을 "사번,이름,급여" 형태로 한 줄씩 저장
    public static void save(List<Emp> employees, String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, StandardCharsets.UTF_8))) {
            for (Emp emp : employees) {
                writer.write(emp.getEmpId() + "," + emp.getEmpName() + "," + emp.getSalary());
                writer.newLine();
            }
        }
    }

    // 저장된 파일을 다시 읽어서 사원 목록으로 복원
    public static List<Emp> load(String fileName) throws IOException {
        List<Emp> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(","); // 콤마 기준으로 필드 단위로 쪼갬
                result.add(new Emp(fields[0], fields[1], Integer.parseInt(fields[2])));
            }
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        List<Emp> employees = List.of(
                new Emp("223", "홍길동", 3000000),
                new Emp("310", "김철수", 2800000)
        );

        save(employees, "employees.csv");
        System.out.println("저장 완료: " + new java.io.File("employees.csv").getAbsolutePath()); // 진단용: 실제 저장 위치 출력

        List<Emp> loaded = load("employees.csv");
        for (Emp emp : loaded) {
            System.out.println("사번:" + emp.getEmpId() + " 이름:" + emp.getEmpName() + " 급여:" + emp.getSalary());
        }
    }
}
