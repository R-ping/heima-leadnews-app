package com.heima.course.config;

import com.heima.course.interceptor.AppTokenInterceptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseWebMvcConfigTest {

    @Mock
    private InterceptorRegistry registry;

    @Mock
    private InterceptorRegistration registration;

    @Test
    void testAddInterceptors() {
        when(registry.addInterceptor(any(AppTokenInterceptor.class))).thenReturn(registration);
        when(registration.addPathPatterns(anyString())).thenReturn(registration);

        CourseWebMvcConfig config = new CourseWebMvcConfig();
        config.addInterceptors(registry);

        verify(registry).addInterceptor(any(AppTokenInterceptor.class));
        verify(registration).addPathPatterns("/**");
    }
}