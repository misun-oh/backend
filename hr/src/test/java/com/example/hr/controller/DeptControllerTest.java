package com.example.hr.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.hr.common.SessionConst;
import com.example.hr.common.exception.DeptInUseException;
import com.example.hr.dto.DeptForm;
import com.example.hr.dto.DeptListRow;
import com.example.hr.dto.LoginMember;
import com.example.hr.service.DeptService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(DeptController.class)
class DeptControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    DeptService deptService;

    /** LoginCheckInterceptor(R6)가 모든 요청을 막으므로, 로그인한 세션을 흉내낸다. */
    private static final LoginMember LOGIN_MEMBER = new LoginMember("admin", "인사담당자");

    @Test
    void 부서_목록_조회() throws Exception {
        given(deptService.listWithStats()).willReturn(List.of(
                new DeptListRow(3L, "마케팅부", "서울", 0, 0),
                new DeptListRow(5L, "해외영업1부", "일본", 5, 2752000)));

        mvc.perform(get("/depts").sessionAttr(SessionConst.LOGIN_MEMBER, LOGIN_MEMBER))
                .andExpect(status().isOk())
                .andExpect(view().name("hr/depts"))
                .andExpect(model().attributeExists("depts", "deptForm", "locations"))
                .andExpect(content().string(containsString("해외영업1부")));
    }

    @Test
    void 부서_추가_성공하면_목록으로_리다이렉트() throws Exception {
        given(deptService.register(any(DeptForm.class))).willReturn(10L);

        mvc.perform(post("/depts")
                        .sessionAttr(SessionConst.LOGIN_MEMBER, LOGIN_MEMBER)
                        .param("deptName", "신사업개발부")
                        .param("location", "서울"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/depts"))
                .andExpect(flash().attributeExists("msg"));
    }

    @Test
    void 부서_수정_성공하면_목록으로_리다이렉트() throws Exception {
        mvc.perform(post("/depts/5")
                        .sessionAttr(SessionConst.LOGIN_MEMBER, LOGIN_MEMBER)
                        .param("deptName", "해외영업1부(개편)")
                        .param("location", "일본"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/depts"))
                .andExpect(flash().attributeExists("msg"));

        then(deptService).should().modify(org.mockito.ArgumentMatchers.eq(5L), any(DeptForm.class));
    }

    @Test
    void 부서명이_없으면_목록화면을_다시_보여준다() throws Exception {
        given(deptService.listWithStats()).willReturn(List.of());

        mvc.perform(post("/depts").sessionAttr(SessionConst.LOGIN_MEMBER, LOGIN_MEMBER).param("deptName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("hr/depts"));
    }

    @Test
    void 부서_삭제_성공하면_목록으로_리다이렉트() throws Exception {
        mvc.perform(post("/depts/3/delete").sessionAttr(SessionConst.LOGIN_MEMBER, LOGIN_MEMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/depts"))
                .andExpect(flash().attributeExists("msg"));
    }

    @Test
    void 소속_사원이_있으면_삭제_실패_메시지와_함께_리다이렉트() throws Exception {
        willThrow(new DeptInUseException("소속 사원이 5명 있어 부서를 삭제할 수 없습니다."))
                .given(deptService).remove(5L);

        mvc.perform(post("/depts/5/delete").sessionAttr(SessionConst.LOGIN_MEMBER, LOGIN_MEMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/depts"))
                .andExpect(flash().attributeExists("errorMsg"));
    }
}
