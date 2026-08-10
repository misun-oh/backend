package ex02;

// import ex06.필수입력;

public class 학과 {
    // private : 외부에서 접근이 불가능!
    // 클래스 내부에서만 사용이 가능하다!
    // 필드를 초기화하지 않으면 타입의 기본값으로 설정
    // @필수입력
    private String 학과번호;
    private String 학과명;
    private int 정원;


    // 생성자의 오버로딩
    
    // 기본생성자
    public 학과(){

    }

    // 🎈 생성자의 오버로딩
    public 학과(String 학과번호){
        // 필드 초기화
        // 값이 들어있는지 확인
        // 문자열이 비어 있는지 확인 -> null이 아닌지 확인, ""인지 확인
        if(학과번호 != null && !학과번호.equals("")){
            this.학과번호 = 학과번호;
        }
    }

    public 학과(String 학과번호, String 학과명){
        // 생성자를 호출은 첫줄에 올수 있다
        this(학과번호);
        this.학과명 = 학과명;
    }

    // 반환타입이 없다 클래스명과 같다 = 생성자
    public 학과(String 학과번호, String 학과명, int 정원){
        // 매개변수명 = 필드명, this키워드를 이용해서 필드에 접근!!!
        //this.학과번호 = 학과번호;
        //this.학과명 = 학과명;
        this(학과번호, 학과명);
        this.정원 = 정원;
    }

    public 학과(String string, int i) {
        this(string);
        this.정원 = i;
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
        // java 8버전(장기지원버전) 일때  사용 불가능
        return """
                학과번호 : %s
                학과명 : %s
                정원 : %d
                """.formatted(학과번호, 학과명, 정원);
    }

    // + info():String 
    // 학과의 정보를 한줄로 반환
    public String info(){
        return "학과번호:%s, 학과명:%s, 정원:%d".formatted(학과번호, 학과명, 정원);
    }

    public static void main(String[] args) {
        학과 학과1 = new 학과("D001", "컴퓨터 공학", 20);
        // toString()
        System.out.println(학과1);
        System.out.println(학과1.info());
    }
}
