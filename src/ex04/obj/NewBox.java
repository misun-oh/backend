package ex04.obj;

// 제네릭 - 외부에서 타입을 지정하는 방식
public class NewBox<T> {
    private T content;

    public NewBox(T content) {
        this.content = content;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    
}
