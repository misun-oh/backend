package ex03.method;

import ex03.method.obj.Basic;

//import ex03.method.obj.*;

public class App {
    public static void main(String[] args) {
        // 패키지가 다른경우 import문을 작성!! -> 자동완성
        // Basic 객체 생성하기
        Basic basic = new Basic();
        // + info(name:String, age:int, height:double):void
        // 이름, 나이, 키를 받아서 출력
        // 메서드의 선언부 정의
        basic.info("이미자", 25, 159.3);
        // + getInfo(name:String, age:int, height:double):String
        // 이름, 나이, 키를 받아서 한줄로 반환
        String basic_info = basic.getInfo("오미자", 22, 160.35);
        // 반환 받은 값을 변수에 저장해서 출력
        System.out.println(basic_info);

        // + getBmi(키-m:double, 몸무계-kg:double):double
        // 메서드를 호출할때는 파라메터만 넘겨주면 된다
        double bmi = basic.getBmi(1.63, 55.5);
        String str = basic.bmiToStr(bmi);
        System.out.println(str);
    }

}
