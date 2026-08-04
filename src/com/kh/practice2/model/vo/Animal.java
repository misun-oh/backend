package com.kh.practice2.model.vo;

// 자식을 통해서만 생성이 가능 - 타입으로 사용
// abstract 추상클래스, 추상 메서드
// 추상메서드를 하나라도 가지면 추상 클래스가 되어야함
// 추상클래스는 생성 할 수 없다!!!!!!!!!!!!
// 추상화 : 불필요한 세부사항을 제거하고 핵심적인 공통기능을 추출하여 단순화 하는 과정
// 중복제거 - 재사용 - 수정이 발생할 경우 한곳만 수정
public abstract class Animal {
    // 필드 - 데이터를 저장
    private String name;
    private String kinds;

    // 기본 생성자 - 반환타입이 없고, 클래스명과 같다
    protected Animal(){

    }
    
    // 매개변수가 있는 생성자
    // 생성자의 오버로딩
    protected Animal(String name, String kind){
        super();
        // 필드를 초기화
        this.name = name;
        // 이름이 다르면 this안써도 됨...
        kinds = kind;
    }

    // @ : 어노테이션 
    // Override : 부모 메서드를 자식이 재 정의
    // 모든 클래스는 Object 객체의 자식
    // print문에 객체를 넣으면 toString메서드의 결과가 출력됨!!!!
    @Override
    public String toString() {
        // super : 부모 메서드, 부모의 필드, 부모의 생성자를 호출
        // 문자열을 만들어서 반환
        String str = "저의 이름은 %s이고, 종류는 %s입니다.".formatted(name, kinds);
        return str;
    }

    // 추상메서드 - 코드블럭이 없다
    // 자식에게 기능구현을 위임
    public abstract void speak();
}
