package ex08;

/*
페이지 처리를 위한 객체
*/
public class PageDto {
    // 사용자가 요청한 페이지 정보
    // 기본값 - 1 (사용자가 요청한 페이지가 없는경우 무조건 1페이지를 보여준다)
    private int page = 1;
    // 페이지당 보여줄 게시물의 수 (페이지의 게시물 건수는 10페이지로 초기화)
    // 기본값 - 10
    private int size = 10;
    // 게시물의 총 건수 - 데이터베이스의 조회 결과 테이블의 게시물 총건수
    private int totalCnt;

    // 게시물의 시작번호, 끝번호 : 데이터베이스 조회 할때 필요한 정보
    // DB 의 종류, 버전에 따라 사용여부가 결정 됨
    private int sNo;
    private int eNo;


    // 페이지 블럭용 필드
    // 페이지 블럭의 시작 번호
    private int sPageNo;
    // 페이지 블럭의 끝번호
    private int ePageNo;
    // 앞으로 가기버튼/뒤로가기 버튼
    // 필드는 타입의 기본값으로 초기화 되어짐
    // boolean타입의 기본값은 false이므로 초기화 할 필요는 없다.
    private boolean isPrev = false;
    private boolean isNext;
    
    // 페이지블럭당 보여주는 페이지의 수
    private int blockSize = 5;

    @Override
    public String toString() {
        // 페이지블럭의 시작번호부터 끝번호까지 반복문을 이용해서 반환
        String str = "";
        if(isPrev){
            str += "◀ ";
        }
        for(int i=sPageNo;i<=ePageNo;i++){
            str += i + " ";
        }
        if(isNext){
            str += "▶";
        }

        return str;
    }
    // totalCnt : 데이터베이스에 입력된 데이터의 총건
    // page : 사용자가 요청한 페이지 번호
    public PageDto(int page, int totalCnt){
        this.page = page;
        this.totalCnt = totalCnt;

        // 연산 : int - int 연산 결과는 int
        //        double - int 연산 결과는 double 타입
        // double타입의 결과를 원하는 경우 - 한쪽을 더블타입으로 형변환
        // (double)size : 명시적 형변환, * 1.0(리터럴-double) 

        // 총 99건의 데이터가 있고 요청한 페이지 번호가 2페이지라면
        // 11번부터 20번까지 데이터를 조회 하고 싶다
        this.eNo = (int)Math.ceil(totalCnt/(double)size) * page;
        this.sNo = eNo - (size-1);
        
        System.out.println("게시물의 시작번호 : " + sNo);
        System.out.println("게시물의 끝번호 : " + eNo);


        // 페이지 네비게이션을 그리기 위해서 값을 초기화
        // 2페이지를 요청한 경우 1번째 블럭(1 2 3 4 5)이 보여 져야함
        // 페이지의끝 번호 를 구함
        ePageNo = (int)Math.ceil(page/(double)blockSize) * blockSize;
        sPageNo = ePageNo - (blockSize-1);

        // prev버튼은 시작페이지번호가 1이 아닐때만 보여준다
        // isPrev : boolean 타입
        // != 비교연산의 결과는 boolean타입이 반환됨
        isPrev = sPageNo!=1;
        
        // 진짜 끝페이지 = 올림(게시물의 건수/페이지당게시물수)
        // 99/10=9.9 진짜 끝 페이지 번호 : 10페이지
        // next버튼은 진짜끝페이지 번호보다 페이지의끝번호보다 크면 보여준다
        int realEndPageNo = (int)Math.ceil( totalCnt/(double)size );
        System.out.println("진짜 끝페이지 번호 : " + realEndPageNo);
        // 진짜끝번호 보다 끝번호가 큰경우 
        ePageNo = ePageNo > realEndPageNo ? realEndPageNo : ePageNo;
         
        isNext = ePageNo < realEndPageNo;
    }


    public PageDto(int totalCnt) {
        // 생성자 호출
        this(1, totalCnt);
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
        
        // 데이터 베이스에서 데이터를 조회하기 위해서 필요한 정보
        // 게시물의 끝번호
        System.out.println((int)(Math.ceil(totalCnt/(size*1.0))*page));
        // 게시물의 시작번호
        System.out.println((int)(Math.ceil(totalCnt/(size*1.0))*page) - (size-1));


        PageDto pageDto = new PageDto(7, 150);
        System.out.println(pageDto);

    }

}
