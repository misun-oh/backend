package ex08.lambda;

public class App {

    // 접근제한자 생략 - default (패키지안에서 사용가능)
    static int operate(int a, int b, Calc calc){
        // 인터페이스의 함수를 호출
        return calc.calc(a, b);
    }

    public static void main(String[] args) {

        /*
        람다식
        함수를 매개변수로 전달하는 간결한 문법
        자바스크립트의 화살표 함수와 비슷하게 생김!
        */

        // 인터페이스의 구현 클래스를 이용해서 호출
        GreeterImpl greeterImpl = new GreeterImpl();
        greeterImpl.greet("미자");

        // 인터페이스는 생성이 불가능 => 추상 메서드!!
        // 추상 메서드를 구현 하면 생성 할 수 있다
        // 익명의 클래스
        Greeter greeter = new Greeter() {

            @Override
            public void greet(String name) {
                System.out.println("익명의클래스를 이용해서 인터페이스를 구현");
                System.out.println(name + "님 반갑습니다.");
            }
            // 추상메서드 구현
        };

        greeter.greet("미자");
        
        // 람다식은 (매개변수) -> { 실행문 } 형태로, 
        // 익명 클래스보다 훨씬 짧게 함수형 인터페이스를 구현하는 문법입니다.
        Greeter greeter2 = (String name) -> {
            System.out.println(name + "님 환영 합니다.");
        };

        // 매개변수의 타입 생략 가능 (추상메서드가 하나밖에 없기 때문에 추론이 가능!!!!)
        // 매개변수가 하나이면 괄호가 생략 가능 
        // 실행문이 한줄이면 코드블럭 생략가능(반환도 생략가능)
        Greeter greeter3 = name -> System.out.println(name + "님 환영 합니다.");

        System.out.println("========================================");
        
        // 람다식을 매개변수로 전달해서 호출
        // 1. 인터페이스 - 추상메서드가 하나인 Calc.calc(int a, int b)
        // 2. 인터페이스를 매개변수로 받아서 실행하는 메서드 만들기
        // 3. 람다식을 이용해서 매개변수에 함수(메서드)를 전달
        // 4. 메서드를 호출해서 결과를 확인
        
        // 익명의 클래스를 이용해서 메서드를 정의
        int res = operate(1, 2, new Calc() {

            @Override
            public int calc(int a, int b) {
                return a * b;
            }
            
        });
        
        System.out.println("1 * 2 = " + res);
        int res1 = operate(10,20,(a,b)-> {return a+b;});
        System.out.println("10 + 20 = "+res1);
        operate(0,0,(a,b)-> a+b);
        

    }

}

// 하나의 파일에 여러개의 클래스/인터페이스를 정의 하는 경우 public은 하나에만 사용할 수 있다
@FunctionalInterface
interface Calc {
    int calc(int a, int b);
}

