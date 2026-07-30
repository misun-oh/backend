package ex03.method;

import java.util.Scanner;

import ex03.method.obj.Basic;

public class MenuApp {
    public static void main(String[] args) {

        

    
        // 사용자의 입력을 받아오는 객체
        Scanner scan = new Scanner(System.in);

        while (true) {
            // 1. 메뉴를 출력
            Basic.printMenu();
            // 2. 사용자 입력 받기
            int menu = 0; // 선언과 초기화

            // 숫자가 입력될때까지 반복
            while(true){
                try {
                    menu = scan.nextInt();
                    break;
                } catch (Exception e) {
                    System.out.println("숫자만 입력이 가능 합니다.");
                    // 오류가 발생되면 입력된값이 남아 있어 무한 반복
                    scan.nextLine();
                }
            }

            System.out.println("menu : " + menu);
            // 3. 입력에 따라서 메서드를 실행
            if(menu == 1){
                // bmi
                // 키, 몸무게
                
                while(true){
                    try {
                        menu = scan.nextInt();
                        break;
                    } catch (Exception e) {
                        System.out.println("숫자만 입력이 가능 합니다.");
                        // 오류가 발생되면 입력된값이 남아 있어 무한 반복
                        scan.nextLine();
                    }
                }

                System.out.println("키를 입력해주세요");

                System.out.println("몸무계를 입력해주세요");
            } else if(menu == 2) {
                // 로또 생성기
                Basic.getLotto();
            } else if(9 == menu){
                // 프로그램 종료
                System.exit(0);
            } else {
                System.out.println("메뉴를 확인후 다시 입력 해주세요");
            }
        }
        
    }
}
