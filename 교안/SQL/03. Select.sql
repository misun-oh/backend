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
ORDER BY SALARY DESC;




