package com.kh.practice2.model;

public class Box<T> { // T는 "아직 정해지지 않은 타입"을 나타내는 자리표시자
    private T content;

    public void setContent(T content) {
        this.content = content;
    }

    public T getContent() {
        return content;
    }
}
