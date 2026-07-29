package ex03.method.obj;

public class Basic {

    public void info(String name, int age, double height) {

        // 숫자를 나타내는 타입
        // 정수형 - int
        // 실수형 - double
        System.out.println("""
                이름 : %s 
                나이 : %d
                키 : %f
                """.formatted(name, age, height));
    }

    public String getInfo(String name, int age, double height) {
        return "이름 : %s, 나이 : %d, 키 : %.2f".formatted(name, age, height);
    }

    // 실수 - m/kg
    public double getBmi(double h, double w) {
        double bmi = 0.0;
        // 신체질량지수 (BMI)
        // bmi = 체중 / (신장(m)*신장(m))
        // 당신의 키 , 몸무계는 , bmi , 정상입니다.
        bmi= w / (h * h);
        return bmi;
    }

    /**
     * bmi를 매개변수로 받아서 문자로 변환
     * @param bmi
     */
    // + bmiToStr(bmi:double):String
    public String bmiToStr(double bmi){
        // 변수 선언
        String str;
        // 변수 초기화
        // 변수를 초기화 하지 않으면 사용불가능
        str="";
        // 18.5미만 저체중, 22.9이하 정상, 24.9 이하 비만전단계, 나머지 비만
        if(bmi < 18.5){
            // 문장의 끝은 ;, 문자열은 ""로 감싼다
            str = "저체중";
        } else if(bmi <= 22.9){
            str = "정상";
        } else if(bmi <= 24.9){
            str = "비만 전단계";
        } else {
            str = "비만";
        }
        
        // String str = ""; // 선언과 초기화를 동시에 진행
        // 필드는 초기화 하지 않은경우 타입의 기본값(객체=null, 숫자=0)
        return str;
    }    

    // 형변환 숫자->문자, 문자->숫자
    // 쿼리스트링으로 넘어온 데이터는 문자로 들어온다!! -> 숫자로 바꿔서 사용
    // 정수 cm/kg
    public String getBmiStr(int h, int w) {
        String bmiStr = "";
        double bmi = 0.0;
        // 신체질량지수 (BMI)
        // bmi = 체중 / (신장(m)*신장(m))
        // 당신의 키 , 몸무계는 , bmi , 정상입니다.
        // 소수점 2자리까지 출력한다


        // 실수 -> 문자
        
        String res = "";



        bmi= w / ((double)h/100 * (double)h/100);
        bmiStr = """
                키:%dcm, 몸무계:%dkg, bmi:%f, %s입니다.
                """.formatted(h, w, bmi, "정상");
        return bmiStr;
    }
}
