package com.example.hr.dto;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageResultTest {

    @Test
    void 전체_건수와_페이지크기로_전체_페이지수를_계산한다() {
        PageResult<String> page = new PageResult<>(List.of("a", "b"), 25, 1, 10);

        assertThat(page.getTotalPages()).isEqualTo(3);
    }

    @Test
    void 데이터가_없으면_전체_페이지수는_최소_1이다() {
        PageResult<String> page = new PageResult<>(List.of(), 0, 1, 10);

        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.isEmpty()).isTrue();
    }

    @Test
    void 첫_페이지는_이전이_없고_마지막_페이지는_다음이_없다() {
        PageResult<String> firstPage = new PageResult<>(List.of("a"), 25, 1, 10);
        PageResult<String> lastPage = new PageResult<>(List.of("a"), 25, 3, 10);

        assertThat(firstPage.isHasPrev()).isFalse();
        assertThat(firstPage.isHasNext()).isTrue();
        assertThat(lastPage.isHasPrev()).isTrue();
        assertThat(lastPage.isHasNext()).isFalse();
    }

    @Test
    void 페이지네이션_창은_5개_단위로_묶인다() {
        // 전체 100건, 페이지당 10건 -> 전체 10페이지
        PageResult<String> page6 = new PageResult<>(List.of(), 100, 6, 10);

        assertThat(page6.getStartPage()).isEqualTo(6);
        assertThat(page6.getEndPage()).isEqualTo(10);

        PageResult<String> page1 = new PageResult<>(List.of(), 100, 1, 10);
        assertThat(page1.getStartPage()).isEqualTo(1);
        assertThat(page1.getEndPage()).isEqualTo(5);
    }
}
