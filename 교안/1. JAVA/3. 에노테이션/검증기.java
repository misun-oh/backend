package ex06;





import java.lang.reflect.Field;

public class 검증기 {
    public static void 검증(Object obj) throws Exception {
        Class<?> clazz = obj.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(필수입력.class)) {
                field.setAccessible(true);
                Object value = field.get(obj);

                if (value == null) {
                    System.out.println(field.getName() + "은(는) 필수 입력값입니다.");
                }
            }
        }
    }
}