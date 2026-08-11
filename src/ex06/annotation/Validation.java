package ex06.annotation;

import java.lang.reflect.Field;

public class Validation {
    // object : 모든 타입
    // 오브젝트를 매개변수로 받아서
    // 필드정보를 정보를 확인
    // Required 어노테이션이 붙어 있는지 확인
    public static void 검증(Object obj) throws Exception{
        // 객체로 부터 클래스 정보 얻어오기
        Class<?> clazz =  obj.getClass();
        
        Field[]  fields = clazz.getDeclaredFields();
        for(Field field : fields){

        }

        for(Field field : clazz.getDeclaredFields()){
            // 선언된 어노테이션의 클래스 정보를 매개변수로 넣어서 어노테이션이 붙어 있는지 확인
            //boolean res = field.isAnnotationPresent(Required.class);
            if(field.isAnnotationPresent(Required.class)){
                // 필수체크 어노테이션이 붙은 컬럼
                // 필드에 값이 들어 있는지 확인
                // 객체로 부터 해당 필드의 값을 읽어온다

                // private필드는 접근할수 없다
                field.setAccessible(true);
                Object o = field.get(obj);
                System.out.println("o : " + o);
                // 참조타입일때 
                if(o == null){
                    System.out.println(field.getName() + "필수 입력 항목 입니다.");
                }
                
                // int일때 
                // int타입의 기본값은 0
                // 값이 0인지 아닌지 비교
                
                // int 타입이면
                if(field.getType() == int.class && (int)o == 0){
                    System.out.println(field.getName() + "필수 입력 항목 입니다.");
                    throw new Exception(field.getName() + " 필수 입력 항목 입니다.");
                }
            }
        }
    }
}
