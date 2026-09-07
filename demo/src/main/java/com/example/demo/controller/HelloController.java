package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.dto.LoginDto;


// 패키지의 위치
// 기본패키지의 하위에 있는 어노테이션을 읽어서 bean으로 등록
// 사용자의 요청을 받아서 페이지를 반환해주는 역할
@Controller 
public class HelloController {

    // get방식 요청이 발생 했을때 실행
    // 요청경로에 해당하는 메서드가 실행!
    // 매개변수 : 요청 파라메터를 수집(문자열, 숫자, 객체 형태로 자동 수집)

    // src/main/resources : 웹페이지, 설정파일
    //  static : 정적파일 (이미지, 스타일시트, 자바스크립트)
    //  templates : 동적파일 (요청 할 때마다 변경되는 파일)
    
    //  뷰템플릿 - 화면을 구현 용도
    //      자바객체의 정보를 제어문과 반복을 사용하여 화면에 출력하는 용도
    //      thymeleaf : 파일을 직접 실행해 볼수 있음 
    //      JSP : 파일을 직접 실행할 수 없음
    //      상용 프로그램을 이용하기도 함

    // GetMapping : 사용자의 요청 URL과 일치하는 메서드를 실행
    // 반환값 
    // 문자열 : templates폴더 하위에 있는 .html인 파일을 찾아서 반환(서비스)
    //          파일이 없는경우 404 오류
    // 없음    : 요청경로와 같은 경로의 파일을 찾아서 반환
    @GetMapping("/")
    public String getMethodName() {
        System.out.println("getMethodName");
        return "hello";
    }

    // /list 요청이 발생하면 /hr/list.html
    // 반환값 있음
    @GetMapping("/list")
    public String getList() {
        // 경로와 파일명을 문자열로 반환
        return "/hr/list";
    }
    

    // /hr/view 요청이 발생하면 /hr/view.html
    // 반환값 없음 - 요청주소와 응답주소가 같을때

    // @RequestParam : 요청정보로 부터 넘어온 파라메터를 컨트롤러에 전달
    // 기본값이 필수로 설정
    //  - 만약 해당 이름의 파라메터가 전달 되지 않으면 오류가 발생
    //  - value : 사용자가 전달 한 이름

    // id=abc&pw=123을 전달 받아서 콘솔창에 출력해봅시다
    // 여러개의 파라메터를 받을경우 ,로 연결 해서 작성
    @GetMapping("/hr/view")
    public void getMethodName(@RequestParam(value="param") String param
                                , @RequestParam(value="id") String id
                                , @RequestParam(value="pw") String pw
    ) {
        System.out.println("param : " + param);
        System.out.println("id : " + id);
        System.out.println("pw : " + pw);
        // return "/hr/view";
    }

    // DTO 객체를 파라메터로 전달 받는 방법
    // 파라메터 자동 수집
    // @ResponseBody : 요청 파라메터를 객체에 담아서 전달
    //      필수값이 아님
    //      필드의 이름으로 요청 파라메터가 전달된경우 
    //      해당 값을 객체에 입력
    
    // URL이 중복될 경우 오류가 발생 -> 프로젝트가 실행 되지 않음
    // 스프링의 경우 객체를 미리 만들어 놓고 실행
    // 프로젝트 오류가 있는경우 프로젝트 자체가 실행이 안됨
    @GetMapping("/hr/view1")
    @ResponseBody
    public String getMethodName(LoginDto loginDto) {
        System.out.println("loginDto : " + loginDto.getId());
        System.out.println("loginDto : " + loginDto.getPw());
        return "main";
    }
    

    

}
