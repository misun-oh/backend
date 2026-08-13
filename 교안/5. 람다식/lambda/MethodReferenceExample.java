package ex10.lambda;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class MethodReferenceExample {
    public static void main(String[] args) {
        // static 메서드 참조: Integer.parseInt(s)를 그대로 넘김
        Function<String, Integer> toNumber = Integer::parseInt;
        System.out.println(toNumber.apply("100")); // 100

        List<String> names = List.of("홍길동", "김철수", "이영희");

        // 특정 객체의 인스턴스 메서드 참조: System.out이라는 정해진 객체의 println을 그대로 넘김
        names.forEach(System.out::println); // name -> System.out.println(name)과 동일

        List<Student> students = new ArrayList<>();
        students.add(new Student("홍길동", 92));
        students.add(new Student("김철수", 78));

        // 임의 객체의 인스턴스 메서드 참조: 매개변수로 들어오는 객체의 getScore()를 호출
        students.sort(Comparator.comparing(Student::getScore)); // (a, b) -> a.getScore() - b.getScore()와 사실상 같은 정렬
        students.forEach(student -> System.out.println(student.getName() + ": " + student.getScore()));
    }
}
