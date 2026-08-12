package ex07.chainning;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class App {
    public static void main(String[] args) {
        // 메서드 체이닝
        // 여러 메서드를 마침표(.)로 연결하여 연속적으로 호출하는 프로그래밍 패턴

        // String 객체의 메서드를 이용해서 공백을 제거하고 대문자로 변환
        String str = "   user123   "; 
        // 공백 제거된값을 반환 - String 타입을 반환
        // 자기자신을 직접 변환, 값을 만들어서 반환
        //str = str.trim();
        // toUpperCase 모두 대문자로 변경
        //str = str.toUpperCase();

        str = str.trim().toUpperCase();
        System.out.println(str);
        // 공백제거, [Error] -> 🧨, 10글자만 출력
        //🧨 Connection Failed!
        //E Connecti
        String str1 = "    [Error] Connection Failed!   [Error]  ";
        /*
        str1 = str1.trim();
        str1 = str1.replace("[Error]", "E");
        str1 = str1.substring(0,10);
        */
        str1 = str1.trim()
                    .replace("[Error]", "🧨")
                    .substring(0,10);
        
        System.out.println(str1);

        double a = 0.1;
        double b = 0.2;

        // 정밀한 연산이 필요한 경우 double, float 방식을 사용하면 안됨
        System.out.println(a + b);

        BigDecimal bd1 = new BigDecimal("0.1");
        BigDecimal bd2 = new BigDecimal("0.2");

        // 실수의 연산 
        System.out.println(bd1.add(bd2));
        
        // 날짜/시간 다루기
        // 현재 날자
        LocalDate today = LocalDate.now();
        // 년,월,일 
        LocalDate hireDate = LocalDate.of(2011, 11, 29);
        // 2025-05-05(기본 형식)
        LocalDate entDate = LocalDate.parse("2016-05-05");

        System.out.println(today);
        System.out.println(hireDate);
        
        System.out.println("pluseDays : " + today.plusDays(10));
        System.out.println("minusDays : " + today.minusDays(10));
        
        System.out.println(today.getYear());
        System.out.println(today.getMonth());
        System.out.println(today.getDayOfMonth());
        
        // 출력형식을 지정 하여 출력하기
        // mm - 시간
        // MM - 월
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        System.out.println(today.format(formatter));




        // 현재 시간
        System.out.println(LocalTime.now());
        // 현재 시간, 날자
        System.out.println(LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yy년MM월dd a hh:mm:ss");
        DateTimeFormatter formatter3 = DateTimeFormatter.ofPattern("오늘은 E요일 입니다.");

        
        System.out.println( now.format(formatter2) );
        System.out.println( now.format(formatter3) );


        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 12);

        // 1. 날짜 단위 계산
        long days = ChronoUnit.DAYS.between(startDate, endDate);     // 588일
        long months = ChronoUnit.MONTHS.between(startDate, endDate); // 19개월
        long years = ChronoUnit.YEARS.between(startDate, endDate);   // 1년

        // 2. 시간 단위 계산 (LocalTime 또는 LocalDateTime 필요)
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(18, 0);

        long hours = ChronoUnit.HOURS.between(startTime, endTime);   // 9시간
        long minutes = ChronoUnit.MINUTES.between(startTime, endTime); // 540분



    }
}
