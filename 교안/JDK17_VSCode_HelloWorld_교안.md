# VS Code로 시작하는 Java 개발환경 구축 (JDK 17)

**학습목표**: JDK 17 설치 → VS Code 확장 설치 → 프로젝트 생성 → Hello World 실행까지 처음부터 끝까지 따라할 수 있다.

**소요시간**: 약 20~30분

---

## 0단계. 자바 버전 이해하기

### 0-1. 버전 번호 읽는 법

`17.0.9` 처럼 점(`.`)으로 3부분으로 나뉩니다.

| 자리 | 이름 | 의미 | 변경 주기 |
|---|---|---|---|
| 1번째 (`17`) | 메이저(Feature) | 우리가 말하는 "버전명", 새 기능 추가 | 6개월 |
| 2번째 (`.0`) | 마이너(Interim) | 사실상 거의 안 쓰임, 항상 0 | (미사용) |
| 3번째 (`.9`) | 업데이트(보안) | 버그/보안 수정만, 기능 변화 없음 | 3개월 |

### 0-2. LTS(장기지원버전)란?

| 구분 | 일반 버전 (Non-LTS) | LTS 버전 |
|---|---|---|
| 예시 | 18, 19, 20, 22, 23 ... | **8, 11, 17, 21, 25** |
| 지원 기간 | 약 6개월 후 종료 | **수년 이상** 보안 패치 제공 |
| 사용 대상 | 최신 기능 실험용 | 실무·학습 등 대부분의 경우 |

**왜 LTS를 써야 할까?**
- 보안 패치를 오래 받을 수 있어 안정적입니다.
- Spring Boot 등 대부분의 프레임워크·강의 자료가 LTS(8, 11, 17, 21) 기준입니다.
- 회사·채용 공고에서도 LTS 버전을 기준으로 요구하는 경우가 대부분입니다.

### 0-3. 지금까지의 LTS 버전 흐름

![Java LTS 버전 흐름](java_lts_flow.svg)

이번 교안에서는 안정성과 범용성이 검증된 **LTS 버전인 17**을 기준으로 설치를 진행합니다.

---

## 1단계. JDK 17 설치 (Windows 기준)

**방법 A) 공식 사이트에서 설치 파일 다운로드 (초보자 추천)**
1. https://adoptium.net 접속
2. 메인 화면에서 버전 선택 드롭다운을 **17 - LTS**로 변경
3. **Windows x64 .msi** 파일 다운로드
4. 다운로드한 설치 파일 실행 → 계속 "Next" 클릭하며 진행
   - 설치 옵션 화면에서 **"Set JAVA_HOME variable"**, **"Add to PATH"** 항목을 반드시 체크 (기본값으로 체크되어 있음)
5. 설치 완료 후 재부팅 또는 새 명령 프롬프트 창을 열기

**방법 B) winget 명령어로 설치 (터미널 사용에 익숙한 경우)**
```
winget install EclipseAdoptium.Temurin.17.JDK
```

**설치 확인**
명령 프롬프트 또는 PowerShell을 열고 아래 명령어 입력:
```
java -version
javac -version
```
아래와 비슷하게 **17.x.x** 버전이 출력되면 성공입니다.
```
openjdk version "17.0.x" ...
javac 17.0.x
```

> ⚠️ 명령어가 인식되지 않는다는 오류가 뜨면, 새 터미널 창을 열었는지 확인하고 그래도 안 되면 PC를 재부팅해보세요. (환경변수 PATH가 새로 적용되지 않은 경우입니다.)

<details>
<summary>▶ Mac에서 설치하기 (클릭해서 펼치기)</summary>

**Homebrew 사용**
```
brew install openjdk@17
```
설치 후 심볼릭 링크 연결(Apple Silicon 기준):
```
sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
```

**설치 확인**
```
java -version
javac -version
```

</details>

<details>
<summary>▶ Linux(Ubuntu/Debian)에서 설치하기 (클릭해서 펼치기)</summary>

```
sudo apt update
sudo apt install openjdk-17-jdk
```

**설치 확인**
```
java -version
javac -version
```

</details>

---

## 2단계. VS Code 확장 프로그램 설치

1. VS Code 실행
2. 왼쪽 사이드바에서 **확장(Extensions)** 아이콘 클릭 (또는 단축키 `Ctrl+Shift+X`, Mac은 `Cmd+Shift+X`)
3. 검색창에 `Extension Pack for Java` 입력
4. **Microsoft**에서 만든 것을 선택 후 **Install** 클릭

> 이 확장 하나만 설치하면 아래 5가지가 한 번에 함께 설치됩니다.
> - Language Support for Java (Red Hat)
> - Debugger for Java
> - Test Runner for Java
> - Maven for Java
> - Project Manager for Java

5. 설치가 끝나면 VS Code를 한 번 재시작합니다.
6. 하단 상태 표시줄이나 알림창에 JDK 관련 경고가 뜨지 않으면 정상 인식된 것입니다.
   (만약 JDK를 자동으로 못 찾는다는 알림이 뜨면 3단계 참고)

### (선택) JDK 경로가 자동으로 인식되지 않는 경우
`Ctrl+Shift+P` → `Preferences: Open User Settings (JSON)` 입력 후 아래 내용 추가:

**Windows 예시**
```json
"java.jdt.ls.java.home": "C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.x-hotspot"
```

**Mac 예시**
```json
"java.jdt.ls.java.home": "/Library/Java/JavaVirtualMachines/openjdk-17.jdk/Contents/Home"
```

**Linux 예시**
```json
"java.jdt.ls.java.home": "/usr/lib/jvm/java-17-openjdk-amd64"
```

---

## 3단계. 프로젝트 생성하기

1. `Ctrl+Shift+P` (Mac: `Cmd+Shift+P`)로 명령 팔레트 열기
2. `Java: Create Java Project` 입력 후 선택
3. 프로젝트 종류 선택 창이 뜨면 **No build tools**(가장 단순한 방식) 선택
   - Maven이나 Gradle을 배우고 싶다면 각각 선택해도 되지만, 처음에는 No build tools 추천
4. 프로젝트를 저장할 폴더 위치 선택
5. 프로젝트 이름 입력 (예: `HelloWorldProject`) 후 Enter
6. 잠시 후 왼쪽에 프로젝트 폴더가 자동으로 열리며 아래와 같은 구조가 생성됩니다.
```
HelloWorldProject/
 ├─ src/
 │   └─ App.java
 ├─ lib/
 └─ bin/
```

---

## 4단계. Hello World 출력하기

1. 왼쪽 탐색기에서 `src/App.java` 파일 열기 (자동 생성된 기본 코드가 있음)
2. 내용을 아래처럼 수정합니다.

```java
public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
    }
}
```

3. 실행하는 방법은 두 가지입니다.

**방법 A) Run 버튼 클릭**
- 코드 위쪽에 자동으로 나타나는 **▷ Run** 글자 클릭

**방법 B) 단축키 사용**
- `F5` (디버그 실행) 또는 `Ctrl+F5`(Mac은 `Cmd+F5`, 디버그 없이 실행)

4. 하단에 **터미널(TERMINAL)** 창이 열리면서 아래처럼 출력되면 성공입니다.
```
Hello, World!
```

---

## 문제 해결 (트러블슈팅)

| 증상 | 원인 / 해결 |
|---|---|
| `java -version`이 인식 안 됨 | 터미널을 새로 열거나 PC 재부팅. 그래도 안 되면 환경변수 PATH에 JDK bin 경로 수동 추가 |
| VS Code 하단에 "No Java Runtime" 경고 | 2단계의 JDK 경로 설정(java.jdt.ls.java.home) 확인 |
| Run 버튼이 안 보임 | 파일 안에 `public static void main` 메서드가 있는지 확인, 파일을 저장(Ctrl+S)했는지 확인 |
| 실행은 되는데 한글이 깨짐 | 터미널 인코딩 문제. VS Code 설정에서 `"terminal.integrated.env.windows"`에 `"JAVA_TOOL_OPTIONS": "-Dfile.encoding=UTF-8"` 추가 |

---

## 다음 단계 (선택 학습)
- 변수와 자료형 다루기
- 사용자 입력 받기 (`Scanner` 클래스)
- 여러 클래스 파일로 프로젝트 구성하기
- Maven/Gradle을 이용한 빌드 관리 배우기
