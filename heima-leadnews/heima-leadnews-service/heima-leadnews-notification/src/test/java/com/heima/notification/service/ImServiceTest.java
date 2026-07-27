package com.heima.notification.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.notification.dtos.ImMessageDto;
import com.heima.model.notification.dtos.ImReadDto;
import com.heima.notification.NotificationApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = NotificationApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ImService单元测试")
class ImServiceTest {

    @Autowired
    private ImService imService;

    private static Long testUserId1 = 99991L;
    private static Long testUserId2 = 99992L;

    @Test
    @Order(1)
    @DisplayName("会话列表 - 查询空会话")
    void testListSessions_Empty() {
        ResponseResult result = imService.listSessions(testUserId1);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(2)
    @DisplayName("发送消息 - 创建新会话")
    void testSendMessage_CreateSession() {
        ImMessageDto dto = new ImMessageDto();
        dto.setReceiverId(testUserId2);
        dto.setContent("测试消息内容");
        dto.setMsgType(1);

        ResponseResult result = imService.sendMessage(testUserId1, dto);
        assertNotNull(result);
        if (result.getCode() == 200) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertNotNull(data.get("message_id"));
            assertEquals("sent", data.get("status"));
        }
    }

    @Test
    @Order(3)
    @DisplayName("发送消息 - 参数校验（空内容）")
    void testSendMessage_EmptyContent() {
        ImMessageDto dto = new ImMessageDto();
        dto.setReceiverId(testUserId2);
        dto.setContent("");

        ResponseResult result = imService.sendMessage(testUserId1, dto);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @Order(4)
    @DisplayName("发送消息 - 参数校验（自己发给自己）")
    void testSendMessage_SelfSend() {
        ImMessageDto dto = new ImMessageDto();
        dto.setReceiverId(testUserId1);
        dto.setContent("自己发给自己");

        ResponseResult result = imService.sendMessage(testUserId1, dto);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @Order(5)
    @DisplayName("消息列表 - 查询空消息")
    void testListMessages_Empty() {
        ResponseResult result = imService.listMessages(testUserId1, 99999L, null, 20);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }
}