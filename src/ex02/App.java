package ex02;

public class App {
    public static void main(String[] args) {
        // 타입 변수명
        // 생성자를 이용해서 필드를 초기화
        학과 학과1 = new 학과("D001", "컴퓨터공학", 10);
        System.out.println(학과1);

        학과 학과2 = new 학과("D002", "철학과", 20);
        System.out.println(학과2);
    }
}
