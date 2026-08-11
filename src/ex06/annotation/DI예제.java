package ex06.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 3_DI컨테이너만들기_교안.md 4-1절 클래스 다이어그램을 그대로 코드로 옮긴,
 * 실행 가능한 전체 예제. 학과/교수/학생/주입/컨테이너를 한 파일에 모아서
 * javac DI예제.java && java DI예제 로 바로 돌려볼 수 있다.
 */
public class DI예제 {

    // ---- 직접 실행해서 동작을 확인해보는 메인 메서드 (교안 4장 코드와 동일) ----
    public static void main(String[] args) throws Exception {

        // 객체를 미리 생성해서 컨테어너에 보관 
        // 주입이라는 어노테이션이 붙어 있으면 내가 가지고 있는 객체를 필드에 주입(setter)

        // 객체를 미리 생성해서 보관 
        //   - 어떤 패키지에 있는 어노테이션을 확인할지 정의
        //   - 어노테이션을 읽어서 컨테이너에 객체를 생성
        컨테이너 컨테이너 = new 컨테이너();

        학과 컴공 = new 학과("컴퓨터공학과");
        교수 김교수 = new 교수("김교수");

        컨테이너.등록(컴공);  // "학과 타입이 필요하면 이 객체를 써라"
        컨테이너.등록(김교수); // "교수 타입이 필요하면 이 객체를 써라"

        학생 학생1 = new 학생("홍길동"); // 소속학과, 지도교수는 아직 비어있음(null)
        컨테이너.주입(학생1);            // 컨테이너가 @주입 필드를 자동으로 채워줌

        System.out.println(학생1.get소속학과().get학과명()); // 컴퓨터공학과
        System.out.println(학생1.get지도교수().get교수명()); // 김교수

        // 등록되지 않은 타입은 채워지지 않는다는 것도 확인해보자.
        학생 학생2 = new 학생("김철수");
        컨테이너.주입(학생2);
        System.out.println("등록 안 된 경우: " + 학생2.get소속학과()); // null
    }
}

/**
 * @주입 - "이 필드는 컨테이너가 자동으로 채워줘야 한다"는 표시.
 * 학과/교수 자체는 이 애노테이션과 아무 관계가 없고(구조적으로),
 * 오직 학생의 필드 선언에만 붙는다는 점을 4-1절 다이어그램에서 확인한다.
 * @Retention(RUNTIME)이 없으면 실행 중에 isAnnotationPresent()가 항상 false를 반환한다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface 주입 {
}

/**
 * 학생 - 소속학과/지도교수를 필드(연관)로 갖되, 생성자에서 채우지 않고
 * 컨테이너가 리플렉션으로 나중에 채워 넣는다.
 */
class 학생 {
    @주입
    private 학과 소속학과;

    @주입
    private 교수 지도교수;

    private final String 이름;

    public 학생(String 이름) {
        this.이름 = 이름;
        // 소속학과, 지도교수는 여기서 초기화하지 않음 (컨테이너가 나중에 채워줄 것이므로)
    }

    public 학과 get소속학과() { return 소속학과; }
    public 교수 get지도교수() { return 지도교수; }
    public String get이름() { return 이름; }
}

class 학과 {
    private final String 학과명;

    public 학과(String 학과명) {
        this.학과명 = 학과명;
    }

    public String get학과명() { return 학과명; }
}

class 교수 {
    private final String 교수명;

    public 교수(String 교수명) {
        this.교수명 = 교수명;
    }

    public String get교수명() { return 교수명; }
}

/**
 * 컨테이너 - 등록()으로 객체를 타입별로 보관해두고,
 * 주입()으로 @주입이 붙은 필드를 찾아 알맞은 타입의 객체를 리플렉션으로 채워 넣는다.
 * Object만 다루기 때문에, 컨테이너 자신은 학생/학과/교수를 전혀 모른 채로 컴파일된다.
 */
class 컨테이너 {
    private final Map<Class<?>, Object> 저장소 = new HashMap<>();

    public void 등록(Object obj) {
        저장소.put(obj.getClass(), obj);
    }

    public void 주입(Object target) throws Exception {
        Class<?> clazz = target.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(주입.class)) {
                Class<?> 필요한타입 = field.getType();
                Object 준비된객체 = 저장소.get(필요한타입);

                if (준비된객체 != null) {
                    field.setAccessible(true);
                    field.set(target, 준비된객체);
                }
            }
        }
    }
}
