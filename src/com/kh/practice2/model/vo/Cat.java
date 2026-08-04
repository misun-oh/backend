package com.kh.practice2.model.vo;

public class Cat extends Animal{
    // 필드를 초기화 하지 않으면 타입의 기본값으로 초기화 된다!!!
    // 전역변수
    private String location;
    private String color;

    public Cat(){
        // 초기화 하지 않으면 사용할 수 없다
        String a = "지역변수";
    }

    // 매개변수
    public Cat(String name, String kinds, String location, String color){
        super(name, kinds);
        this.color=color;
        this.location=location;
    }

    public void setColor(String color) {
        this.color = color;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getColor() {
        return color;
    }
    public String getLocation() {
        return location;
    }

    @Override
    public void speak() {
        System.out.println(super.toString() 
            + "%s에 서식하며, 색상은 %s 입니다.".formatted(location, color));
    }

}
