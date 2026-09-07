package com.example.hr.jpa.repository;

import com.example.hr.jpa.domain.Emp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmpRepository extends JpaRepository<Emp, Long> {

    // 메서드 이름만으로 쿼리가 생성된다 ("쿼리 메서드")
    List<Emp> findByDeptDeptId(Long deptId);

    List<Emp> findBySalaryGreaterThanEqual(int salary);

    @Query("select e.dept.deptName, avg(e.salary) from Emp e group by e.dept.deptName")
    List<Object[]> avgSalaryByDept();

    @Query("select e from Emp e where e.salary >= :min order by e.salary desc")
    List<Emp> findHighPaid(@Param("min") int min);

    // 연관 엔티티까지 한 번에 가져오는 fetch join (N+1 예방)
    @Query("select e from Emp e join fetch e.dept where e.dept.deptId = :deptId")
    List<Emp> findByDeptIdWithDept(@Param("deptId") Long deptId);

    // 정말 복잡한 통계·튜닝이 필요하면 네이티브 SQL도 탈출구로 열려있다
    @Query(value = "SELECT * FROM EMP WHERE SALARY >= :min", nativeQuery = true)
    List<Emp> findHighPaidNative(@Param("min") int min);
}
