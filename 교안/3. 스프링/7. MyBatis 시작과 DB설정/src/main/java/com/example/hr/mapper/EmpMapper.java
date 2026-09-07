package com.example.hr.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.hr.domain.Emp;

@Mapper
public interface EmpMapper {
    List<Emp> findAll();
    Emp findById(Long empId);
    List<Emp> findByNameLike(String keyword);
}
