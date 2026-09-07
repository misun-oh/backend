package com.example.hr;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class EmpServiceUnitTest {
    @Mock EmpMapper empMapper;
    @InjectMocks EmpServiceImpl empService;

    @Test void 없는_사원을_get하면_예외() {
        given(empMapper.findById(999L)).willReturn(null);
        assertThatThrownBy(() -> empService.get(999L))
            .isInstanceOf(NoSuchElementException.class);
        then(empMapper).should().findById(999L);
    }
}
