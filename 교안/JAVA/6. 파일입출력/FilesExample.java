package ex09.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FilesExample {
    public static void main(String[] args) throws IOException {

        Path root = Path.of("folder100");

        for (int i = 1; i <= 100; i++) {
            Path folder = root.resolve(String.valueOf(i)); // folder100/1, folder100/2, ... folder100/100
            Files.createDirectories(folder);
        }

        System.out.println("100개 폴더 생성 완료: " + root.toAbsolutePath());

        
        Path dir = Path.of("data", "backup");   // 아직 존재하지 않는 경로
        System.out.println("존재하나요? " + Files.exists(dir)); // false

        Files.createDirectories(dir);            // data/backup 폴더를 한 번에 생성 (중간 폴더까지 다 만듦)
        System.out.println("존재하나요? " + Files.exists(dir)); // true

        Path file = dir.resolve("memo.txt");      // data/backup/memo.txt 경로 조합
        Files.writeString(file, "안녕하세요");     // 문자열을 바로 파일로 저장 (Java 11+)

        String content = Files.readString(file);  // 파일을 바로 문자열로 읽기 (Java 11+)
        System.out.println("읽은 내용: " + content);

        // sample.png는 어떤 종류의 파일이든 상관없음 - 이미지든 엑셀이든 PDF든 zip이든 동일하게 동작
        Path source = Path.of("sample.png");
        Path target = Path.of("sample_copy.png");

        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("복사 완료: " + target.toAbsolutePath());
        System.out.println("원본 크기: " + Files.size(source) + " bytes");
        System.out.println("복사본 크기: " + Files.size(target) + " bytes");

        
    }
}
