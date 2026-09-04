package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Day 2 — 사원 데이터 "REST API" 컨트롤러 (JSON 응답).
 *
 * <p>{@code @RestController} = {@code @Controller} + {@code @ResponseBody}.
 * 그래서 반환값을 <b>뷰 이름으로 해석하지 않고</b> 값 그 자체를 응답 본문에
 * 쓴다. 객체·리스트를 돌려주면 Jackson 이 JSON 으로 직렬화한다.
 * ({@link EmpController} 는 화면 HTML, 이 클래스는 데이터 JSON — 역할이 다르다.)
 *
 * <p>{@code @RequestMapping("/api/emps")} 로 공통 경로를 클래스에 모아두고,
 * 각 메서드는 나머지 경로 + HTTP 메서드만 적는다.
 * DB·Service 계층은 Day 7~ 이므로 지금은 메모리 리스트로 흉내만 낸다.
 */
@RestController
@RequestMapping("/api/emps")
public class EmpRestController {

    /** 응답용 사원 레코드(임시). Day 7 에서 Entity/DTO 로 교체한다. */
    public record Emp(String id, String name, String dept, int salary) {}

    /** DB 대용 메모리 저장소. */
    private final List<Emp> store = new ArrayList<>(List.of(
        new Emp("200", "곽상혁", "총무부", 8_000_000),
        new Emp("201", "권진우", "총무부", 6_000_000),
        new Emp("213", "이다현", "인사관리부", 2_780_000)
    ));

    /**
     * GET /api/emps — 사원 전체 목록을 JSON 배열로 돌려준다.
     * <p>{@code ?dept=총무부} 처럼 쿼리 파라미터가 오면 부서로 거른다.
     */
    @GetMapping
    public List<Emp> list(@RequestParam(required = false) String dept) {
        if (dept == null || dept.isBlank()) {
            return store;
        }
        return store.stream().filter(e -> e.dept().equals(dept)).toList();
    }

    /**
     * GET /api/emps/{id} — 사원 1건을 조회한다.
     * <p>있으면 200 + JSON, 없으면 404.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Emp> one(@PathVariable String id) {
        return store.stream()
            .filter(e -> e.id().equals(id))
            .findFirst()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/emps — 사원을 등록한다.
     * <p>요청 본문 JSON 을 {@code @RequestBody} 로 {@link Emp} 에 바인딩한다.
     * 성공하면 201 Created + 저장된 객체.
     */
    @PostMapping
    public ResponseEntity<Emp> create(@RequestBody Emp emp) {
        store.add(emp);
        return ResponseEntity.status(201).body(emp);
    }

    /**
     * PUT /api/emps/{id} — 사원 정보를 통째로 수정한다.
     * <p>같은 {@code id} 를 찾아 새 값으로 교체한다. 대상이 없으면 404.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Emp> update(@PathVariable String id, @RequestBody Emp emp) {
        for (int i = 0; i < store.size(); i++) {
            if (store.get(i).id().equals(id)) {
                Emp updated = new Emp(id, emp.name(), emp.dept(), emp.salary());
                store.set(i, updated);
                return ResponseEntity.ok(updated);
            }
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * DELETE /api/emps/{id} — 사원을 삭제한다.
     * <p>지웠으면 204 No Content, 대상이 없으면 404.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean removed = store.removeIf(e -> e.id().equals(id));
        return removed
            ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build();
    }
}
