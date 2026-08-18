package ex09.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.imageio.plugins.bmp.BMPImageWriteParam;

import org.apache.ibatis.javassist.bytecode.ByteArray;

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

    public void fileByteStream(){

        // 선언부를 try문 밖으로 이동
        FileOutputStream fos = null;
        // 메모리 -> 디스크
        try {
            // name : 파일의 경로
            // append : 이어쓰기 여부 (기본값(false) : 덮어쓰기, true : 이어쓰기)
            fos = new FileOutputStream("a_byte.dat");
            // 숫자에 대응하는 아스키 코드 문자열이 파일에 저장됨
            fos.write(97);
            fos.write('A');
            fos.write(10);

            // 한글의 경우 2-3바이트를 이용하므로 글자 깨짐 현상이 발생
            fos.write('한');
            
            // 스트림객체를 사용한 경우, 닫아주지 않으면 
            // [이미 사용중인 파일 입니다] 오류 발생
            // fos.close(); // 중간에 오류가 발생 하면 실행 안됨!!!!
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally { 
            // 자원의 반납/해제
            try {
                if(fos != null) fos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


    }

    public void fileByteStream1(){
        // 스트림을 이용하면 닫아 주어야 하기때문에 
        // try ()안에서 객체를 생성
        try (FileOutputStream fos = new FileOutputStream("a_byte.dat")){
            // int 타입을 매개변수로 받고 있음
            // char -> int 자동형변환
            fos.write('a');
            fos.write('b');
            fos.write('c');
            fos.write(10); // 줄바꿈
            
            byte[] arr= {'a', 'b', 'c', 'd'}; // -> 아스키코드표에 대응하는 숫자값이 저장
            System.out.println("arr : " + Arrays.toString(arr) );


            byte[] arr2 = new byte[5];
            // byte에 입력가능 범위 
            // 8비트 -128~127
            // 1비트 - 부호
            // 7비트 숫자를 표험
            arr2[0] = 'a'; // 
            
            // 아스키코드표에 대응하는 문자가 파일에 출력
            fos.write( arr);

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

    public void fileRead(){
        // 파일에서 데이터를 바이트 단위로 읽어온다
        try (FileInputStream fis = new FileInputStream("a_byte.dat");){
            // System.out.println((char)fis.read());
            // System.out.println(fis.read(new byte[100]));

            // fis.read() 1바이트씩 읽어서 값을 반환
            // 파일을 다 읽었으면 -1을 반환 
            // -1이면 반복문을 탈출
            System.out.println("-------------");
            int value = 0;
            while ((value = fis.read()) != -1) {
                System.out.print((char)value);
            }
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public void charSave(){
        System.out.println("문자입출력 ------------");
        
        // 출력하는 역할
        // 생성자를 통해 인코딩을 설정
        // UTF_8 전세계 언어 표현
        try (FileWriter fw = new FileWriter("b_char.txt", StandardCharsets.UTF_8, true);){
            fw.write('A');
            fw.write(100);
            fw.write('\n'); // 줄바꿈
            fw.write("IO 스트림을 이용한 파일 입출력");
            // 기본타입은 소문자로 시작
            // 클래스 대문자로 시작 = String
            fw.write(new char[]{'a', 'b', 'c', 'd'});

            // 인코딩 확인
            System.out.println("window 기본인코딩 : " + fw.getEncoding());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public void charRead(){
        System.out.println("charRead===========");
        try (FileReader fr = new FileReader("b_char.txt", StandardCharsets.UTF_8);){
            int value = 0;
            // 데이터를 문자 단위로 읽어온다
            while((value = fr.read()) != -1){
                System.out.print((char)value);
            }

            System.out.println(fr.getEncoding());
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }


    public static void main(String[] args) {
        // 인스턴스 메서드는 생성해서 사용해야 한다!!
        MyFile myFile = new MyFile();
        // myFile.method1();
        
        // 정적 메서드는 생성하지 않고 클래스 이름으로 접근
        //MyFile.method1();

        // 파일 write
        // myFile.fileByteStream();
        //myFile.fileByteStream1();
        
        // 파일 Read
        // myFile.fileRead();

        // 문자 입출력
        // 인스턴스 메서드 호출
        //myFile.charSave();

        // 문자 Read
        myFile.charRead();

    }
}
