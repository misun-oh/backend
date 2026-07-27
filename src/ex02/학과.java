package ex02;

public class 학과 {
    // private : 외부에서 접근이 불가능!
    // 클래스 내부에서만 사용이 가능하다!
    private String 학과번호;
    private String 학과명;
    private int 정원;

    // 반환타입이 없다 클래스명과 같다
    public 학과(String 학과번호, String 학과명, int 정원){
        // 매개변수명 = 필드명, this키워드를 이용해서 필드에 접근!!!
        this.학과번호 = 학과번호;
        this.학과명 = 학과명;
        this.정원 = 정원;
    }

    // 반환타입을 작성하면 return을 적야야 함
    public String get학과번호(){
        return 학과번호;
    }

    public String get학과명(){
        return 학과명;
    }

    public int get정원(){
        return 정원;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        //return super.toString();
        // 텍스트 블록을 이용해서 문자열을 만들어 봅시다
        // java 8버전(장기지원버전) 일때 
        return """
                학과번호 : %s
                학과명 : %s
                정원 : %d
                """.formatted(학과번호, 학과명, 정원);
    }
}
