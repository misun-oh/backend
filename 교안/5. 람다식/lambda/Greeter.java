package ex10.lambda;

@FunctionalInterface // 이 인터페이스가 함수형 인터페이스임을 명시 (필수는 아니지만 실수 방지에 유용)
public interface Greeter {
    void greet(String name); // 추상 메서드가 정확히 1개
}
