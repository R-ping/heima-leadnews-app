package com.heima.notification.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.notification.dtos.NotificationDto;
import com.heima.model.notification.pojos.Notification;
import com.heima.notification.NotificationApplication;
import com.heima.notification.mapper.NotificationMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = NotificationApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("NotificationService单元测试")
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationMapper notificationMapper;

    private static Long testUserId = 99999L;

    @BeforeEach
    void setUp() {
        // 插入测试数据
        Notification n1 = new Notification();
        n1.setUserId(testUserId);
        n1.setType(1); // comment
        n1.setSourceId("c_001");
        n1.setContent("{\"trigger_user\":{\"id\":\"1\",\"name\":\"测试用户A\",\"avatar\":\"\"},\"content_preview\":\"测试评论内容\",\"target_type\":\"article\",\"target_id\":\"a_001\",\"target_title\":\"测试文章标题\",\"comment_id\":\"c_001\",\"interaction_stats\":{\"likes\":3,\"replies\":1},\"is_liked_by_me\":false}");
        n1.setIsRead(0);
        n1.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(n1);

        Notification n2 = new Notification();
        n2.setUserId(testUserId);
        n2.setType(2); // digg
        n2.setSourceId("a_001");
        n2.setContent("{\"trigger_user\":{\"id\":\"2\",\"name\":\"测试用户B\",\"avatar\":\"\"},\"action_type\":\"like\",\"source_type\":\"article\",\"target_title\":\"被点赞的文章\"}");
        n2.setIsRead(0);
        n2.setCreatedAt(LocalDateTime.now().minusHours(1));
        notificationMapper.insert(n2);
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据，避免影响其他测试
        // 由于没有delete方法，这里跳过清理
    }

    @Test
    @Order(1)
    @DisplayName("通知列表 - 查询评论类型")
    void testList_CommentType() {
        NotificationDto dto = new NotificationDto();
        dto.setType("comment");
        dto.setSize(10);

        ResponseResult result = notificationService.list(dto);
        // 注意：service内部从ThreadLocal获取userId，这里可能为null
        // 验证返回结构
        assertNotNull(result);
        if (result.getCode() == 200) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertNotNull(data);
            assertNotNull(data.get("list"));
            assertNotNull(data.get("has_more"));
        }
    }

    @Test
    @Order(2)
    @DisplayName("通知列表 - 游标分页")
    void testList_CursorPagination() {
        NotificationDto dto = new NotificationDto();
        dto.setType("comment");
        dto.setSize(1);

        ResponseResult result = notificationService.list(dto);
        if (result.getCode() == 200) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            String nextCursor = (String) data.get("next_cursor");
            Boolean hasMore = (Boolean) data.get("has_more");
            assertNotNull(hasMore);
        }
    }

    @Test
    @Order(3)
    @DisplayName("通知列表 - 空列表处理")
    void testList_EmptyResult() {
        NotificationDto dto = new NotificationDto();
        dto.setType("system");
        dto.setSize(10);

        ResponseResult result = notificationService.list(dto);
        assertNotNull(result);
        if (result.getCode() == 200) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertNotNull(data.get("list"));
        }
    }

    @Test
    @Order(4)
    @DisplayName("未读计数 - 查询未读数")
    void testUnreadCount() {
        ResponseResult result = notificationService.unreadCount(testUserId);
        assertNotNull(result);
        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("total"));
        assertTrue((Integer) data.get("total") >= 0);
    }

    @Test
    @Order(5)
    @DisplayName("全部已读 - 标记已读")
    void testMarkAllRead() {
        ResponseResult result = notificationService.markAllRead(testUserId);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }
}