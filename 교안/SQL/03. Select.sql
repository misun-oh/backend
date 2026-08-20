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








