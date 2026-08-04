package com.heima.content.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.user.pojos.UserScoreSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.math.BigDecimal;

@Mapper
public interface UserScoreSummaryMapper extends BaseMapper<UserScoreSummary> {

    @Select("SELECT COALESCE(SUM(total_score), 0) FROM user_score_summary WHERE user_id = #{userId}")
    BigDecimal sumTotalScore(@Param("userId") Long userId);

    @Select("SELECT COALESCE(SUM(${field}), 0) FROM user_score_summary WHERE user_id = #{userId}")
    BigDecimal sumFieldScore(@Param("userId") Long userId, @Param("field") String field);
}