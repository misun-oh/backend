package ex10.lambda;

import java.util.List;
import java.util.ArrayList;

public class LambdaWithListExample {
    public static void main(String[] args) {
        List<Student> students = new ArrayList<>();
        students.add(new Student("홍길동", 92));
        students.add(new Student("김철수", 78));
        students.add(new Student("이영희", 85));

        // forEach: 반복문 대신 람다식으로 각 요소에 대해 동작 수행
        students.forEach(student -> System.out.println(student.getName() + ": " + student.getScore()));

        // sort: Comparator 람다로 정렬 기준을 즉석에서 정의 (점수 오름차순)
        students.sort((a, b) -> a.getScore() - b.getScore());

        System.out.println("정렬 후:");
        students.forEach(student -> System.out.println(student.getName() + ": " + student.getScore()));
    }
}
