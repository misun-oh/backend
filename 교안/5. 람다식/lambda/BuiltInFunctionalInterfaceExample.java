package ex10.lambda;

import java.util.Comparator;

public class BuiltInFunctionalInterfaceExample {
    public static void main(String[] args) {
        Runnable task = () -> System.out.println("작업을 실행합니다."); // 매개변수 없으면 빈 괄호
        task.run();

        Comparator<Student> byScore = (a, b) -> a.getScore() - b.getScore(); // 오름차순 비교 규칙
        System.out.println(byScore.compare(new Student("홍길동", 90), new Student("김철수", 85))); // 양수(90-85=5)
    }
}
