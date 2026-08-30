package ex10.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StreamCollectorsExample {
    public static void main(String[] args) {
        List<Student> students = new ArrayList<>();
        students.add(new Student("홍길동", 92));
        students.add(new Student("김철수", 78));
        students.add(new Student("이영희", 85));

        // joining: 이름들을 ", "로 이어붙인 하나의 문자열로 만들기
        String names = students.stream()
                .map(student -> student.getName())
                .collect(Collectors.joining(", "));
        System.out.println(names); // 홍길동, 김철수, 이영희

        // groupingBy: 합격(90점 이상)/불합격으로 묶어서 Map으로 모으기
        Map<Boolean, List<Student>> grouped = students.stream()
                .collect(Collectors.groupingBy(student -> student.getScore() >= 90));

        System.out.println("합격: " + grouped.get(true).size() + "명");   // 1명
        System.out.println("불합격: " + grouped.get(false).size() + "명"); // 2명
    }
}
