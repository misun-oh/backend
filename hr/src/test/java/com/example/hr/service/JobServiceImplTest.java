package com.example.hr.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.hr.domain.Job;
import com.example.hr.mapper.JobMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock
    JobMapper jobMapper;

    @InjectMocks
    JobServiceImpl jobService;

    @Test
    void 직급_목록_조회는_매퍼에_그대로_위임한다() {
        given(jobMapper.selectAll()).willReturn(List.of(
                new Job("J1", "대표"),
                new Job("J7", "사원")));

        List<Job> jobs = jobService.listAll();

        assertThat(jobs).extracting(Job::getJobName).containsExactly("대표", "사원");
        then(jobMapper).should().selectAll();
    }
}
