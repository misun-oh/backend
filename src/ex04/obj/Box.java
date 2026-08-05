package ex04.obj;

public class Box {
    private Object content;

    // 초기화
    public Box(Object content) {
        this.content = content;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
    
}
