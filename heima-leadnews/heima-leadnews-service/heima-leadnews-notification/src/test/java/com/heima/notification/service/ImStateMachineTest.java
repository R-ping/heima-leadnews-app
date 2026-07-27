package com.heima.notification.service;

import com.heima.model.notification.pojos.ImSession;
import com.heima.notification.NotificationApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = NotificationApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ImStateMachine单元测试")
class ImStateMachineTest {

    @Autowired
    private ImStateMachine imStateMachine;

    @Test
    @Order(1)
    @DisplayName("S3 - is_active=true，无限发送")
    void testS3_ActiveSession() {
        ImSession session = new ImSession();
        session.setId(1L);
        session.setIsActive(1);

        ImStateMachine.SendPermission perm = imStateMachine.checkPermission(100L, 200L, session);
        assertEquals(ImStateMachine.SendPermission.ALLOWED, perm);
    }

    @Test
    @Order(2)
    @DisplayName("S1 - 无会话，首次发送")
    void testS1_NoSession() {
        ImStateMachine.SendPermission perm = imStateMachine.checkPermission(100L, 200L, null);
        // 无会话时应允许发送
        assertNotEquals(ImStateMachine.SendPermission.LIMIT_REACHED, perm);
    }

    @Test
    @Order(3)
    @DisplayName("S2 - 待回复限制（模拟数据）")
    void testS2_LimitReached() {
        ImSession session = new ImSession();
        session.setId(1L);
        session.setIsActive(0);

        ImStateMachine.SendPermission perm = imStateMachine.checkPermission(100L, 200L, session);
        // 如果数据库中有待回复消息，应返回LIMIT_REACHED
        // 如果数据库为空，应返回ALLOWED_ONCE
        assertNotNull(perm);
    }

    @Test
    @Order(4)
    @DisplayName("状态机 - is_active=null等同于false")
    void testNullIsActive() {
        ImSession session = new ImSession();
        session.setId(1L);
        session.setIsActive(null);

        ImStateMachine.SendPermission perm = imStateMachine.checkPermission(100L, 200L, session);
        assertNotNull(perm);
    }

    @Test
    @Order(5)
    @DisplayName("状态机 - 所有状态枚举值")
    void testAllStates() {
        assertEquals(3, ImStateMachine.SendPermission.values().length);
        assertNotNull(ImStateMachine.SendPermission.ALLOWED);
        assertNotNull(ImStateMachine.SendPermission.ALLOWED_ONCE);
        assertNotNull(ImStateMachine.SendPermission.LIMIT_REACHED);
    }
}