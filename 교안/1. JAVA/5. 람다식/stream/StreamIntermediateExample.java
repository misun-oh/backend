package ex10.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StreamIntermediateExample {
    public static void main(String[] args) {
        List<Student> students = new ArrayList<>();
        students.add(new Student("홍길동", 92));
        students.add(new Student("김철수", 78));
        students.add(new Student("이영희", 85));

        // filter + map + sorted를 연결: 80점 이상 학생을 이름만 뽑아 점수 내림차순으로
        List<String> result = students.stream()
                .filter(student -> student.getScore() >= 80)                 // 80점 이상만
                .sorted((a, b) -> b.getScore() - a.getScore())               // 점수 내림차순 정렬
                .map(student -> student.getName())                          // 이름만 추출
                .collect(Collectors.toList());

        System.out.println(result); // [홍길동, 이영희]
    }
}
