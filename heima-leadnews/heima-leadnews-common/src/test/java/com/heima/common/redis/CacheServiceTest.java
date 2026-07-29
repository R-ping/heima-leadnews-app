package com.heima.common.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CacheService 单元测试")
class CacheServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private HashOperations<String, Object, Object> hashOperations;
    @Mock
    private ListOperations<String, String> listOperations;
    @Mock
    private SetOperations<String, String> setOperations;
    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
        when(stringRedisTemplate.opsForList()).thenReturn(listOperations);
        when(stringRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    // ==================== Key 操作 ====================

    @Nested
    @DisplayName("Key相关操作测试")
    class KeyOperationsTests {

        @Test
        @DisplayName("删除单个key")
        void shouldDeleteKey() {
            cacheService.delete("test-key");
            verify(stringRedisTemplate).delete("test-key");
        }

        @Test
        @DisplayName("批量删除key")
        void shouldDeleteKeys() {
            List<String> keys = Arrays.asList("key1", "key2", "key3");
            cacheService.delete(keys);
            verify(stringRedisTemplate).delete(keys);
        }

        @Test
        @DisplayName("检查key是否存在")
        void shouldCheckKeyExists() {
            when(stringRedisTemplate.hasKey("my-key")).thenReturn(true);
            assertTrue(cacheService.exists("my-key"));

            when(stringRedisTemplate.hasKey("missing-key")).thenReturn(false);
            assertFalse(cacheService.exists("missing-key"));
        }

        @Test
        @DisplayName("设置过期时间")
        void shouldSetExpire() {
            when(stringRedisTemplate.expire("key", 60, TimeUnit.SECONDS)).thenReturn(true);
            assertTrue(cacheService.expire("key", 60, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("获取过期时间")
        void shouldGetExpire() {
            when(stringRedisTemplate.getExpire("key", TimeUnit.SECONDS)).thenReturn(120L);
            assertEquals(120L, cacheService.getExpire("key", TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("获取key类型")
        void shouldGetKeyType() {
            when(stringRedisTemplate.type("key")).thenReturn(DataType.STRING);
            assertEquals(DataType.STRING, cacheService.type("key"));
        }

        @Test
        @DisplayName("获取随机key")
        void shouldGetRandomKey() {
            when(stringRedisTemplate.randomKey()).thenReturn("random-key");
            assertEquals("random-key", cacheService.randomKey());
        }

        @Test
        @DisplayName("修改key名称")
        void shouldRenameKey() {
            cacheService.rename("old", "new");
            verify(stringRedisTemplate).rename("old", "new");
        }
    }

    // ==================== String 操作 ====================

    @Nested
    @DisplayName("String相关操作测试")
    class StringOperationsTests {

        @Test
        @DisplayName("设置和获取值")
        void shouldSetAndGet() {
            cacheService.set("key", "value");
            verify(valueOperations).set("key", "value");

            when(valueOperations.get("key")).thenReturn("value");
            assertEquals("value", cacheService.get("key"));
        }

        @Test
        @DisplayName("带过期时间的设置")
        void shouldSetWithExpiration() {
            cacheService.setEx("key", "value", 10, TimeUnit.MINUTES);
            verify(valueOperations).set("key", "value", 10, TimeUnit.MINUTES);
        }

        @Test
        @DisplayName("不存在时设置")
        void shouldSetIfAbsent() {
            when(valueOperations.setIfAbsent("key", "value")).thenReturn(true);
            assertTrue(cacheService.setIfAbsent("key", "value"));

            when(valueOperations.setIfAbsent("key", "value2")).thenReturn(false);
            assertFalse(cacheService.setIfAbsent("key", "value2"));
        }

        @Test
        @DisplayName("自增")
        void shouldIncrement() {
            when(valueOperations.increment("counter", 1L)).thenReturn(6L);
            assertEquals(6L, cacheService.incrBy("counter", 1L));
        }

        @Test
        @DisplayName("批量获取")
        void shouldMultiGet() {
            List<String> keys = Arrays.asList("k1", "k2");
            List<String> values = Arrays.asList("v1", "v2");
            when(valueOperations.multiGet(keys)).thenReturn(values);
            assertEquals(values, cacheService.multiGet(keys));
        }
    }

    // ==================== Hash 操作 ====================

    @Nested
    @DisplayName("Hash相关操作测试")
    class HashOperationsTests {

        @Test
        @DisplayName("获取hash字段值")
        void shouldHGet() {
            when(hashOperations.get("hash-key", "field")).thenReturn("value");
            assertEquals("value", cacheService.hGet("hash-key", "field"));
        }

        @Test
        @DisplayName("获取所有hash字段")
        void shouldHGetAll() {
            Map<Object, Object> map = new HashMap<>();
            map.put("f1", "v1");
            when(hashOperations.entries("hash-key")).thenReturn(map);
            assertEquals(map, cacheService.hGetAll("hash-key"));
        }

        @Test
        @DisplayName("设置hash字段")
        void shouldHPut() {
            cacheService.hPut("hash-key", "field", "value");
            verify(hashOperations).put("hash-key", "field", "value");
        }

        @Test
        @DisplayName("hash字段不存在时设置")
        void shouldHPutIfAbsent() {
            when(hashOperations.putIfAbsent("hash-key", "field", "value")).thenReturn(true);
            assertTrue(cacheService.hPutIfAbsent("hash-key", "field", "value"));
        }

        @Test
        @DisplayName("检查hash字段是否存在")
        void shouldHExists() {
            when(hashOperations.hasKey("hash-key", "field")).thenReturn(true);
            assertTrue(cacheService.hExists("hash-key", "field"));
        }

        @Test
        @DisplayName("获取hash大小")
        void shouldHSize() {
            when(hashOperations.size("hash-key")).thenReturn(3L);
            assertEquals(3L, cacheService.hSize("hash-key"));
        }
    }

    // ==================== List 操作 ====================

    @Nested
    @DisplayName("List相关操作测试")
    class ListOperationsTests {

        @Test
        @DisplayName("左侧入队")
        void shouldLeftPush() {
            when(listOperations.leftPush("list", "value")).thenReturn(1L);
            assertEquals(1L, cacheService.lLeftPush("list", "value"));
        }

        @Test
        @DisplayName("左侧出队")
        void shouldLeftPop() {
            when(listOperations.leftPop("list")).thenReturn("value");
            assertEquals("value", cacheService.lLeftPop("list"));
        }

        @Test
        @DisplayName("右侧入队")
        void shouldRightPush() {
            when(listOperations.rightPush("list", "value")).thenReturn(1L);
            assertEquals(1L, cacheService.lRightPush("list", "value"));
        }

        @Test
        @DisplayName("获取范围")
        void shouldLRange() {
            List<String> values = Arrays.asList("a", "b", "c");
            when(listOperations.range("list", 0, -1)).thenReturn(values);
            assertEquals(values, cacheService.lRange("list", 0, -1));
        }

        @Test
        @DisplayName("获取列表长度")
        void shouldLLen() {
            when(listOperations.size("list")).thenReturn(3L);
            assertEquals(3L, cacheService.lLen("list"));
        }
    }

    // ==================== Set 操作 ====================

    @Nested
    @DisplayName("Set相关操作测试")
    class SetOperationsTests {

        @Test
        @DisplayName("添加元素")
        void shouldSAdd() {
            when(setOperations.add("set", "a", "b")).thenReturn(2L);
            assertEquals(2L, cacheService.sAdd("set", "a", "b"));
        }

        @Test
        @DisplayName("判断成员")
        void shouldSIsMember() {
            when(setOperations.isMember("set", "a")).thenReturn(true);
            assertTrue(cacheService.sIsMember("set", "a"));
        }

        @Test
        @DisplayName("获取集合大小")
        void shouldSSize() {
            when(setOperations.size("set")).thenReturn(5L);
            assertEquals(5L, cacheService.sSize("set"));
        }

        @Test
        @DisplayName("获取所有成员")
        void shouldSetMembers() {
            Set<String> members = new HashSet<>(Arrays.asList("a", "b", "c"));
            when(setOperations.members("set")).thenReturn(members);
            assertEquals(members, cacheService.setMembers("set"));
        }
    }

    // ==================== ZSet 操作 ====================

    @Nested
    @DisplayName("ZSet相关操作测试")
    class ZSetOperationsTests {

        @Test
        @DisplayName("添加元素")
        void shouldZAdd() {
            when(zSetOperations.add("zset", "value", 1.0)).thenReturn(true);
            assertTrue(cacheService.zAdd("zset", "value", 1.0));
        }

        @Test
        @DisplayName("获取范围")
        void shouldZRange() {
            Set<String> values = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
            when(zSetOperations.range("zset", 0, -1)).thenReturn(values);
            assertEquals(values, cacheService.zRange("zset", 0, -1));
        }

        @Test
        @DisplayName("获取大小")
        void shouldZSize() {
            when(zSetOperations.size("zset")).thenReturn(10L);
            assertEquals(10L, cacheService.zSize("zset"));
        }

        @Test
        @DisplayName("获取score")
        void shouldZScore() {
            when(zSetOperations.score("zset", "a")).thenReturn(99.0);
            assertEquals(99.0, cacheService.zScore("zset", "a"));
        }
    }

    // ==================== zRemove(Collection) ====================

    @Nested
    @DisplayName("zRemove(Collection) 方法测试")
    class ZRemoveCollectionTests {

        @Test
        @DisplayName("删除非空集合中的元素")
        void shouldRemoveNonEmptyCollection() {
            List<String> values = Arrays.asList("a", "b");
            when(zSetOperations.remove(eq("zset"), any())).thenReturn(2L);

            assertEquals(2L, cacheService.zRemove("zset", values));
        }

        @Test
        @DisplayName("删除null集合返回0")
        void shouldReturnZeroForNullCollection() {
            assertEquals(0L, cacheService.zRemove("zset", (Collection<String>) null));
        }

        @Test
        @DisplayName("删除空集合返回0")
        void shouldReturnZeroForEmptyCollection() {
            assertEquals(0L, cacheService.zRemove("zset", Collections.emptyList()));
        }
    }
}