package ex10.lambda;

public class AnonymousClassExample {
    public static void main(String[] args) {
        // 이름 없는 클래스를 그 자리에서 즉석으로 만들어 Greeter를 구현
        Greeter greeter = new Greeter() {
            @Override
            public void greet(String name) {
                System.out.println("안녕하세요, " + name + "님!");
            }
        };

        greeter.greet("홍길동");
    }
}
