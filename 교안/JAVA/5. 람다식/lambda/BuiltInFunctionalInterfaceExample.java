package ex10.lambda;

// java.util 패키지에 있는 Comparator 인터페이스를 가져다 씀
// Comparator<T>: 두 객체(T 타입)를 비교해서 정렬 순서를 정하는 규칙을 표현하는 함수형 인터페이스
// 추상 메서드가 int compare(T a, T b) 하나뿐이라서 람다식으로 구현 가능
import java.util.Comparator;

public class BuiltInFunctionalInterfaceExample {
    public static void main(String[] args) {

        // Runnable: 매개변수도 없고 반환값도 없는 작업 하나를 표현하는 표준 함수형 인터페이스
        // 추상 메서드: void run()
        // 람다식으로 구현할 때, 매개변수가 없으므로 빈 괄호 ()를 씀
        Runnable task = () -> System.out.println("작업을 실행합니다.");

        // Runnable은 만들어두기만 하면 실행되지 않음 - run()을 직접 호출해야 위 람다식 본문이 실행됨
        task.run();

        // Comparator<Student>: Student 두 명을 비교하는 규칙을 람다식으로 정의
        // compare(a, b)의 관례: a가 b보다 작으면 음수, 크면 양수, 같으면 0을 반환
        // a.getScore() - b.getScore()는 점수가 높을수록 양수가 나오므로 "오름차순" 비교 규칙이 됨
        Comparator<Student> byScore = (a, b) -> a.getScore() - b.getScore();

        // compare()를 직접 호출해서 두 학생을 비교
        // 홍길동(90점) - 김철수(85점) = 5 (양수) -> "홍길동이 더 크다(순서가 뒤에 온다)"는 뜻
        System.out.println(byScore.compare(new Student("홍길동", 90), new Student("김철수", 85))); // 5
    }
}
