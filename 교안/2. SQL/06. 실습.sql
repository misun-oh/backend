-- --------------------------------------
-- SELECT 실습
-- --------------------------------------
-- 학과, 학생, 과목, 교수, 과목-교수, 성적
-- 국어국문학과에 재학중인 학생
-- ABSENCE_YN : 휴학여부
SELECT 	* 
FROM 	TB_STUDENT
-- INNER JOIN : 조건이 일치하는 행만 조회 - 누락되는 행이 발생
JOIN 	TB_DEPARTMENT USING (DEPARTMENT_NO)
WHERE 	DEPARTMENT_NAME = '국어국문학과'
AND 	ABSENCE_YN = 'N' -- 휴학여부 컬럼을 이용해서 재학생만 조회
;
-- 여학생만 조회
SELECT 	STUDENT_SSN, SUBSTRING(STUDENT_SSN, 8, 1)
FROM 	TB_STUDENT
WHERE 	SUBSTRING(STUDENT_SSN, 8, 1) IN ('2', '4');

-- 입학정원이 20명 이상 30명 이하인 학과의 이름과 계열을 출력
SELECT 	DEPARTMENT_NAME '학과 명', CATEGORY 계열
FROM 	TB_DEPARTMENT
-- WHERE 	CAPACITY >= 20
--   AND 	CAPACITY <= 30;
WHERE 	CAPACITY BETWEEN 20 AND 30;

-- 총장을 제외하고 모든 교수들이 소속 학과를 가지고 있다. 총장의 이름은?
SELECT 	PROFESSOR_NAME
FROM 	TB_PROFESSOR
WHERE	DEPARTMENT_NO IS NULL;

-- 전산상의 착오로 학과가 지정되어 있지 않은 학생이 있는지 확인해보자
SELECT 	*
FROM 	TB_STUDENT
WHERE 	DEPARTMENT_NO IS NULL;

-- 수강신청을 하려고 한다. 선수과목 여부를 확인해야 하는데, 선수과목이 존재하는 과목들은 어떤 과목인지 과목 번호를 조회
SELECT 	* 
FROM 	TB_CLASS
WHERE 	PREATTENDING_CLASS_NO IS NOT NULL;

-- 대학에는 어떤 계열(CATEGORY)들이 있는지 조회
SELECT 	DISTINCT CATEGORY 계열
FROM	TB_DEPARTMENT;

-- 02학번 전주 거주자들의 모임 (휴학한 사람들은 제외한 재학중인 학생들의 학번, 이름, 주민번호를 출력)
SELECT 	STUDENT_NO, STUDENT_NAME, STUDENT_SSN
FROM 	TB_STUDENT
WHERE 	STUDENT_NO LIKE 'A02%'
  AND	STUDENT_ADDRESS LIKE '%전주%'
  AND   ABSENCE_YN = 'N';




-- --------------------------------------
-- SELECT(FUNCTION) 실습
-- --------------------------------------
-- 영어영문학과(학과코드 002) 학생들의 학번과 이름, 입학 년도를 입학 년도가 빠른 순으로 표시하는 SQL문장을 작성하시오.
-- (단, 헤더는 "학번", "이름", "입학년도" 가 표시되도록 한다.)
-- 이름이 같은 컬럼을 조회 할때, 테이블명을 명시
SELECT 	S.STUDENT_NO 학번, STUDENT_NAME 이름, DATE_FORMAT(ENTRANCE_DATE, '%Y') 입학년도
		, EXTRACT(YEAR FROM ENTRANCE_DATE)
-- 테이블에 별칭을 주면 별칭 사용가능
FROM 	TB_STUDENT S
JOIN	TB_DEPARTMENT D ON S.DEPARTMENT_NO = D.DEPARTMENT_NO
WHERE	DEPARTMENT_NAME = '영어영문학과'
-- SELECT절에 조회 결과를 사용할 수 있다
ORDER BY 입학년도;

-- 2027-12-25 날자로 변환
-- 앞에있는 문자열의 형식과 뒤에 있는 형식이 일치해야 함!
SELECT STR_TO_DATE('2027-12-25', '%Y-%m-%d');

-- 교수 이름이 3글자가 아닌 사람, (2글자인 사람)
SELECT 	PROFESSOR_NAME, CHAR_LENGTH(PROFESSOR_NAME)
FROM 	TB_PROFESSOR
-- != <>
-- WHERE	CHAR_LENGTH(PROFESSOR_NAME) != 3;
-- WHERE PROFESSOR_NAME LIKE '__';
WHERE PROFESSOR_NAME NOT LIKE '___';

-- 2000년도 이전 입학자들을 출력(A를 포함하지 않는)
SELECT 	*
FROM 	TB_STUDENT
-- WHERE	STUDENT_NO LIKE 'A%';
WHERE 	 EXTRACT(YEAR FROM ENTRANCE_DATE) < 2000;

-- 학번이 A121056 한아름 학생의 학점 총 평점을 구하는 SQL문
SELECT  TRUNCATE(AVG(POINT), 2) FROM TB_GRADE WHERE STUDENT_NO = 'A121056';

-- 지도교수를 배정받지 못한 학생의 수
SELECT 	COUNT(*)
FROM 	TB_STUDENT
WHERE 	COACH_PROFESSOR_NO IS NULL;

-- 동명이인의 이름과 수 - 그룹
SELECT 	STUDENT_NAME, COUNT(*)
FROM 	TB_STUDENT
GROUP BY STUDENT_NAME
HAVING	COUNT(*) > 1;

-- '인문사회' 계열에 속한 과목이름(TB_CLASS)과 교수(TB_PROFESSOR) 이름
-- TB_DEPARTMENT
SELECT *
FROM TB_CLASS
JOIN TB_DEPARTMENT USING (DEPARTMENT_NO)
WHERE CATEGORY = '인문사회';


-- TB_CLASS_PROFESSOR (과목 - 교수)
SELECT CLASS_NAME, PROFESSOR_NAME, CATEGORY
FROM TB_CLASS C
JOIN tb_class_professor USING (CLASS_NO)
JOIN tb_professor USING (PROFESSOR_NO)
-- DEPARTMENT_NO 테이블이 여러개 있어서 오류
JOIN TB_DEPARTMENT D ON C.DEPARTMENT_NO = D.DEPARTMENT_NO
WHERE CATEGORY = '인문사회'






