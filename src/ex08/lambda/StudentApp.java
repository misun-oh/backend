package ex08.lambda;

public class StudentApp {
    public static void main(String[] args) {
       // 학생객체의 점수가 90점 이상이면 true를 반환해서 출력 
       // StudentFilter.test(Student s) -> 함수형 인터페이스
        Student s = new Student("오미자", 85);

        // 외부에서 기능을 정의해서 함수로 전달
        // 함수 - 인터페이스의 구현제를 람다식(화살표 함수)으로 작성
        StudentFilter filter = ss -> ss.getScore() > 90;

        StudentFilter filter1 = new StudentFilter() {

            @Override
            public boolean test(Student s) {
                return s.getScore() >= 80;
            }
            
        };

        System.out.println(   filter.test(s)   );

        System.out.println(s.check(ss->ss.getScore()>70));


        Student s1 = new Student("이미자", 55, filter);
        System.out.println(s1);
        Student s2 = new Student("이미자", 60, ss->ss.getScore()>=60);
        System.out.println(s2);
    }
}

// 구현체를 함수로 만들어서 전달
@FunctionalInterface
interface StudentFilter {
    boolean test(Student s);
}

