package com.kh.inherit.practice;

import ex06.annotation.Required;

public class Employee extends Person{
    @Required
    private int salary;
    private String dept;

    public Employee() {
    }

    public Employee(String name, int age, double height, double weight, int salary, String dept) {
        // 부모의 생성자 호출 - 맨위에 적어 주어야 함!!!!
        super(age, height, weight);
        setName(name);
        this.salary = salary;
        this.dept = dept;
    }

    @Override
    public String toString() {
        return super.toString()+"""
                급여 : %d
                부서 : %s
                """.formatted(salary, dept);
    }
    @Override
    public String information() {
        return super.information()+"""
                급여 : %d
                부서 : %s
                """.formatted(salary, dept);
    }
    
    
}
