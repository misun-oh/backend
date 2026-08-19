package ex10.lambda;

public class StudentFilterExample {
    public static void main(String[] args) {
        Student student1 = new Student("홍길동", 92);

        // 점수가 90 이상인지 검사하는 규칙을 람다식으로 즉석에서 정의
        StudentFilter highScoreFilter = student -> student.getScore() >= 90;

        System.out.println(highScoreFilter.test(student1)); // true
    }
}
