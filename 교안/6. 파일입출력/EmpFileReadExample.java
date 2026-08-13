package ex09.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EmpFileReadExample {
    public static void main(String[] args) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("emp_list.txt", StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) { // 더 이상 읽을 줄이 없으면 null
                System.out.println("읽은 사원명: " + line);
            }
        }
    }
}
