package com.example.hr.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hr.domain.Job;
import com.example.hr.mapper.JobMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobServiceImpl implements JobService {

    private final JobMapper jobMapper;

    @Override
    public List<Job> listAll() {
        return jobMapper.selectAll();
    }
}
