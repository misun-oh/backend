package ex08;

/*
페이지 처리를 위한 객체
*/
public class PageDto {
    // 사용자가 요청한 페이지 정보
    private int page;
    // 페이지당 보여줄 게시물의 수
    // 기본값 - 10
    private int size = 10;
    // 게시물의 총 건수
    private int totalCnt;
    
    // totalCnt : 데이터베이스에 입력된 데이터의 총건
    // page : 사용자가 요청한 페이지 번호
    public PageDto(int page, int totalCnt){
        this.page = page;
        this.totalCnt = totalCnt;

        totalCnt = 99;
        // 11 - 20
        page = 2;

        int endPageNo = totalCnt/size;
        System.out.println(endPageNo);

        // 99/10 -> 올림(9.99) * page 
        
    }


    public static void main(String[] args) {
        int size = 10;
        int totalCnt = 99;
        int page = 2;
        // int 타입 연산결과는 int형 
        System.out.println(totalCnt/size);
        // 더블타입으로 형변환
        System.out.println(size*1.0);
        System.out.println((double)size);
        System.out.println(totalCnt/(size*1.0));
        // Math.ceil() 올림처리
        
        // 게시물의 끝번호
        System.out.println((int)(Math.ceil(totalCnt/(size*1.0))*page));
        // 게시물의 시작번호
        System.out.println((int)(Math.ceil(totalCnt/(size*1.0))*page) - (size-1));


    }

}
