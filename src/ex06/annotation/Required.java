package ex06.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 사용자 정의 어노테이션 정의
// 1. 정의
// 2. 확인 하고 기능 정의
// 메타어노테이션 - 어노테이션의 어노테이션 
// 어노테이션을 정의 하기 위해서 사용하는 어노테이션
@Target(ElementType.FIELD) // 어디에다 붙일수 있는지 - 필드, 메서드, 클래스
@Retention(RetentionPolicy.RUNTIME)
// 어노테이션 정의
public @interface Required {

}
