package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


// html 화면을 서비스 
@Controller 
public class EmpController {

    
    /*
    url호출이 되면 html페이지를 서비스
    - localhost:port/url
        - port번호의 경우 여러개의 서비스를 실행 시 변경될수 있으므로 
          설정파일의 정보를 확인해야한다!
    - 파라메터 수집
        - 문자열, 숫자등 하나의 값을 수집(@RequestParam)
        - 객체(dto)로 수집(자동)
        - 요청경로로 부터 수집(@PathVariable)
    - 페이지 반환
        - 반환값 있음 - 반환되는 문자열의 경로에 있는 파일 반환
        - 반환값 없음 - 요청 경로에 대항하는 페이지
    
    - templates 
        - 동적 페이지 - 컨트롤러를 통해서 서비스 하는 페이지
    - static
        - 정적 페이지 - 요청하면 서비스
                        css, js, image
     */
    @GetMapping("/emps")
    public String getMethodName() {
        return "/hr/index";
    }

    /*
    @RequestParam : 기본값이 필수임
    */
    @GetMapping("/param")
    public String getMethodName(@RequestParam String param) {
        return "index";
    }
    
    
}
