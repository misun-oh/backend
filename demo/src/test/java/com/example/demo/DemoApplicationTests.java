package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.dto.Emp;
import com.example.demo.dto.EmpSearchCond;
import com.example.demo.mapper.EmpMapper;

@SpringBootTest
class DemoApplicationTests {

	@Autowired 
	EmpMapper mapper;

	@Test
	void test1() {
		
		List<Emp> list = mapper.selectByCond(new EmpSearchCond());
		
		System.out.println(list.size());
		assertEquals(22, list.size());
	}

	@Test
	void test2() {
		
		List<Emp> list = mapper.selectByCond(new EmpSearchCond());
		
		System.out.println(list.size());
		assertEquals(1, list.size());
	}

}
