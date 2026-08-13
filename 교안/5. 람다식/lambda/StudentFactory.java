package ex10.lambda;

@FunctionalInterface
public interface StudentFactory {
    Student create(String name, int score); // Student(String, int) 생성자와 매개변수 구성이 같음
}
