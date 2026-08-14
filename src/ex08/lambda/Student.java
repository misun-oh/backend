package ex08.lambda;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/*
@Setter
@Getter
@ToString
*/
@Data
@AllArgsConstructor
public class Student {
    private String name;
    private int score;
    private boolean check;

    public Student(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public Student(String name, int score, StudentFilter filter) {
        this.name = name;
        this.score = score;

        this.check = filter.test(this);
    }

    // 반환하는 메서드 -  필터를 이용해서 합격여부를 판단
    boolean check(StudentFilter filter){
        // 필드에 저장
        return filter.test(this);
    }

    // 필드의 값을 변경하는 메서드
    // 필터를 이용해서 합격여부를 판단
    void check1(StudentFilter filter){
        // 필드에 저장
        check = filter.test(this);
    }
}
