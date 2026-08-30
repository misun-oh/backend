-- SELECT 컬럼명 [, 컬럼명, ...]
-- FROM 테이블명
-- WHERE 조건식; (생략 가능)

-- emp 테이블의 모든 데이터(*)를 조회
-- ctrl + enter : 한 문장 실행
select 	* 
from 	emp;

-- emp테이블에서 사원의 이름과 급여만 조회
select	emp_name, SALARY
from	emp;

SELECT	EMP_NAME, SALARY
FROM	EMP
WHERE 	EMP_NAME = '오미자'; -- 문자열은 ''로 감싸줍니다.

-- 급여가 350만원 이상인 사람을 조회
-- WHERE : 조건절
-- WHERE 절의 조건이 TRUE인 행만 조회가 된다!!
SELECT	EMP_NAME, SALARY, DEPT_ID
FROM	EMP
WHERE 	SALARY >= 3500000; -- 문자열은 ''로 감싸줍니다.

-- 급여가 350만원 이상이고 부서가 회계관리부이거나 총무부인 사람을 조회
-- WHERE : 조건절
-- WHERE 절의 조건이 TRUE인 행만 조회가 된다!!
-- 1. 부서코드 확인하기 -> D2, D9
SELECT 	*
FROM 	DEPT; 

SELECT	EMP_NAME, SALARY -- 컬럼을 ,로 연결
FROM	EMP
WHERE 	SALARY >= 3500000	-- 조건을 연산자(AND, OR)를 이용해서 연결
AND		(DEPT_ID = 'D2'
OR		DEPT_ID = 'D9')
; -- 문자열은 ''로 감싸줍니다.

-- 별칭 - 컬럼이름에 연산식, 함수식이 들어간 경우 조회된 결과 컬럼명에 별칭을 달아준다
-- 1. 별칭을 작성할때 AS 키워드가 이용 가능(생략)
-- 2. 만약 공백/특수문자이 있다면 '', ""로 묶어줘야 함
-- 연봉계산 * + - /
-- 월급여 * 12
SELECT 	EMP_NAME 사원명, SALARY * 12 AS "연  봉"
FROM	EMP;


SELECT EMP_NAME, SALARY, BONUS FROM EMP;

-- 집계함수를 이용해서 사원의 수를 카운트 - 21명
SELECT COUNT(*) FROM EMP;

-- 급여 + (급여*보너스)
-- NULL은 연산이 불가능함
-- NULL을 다른값으로 치환하는 함수 값을 변경
-- IFNULL(컬럼이름, 변경할값) : 컬럼의 값이 NULL인경우 다른값으로 치환
SELECT EMP_NAME, SALARY, IFNULL(BONUS, 0),
		SALARY*IFNULL(BONUS,0) 보너스,  SALARY+(SALARY*IFNULL(BONUS,0)) '보너스를 포함한 급여'
FROM EMP;

-- JAVA 출력할때 + 와 비슷
-- 보너스를 합한 연봉
SELECT concat(EMP_NAME, '님의 연봉은 ', FLOOR(SALARY+(SALARY*IFNULL(BONUS,0))), '원 입니다.') 
		, FLOOR(SALARY+(SALARY*IFNULL(BONUS,0))) 연봉, '원' 단위
FROM EMP; 

-- 인사관리부 소속 사원을 모든컬럼 조회
-- 급여가 150만원 이상 300만원 이하인
-- BETWEEN A AND B A와 B 사이에 있는 데이터 (범위의 조건)
-- NOT BETWEEN : 반대
SELECT 	* 
FROM 	EMP 
WHERE 	DEPT_ID='D1'
AND 	SALARY NOT BETWEEN 1500000 AND 3000000;

-- 대문자 자동완성
-- 상단 메뉴에서 Edit ->️ Preferences 
-- -> Query Editor -> Use UPPERCASE keywords on completion 체크
-- autocommit 해제
-- -> SQL Execution -> New connections use auto commit mode 체크해제

-- 오토커밋 : 쿼리의 실행결과가 바로 반영
-- 0 : FALSE, 1: TRUE
-- @@ : MYSQL 시스템 변수
SELECT @@autocommit;
-- 1. 쿼리를 통해서 변경
-- 트랜젝션 처리(여러개의 실행쿼리를 하나로 묶는 작업) - DML
SET AUTOCOMMIT = 0; -- 오토커밋 해제

COMMIT; -- DB 반영
ROLLBACK; -- 취소
SET AUTOCOMMIT = 1; -- 오토커밋 설정

-- 2. 워크벤치의 설정을 통해서 변경

SELECT * FROM emp;

-- 테이블이 가지고 있는 컬럼정보 확인
DESC EMP;

-- 부서코드가 D5 인 직원의 사번, 이름, 입사일을 조회 (급여가 높은순으로 내림차순 정렬 - ORDER BY)
-- ORDER BY 컬럼이름 [ASC/DESC]
-- ASC 오름차순 정렬 - 기본값 생략가능
-- DESC 내림차순 정렬
SELECT	EMP_ID, EMP_NAME, HIRE_DATE, SALARY
FROM	EMP
WHERE	DEPT_ID = 'D5'
-- 날자를 문자열로 작성시에는 /, -
-- 20250525, 2025-05-25, 2025/05/25
-- 날자 비교 문자열형식으로 넣어주어도 자동 형변환이 됨
AND		HIRE_DATE > '20170101'
ORDER BY SALARY DESC;

-- 중복제거
SELECT 	DISTINCT DEPT_ID
FROM 	EMP;

-- 검색 - LIKE (조건절)
-- 성이 김씨인 사원을 조회
-- 컬럼이름 LIKE '김%'
-- '김%' : 김으로 시작하는
-- '%김' : 김으로 끝나는
-- '%김%' : 김을 포함하는
-- 제목에 ~을 포함하는, 작성자에 ~을 포함하는
-- 제목, 내용, 작성자 ~포함하는
SELECT	*
FROM	EMP
-- 김씨 이거나 이메일에 e가 포함된 사람을 조회
-- MYSQL에서 검색시 대소문자 상관없이 검색 
WHERE 	EMP_NAME LIKE '김%'
OR 		EMAIL LIKE '%e%';

SELECT 	*
FROM 	EMP
-- 두번째 글자가 해인 사원
-- _ : 한글자
WHERE 	EMP_NAME LIKE '_용%';

-- 이메일에 _가 포함되어 있는 사원
SELECT	* 
FROM	EMP
WHERE 	EMAIL LIKE '%_%' -- _ : 하나의 문자를 가르키는 와일드카드
-- 와일드카드 : 아무 문자나 와도 된다는 특수기호
-- WHERE 	EMAIL LIKE '%$_%' ESCAPE '$'
ORDER BY EMP_ID DESC;

-- _가 없는 이메일 삽입
DESC EMP;
INSERT INTO EMP (EMP_ID, EMAIL) VALUES ('300', 'abc@bbb.com');
SELECT * FROM EMP;

-- 이씨가 아닌 사람
-- !=, ^=, <>
-- LIKE, NOT LIKE
SELECT	*
FROM	EMP
WHERE 	EMP_NAME NOT LIKE '이%';

SELECT	*
FROM	EMP
WHERE 	DEPT_ID != 'D8';

-- IN 여러값중 하나와 일치하면 
-- 부서코드가 D1, D2, D3인 부서의 사원을 조회
SELECT	*
FROM	EMP
-- WHERE 	DEPT_ID = 'D1' OR DEPT_ID ='D2' OR DEPT_ID='D3';
WHERE 	DEPT_ID IN ('D1', 'D2', 'D3');

-- 사원테이블에서 사용중인 부서코드
-- D3이 존재하는지 확인
SELECT DISTINCT DEPT_ID FROM EMP ORDER BY DEPT_ID;


-- 보너스를 받지 않는 사원 = BONUS IS NULL
-- 컬럼이름 IS [NOT] NULL 
-- NULL은 비교연산자를 사용할 수 없다!
SELECT	*
FROM	EMP
WHERE 	BONUS IS NOT NULL;







