// 패키지 선언부
package com.kh.object.practice;

// 클래스 선언부
public class NonStaticSample {
    // 필드
    
    // 메서드 선언부
    // 접근제한자 반환타입 메서드명(매개변수타입 매개변수명){}
    public void printLottoNumber(){
        // 자바 표준 출력
        // 콘솔창에 출력할때 사용
        System.out.print("printLottoNumber()가 호출"); // 줄바꿈 없이 출력
        System.out.println(); // 출력후 줄바꿈
        System.out.printf(""); // 형식을 이용한 출력
    }

    public void outputChar(int num, char c){
        // return ""
        // return ''
        // return 0
    }

    // 반환타입을 적고 return을 안하면 오류가 발생
    public char alphabette(){

        return '가';
    }    

    public String mySubString(String str, int index1, int index2){
        return "";
    }


    // 생성자

    
}
