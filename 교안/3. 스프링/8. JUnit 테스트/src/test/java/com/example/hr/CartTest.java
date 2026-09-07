package com.example.hr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CartTest {
    Cart cart;

    @BeforeEach void 새_장바구니() { cart = new Cart(); }

    @Test void 하나_담으면_1() {
        cart.add("book");
        assertThat(cart.totalCount()).isEqualTo(1);
    }

    @Test void 두번_담으면_2() {
        cart.add("book");
        cart.add("pen");
        assertThat(cart.totalCount()).isEqualTo(2);
    }
}
