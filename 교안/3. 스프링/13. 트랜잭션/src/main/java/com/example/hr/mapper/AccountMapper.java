package com.example.hr.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Day 13 — 본문 1절 TransferService 에서 사용하는 매퍼(SQL 과정의 ACCOUNT 이체 예제에 대응).
 */
@Mapper
public interface AccountMapper {
    void minusBalance(@Param("id") Long id, @Param("amount") int amount);
    void plusBalance(@Param("id") Long id, @Param("amount") int amount);
}
