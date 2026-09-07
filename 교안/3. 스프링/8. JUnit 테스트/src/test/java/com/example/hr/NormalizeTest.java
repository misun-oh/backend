package com.example.hr;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class NormalizeTest {

    static int normalizeSalary(Integer s) {
        return (s == null || s < 0) ? 0 : s;
    }

    @Test void null이면_0() {
        assertThat(normalizeSalary(null)).isZero();
    }
    @Test void 음수면_0() {
        assertThat(normalizeSalary(-1)).isZero();
    }
    @Test void 정상값은_그대로() {
        assertThat(normalizeSalary(2_500_000)).isEqualTo(2_500_000);
    }
}
