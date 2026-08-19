package ex10.lambda;

public class LambdaExample {
    public static void main(String[] args) {
        // 1번의 익명 클래스 버전과 완전히 같은 동작을 하는 람다식
        Greeter greeter = (String name) -> {
            System.out.println("안녕하세요, " + name + "님!");
        };

        greeter.greet("홍길동");

        // 축약 형태 (완전히 동일하게 동작)
        Greeter greeter2 = name -> System.out.println("안녕, " + name);
        greeter2.greet("김철수");
    }
}
