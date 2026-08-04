package ex01;

public class App {
    public static void main(String[] args) {
        // 학생 객체를 생성
        // 타입 변수명 = new 타입();
        학생 학생1 = new 학생();
        
        // 필드를 초기화
        학생1.이름 = "오미자";
        학생1.나이 = 20;

        학생 학생2 = new 학생();
        학생2.이름 = "이미자";
        학생2.나이 = 25;

        // 매개변수가 있는 생성자를 이용해서 학생객체를 생성해봅시다
        학생 학생3 = new 학생("미자", 20, "스카이에듀");
        학생 학생4 = new 학생("미순", 25, "스카이에듀");
        
        // 객체를 print 메서를 이용해서 출력하면 toString()메서드가 호출
        // toString()메서드는 패키지를포함한 클래스이름@ 16진수 주소
        // -> 메서드 재정의를 통해서 내가 다시 정의
        System.out.println(학생3);
        System.out.println(학생4);
    }
}
