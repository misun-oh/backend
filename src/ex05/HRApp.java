package ex05;

import java.util.List;

import com.kh.util.InputUtil;

import ex05.dao.DeptDao;
import ex05.dao.EmpDao;
import ex05.dto.DeptDTO;
import ex05.dto.EmpDTO;

public class HRApp {
    // 필드로 만들기
    // 사원 목록 - empDao
    //static EmpDao empDao = new EmpDao();
    // 부서 목록 - deptDao
    //static DeptDao deptDao = new DeptDao();

    public static void main(String[] args) {
        
        // 사원 목록 - empDao
        EmpDao empDao = new EmpDao();
        // 부서 목록 - deptDao
        DeptDao deptDao = new DeptDao();

        System.out.println("""
                -------------------------
                사원관리 프로그램에 오신걸 환영합니다🎈
                오늘도 행복한 하루 보내세요.
                -------------------------    
                """);
        while (true) {
            printMenu();
            // 입력을 대기하고 있다가 사용자의 입력을 정수로 반환
            int menu = InputUtil.getInt("메뉴를 입력해주세요");
    
            if(menu == 1){
                List<EmpDTO> list = empDao.findAll();
    
                System.out.println(list);
            } else if(menu == 2){
                List<DeptDTO> list = deptDao.findAll();
    
                System.out.println(list);
            } else if(menu == 0){
                System.out.println("""
                -------------------------
                사원관리 프로그램을 종료합니다✨
                오늘도 행복한 하루 보내세요.
                -------------------------    
                """);
                System.exit(0);
            } else if(menu == 3){
                String name = InputUtil.getString("사원명 : ");
                // 한명의 사원 정보를 반환
                EmpDTO empDto = empDao.find(name);
                // null 아니면 사원정보를 출력
                if(empDto != null){
                    System.out.println(empDto);

                    String ent_yn = InputUtil.getString("퇴사 처리 하시겠습니까?(Y/N)");
                    if("y".equalsIgnoreCase(ent_yn)){
                        // 퇴사 처리 - 사번을 이용해서 퇴사 처리
                        int res = empDao.updateEntYn(empDto.getEmpId());
                        // 1개 이상의 행이 영향을 받음.
                        if(res > 0 ){
                            System.out.println("퇴사 처리 되었습니다.");
                        }

                    }

                } else {
                    System.out.println("%s님은 존재하지 않습니다.".formatted(name));
                }

            }
            
        }
    }

    private static void printMenu(){
        System.out.println("""
            메뉴
            1. 사원목록
            2. 부서목록
            3. 사원조회-이름
            4. 사원조회-사번
            0. 프로그램종료
        """);
    }
}