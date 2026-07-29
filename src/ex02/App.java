package ex02;

public class App {
    // 프로그램의 시작
    public static void main(String[] args) {
        // 타입 변수명
        // 생성자를 이용해서 필드를 초기화
        학과 학과1 = new 학과("D002", "컴퓨터공학", 10);
        System.out.println(학과1);

        // new를 만나서 생성자가 실행이 된다!
        학과 학과2 = new 학과("D002", 0);
        System.out.println(학과2);
    }
}
