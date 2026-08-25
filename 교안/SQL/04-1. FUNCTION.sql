# 형변환 함수
# DATE_FORMAT : 날자형식 -> 문자형식
# STR_TO_DATE : 문자 -> 날자
# y : 2자리 년도
# y : 4자리 년도
SELECT 	HIRE_DATE
, date_format(HIRE_DATE, '%Y-%M-%D'), date_format(HIRE_DATE, '%y-%m-%d')
, SALARY
, CAST(SALARY AS CHAR)
-- 문자타입 -> 숫자타입으로
-- 형변환 연산 (문자열 숫자)
, CAST('12345' AS SIGNED)
-- NULL 연산 대상이 아님 -> 치환
, IFNULL(BONUS, 0)

FROM 	EMP;

-- 자동형변환 되어져서 연산이 됨
-- 함수확인, 연산
SELECT '1' + 123;


-- 치환 
-- 주민등록번호 뒷자리가 1,3이면 '남', 2,4이면 '여' 
-- CASE 값 WHEN 값1 THEN 결과1
--			WHEN 값2 THEN 결과2 END 기본결과
-- IF(조건문, 참일_때_값, 거짓일_때_값)
-- 중첩IF
-- IF(조건1, 참일_때_값1, IF(조건2, 참일_때_값2, 모든_조건이_거짓일_때_값))           
SELECT 	EMP_NO, SUBSTRING(EMP_NO, 8, 1) '성별코드'
		, CASE SUBSTRING(EMP_NO, 8, 1) WHEN '1' THEN '남'
										WHEN '2' THEN '여'
                                        WHEN '3' THEN '남'
                                        WHEN '4' THEN '여'
                                        ELSE '기본값' END CASE성별1
		-- 비교문 사용
		, CASE WHEN SUBSTRING(EMP_NO, 8, 1) = '1' OR  SUBSTRING(EMP_NO, 8, 1) = '3' THEN '남' 
				WHEN SUBSTRING(EMP_NO, 8, 1) = '2' OR  SUBSTRING(EMP_NO, 8, 1) = '4' THEN '여' 
				ELSE '성별을 확인할 수 없습니다.' END CASE성별2
		, IF(SUBSTR(EMP_NO, 8, 1) IN ('1', '3'), '남', '여') AS gender
        -- 1,3 남, 2,4 여, 확인불가
		, IF(SUBSTR(EMP_NO, 8, 1) IN ('1', '3'), '남', 
				IF(SUBSTR(EMP_NO, 8, 1) IN ('2', '4'), '여', '확인불가')) AS gender

        , IF(SUBSTR(EMP_NO, 8, 1) = '1' OR  SUBSTR(EMP_NO, 8, 1) = '3', '남', '여') AS gender
FROM 	EMP;

SELECT 
 CASE SUBSTRING('123456-5234567', 8, 1) WHEN '1' THEN '남'
										WHEN '2' THEN '여'
                                        WHEN '3' THEN '남'
                                        WHEN '4' THEN '여'
                                        ELSE '기본값' END CASE성별1
, IF(SUBSTR('123456-5234567', 8, 1) IN ('1', '3'), '남', 
				IF(SUBSTR('123456-5234567', 8, 1) IN ('2', '4'), '여', '확인불가')) AS gender;

# 그룹함수의 조건문!
-- 부서별 급여의 합계를 구해봅시다, SUM(컬럼이름), GROUP BY 부서, 
-- 부서코드, 부서명, 합계를 조회
SELECT	DEPT_ID, DEPT_TITLE, SUM(SALARY) '급여의 합계'
FROM 	EMP
JOIN	DEPT USING (DEPT_ID)
-- 그룹으로 묶는다는것은 여러개의 행이 하나로 합쳐지는 것을 의미
-- GROUP BY절에 사용된 컬럼과 집계함수만 SELECT절에 올수 있다!!!!!!
-- 평균 AVG(), 합계 SUM(), 최대값 MAX(), 최소값 MIN(), 행의개수 COUNT()
-- 부서별 급여의 합계가 1000만원 이상인 부서를 조회
GROUP BY DEPT_ID, DEPT_TITLE
HAVING SUM(SALARY) >= 10000000
-- SELECT절에 사용된 별칭을 사용 할 수 있다
-- ORDER BY '급여의 합계' DESC
ORDER BY 3 DESC;

-- 직급별 급여의 평균을 
-- 김씨이거나 이씨성을 가진 사원의 직급명, 급여의 평균
SELECT 	JOB_NAME, FLOOR(AVG(SALARY)) 급여
FROM 	EMP
JOIN	JOB USING (JOB_CODE)
-- 조건절에는 집계합수를 사용할수 없다!!
WHERE	EMP_NAME LIKE '김%'
	OR	EMP_NAME LIKE '이%'
GROUP BY JOB_CODE, JOB_NAME
-- 평균급여(집계합수의 결과)가  300만원 이상인 직급만 조회
HAVING AVG(SALARY) >= 3000000
-- 정렬 : 컬럼이름, 컬럼순서(인덱스 1부텉 시작)
-- DESC : 내림차순
-- ORDER BY JOB_NAME DESC;
ORDER BY 1 DESC;

-- JOIN 대신 SUBQUERY를 사용 해봅시다
-- 쿼리(MAIN) 안에 쿼리(SUB)를 작성 
-- 서브쿼리는 괄호 안에 작성
SELECT 	DEPT_ID, (SELECT DEPT_TITLE FROM DEPT WHERE DEPT_ID = EMP.DEPT_ID) DEPT_TITLE
FROM 	EMP;


SELECT HIRE_DATE, date_format(HIRE_DATE, '%Y') FROM EMP;

-- 급여가 평균급여보다 높은 사람
-- 사원이름, 급여, 평균급여
-- 집계함수를 이용할 경우 일반 컬럼은 조회 할수 없음 -> 
-- 집계된 값을 출력하고 싶은 경우 서브쿼리를 사용한다!
SELECT 	EMP_NAME, SALARY, (SELECT AVG(SALARY) FROM EMP) 평균급여
FROM	EMP
-- FROM절에 집계함수롤 이용할 수 없으므로 서브쿼리를 사용
-- 단일행 단일컬럼 : =, >=, <=, >, <, !=
WHERE 	SALARY >= (SELECT AVG(SALARY) FROM EMP);

-- 조회결과 행이 하나인 경우 단일행
-- 조회결과 행이 여러개인 경우 다중행
-- 단일행 다중컬럼
-- NULL은 제외 
SELECT AVG(SALARY), SUM(SALARY), MAX(EMP_NAME), MIN(BONUS) FROM EMP;

-- 다중행 단일컬럼 서브쿼리
-- D1, D2, D3
-- IN ('', '', '')
SELECT 	*
FROM 	EMP
-- IN절 안에는 여러개의 값이 나열 -> 서브쿼리의 실행 결과가 다중행인 경우 사용 가능
WHERE 	DEPT_ID IN (SELECT 	DISTINCT DEPT_ID
					FROM 	EMP
					WHERE 	JOB_CODE = 'J6');

-- 다중컬럼
-- 이광렬사원과 같은 부서, 같은 직군에 있는 사람을 조회
SELECT 	DEPT_ID, JOB_CODE
FROM 	EMP
WHERE 	EMP_NAME='이광렬';

SELECT 	*
FROM 	EMP
-- WHERE	(DEPT_ID, JOB_CODE) = ('D8','J6');
WHERE	(DEPT_ID, JOB_CODE) = (SELECT 	DEPT_ID, JOB_CODE
								FROM 	EMP
								WHERE 	EMP_NAME='이광렬');

-- 직급 코드 J6인 사원의 부서 코드
-- DISTINCT 중복제거
SELECT 	DISTINCT DEPT_ID
FROM 	EMP
WHERE 	JOB_CODE = 'J6';

SELECT * FROM EMP;
SELECT AVG(SALARY) FROM EMP;

-- 윤정주 사원보다 급여가 높은사원의 이름과 급여를 내림차순으로 정렬
-- 이름, 급여, 윤정주사원의 급여 출력
-- 윤정주 사원의 급여 (서브쿼리)
SELECT SALARY FROM EMP WHERE EMP_NAME = '윤정주';
SELECT 	EMP_NAME, SALARY, (SELECT SALARY FROM EMP WHERE EMP_NAME = '윤정주') '윤정주사원의 급여'
FROM	EMP
-- WHERE 	SALARY >= 3760000;
WHERE 	SALARY >= (SELECT SALARY FROM EMP WHERE EMP_NAME = '윤정주')
AND		EMP_NAME != '윤정주';

-- 부장직급이 존재하는 부서의 모든 사원정보를 출력
-- 부서코드, 이름
-- 사번 오름차순으로 정렬
SELECT * FROM JOB;
-- 부장인 사원의 부서코드
SELECT 	DISTINCT DEPT_ID 
FROM 	EMP
JOIN 	JOB USING (JOB_CODE)
WHERE 	JOB_NAME = '부장';

SELECT 	DEPT_ID, EMP_NAME
FROM 	EMP
-- = 은 단일행단일컬럼, 다중행인 경우 IN절을 이용
-- 서브쿼리의 실행 결과에 따라 사용되는 연산자가 달라짐!
WHERE	DEPT_ID IN (SELECT 	DISTINCT DEPT_ID 
					FROM 	EMP
					JOIN 	JOB USING (JOB_CODE)
					WHERE 	JOB_NAME = '부장')
ORDER BY EMP_ID DESC;


-- 주민번호를 기준으로 나이를 구하시오
-- 이름, 나이, 성별 
-- 이름을 오름차순으로 정렬
-- 날자연산 TIMESTAMPDIFF(비교단위, 시작일, 종료일)
-- 주민번호로 부터 생년월일을 꺼내서 날자로 형변환 (년도가 2자리이기 때문에 주민번호 뒤의 한자리를 이용해서 4자리 년도로 변환)
-- 951212-1234567 -> 8번째 1자리 가 1이므로 19951212를 날자형식으로 변환 후 날자비교 함수를 적용
-- 날자 형변환 STR_TO_DATE(컬럼, 형식)
SELECT HIRE_DATE, TIMESTAMPDIFF(YEAR, HIRE_DATE, NOW()) FROM EMP;
-- 주민등록번호의 앞 2자리를 가지고 년도
SELECT  EMP_NO, SUBSTRING(EMP_NO, 1, 2) 년도, SUBSTRING(EMP_NO, 8, 1) 뒤자리
		, STR_TO_DATE( CASE WHEN SUBSTRING(EMP_NO, 8, 1) IN ('1', '2') THEN CONCAT('19', SUBSTRING(EMP_NO, 1, 6))
				WHEN SUBSTRING(EMP_NO, 8, 1) IN ('3', '4') THEN CONCAT('20', SUBSTRING(EMP_NO, 1, 6))
        END, '%Y%m%d')
        생년월일
        , SUBSTRING(EMP_NO, 1, 6)
        , TIMESTAMPDIFF(YEAR, STR_TO_DATE( CASE WHEN SUBSTRING(EMP_NO, 8, 1) IN ('1', '2') THEN CONCAT('19', SUBSTRING(EMP_NO, 1, 6))
				WHEN SUBSTRING(EMP_NO, 8, 1) IN ('3', '4') THEN CONCAT('20', SUBSTRING(EMP_NO, 1, 6))
        END, '%Y%m%d'), NOW()) 나이
FROM 	EMP;





-- 가장 최근에 입사한 사원과 성이 같은 사원의 정보











