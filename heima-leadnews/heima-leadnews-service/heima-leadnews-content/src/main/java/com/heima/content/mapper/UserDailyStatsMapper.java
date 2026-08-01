package com.heima.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.article.pojos.UserDailyStats;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDailyStatsMapper extends BaseMapper<UserDailyStats> {
}