package com.kh.practice2.model.vo;

// vo/dto : 필드와 setter, getter메서드를 가진 
// 데이터를 담는 그릇과 같은 역할
// 자식이 부모를 지정 (extends키워드를 이용)
// 자식은 부모가 가진 모든 필드와 메서드를 상속
// 미구현된 메서드가 있다면 구현해야함 
// -> 아니면 추상 클래스가 되어야함
public class Dog extends Animal{
    public static final String PLACE = "애견카페";
    private int weight;

    // 접근제한자를 안쓸경우 Default 접근제한자가 됨 
    // - 같은패키지에서 접근 가능
    public Dog(){

    }

    public Dog(String name, String kinds, int weight){
        // 초기화
        // 부모의 생성자를 호출해서 초기화
        super(name, kinds);
        this.weight = weight;
    }


    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    // @Override : 부모 메서드 재정의
    @Override
    public void speak() {
        System.err.println( super.toString() + "몸무계는 %skg입니다".formatted(weight));
        // 예외를 발생시킴 -> 호출한곳으로 예외를 던짐! -> 비정상적인 종료
        //throw new UnsupportedOperationException("Unimplemented method 'speak'");
    }


}
