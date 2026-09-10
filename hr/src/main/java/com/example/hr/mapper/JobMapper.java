package com.example.hr.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.hr.domain.Job;

@Mapper
public interface JobMapper {

    List<Job> selectAll();
}
