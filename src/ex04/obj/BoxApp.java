package ex04.obj;

import java.util.ArrayList;
import java.util.List;

public class BoxApp {
    public static void main(String[] args) {
        Box intBox = new Box(1);
        Box StringBox = new Box("str");
        Box StudentBox = new Box(new Student("이미자", "2026000001"));

        // 타입을 확인후 변환하거나 try-catch로 묶어야함
        // ClassCastException Err 
        //String str = (String)intBox.getContent();

        NewBox<String> box1 = new NewBox<String>("제네릭을 이용해서 외부에서 타입을 지정!");
        box1.getContent();



        // 제네릭을 이용하면 외부에서 타입을 지정 할 수 있다
        // 형변환 없이 사용할 수 있다
        List<String> list = new ArrayList<>();
        list.add("test");
        list.add("test1");
        list.add("test2");
        System.out.println(list);

        List<Student> list1 = new ArrayList<>();
        list1.add(new Student("이미자", "2026000001"));
        list1.add(new Student("오미자", "2026000002"));

        System.out.println(list);

    }
}
