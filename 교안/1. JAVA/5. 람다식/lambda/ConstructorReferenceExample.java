package ex10.lambda;

public class ConstructorReferenceExample {
    public static void main(String[] args) {
        // 생성자 참조: new Student(name, score)를 그대로 넘김
        StudentFactory factory = Student::new; // (name, score) -> new Student(name, score)와 동일

        Student student = factory.create("박민수", 77);
        System.out.println(student.getName() + ": " + student.getScore());
    }
}
