package ex03.method;

import ex03.method.obj.Basic;

/*
정적 메서드/ 필드
프로그램이 시작할때 메모리에 미리 올라가며, 생성하지 않고 사용
사용방법 : 클래스명.메서드명, 클래스명.필드명
 */ 
public class StaticBmiApp {

    // 정적메서드에서는 정적메서드만 호출이 가능하다!!!
    public static void main(String[] args) {
        // 생성하지 않고 사용할 수 있다
        double bmi = Basic.getBmi(1.63, 55);
        String bmiStr = Basic.bmiToStr(bmi);
        System.out.println(bmi);
        System.out.println(bmiStr);

        
    }
}
