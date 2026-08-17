package ex08.lambda;

/**
 * 함수형 인터페이스
 * - 람다식을 이용해서 사용
 * - 추상메서드가 한개여야함!!!!
 * Greeter
 */
// FunctionalInterface : 인터페이스에 선언된 추상메서드의 갯수를 체크
@FunctionalInterface
public interface Greeter {
    void greet(String name);
    // void greet1(String name);
    
}
