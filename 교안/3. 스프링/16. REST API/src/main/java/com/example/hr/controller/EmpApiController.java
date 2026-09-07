package com.example.hr.controller;

import com.example.hr.domain.Emp;
import com.example.hr.dto.EmpCreateRequest;
import com.example.hr.dto.EmpResponse;
import com.example.hr.dto.EmpUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/emps")
@RequiredArgsConstructor
public class EmpApiController {

    private final EmpService empService;

    // 목록 (페이징) : GET /api/emps?keyword=김&page=1&size=10
    @GetMapping
    public PageResult<EmpResponse> list(EmpSearchCond cond) {
        PageResult<Emp> page = empService.search(cond);      // 서비스는 도메인 Emp
        return new PageResult<>(                             // API 경계에서 응답 DTO로
                page.content().stream().map(EmpResponse::from).toList(),
                page.totalElements(), page.page(), page.size());
    }

    // 단건 : GET /api/emps/205
    @GetMapping("/{id}")
    public EmpResponse detail(@PathVariable Long id) {
        return EmpResponse.from(empService.get(id));
    }

    // 등록 : POST /api/emps  (본문 JSON)
    @PostMapping
    public ResponseEntity<EmpResponse> create(@Valid @RequestBody EmpCreateRequest req) {
        Long id = empService.register(req.toForm());
        EmpResponse body = EmpResponse.from(empService.get(id));
        return ResponseEntity
                .created(URI.create("/api/emps/" + id))   // 201 + Location 헤더
                .body(body);
    }

    // 수정 : PUT /api/emps/205
    @PutMapping("/{id}")
    public EmpResponse update(@PathVariable Long id, @Valid @RequestBody EmpUpdateRequest req) {
        empService.modify(id, req.toForm());
        return EmpResponse.from(empService.get(id));
    }

    // 삭제 : DELETE /api/emps/205
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        empService.remove(id);
        return ResponseEntity.noContent().build();          // 204
    }
}
