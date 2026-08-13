package ex07.builder;

import ex07.builder.StudentDto.Builder;

public class StudentApp {
    public static void main(String[] args) {
        // 메서드체이닝을 이용한 객체 생성
        StudentDto.Builder()
            .setStudentId("S001")
            .setAge(23)
            .setMajor("컴퓨터공학")
            .build(); // 객체생성자를 호출해서 객체를 생성후 반환
        
        Builder b = StudentDto.Builder();
        b.setAge(0);
        b.setMajor(null);
        b.setName(null);
        b.setStudentId(null);
        StudentDto s = b.build();
    }
}
