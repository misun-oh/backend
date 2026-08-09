package ex06;

import ex02.학과;
import ex05.dao.EmpDao;
import java.lang.reflect.Field;

public class 실행 {
    public static void main(String[] args) {


        System.out.println("class : "+EmpDao.class );

        학과 학과1 = new 학과("컴퓨터공학", 40);
        
        Field[] fields = 학과1.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(필수입력.class)) {
                System.out.println(field.getName() + " 필드는 @필수입력이 붙어있습니다.");
            }
        }

        

        Class<?> clazzA = 학과1.getClass(); // 객체가 있어야 호출 가능
        Class<?> clazzB = 학과.class;       // 객체 없이, 클래스 이름만으로 바로 얻음

        System.out.println(clazzA == clazzB); // true! 같은 클래스를 가리키므로 결과는 항상 같음

        학과 학과2 = new 학과(null, 40);
        try {
            검증기.검증(학과2);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
