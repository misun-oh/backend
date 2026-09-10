package com.example.hr.controller;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import com.example.hr.common.SessionConst;
import com.example.hr.common.exception.NotFoundException;
import com.example.hr.domain.Dept;
import com.example.hr.domain.Emp;
import com.example.hr.domain.Job;
import com.example.hr.dto.EmpForm;
import com.example.hr.dto.EmpSearchCond;
import com.example.hr.dto.LoginMember;
import com.example.hr.dto.PageResult;
import com.example.hr.service.DeptService;
import com.example.hr.service.EmpService;
import com.example.hr.service.JobService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(EmpController.class)
class EmpControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    EmpService empService;

    @MockitoBean
    DeptService deptService;

    @MockitoBean
    JobService jobService;

    /** LoginCheckInterceptor(R6)가 모든 요청을 막으므로, 로그인한 세션을 흉내낸다. */
    private static final LoginMember LOGIN_MEMBER = new LoginMember("admin", "인사담당자");

    @Test
    void 사원_목록_조회() throws Exception {
        Emp emp = Emp.builder()
                .empId(205L).empName("박지민").deptName("해외영업1부").jobName("부장")
                .hireDate(LocalDate.of(2015, 5, 20)).active(true).build();
        given(empService.search(any(EmpSearchCond.class)))
                .willReturn(new PageResult<>(List.of(emp), 1, 1, 10));
        given(deptService.listAll()).willReturn(List.of(Dept.builder().deptId(5L).deptName("해외영업1부").build()));
        given(empService.countAll()).willReturn(21L);
        given(empService.countActive()).willReturn(20L);

        mvc.perform(get("/emps").sessionAttr(SessionConst.LOGIN_MEMBER, LOGIN_MEMBER))
                .andExpect(status().isOk())
                .andExpect(view().name("hr/index"))
                .andExpect(model().attributeExists("page", "depts", "cond"))
                .andExpect(content().string(containsString("박지민")));
    }

    @Test
    void 사원_상세_조회() throws Exception {
        Emp manager = Emp.builder().empId(200L).empName("곽상혁").build();
        Emp emp = Emp.builder()
                .empId(205L).empName("박지민").email("park_jm@company.com")
                .deptId(5L).deptName("해외영업1부").jobCode("J3").jobName("부장")
                .managerId(200L).managerName("곽상혁")
                .salary(3500000).hireDate(LocalDate.of(2015, 5, 20)).active(true).build();
        given(empService.get(205L)).willReturn(emp);
        given(empService.listSubordinates(205L)).willReturn(List.of(
                Emp.builder().empId(206L).empName("염성원").jobName("과장").build()));

        mvc.perform(get("/emps/205").sessionAttr(SessionConst.LOGIN_MEMBER, LOGIN_MEMBER))
                .andExpect(status().isOk())
                .andExpect(view().name("hr/emp-detail"))
                .andExpect(model().attribute("emp", emp))
                .andExpect(content().string(containsString("곽상혁")))
                .andExpect(content().string(containsString("염성원")));
    }

    @Test
    void 없는_사원_상세조회는_404() throws Exception {
        given(empService.get(999L)).willThrow(new NotFoundException("사원을 찾을 수 없습니다."));

        mvc.perform(get("/emps/999").sessionAttr(SessionConst.LOGIN_MEMBER, LOGIN_MEMBER))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }

    @Test
    void 사원_등록_폼_조회() throws Exception {
        given(deptService.listAll()).willReturn(List.of());
        given(jobService.listAll()).willReturn(List.of(new Job("J7", "사원")));
        given(empService.listActiveForDropdown()).willReturn(List.of());

        mvc.perform(get("/emps/new").sessionAttr(SessionConst.LOGIN_MEMBER, LOGIN_MEMBER))
                .andExpect(status().isOk())
                .andExpect(view().name("hr/emp-form"))
                .andExpect(model().attributeExists("empForm"));
    }

    @Test
    void 사원_등록_성공하면_상세페이지로_리다이렉트() throws Exception {
        given(empService.register(any(EmpForm.class))).willReturn(221L);

        mvc.perform(post("/emps")
                        .sessionAttr(SessionConst.LOGIN_MEMBER, LOGIN_MEMBER)
                        .param("empName", "신입사원")
                        .param("email", "new@company.com")
                        .param("deptId", "5")
                        .param("jobCode", "J7")
                        .param("salary", "2500000")
                        .param("hireDate", "2026-09-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emps/221"))
                .andExpect(flash().attributeExists("msg"));
    }

    @Test
    void 사원_등록_필수값_누락이면_폼을_다시_보여준다() throws Exception {
        given(deptService.listAll()).willReturn(List.of());
        given(jobService.listAll()).willReturn(List.of());
        given(empService.listActiveForDropdown()).willReturn(List.of());

        mvc.perform(post("/emps").sessionAttr(SessionConst.LOGIN_MEMBER, LOGIN_MEMBER).param("empName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("hr/emp-form"));
    }

    @Test
    void 사원_삭제하면_목록으로_리다이렉트() throws Exception {
        given(empService.get(205L)).willReturn(Emp.builder().empId(205L).empName("박지민").build());

        mvc.perform(post("/emps/205/delete").sessionAttr(SessionConst.LOGIN_MEMBER, LOGIN_MEMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emps"))
                .andExpect(flash().attributeExists("msg"));
    }

    // --- Model 파라미터를 직접 채우는 메서드는 MockMvc 없이도 검증할 수 있다 --------------------

    @Test
    void addFormSupport는_실제_Model에_부서_직급_관리자후보를_채운다() {
        // "진짜" Model 구현체(ConcurrentModel)를 넣고, 최종 상태를 assertThat으로 검증(상태 기반 검증)
        List<Dept> depts = List.of(Dept.builder().deptId(5L).deptName("해외영업1부").build());
        List<Job> jobs = List.of(new Job("J7", "사원"));
        given(deptService.listAll()).willReturn(depts);
        given(jobService.listAll()).willReturn(jobs);
        given(empService.listActiveForDropdown()).willReturn(List.of(
                Emp.builder().empId(200L).empName("곽상혁").build(),
                Emp.builder().empId(205L).empName("박지민").build()));

        EmpController controller = new EmpController(empService, deptService, jobService);
        Model model = new ConcurrentModel();

        controller.addFormSupport(model, 205L);   // 205번(자기 자신)은 관리자 후보에서 빠져야 한다

        assertThat(model.getAttribute("depts")).isSameAs(depts);
        assertThat(model.getAttribute("jobs")).isSameAs(jobs);
        assertThat((List<?>) model.getAttribute("managers"))
                .extracting("empId")
                .containsExactly(200L);
    }

    @Test
    void addFormSupport는_Model을_목으로_검증할_수도_있다() {
        // Model을 목으로 두고, "어떤 메서드가 어떤 인자로 호출됐는지"만 검증(상호작용 기반 검증)
        given(deptService.listAll()).willReturn(List.of());
        given(jobService.listAll()).willReturn(List.of());
        given(empService.listActiveForDropdown()).willReturn(List.of());

        EmpController controller = new EmpController(empService, deptService, jobService);
        Model mockModel = mock(Model.class);   // @Mock 애노테이션 없이도 Mockito.mock()으로 바로 생성 가능

        controller.addFormSupport(mockModel, null);

        then(mockModel).should().addAttribute(eq("depts"), any());
        then(mockModel).should().addAttribute(eq("jobs"), any());
        then(mockModel).should().addAttribute(eq("managers"), any());
    }

    @Test
    void 로그인하지_않으면_로그인페이지로_리다이렉트된다() throws Exception {
        // 세션에 LOGIN_MEMBER를 넣지 않음 -> LoginCheckInterceptor가 컨트롤러 진입 자체를 막는다
        mvc.perform(get("/emps"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", startsWith("/login")));
    }
}
