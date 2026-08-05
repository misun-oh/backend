package ex04.obj;

public class App {
    public static void main(String[] args) {
        Student s1 = new Student("이미자", "20260111111");
        Student s2 = new Student("이미자", "20260111111");

        if(s1.equals(s2)){
            System.out.println("같은 객체 입니다.");
        } else {
            System.out.println("다른 객체 입니다.");
        }

        // String객체의 equals 메서드도 재정의 되어 있다!!!! 
        // 같은 문자열이면 같다고 판단!!!!!
        //"".equals(s2);
    }
}
