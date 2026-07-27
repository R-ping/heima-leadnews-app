package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dto.BlockDTO;
import com.heima.user.service.BlockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BlockController 单元测试")
class BlockControllerTest {

    @Mock
    private BlockService blockService;

    @InjectMocks
    private BlockController blockController;

    // ==================== getBlocks ====================

    @Nested
    @DisplayName("getBlocks 方法测试")
    class GetBlocksTests {

        @Test
        @DisplayName("正常获取屏蔽列表 - 使用默认参数")
        void shouldReturnBlocksWithDefaults() {
            ResponseResult expected = ResponseResult.okResult("blocks-data");
            when(blockService.getBlocks(1, 1, 10)).thenReturn(expected);

            ResponseResult result = blockController.getBlocks(1, 1, 10);

            assertSame(expected, result);
            verify(blockService).getBlocks(1, 1, 10);
        }

        @Test
        @DisplayName("获取屏蔽列表 - 自定义分页参数")
        void shouldReturnBlocksWithCustomPagination() {
            ResponseResult expected = ResponseResult.okResult("blocks-data");
            when(blockService.getBlocks(2, 3, 20)).thenReturn(expected);

            ResponseResult result = blockController.getBlocks(2, 3, 20);

            assertSame(expected, result);
            verify(blockService).getBlocks(2, 3, 20);
        }

        @Test
        @DisplayName("获取屏蔽列表 - type=1（作者）")
        void shouldReturnBlocksByAuthor() {
            ResponseResult expected = ResponseResult.okResult("author-blocks");
            when(blockService.getBlocks(1, 1, 10)).thenReturn(expected);

            ResponseResult result = blockController.getBlocks(1, 1, 10);

            assertSame(expected, result);
            verify(blockService).getBlocks(1, 1, 10);
        }

        @Test
        @DisplayName("获取屏蔽列表 - type=2（标签）")
        void shouldReturnBlocksByTag() {
            ResponseResult expected = ResponseResult.okResult("tag-blocks");
            when(blockService.getBlocks(2, 1, 10)).thenReturn(expected);

            ResponseResult result = blockController.getBlocks(2, 1, 10);

            assertSame(expected, result);
            verify(blockService).getBlocks(2, 1, 10);
        }

        @Test
        @DisplayName("获取屏蔽列表 - 服务返回错误")
        void shouldReturnErrorWhenServiceFails() {
            ResponseResult expected = ResponseResult.errorResult(500, "获取屏蔽列表失败");
            when(blockService.getBlocks(anyInt(), anyInt(), anyInt())).thenReturn(expected);

            ResponseResult result = blockController.getBlocks(1, 1, 10);

            assertEquals(500, result.getCode());
            assertEquals("获取屏蔽列表失败", result.getMessage());
        }
    }

    // ==================== addBlock ====================

    @Nested
    @DisplayName("addBlock 方法测试")
    class AddBlockTests {

        @Test
        @DisplayName("正常添加屏蔽 - 作者")
        void shouldAddBlockAuthorSuccessfully() {
            BlockDTO dto = new BlockDTO();
            dto.setType(1);
            dto.setTargetId(1001L);
            ResponseResult expected = ResponseResult.okResult();
            when(blockService.addBlock(any(BlockDTO.class))).thenReturn(expected);

            ResponseResult result = blockController.addBlock(dto);

            assertSame(expected, result);
            verify(blockService).addBlock(dto);
        }

        @Test
        @DisplayName("正常添加屏蔽 - 标签")
        void shouldAddBlockTagSuccessfully() {
            BlockDTO dto = new BlockDTO();
            dto.setType(2);
            dto.setTargetId(2001L);
            ResponseResult expected = ResponseResult.okResult();
            when(blockService.addBlock(any(BlockDTO.class))).thenReturn(expected);

            ResponseResult result = blockController.addBlock(dto);

            assertSame(expected, result);
            verify(blockService).addBlock(dto);
        }

        @Test
        @DisplayName("添加屏蔽 - type无效")
        void shouldHandleInvalidType() {
            BlockDTO dto = new BlockDTO();
            dto.setType(5);
            dto.setTargetId(1001L);
            ResponseResult expected = ResponseResult.errorResult(503, "type参数无效");
            when(blockService.addBlock(any(BlockDTO.class))).thenReturn(expected);

            ResponseResult result = blockController.addBlock(dto);

            assertEquals(503, result.getCode());
            verify(blockService).addBlock(dto);
        }

        @Test
        @DisplayName("添加屏蔽 - targetId为null")
        void shouldHandleNullTargetId() {
            BlockDTO dto = new BlockDTO();
            dto.setType(1);
            dto.setTargetId(null);
            ResponseResult expected = ResponseResult.errorResult(503, "targetId不能为空");
            when(blockService.addBlock(any(BlockDTO.class))).thenReturn(expected);

            ResponseResult result = blockController.addBlock(dto);

            assertEquals(503, result.getCode());
            verify(blockService).addBlock(dto);
        }

        @Test
        @DisplayName("添加屏蔽 - DTO为null")
        void shouldHandleNullDto() {
            ResponseResult expected = ResponseResult.errorResult(400, "参数不能为空");
            when(blockService.addBlock(null)).thenReturn(expected);

            ResponseResult result = blockController.addBlock(null);

            assertEquals(400, result.getCode());
            verify(blockService).addBlock(null);
        }

        @Test
        @DisplayName("添加屏蔽 - 重复屏蔽")
        void shouldHandleDuplicateBlock() {
            BlockDTO dto = new BlockDTO();
            dto.setType(1);
            dto.setTargetId(1001L);
            ResponseResult expected = ResponseResult.errorResult(503, "已屏蔽该用户");
            when(blockService.addBlock(any(BlockDTO.class))).thenReturn(expected);

            ResponseResult result = blockController.addBlock(dto);

            assertEquals(503, result.getCode());
            verify(blockService).addBlock(dto);
        }
    }

    // ==================== removeBlock ====================

    @Nested
    @DisplayName("removeBlock 方法测试")
    class RemoveBlockTests {

        @Test
        @DisplayName("正常取消屏蔽")
        void shouldRemoveBlockSuccessfully() {
            ResponseResult expected = ResponseResult.okResult();
            when(blockService.removeBlock(1001L)).thenReturn(expected);

            ResponseResult result = blockController.removeBlock(1001L);

            assertSame(expected, result);
            verify(blockService).removeBlock(1001L);
        }

        @Test
        @DisplayName("取消屏蔽 - id不存在")
        void shouldHandleNonExistentId() {
            ResponseResult expected = ResponseResult.errorResult(503, "屏蔽记录不存在");
            when(blockService.removeBlock(9999L)).thenReturn(expected);

            ResponseResult result = blockController.removeBlock(9999L);

            assertEquals(503, result.getCode());
            verify(blockService).removeBlock(9999L);
        }

        @Test
        @DisplayName("取消屏蔽 - id为null")
        void shouldHandleNullId() {
            ResponseResult expected = ResponseResult.errorResult(400, "id不能为空");
            when(blockService.removeBlock(null)).thenReturn(expected);

            ResponseResult result = blockController.removeBlock(null);

            assertEquals(400, result.getCode());
            verify(blockService).removeBlock(null);
        }

        @Test
        @DisplayName("取消屏蔽 - 服务返回错误")
        void shouldReturnErrorWhenRemoveFails() {
            ResponseResult expected = ResponseResult.errorResult(500, "取消屏蔽失败");
            when(blockService.removeBlock(anyLong())).thenReturn(expected);

            ResponseResult result = blockController.removeBlock(1001L);

            assertEquals(500, result.getCode());
            assertEquals("取消屏蔽失败", result.getMessage());
        }
    }
}