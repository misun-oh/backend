package com.example.hr;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmpController.class)
class EmpControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean EmpService empService;      // Boot 4: @MockBean → @MockitoBean

    @Test void 목록_JSON() throws Exception {
        given(empService.findAll()).willReturn(List.of(
            Emp.builder().empId(200L).empName("곽상혁").deptName("총무부").hireDate(LocalDate.now()).active(true).build()));
        mvc.perform(get("/api/emps"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].empName").value("곽상혁"));
    }
}
