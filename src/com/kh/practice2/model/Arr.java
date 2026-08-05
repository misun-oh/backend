package com.kh.practice2.model;

import java.util.Arrays;

import ex01.학생;

public class Arr { // T는 "아직 정해지지 않은 타입"을 나타내는 자리표시자
    private Object[] obj;
    int index;
    
    Arr(){
        obj = new Object[10];
        index = 0;
    }

    void add(Object o){
        obj[index] = o;
    }

    Object get(int i){
        return obj[i];
    }

    void remove(int i){
        obj[i] = null;
    }

    @Override
    public String toString() {
        String str="";
        for(Object o:obj){
            str += o.toString();
        }

        return str;
    }
    public static void main(String[] args) {
        Arr myArr = new Arr();
        myArr.add(1);
        myArr.add("String");
        myArr.add(new 학생());

        System.out.println(myArr);
    }
}
