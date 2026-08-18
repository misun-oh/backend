package ex09.file;

import java.io.File;
import java.io.IOException;

public class MyFile {
    public static void method1(){
        try {
            // 파일 클래스 - 메모리상에만 존재하는 객체
            File f = new File("test1.txt");
            
            System.out.println("파일명 : " + f.getName());
            System.out.println("파일 상대경로 : " + f.getPath()); // 프로젝트 루트폴더
            System.out.println("파일 절대경로 : " + f.getAbsolutePath());
            System.out.println("파일용량 : " + f.length());
            System.out.println("존재여부" + f.exists());

            f.createNewFile();

            File f1 = new File("D:/test1.txt"); // 만약 해당 경로가 존재하지 않으면 오류
            // 파일 생성
            f1.createNewFile();

            
            // File f2 = new File("D:/bbb/test1.txt"); // 만약 해당 경로가 존재하지 않으면 오류
            // f2.createNewFile();

            File f3 = new File("d:/bbb");

            // 첨부파일을 저장할때
            // 파일의 이름이 중복될 경우 -> 파일이 소실될수 있다!!!
            // ----> 폴더를 나눠서 저장, 파일명_uuid.txt
            // 폴더가 존재하지 않으면 폴더를 생성
            if(!f3.exists()){
                f3.mkdir(); // 폴더 생성
            }
            File f2 = new File("D:/bbb/test1.txt"); // 만약 해당 경로가 존재하지 않으면 오류
            f2.createNewFile();

            System.out.println("f2 존재 여부 : " + f2.exists());
			System.out.println("f2.isFile() : " + f2.isFile());
			System.out.println("f3.isFile() : " + f3.isFile());
			System.out.println("f3.isDirectory() : " + f3.isDirectory());


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // 실제 파일을 생성 - 프로젝트 루트폴더 파일을 생성한다

    }

    public static void main(String[] args) {
        // 인스턴스 메서드는 생성해서 사용해야 한다!!
        // MyFile myFile = new MyFile();
        // myFile.method1();
        
        // 정적 메서드는 생성하지 않고 클래스 이름으로 접근
        MyFile.method1();
    }
}
