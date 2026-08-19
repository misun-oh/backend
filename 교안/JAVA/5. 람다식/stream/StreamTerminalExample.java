package ex10.stream;

import java.util.ArrayList;
import java.util.List;

public class StreamTerminalExample {
    public static void main(String[] args) {
        List<Student> students = new ArrayList<>();
        students.add(new Student("홍길동", 92));
        students.add(new Student("김철수", 78));
        students.add(new Student("이영희", 85));

        // forEach: 화면 출력
        students.stream()
                .filter(student -> student.getScore() >= 80)
                .forEach(student -> System.out.println(student.getName() + ": " + student.getScore()));

        // count: 개수 세기
        long count = students.stream()
                .filter(student -> student.getScore() >= 80)
                .count();
        System.out.println("80점 이상 인원: " + count + "명"); // 2명
    }
}
