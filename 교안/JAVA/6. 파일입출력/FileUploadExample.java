package ex09.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

public class FileUploadExample {
    // originalFileName: 사용자가 올린 원래 파일명 (예: "사진.jpg")
    // content: 업로드된 파일의 실제 바이트 데이터
    public static Path saveUploadedFile(String originalFileName, byte[] content) throws IOException {
        // 1) 날짜별 폴더로 분산 저장 -> 폴더 하나에 파일이 몰리는 것도 막고, 충돌 확률도 낮춤
        LocalDate today = LocalDate.now();
        Path dir = Path.of("uploads",
                String.valueOf(today.getYear()),
                "%02d".formatted(today.getMonthValue()),
                "%02d".formatted(today.getDayOfMonth()));
        Files.createDirectories(dir); // 폴더가 없으면 생성, 이미 있으면 그냥 넘어감

        // 2) 확장자는 유지하되, 저장용 파일명은 UUID로 새로 만들어 충돌을 원천 차단
        String ext = originalFileName.substring(originalFileName.lastIndexOf('.')); // ".jpg"
        String savedName = UUID.randomUUID() + ext; // 예: 3f2a1b0c-....jpg

        Path savedPath = dir.resolve(savedName);
        Files.write(savedPath, content); // 실제 저장

        System.out.println("원본 이름: " + originalFileName + " -> 저장된 경로: " + savedPath);
        return savedPath;
        // 실무라면 여기서 (originalFileName, savedPath)를 DB에 기록해서
        // 나중에 "원래 이름으로 다운로드"를 지원합니다.
    }

    public static void main(String[] args) throws IOException {
        // 서로 다른 두 사용자가 우연히 같은 이름("사진.jpg")으로 업로드해도 충돌하지 않음
        saveUploadedFile("사진.jpg", "첫 번째 사용자의 사진 데이터".getBytes());
        saveUploadedFile("사진.jpg", "두 번째 사용자의 사진 데이터".getBytes());
    }
}
