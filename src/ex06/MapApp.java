package ex06;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kh.inherit.practice.Employee;

import ex05.dto.DeptDTO;

public class MapApp {
    public static void main(String[] args) {
        // 데이터를 키와 값으로 저장
        // 여러타입의 데이터를 저장하는 이름로 저장해서 사용할수 있다!
        // 게시물 조회 - 리스트, 페이지정보
        // 키의 타입 - 문자열, 값의 타입
        // Map은 인터페이스라 직접 생성 할수 없다! -> 인터페이스의 구현체인 HashMap을 이용해서 생성
        // Map<키의 타입, 값의 타입>
        Map<String, Employee> map = new HashMap<>();

        // 키-값 쌍으로 데이터를 관리
        // 키는 중복될 수 없다 - 엎어써버림!!!
        map.put("사원1", new Employee("오미자", 23, 0, 0, 0, "d1"));
        map.put("사원2", new Employee("오미자", 23, 0, 0, 0, "d1"));
        map.put("사원3", new Employee("오미자3", 23, 0, 0, 0, "d1"));

        // map의 데이터를 꺼내올때는 키값을 매개변수로 사용한다!
        // get : 키에 저장된 값을 반환
        System.out.println(map.get("사원1"));
        System.out.println(map.get("사원2"));
        System.out.println(map.get("사원3"));

        System.out.println(map.containsKey("사원"));

        System.out.println(map.get("사원"));
        // map에 있는 사원이라는 키를 사용하는 값을 꺼내서 확인
        if(map.get("사원") == null){
            System.out.println("사원은 존재하지 않습니다.");
        }
        // containsKey 메서드를 이용 확인
        if(!map.containsKey("사원")){
            System.out.println("사원은 존재하지 않습니다.");
        }

        Map<String, Object> map1 = new HashMap<>();
        List<Employee> list = new ArrayList<>();
        list.add(new Employee("오미자", 23, 0, 0, 0, null));
        list.add(new Employee("이미자", 25, 0, 0, 0, null));
        Employee emp = new Employee("아직자", 27, 0, 0, 0, null);
        list.add(emp);

        map1.put("title", "map을 학습 중입니다.");
        map1.put("dept", new DeptDTO("D1", "인사관리부", "S1"));
        map1.put("list", list);

        // 명시적 형변환
        String title = (String)map1.get("title");
        System.out.println(title);
        System.out.println((String)map1.get("title"));

        // 리스트 꺼내서 출력하기
        List<Employee> mapList = (List<Employee>)map1.get("list");
        System.out.println(mapList);

        // map이 가지고 있는 키의 목록
        Set<String> keys =  map.keySet();
        System.out.println(keys);

        // map에 담겨있는 키의 목록을 조회 해서 값을 확인 
        for(String key : keys){
            System.out.println(map.get(key));
        }

    }
}
