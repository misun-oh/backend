package ex06.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.kh.inherit.practice.Employee;

public class App {
    public static void main(String[] args) {
        // 클래스 -> new 클래스 -> 객체 
        // 1. 객체로 부터 클래스의 정보를 조사
        Employee emp = 
            new Employee("미자", 22, 150, 55, 1000000, "");
        
        
        Class<?> clazz = emp.getClass();
        // 선언된 필드의 목록을 조회
        Field[] fields = clazz.getDeclaredFields();
        for(Field field : fields){
            // 접근제한자 -> 숫자에서 문자로 
            String modifier = Modifier.toString(field.getModifiers()); // 정수를 "private" 같은 문자열로 변환
            // 필드의 선언부
            System.out.println("필드 선언부 : " + modifier + " " + field.getType() + " " + field.getName());

            // 어노테이션이 붙어 있는지 확인
            if(field.isAnnotationPresent(Required.class)){
                System.out.println("Required 어노테이션이 붙어 있는 필드");
                System.out.println("필수 입력 입니다!!");
            }
        }



        
    }
}
