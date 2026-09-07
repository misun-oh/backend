package com.example.demo.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.example.demo.dto.Emp;
import com.example.demo.dto.EmpSearchCond;

@Mapper
public interface EmpMapper {
    //@Select("select * from EMP")
    List<Emp> selectByCond(EmpSearchCond cond);
}
