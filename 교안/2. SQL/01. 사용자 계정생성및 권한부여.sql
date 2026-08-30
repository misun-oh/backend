-- 주석
-- 사용자 계정생성및 권한부여

-- 1) 계정 생성 (계정명, 비밀번호는 원하는 값으로 바꿔서 사용)
CREATE USER 'emp'@'localhost' IDENTIFIED BY '1234';

-- 2) 데이터베이스 생성 권한 + 실습에 필요한 모든 권한 부여
GRANT ALL PRIVILEGES ON *.* TO 'emp'@'localhost';

-- 3) 권한 변경 사항을 즉시 반영
FLUSH PRIVILEGES;