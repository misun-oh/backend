package com.kh.inherit.practice;

public class Student extends Person{
    private int grade;
    private String major;

    
    public Student() {
    }

    public Student(int grade, String major) {
        this.grade = grade;
        this.major = major;
    }

    public Student(String name, int age, double height, double weight, int grade, String major) {
        super(age, height, weight);
        super.name = name;
        this.grade = grade;
        this.major = major;
    }

    @Override
    public String toString() {
        
        return super.toString() + """
                학년 : %d
                전공 : %s
                """.formatted(grade, major);
    }
    @Override
    public String information() {
        // TODO Auto-generated method stub
        return super.information();
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    
    
}
