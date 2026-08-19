package ex09.file;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageCopyExample {
    public static void main(String[] args) throws IOException {
        try (FileInputStream in = new FileInputStream("sample.png");
             FileOutputStream out = new FileOutputStream("sample_copy.png")) {

            byte[] buffer = new byte[1024]; // 1KB씩 나눠서 복사
            int len;
            while ((len = in.read(buffer)) != -1) { // 더 이상 읽을 바이트가 없으면 -1
                out.write(buffer, 0, len); // 실제로 읽은 만큼(len)만 씀
            }
        }

        System.out.println("이미지 복사 완료: sample_copy.png");
    }
}
