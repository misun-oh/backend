// 패키지
// 클래스의 일부이며 클래스를 식별하는 용도
// 상위 패키지, 하위 패키지로 구분 (.으로)
// 패키지 선언은 최상단에 위치하며 패키지가 잘못 작성된 경우 오류 발생
package com.kh.object;

import com.kh.object.practice.NonStaticSample;

// 다른패키지에 있는 클래스를 사용 하려면 import문을 이용해서 
// 어떤 패키지의 어떤 클래스 인지 명시 해야한다
// import문은 패키지의 선언과 클래스의 선언 사이에 작성
// 자동완성시 임포트문이 자동으로 완성되지만 코드를 따라친 경우 안나옴!!!


// 클래스의  선언부
public class App {

    // 프로그램의 시작
    // -> 없으면 실행 안됨!!!!
    public static void main(String[] args) {
        
        // 1. 객체 생성 : 클래스(설계도)를 통해서 객체를 생성
        // 타입 변수명 = new 타입();
        // new 연산자를 통해 생성자를 호출하여 객체를 생성하고 변수에 담아준다
        // 클래스 -> 인스턴스(메모리에 올라가서 사용가능 상태)
        // 1. 빠른수정, 2. 단축키(alt+shift+o), 3. 직접 작성
        NonStaticSample sample = new NonStaticSample();
        

        // 변수명에 .을 찍으면 객체가 가지고 있는 속성, 메서드에 접근할 수 있다
        // 리소스 찾기(ctrl+p)
        sample.printLottoNumber();

    }

}
