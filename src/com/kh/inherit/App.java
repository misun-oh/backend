package com.kh.inherit;

import com.kh.inherit.practice.Employee;
import com.kh.inherit.practice.Student;
import com.kh.util.InputUtil;

public class App {
    public static void main(String[] args) {
        
        // 3명의 학생 정보를 기록 할수 있는 배열을 생성
        Student[] students = new Student[3];
        students[0] = 
            new Student("홍길동", 20, 178.2, 70.0, 1, "정보시스템공학과");
        students[1] = 
            new Student("김말똥", 21, 187.3, 80.0, 2, "경영학과");
        students[2] = 
            new Student("강개순", 23, 167, 45, 4, "정보통신공학과");

        for(Student s:students){
            // toString() 실행결과 반환받은 문자열을 출력
            System.out.println(s);
        }

        // 2명의 사원 정보를 기록 할수 있는 배열을 생성


        // 10명의 사원정보를 기록할수 있는 배열을 생성
        // [] 방의 갯수 = 배열의 길이
        // 10개, 인덱스는 0~9까지, 0~(n-1)
        Employee[] emps = new Employee[10];    
        emps[0] = 
            new Employee("박보검", 28, 180.3, 72, 1000000, "영업부");
        System.out.println(emps[0]);

        int index = 0;
        while (true) {
            
            // 키보드로 부터 사원의 정보를 입력 받아 배열에 저장 합니다.
            String name = InputUtil.getString("이름 : ");
            int age = InputUtil.getInt("나이 : ");
            int salary = InputUtil.getInt("급여 : ");
            String dept = InputUtil.getString("부서 : ");
            double height = InputUtil.getDouble("키 : ");
            double weight = InputUtil.getDouble("몸무게 : ");
            //System.out.println("name : "+name);
            //System.out.println("age : "+age);
    
            // 사용자의 입력값을 변수로 저장해 두었다가 객체를 생성할때 사용
            emps[index] = new Employee(name, age, height, weight, salary, dept);
            System.out.println(emps[index]);

            String res = InputUtil.getString("계속 사원을 등록 하시겠습까?(Y/N)");
            // y가 아니면 반복문 탈출
            if(!res.equalsIgnoreCase("Y")){
                break;
            }

            index++;
        }



    }
}
