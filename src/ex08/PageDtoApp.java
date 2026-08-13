package ex08;

public class PageDtoApp {
    public static void main(String[] args) {
        
        
        // 요청페이지 = 1, 페이지당 게시물의 수 = 10, 블럭당 페이지의 수 = 5 초기화
        // 처음 게시물을 요청하는 경우 = 1페이지 요청
        PageDto pageDto = new PageDto(150);
        System.out.println(pageDto);
        // 사용자의 요청 페이지 번호 : 10
        // 총 건수 : 150
        PageDto pageDto1 = new PageDto(10, 75);
        System.out.println(pageDto1);
    }
}
