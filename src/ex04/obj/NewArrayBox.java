package ex04.obj;

public class NewArrayBox<T> {
    // 배열은 타입을 지정
    private T[] arr;
    private int index;
    public NewArrayBox() {
        arr = (T[])new Object[10];
    }

    public void add(T obj){
        arr[index] = obj;
        index++;
    }

    

}
