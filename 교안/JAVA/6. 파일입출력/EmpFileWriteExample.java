package ex09.file;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EmpFileWriteExample {
    public static void main(String[] args) throws IOException {
        List<String> empNames = List.of("홍길동", "김철수", "이영희");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("emp_list.txt", StandardCharsets.UTF_8))) {
            for (String name : empNames) {
                writer.write(name);
                writer.newLine(); // 줄바꿈
            }
        }

        System.out.println("파일 저장 완료: emp_list.txt");

        compareWriteSpeed();
    }

    // 버퍼를 사용했을 때/안 했을 때 쓰기 속도를 실제로 재서 비교
    private static void compareWriteSpeed() throws IOException {
        int lineCount = 50000;

        // 1) 버퍼 없이 FileWriter로 직접 쓰기 -> write() 호출마다 매번 디스크에 접근
        long start1 = System.currentTimeMillis();
        try (FileWriter writer = new FileWriter("no_buffer.txt", StandardCharsets.UTF_8)) {
            for (int i = 0; i < lineCount; i++) {
                writer.write("사원" + i + "\n");
            }
        }
        long noBufferMs = System.currentTimeMillis() - start1;

        // 2) BufferedWriter로 감싸서 쓰기 -> 버퍼에 모았다가 한꺼번에 디스크에 접근
        long start2 = System.currentTimeMillis();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("with_buffer.txt", StandardCharsets.UTF_8))) {
            for (int i = 0; i < lineCount; i++) {
                writer.write("사원" + i);
                writer.newLine();
            }
        }
        long bufferMs = System.currentTimeMillis() - start2;

        System.out.println();
        System.out.println(lineCount + "줄 쓰기 속도 비교");
        System.out.println("버퍼 없이 (FileWriter만): " + noBufferMs + "ms");
        System.out.println("버퍼 사용 (BufferedWriter): " + bufferMs + "ms");
    }
}
