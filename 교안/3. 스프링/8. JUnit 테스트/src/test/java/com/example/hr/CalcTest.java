package com.example.hr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CalcTest {
    Calculator calc;

    @BeforeEach void setUp() { calc = new Calculator(); }   // 각 @Test 전마다 새로

    @Test void 더하기() { assertThat(calc.add(2, 3)).isEqualTo(5); }
    @Test void 빼기()   { assertThat(calc.sub(5, 2)).isEqualTo(3); }
}
