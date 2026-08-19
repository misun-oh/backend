/**
 * 01_교안_GoF디자인패턴.md 7절 "GoF 원조 Builder 구조 미리보기" 클래스 다이어그램을
 * 그대로 코드로 옮긴 예제. Employee 같은 구체적인 도메인 없이,
 * Director / Builder / ConcreteBuilder / Product라는 역할 이름 그대로 구현했다.
 * (Employee로 구체화한 버전은 EmployeeGofBuilder.java 참고)
 */
public class GofBuilderPreview {

    // ---- 직접 실행해서 동작을 확인해보는 메인 메서드 ----
    public static void main(String[] args) {
        Builder builder = new ConcreteBuilder();
        Director director = new Director(builder);

        Product product = director.construct();
        System.out.println(product);
    }
}

/**
 * 1) Builder 인터페이스 - Product를 조립하는 데 필요한 단계(메서드)만 선언한다.
 */
interface Builder {
    void buildPartA();
    void buildPartB();
    Product getResult();
}

/**
 * 2) ConcreteBuilder - 인터페이스를 구현(implements)해서 실제로 부품을 조립한다.
 */
class ConcreteBuilder implements Builder {
    private final Product product = new Product();

    @Override
    public void buildPartA() {
        product.addPart("Part A");
    }

    @Override
    public void buildPartB() {
        product.addPart("Part B");
    }

    @Override
    public Product getResult() {
        return product;
    }
}

/**
 * 3) Director - Builder의 메서드를 "어떤 순서로" 호출할지 아는 지휘자.
 *    Builder 인터페이스만 알 뿐, 어떤 ConcreteBuilder가 꽂혀 있는지는 모른다 (다형성).
 */
class Director {
    private final Builder builder;

    public Director(Builder builder) {
        this.builder = builder;
    }

    public Product construct() {
        builder.buildPartA();
        builder.buildPartB();
        return builder.getResult();
    }
}

/**
 * 4) Product - 최종적으로 만들어지는 결과물.
 */
class Product {
    private final java.util.List<String> parts = new java.util.ArrayList<>();

    public void addPart(String part) {
        parts.add(part);
    }

    @Override
    public String toString() {
        return "Product" + parts;
    }
}
