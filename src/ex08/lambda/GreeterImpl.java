package ex08.lambda;

// implements : 인터페이스 구현체 만들기
public class GreeterImpl implements Greeter {

    @Override
    public void greet(String name) {
        System.out.println(name + "님 환영합니다.");
    }

}
