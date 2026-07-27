package com.heima.notification.service.impl;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.notification.dtos.ImMessageDto;
import com.heima.model.notification.dtos.ImReadDto;
import com.heima.model.notification.pojos.ImMessage;
import com.heima.model.notification.pojos.ImSession;
import com.heima.notification.mapper.ImMessageMapper;
import com.heima.notification.mapper.ImSessionMapper;
import com.heima.notification.service.ImStateMachine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImServiceImpl 单元测试")
class ImServiceImplTest {

    @Mock
    private ImSessionMapper imSessionMapper;

    @Mock
    private ImMessageMapper imMessageMapper;

    @Mock
    private ImStateMachine imStateMachine;

    @InjectMocks
    private ImServiceImpl imService;

    private static final Long USER_ID = 1001L;
    private static final Long PEER_ID = 2002L;
    private static final Long SESSION_ID = 10L;

    // ==================== listSessions ====================

    @Nested
    @DisplayName("listSessions 方法测试")
    class ListSessionsTests {

        @Test
        @DisplayName("正常流程 - 返回会话列表")
        void shouldReturnSessionsWhenExists() {
            // given
            ImSession session = buildSession(SESSION_ID, USER_ID, PEER_ID, "最后一条消息", 3);
            when(imSessionMapper.selectByUserId(USER_ID)).thenReturn(Arrays.asList(session));

            // when
            ResponseResult result = imService.listSessions(USER_ID);

            // then
            assertNotNull(result);
            assertEquals(200, result.getCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertNotNull(data);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
            assertNotNull(list);
            assertEquals(1, list.size());
            Map<String, Object> item = list.get(0);
            assertEquals(SESSION_ID, item.get("session_id"));
            assertEquals(PEER_ID, item.get("peer_id"));
            assertEquals("最后一条消息", item.get("last_message"));
            assertEquals(3, item.get("unread_count"));
            assertEquals(true, item.get("is_active"));
        }

        @Test
        @DisplayName("正常流程 - 多会话列表，peer_id 正确指向对方")
        void shouldReturnCorrectPeerId() {
            // given: user1Id 匹配 userId，peer 应为 user2Id
            ImSession s1 = buildSession(1L, USER_ID, 2001L, "msg1", 0);
            // user2Id 匹配 userId，peer 应为 user1Id
            ImSession s2 = buildSession(2L, 3001L, USER_ID, "msg2", 5);
            when(imSessionMapper.selectByUserId(USER_ID)).thenReturn(Arrays.asList(s1, s2));

            // when
            ResponseResult result = imService.listSessions(USER_ID);

            // then
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
            assertEquals(2, list.size());
            assertEquals(2001L, list.get(0).get("peer_id"));
            assertEquals(3001L, list.get(1).get("peer_id"));
        }

        @Test
        @DisplayName("空列表 - 无会话时返回空列表")
        void shouldReturnEmptyListWhenNoSessions() {
            when(imSessionMapper.selectByUserId(USER_ID)).thenReturn(Collections.emptyList());

            ResponseResult result = imService.listSessions(USER_ID);

            assertEquals(200, result.getCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
            assertTrue(list.isEmpty());
        }

        @Test
        @DisplayName("user 为 null - mapper 返回空列表")
        void shouldReturnEmptyListWhenUserIdIsNull() {
            when(imSessionMapper.selectByUserId(null)).thenReturn(Collections.emptyList());

            ResponseResult result = imService.listSessions(null);

            assertEquals(200, result.getCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
            assertTrue(list.isEmpty());
        }

        @Test
        @DisplayName("lastMessageAt 为 null - 映射为 null")
        void shouldMapNullLastMessageAt() {
            ImSession session = buildSession(SESSION_ID, USER_ID, PEER_ID, "msg", 0);
            session.setLastMessageAt(null);
            when(imSessionMapper.selectByUserId(USER_ID)).thenReturn(Arrays.asList(session));

            ResponseResult result = imService.listSessions(USER_ID);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
            assertNull(list.get(0).get("last_message_at"));
        }

        @Test
        @DisplayName("isActive 为 0 - 映射为 false")
        void shouldMapIsActiveFalse() {
            ImSession session = buildSession(SESSION_ID, USER_ID, PEER_ID, "msg", 0);
            session.setIsActive(0);
            when(imSessionMapper.selectByUserId(USER_ID)).thenReturn(Arrays.asList(session));

            ResponseResult result = imService.listSessions(USER_ID);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
            assertEquals(false, list.get(0).get("is_active"));
        }
    }

    // ==================== listMessages ====================

    @Nested
    @DisplayName("listMessages 方法测试")
    class ListMessagesTests {

        @Test
        @DisplayName("正常流程 - 返回消息列表，含 hasMore 和 nextCursor")
        void shouldReturnMessagesWithHasMoreTrue() {
            // given: 返回的消息数量等于 limit，hasMore = true
            List<ImMessage> messages = buildMessages(3, SESSION_ID, USER_ID);
            when(imMessageMapper.selectBySessionId(SESSION_ID, null, 20)).thenReturn(messages);

            // when
            ResponseResult result = imService.listMessages(USER_ID, SESSION_ID, null, 20);

            // then
            assertEquals(200, result.getCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
            assertEquals(3, list.size());
            assertEquals(true, data.get("has_more"));
            // nextCursor 应为 reverse 后第一条消息的 id
            assertEquals(messages.get(0).getId(), data.get("next_cursor"));
        }

        @Test
        @DisplayName("空列表 - 无消息时返回空列表，hasMore=false")
        void shouldReturnEmptyListWhenNoMessages() {
            when(imMessageMapper.selectBySessionId(SESSION_ID, null, 20)).thenReturn(Collections.emptyList());

            ResponseResult result = imService.listMessages(USER_ID, SESSION_ID, null, 20);

            assertEquals(200, result.getCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
            assertTrue(list.isEmpty());
            assertEquals(false, data.get("has_more"));
            assertNull(data.get("next_cursor"));
        }

        @Test
        @DisplayName("游标分页 - hasMore=false 时 nextCursor 为 null")
        void shouldReturnHasMoreFalseWhenFewerMessages() {
            // given: 返回的消息数量小于 limit
            List<ImMessage> messages = buildMessages(2, SESSION_ID, USER_ID);
            when(imMessageMapper.selectBySessionId(SESSION_ID, 50L, 20)).thenReturn(messages);

            ResponseResult result = imService.listMessages(USER_ID, SESSION_ID, 50L, 20);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertEquals(false, data.get("has_more"));
            assertNull(data.get("next_cursor"));
        }

        @Test
        @DisplayName("size 为 null - 默认取 20")
        void shouldDefaultSizeTo20WhenNull() {
            when(imMessageMapper.selectBySessionId(SESSION_ID, null, 20)).thenReturn(Collections.emptyList());

            imService.listMessages(USER_ID, SESSION_ID, null, null);

            verify(imMessageMapper).selectBySessionId(SESSION_ID, null, 20);
        }

        @Test
        @DisplayName("size 为 0 - 默认取 20")
        void shouldDefaultSizeTo20WhenZero() {
            when(imMessageMapper.selectBySessionId(SESSION_ID, null, 20)).thenReturn(Collections.emptyList());

            imService.listMessages(USER_ID, SESSION_ID, null, 0);

            verify(imMessageMapper).selectBySessionId(SESSION_ID, null, 20);
        }

        @Test
        @DisplayName("size 为负数 - 默认取 20")
        void shouldDefaultSizeTo20WhenNegative() {
            when(imMessageMapper.selectBySessionId(SESSION_ID, null, 20)).thenReturn(Collections.emptyList());

            imService.listMessages(USER_ID, SESSION_ID, null, -5);

            verify(imMessageMapper).selectBySessionId(SESSION_ID, null, 20);
        }

        @Test
        @DisplayName("size 超过 50 - 上限截断为 50")
        void shouldCapSizeTo50WhenLarge() {
            when(imMessageMapper.selectBySessionId(SESSION_ID, null, 50)).thenReturn(Collections.emptyList());

            imService.listMessages(USER_ID, SESSION_ID, null, 100);

            verify(imMessageMapper).selectBySessionId(SESSION_ID, null, 50);
        }

        @Test
        @DisplayName("size 恰好为 50 - 不截断")
        void shouldNotCapSizeWhenExactly50() {
            when(imMessageMapper.selectBySessionId(SESSION_ID, null, 50)).thenReturn(Collections.emptyList());

            imService.listMessages(USER_ID, SESSION_ID, null, 50);

            verify(imMessageMapper).selectBySessionId(SESSION_ID, null, 50);
        }

        @Test
        @DisplayName("is_self 标记 - 自己的消息 vs 对方的消息")
        void shouldMarkIsSelfCorrectly() {
            ImMessage myMsg = createMessage(1L, SESSION_ID, USER_ID, PEER_ID, "我的消息");
            ImMessage peerMsg = createMessage(2L, SESSION_ID, PEER_ID, USER_ID, "对方消息");
            List<ImMessage> messages = Arrays.asList(myMsg, peerMsg);
            when(imMessageMapper.selectBySessionId(SESSION_ID, null, 20)).thenReturn(messages);

            ResponseResult result = imService.listMessages(USER_ID, SESSION_ID, null, 20);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
            assertEquals(true, list.get(0).get("is_self"));
            assertEquals(false, list.get(1).get("is_self"));
        }
    }

    // ==================== sendMessage ====================

    @Nested
    @DisplayName("sendMessage 方法测试")
    class SendMessageTests {

        @Test
        @DisplayName("正常流程 - 发送消息成功，返回 message_id")
        void shouldSendMessageSuccessfully() {
            // given
            ImMessageDto dto = buildDto(PEER_ID, "Hello World", 1);
            ImSession session = buildSession(SESSION_ID, USER_ID, PEER_ID, null, 0);
            when(imSessionMapper.selectBySessionKey("1001_2002")).thenReturn(session);
            when(imStateMachine.checkPermission(eq(USER_ID), eq(PEER_ID), any(ImSession.class)))
                    .thenReturn(ImStateMachine.SendPermission.ALLOWED_ONCE);
            when(imMessageMapper.insert(any(ImMessage.class))).thenReturn(1);
            when(imSessionMapper.updateById(any(ImSession.class))).thenReturn(1);

            // when
            ResponseResult result = imService.sendMessage(USER_ID, dto);

            // then
            assertEquals(200, result.getCode());
            assertNotNull(result.getData());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertEquals("sent", data.get("status"));

            // 验证消息插入
            ArgumentCaptor<ImMessage> msgCaptor = ArgumentCaptor.forClass(ImMessage.class);
            verify(imMessageMapper).insert(msgCaptor.capture());
            ImMessage inserted = msgCaptor.getValue();
            assertEquals(SESSION_ID, inserted.getSessionId());
            assertEquals(USER_ID, inserted.getSenderId());
            assertEquals(PEER_ID, inserted.getReceiverId());
            assertEquals("Hello World", inserted.getContent());
            assertEquals(1, inserted.getMsgType());
            assertEquals(0, inserted.getStatus());

            // 验证会话更新
            verify(imSessionMapper).updateById(any(ImSession.class));
        }

        @Test
        @DisplayName("receiverId 为 null - 返回 PARAM_INVALID")
        void shouldReturnParamInvalidWhenReceiverIdIsNull() {
            ImMessageDto dto = new ImMessageDto();
            dto.setReceiverId(null);
            dto.setContent("Hello");

            ResponseResult result = imService.sendMessage(USER_ID, dto);

            assertNotEquals(200, result.getCode());
            verifyNoInteractions(imSessionMapper);
            verifyNoInteractions(imMessageMapper);
        }

        @Test
        @DisplayName("receiverId 等于 senderId - 返回 PARAM_INVALID")
        void shouldReturnParamInvalidWhenSameSenderId() {
            ImMessageDto dto = buildDto(USER_ID, "Hello", 1);

            ResponseResult result = imService.sendMessage(USER_ID, dto);

            assertNotEquals(200, result.getCode());
            verifyNoInteractions(imSessionMapper);
            verifyNoInteractions(imMessageMapper);
        }

        @Test
        @DisplayName("content 为 null - 返回 PARAM_INVALID")
        void shouldReturnParamInvalidWhenContentIsNull() {
            ImMessageDto dto = new ImMessageDto();
            dto.setReceiverId(PEER_ID);
            dto.setContent(null);

            ResponseResult result = imService.sendMessage(USER_ID, dto);

            assertNotEquals(200, result.getCode());
            verifyNoInteractions(imSessionMapper);
            verifyNoInteractions(imMessageMapper);
        }

        @Test
        @DisplayName("content 为空字符串 - 返回 PARAM_INVALID")
        void shouldReturnParamInvalidWhenContentIsEmpty() {
            ImMessageDto dto = buildDto(PEER_ID, "", 1);

            ResponseResult result = imService.sendMessage(USER_ID, dto);

            assertNotEquals(200, result.getCode());
            verifyNoInteractions(imSessionMapper);
            verifyNoInteractions(imMessageMapper);
        }

        @Test
        @DisplayName("content 仅含空白字符 - 返回 PARAM_INVALID")
        void shouldReturnParamInvalidWhenContentIsBlank() {
            ImMessageDto dto = buildDto(PEER_ID, "   ", 1);

            ResponseResult result = imService.sendMessage(USER_ID, dto);

            assertNotEquals(200, result.getCode());
            verifyNoInteractions(imSessionMapper);
            verifyNoInteractions(imMessageMapper);
        }

        @Test
        @DisplayName("新会话创建 - session 不存在时自动创建并插入")
        void shouldCreateNewSessionWhenNotExists() {
            // given
            ImMessageDto dto = buildDto(PEER_ID, "Hello", 1);
            when(imSessionMapper.selectBySessionKey("1001_2002")).thenReturn(null);
            when(imStateMachine.checkPermission(eq(USER_ID), eq(PEER_ID), any(ImSession.class)))
                    .thenReturn(ImStateMachine.SendPermission.ALLOWED_ONCE);
            when(imSessionMapper.insert(any(ImSession.class))).thenReturn(1);
            when(imMessageMapper.insert(any(ImMessage.class))).thenReturn(1);
            when(imSessionMapper.updateById(any(ImSession.class))).thenReturn(1);

            // when
            ResponseResult result = imService.sendMessage(USER_ID, dto);

            // then
            assertEquals(200, result.getCode());

            // 验证新会话被插入
            ArgumentCaptor<ImSession> sessionCaptor = ArgumentCaptor.forClass(ImSession.class);
            verify(imSessionMapper).insert(sessionCaptor.capture());
            ImSession newSession = sessionCaptor.getValue();
            assertEquals("1001_2002", newSession.getSessionKey());
            assertEquals(USER_ID, newSession.getUser1Id());
            assertEquals(PEER_ID, newSession.getUser2Id());
            assertEquals(0, newSession.getIsActive());
            assertEquals(0, newSession.getUser1UnreadCount());
            assertEquals(0, newSession.getUser2UnreadCount());
            assertNotNull(newSession.getCreatedAt());
        }

        @Test
        @DisplayName("LIMIT_REACHED - 权限检查返回 LIMIT_REACHED 时拒绝发送")
        void shouldReturnErrorWhenLimitReached() {
            // given
            ImMessageDto dto = buildDto(PEER_ID, "Hello", 1);
            ImSession session = buildSession(SESSION_ID, USER_ID, PEER_ID, null, 0);
            when(imSessionMapper.selectBySessionKey("1001_2002")).thenReturn(session);
            when(imStateMachine.checkPermission(eq(USER_ID), eq(PEER_ID), any(ImSession.class)))
                    .thenReturn(ImStateMachine.SendPermission.LIMIT_REACHED);

            // when
            ResponseResult result = imService.sendMessage(USER_ID, dto);

            // then
            assertEquals(403, result.getCode());
            verify(imMessageMapper, never()).insert(any(ImMessage.class));
        }

        @Test
        @DisplayName("长内容截断 - content 超过 50 字符时 lastMessage 截断为 50 + '...'")
        void shouldTruncateLongContentInLastMessage() {
            // given
            String longContent = "A".repeat(60); // 60 characters
            ImMessageDto dto = buildDto(PEER_ID, longContent, 1);
            ImSession session = buildSession(SESSION_ID, USER_ID, PEER_ID, null, 0);
            when(imSessionMapper.selectBySessionKey("1001_2002")).thenReturn(session);
            when(imStateMachine.checkPermission(eq(USER_ID), eq(PEER_ID), any(ImSession.class)))
                    .thenReturn(ImStateMachine.SendPermission.ALLOWED_ONCE);
            when(imMessageMapper.insert(any(ImMessage.class))).thenReturn(1);
            when(imSessionMapper.updateById(any(ImSession.class))).thenReturn(1);

            // when
            imService.sendMessage(USER_ID, dto);

            // then
            ArgumentCaptor<ImSession> sessionCaptor = ArgumentCaptor.forClass(ImSession.class);
            verify(imSessionMapper).updateById(sessionCaptor.capture());
            String lastMessage = sessionCaptor.getValue().getLastMessage();
            assertEquals(50 + 3, lastMessage.length());
            assertTrue(lastMessage.endsWith("..."));
            assertTrue(lastMessage.startsWith("A"));
        }

        @Test
        @DisplayName("短内容不截断 - content 不超过 50 字符时完整保存")
        void shouldNotTruncateShortContent() {
            // given
            String shortContent = "Hello World"; // 11 characters
            ImMessageDto dto = buildDto(PEER_ID, shortContent, 1);
            ImSession session = buildSession(SESSION_ID, USER_ID, PEER_ID, null, 0);
            when(imSessionMapper.selectBySessionKey("1001_2002")).thenReturn(session);
            when(imStateMachine.checkPermission(eq(USER_ID), eq(PEER_ID), any(ImSession.class)))
                    .thenReturn(ImStateMachine.SendPermission.ALLOWED_ONCE);
            when(imMessageMapper.insert(any(ImMessage.class))).thenReturn(1);
            when(imSessionMapper.updateById(any(ImSession.class))).thenReturn(1);

            // when
            imService.sendMessage(USER_ID, dto);

            // then
            ArgumentCaptor<ImSession> sessionCaptor = ArgumentCaptor.forClass(ImSession.class);
            verify(imSessionMapper).updateById(sessionCaptor.capture());
            assertEquals("Hello World", sessionCaptor.getValue().getLastMessage());
        }

        @Test
        @DisplayName("更新未读计数 - receiver 为 user2 时增加 user2UnreadCount")
        void shouldIncrementUser2UnreadCountWhenReceiverIsUser2() {
            // given: user1=USER_ID, user2=PEER_ID, receiver=PEER_ID → user2UnreadCount++
            ImMessageDto dto = buildDto(PEER_ID, "Hello", 1);
            ImSession session = buildSession(SESSION_ID, USER_ID, PEER_ID, null, 0);
            session.setUser2UnreadCount(5);
            when(imSessionMapper.selectBySessionKey("1001_2002")).thenReturn(session);
            when(imStateMachine.checkPermission(eq(USER_ID), eq(PEER_ID), any(ImSession.class)))
                    .thenReturn(ImStateMachine.SendPermission.ALLOWED_ONCE);
            when(imMessageMapper.insert(any(ImMessage.class))).thenReturn(1);
            when(imSessionMapper.updateById(any(ImSession.class))).thenReturn(1);

            // when
            imService.sendMessage(USER_ID, dto);

            // then
            ArgumentCaptor<ImSession> sessionCaptor = ArgumentCaptor.forClass(ImSession.class);
            verify(imSessionMapper).updateById(sessionCaptor.capture());
            assertEquals(6, sessionCaptor.getValue().getUser2UnreadCount());
        }
    }

    // ==================== markRead ====================

    @Nested
    @DisplayName("markRead 方法测试")
    class MarkReadTests {

        @Test
        @DisplayName("正常流程 - 标记已读成功")
        void shouldMarkReadSuccessfully() {
            // given
            ImReadDto dto = new ImReadDto();
            dto.setSessionId(SESSION_ID);
            dto.setLastReadId(100L);
            ImSession session = buildSession(SESSION_ID, USER_ID, PEER_ID, "msg", 3);
            when(imMessageMapper.markRead(SESSION_ID, 100L, USER_ID)).thenReturn(1);
            when(imSessionMapper.selectById(SESSION_ID)).thenReturn(session);
            when(imSessionMapper.updateById(any(ImSession.class))).thenReturn(1);

            // when
            ResponseResult result = imService.markRead(USER_ID, dto);

            // then
            assertEquals(200, result.getCode());
            verify(imMessageMapper).markRead(SESSION_ID, 100L, USER_ID);
            verify(imSessionMapper).updateById(any(ImSession.class));
        }

        @Test
        @DisplayName("sessionId 为 null - 返回 PARAM_INVALID")
        void shouldReturnParamInvalidWhenSessionIdIsNull() {
            ImReadDto dto = new ImReadDto();
            dto.setSessionId(null);
            dto.setLastReadId(100L);

            ResponseResult result = imService.markRead(USER_ID, dto);

            assertNotEquals(200, result.getCode());
            verifyNoInteractions(imMessageMapper);
            verifyNoInteractions(imSessionMapper);
        }

        @Test
        @DisplayName("lastReadId 为 null - 默认使用 Long.MAX_VALUE")
        void shouldUseMaxValueWhenLastReadIdIsNull() {
            // given
            ImReadDto dto = new ImReadDto();
            dto.setSessionId(SESSION_ID);
            dto.setLastReadId(null);
            ImSession session = buildSession(SESSION_ID, USER_ID, PEER_ID, "msg", 3);
            when(imMessageMapper.markRead(SESSION_ID, Long.MAX_VALUE, USER_ID)).thenReturn(1);
            when(imSessionMapper.selectById(SESSION_ID)).thenReturn(session);
            when(imSessionMapper.updateById(any(ImSession.class))).thenReturn(1);

            // when
            ResponseResult result = imService.markRead(USER_ID, dto);

            // then
            assertEquals(200, result.getCode());
            verify(imMessageMapper).markRead(SESSION_ID, Long.MAX_VALUE, USER_ID);
        }

        @Test
        @DisplayName("session 不存在 - 不更新会话，正常返回")
        void shouldNotUpdateSessionWhenNotFound() {
            // given
            ImReadDto dto = new ImReadDto();
            dto.setSessionId(SESSION_ID);
            dto.setLastReadId(100L);
            when(imMessageMapper.markRead(SESSION_ID, 100L, USER_ID)).thenReturn(1);
            when(imSessionMapper.selectById(SESSION_ID)).thenReturn(null);

            // when
            ResponseResult result = imService.markRead(USER_ID, dto);

            // then
            assertEquals(200, result.getCode());
            verify(imMessageMapper).markRead(SESSION_ID, 100L, USER_ID);
            verify(imSessionMapper, never()).updateById(any(ImSession.class));
        }

        @Test
        @DisplayName("isActive 为 0 - 激活为 1")
        void shouldActivateSessionWhenIsActiveIsZero() {
            // given
            ImReadDto dto = new ImReadDto();
            dto.setSessionId(SESSION_ID);
            dto.setLastReadId(100L);
            ImSession session = buildSession(SESSION_ID, USER_ID, PEER_ID, "msg", 5);
            session.setIsActive(0);
            when(imMessageMapper.markRead(SESSION_ID, 100L, USER_ID)).thenReturn(1);
            when(imSessionMapper.selectById(SESSION_ID)).thenReturn(session);
            when(imSessionMapper.updateById(any(ImSession.class))).thenReturn(1);

            // when
            imService.markRead(USER_ID, dto);

            // then
            ArgumentCaptor<ImSession> sessionCaptor = ArgumentCaptor.forClass(ImSession.class);
            verify(imSessionMapper).updateById(sessionCaptor.capture());
            assertEquals(1, sessionCaptor.getValue().getIsActive());
        }

        @Test
        @DisplayName("isActive 为 null - 激活为 1")
        void shouldActivateSessionWhenIsActiveIsNull() {
            // given
            ImReadDto dto = new ImReadDto();
            dto.setSessionId(SESSION_ID);
            dto.setLastReadId(100L);
            ImSession session = buildSession(SESSION_ID, USER_ID, PEER_ID, "msg", 5);
            session.setIsActive(null);
            when(imMessageMapper.markRead(SESSION_ID, 100L, USER_ID)).thenReturn(1);
            when(imSessionMapper.selectById(SESSION_ID)).thenReturn(session);
            when(imSessionMapper.updateById(any(ImSession.class))).thenReturn(1);

            // when
            imService.markRead(USER_ID, dto);

            // then
            ArgumentCaptor<ImSession> sessionCaptor = ArgumentCaptor.forClass(ImSession.class);
            verify(imSessionMapper).updateById(sessionCaptor.capture());
            assertEquals(1, sessionCaptor.getValue().getIsActive());
        }

        @Test
        @DisplayName("isActive 已为 1 - 保持为 1")
        void shouldKeepActiveWhenAlreadyActive() {
            // given
            ImReadDto dto = new ImReadDto();
            dto.setSessionId(SESSION_ID);
            dto.setLastReadId(100L);
            ImSession session = buildSession(SESSION_ID, USER_ID, PEER_ID, "msg", 5);
            session.setIsActive(1);
            when(imMessageMapper.markRead(SESSION_ID, 100L, USER_ID)).thenReturn(1);
            when(imSessionMapper.selectById(SESSION_ID)).thenReturn(session);
            when(imSessionMapper.updateById(any(ImSession.class))).thenReturn(1);

            // when
            imService.markRead(USER_ID, dto);

            // then
            ArgumentCaptor<ImSession> sessionCaptor = ArgumentCaptor.forClass(ImSession.class);
            verify(imSessionMapper).updateById(sessionCaptor.capture());
            assertEquals(1, sessionCaptor.getValue().getIsActive());
        }

        @Test
        @DisplayName("清除未读计数 - user1 匹配时清零 user1UnreadCount")
        void shouldClearUser1UnreadCount() {
            // given
            ImReadDto dto = new ImReadDto();
            dto.setSessionId(SESSION_ID);
            dto.setLastReadId(100L);
            ImSession session = buildSession(SESSION_ID, USER_ID, PEER_ID, "msg", 5);
            when(imMessageMapper.markRead(SESSION_ID, 100L, USER_ID)).thenReturn(1);
            when(imSessionMapper.selectById(SESSION_ID)).thenReturn(session);
            when(imSessionMapper.updateById(any(ImSession.class))).thenReturn(1);

            // when
            imService.markRead(USER_ID, dto);

            // then
            ArgumentCaptor<ImSession> sessionCaptor = ArgumentCaptor.forClass(ImSession.class);
            verify(imSessionMapper).updateById(sessionCaptor.capture());
            assertEquals(0, sessionCaptor.getValue().getUser1UnreadCount());
        }
    }

    // ==================== helpers ====================

    private ImSession buildSession(Long id, Long user1Id, Long user2Id, String lastMessage, int unreadCount) {
        ImSession session = new ImSession();
        session.setId(id);
        session.setSessionKey(Math.min(user1Id, user2Id) + "_" + Math.max(user1Id, user2Id));
        session.setUser1Id(user1Id);
        session.setUser2Id(user2Id);
        session.setLastMessage(lastMessage);
        session.setLastMessageAt(LocalDateTime.now());
        session.setUser1UnreadCount(unreadCount);
        session.setUser2UnreadCount(0);
        session.setIsActive(1);
        session.setCreatedAt(LocalDateTime.now());
        return session;
    }

    private ImMessage createMessage(Long id, Long sessionId, Long senderId, Long receiverId, String content) {
        ImMessage message = new ImMessage();
        message.setId(id);
        message.setSessionId(sessionId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setMsgType(1);
        message.setStatus(0);
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }

    private List<ImMessage> buildMessages(int count, Long sessionId, Long userId) {
        List<ImMessage> messages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            messages.add(createMessage((long) (100 + i), sessionId, userId, PEER_ID, "Message " + i));
        }
        return messages;
    }

    private ImMessageDto buildDto(Long receiverId, String content, Integer msgType) {
        ImMessageDto dto = new ImMessageDto();
        dto.setReceiverId(receiverId);
        dto.setContent(content);
        dto.setMsgType(msgType);
        return dto;
    }
}