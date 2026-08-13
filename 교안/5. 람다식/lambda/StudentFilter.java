package ex10.lambda;

@FunctionalInterface
public interface StudentFilter {
    boolean test(Student student); // 학생 한 명을 검사해서 조건에 맞으면 true
}
