package com.heima.model.level.dtos;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class JScoreDetailVO {
    private List<JScoreDetailItem> list;
    private String nextCursor;
    private Boolean hasMore;

    @Data
    public static class JScoreDetailItem {
        private String id;
        private String createdAt;
        private String actionCode;
        private String actionDesc;
        private BigDecimal score;
        private String category;
    }
}