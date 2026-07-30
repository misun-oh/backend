package com.kh.util;

import java.util.Scanner;

public class InputUtil {

    private static Scanner scan = new Scanner(System.in);
    
    // 생성자 접근 불가능 하도록 
    // 반환없다 클래스명과 동일하다!
    // 기본 생성자
    private InputUtil(){
        System.out.print("기본생성자 접근 금지!");
    }

    // 메서드 선언부
    // 접.제 반환타입 이름(매.타 매.이){}
    // 메세지를 출력하고 스캐너로 부터 입력 받아서 문자열을 반환 하는 메서드 getString
    public static String getString(String msg){
        System.out.print(msg);
        while (true) {
            String text = "";
            // trim(): 앞뒤 공백 제거
            text = scan.nextLine().trim();
            System.out.println("사용자 입력값 : " + text);
            if(text.isEmpty()){
                // 남은 코드블럭을 실행하지 않고
                // 다음 반복문으로 넘어가기
                continue;
            }

            // 메서드는 반환을 만나면 끝남
            // 나를 호출한곳으로 반환값을 전달
            return text;
        }
    }

    // 스캐너로 부터 입력 받아서 정수를 반환 하는 메서드 getInt
    
    public static int getInt(String msg){
        int i = 0;
        while (true) {
            System.out.print(msg);
            try {
                i = scan.nextInt();
                // 숫자만 가지고 가서 엔터가 남아 있음 -> nextLine으로 엔터 제거
                scan.nextLine();
            } catch (Exception e) {
                System.out.println("숫자만 입력이 가능합니다.");
                // 오류가 발생 했을때 입력값이 그대로 남아 있어서 입력값 제거
                scan.nextLine();
                continue;
            }
            System.out.println("사용자 입력값 : " + i);
            return i;
        }
    }




    public static void main(String[] args) {
        // 정적필드, 정적메서드 
        // 클래스명.필드명, 클래스명.메서드명
        InputUtil.getString("이름 : ");
        InputUtil.getInt("나이 : ");
        InputUtil.getString("주소 : ");
    }
}
