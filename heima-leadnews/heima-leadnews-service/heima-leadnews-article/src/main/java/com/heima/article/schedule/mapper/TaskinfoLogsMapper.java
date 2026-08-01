package com.heima.article.schedule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itheima
 */
@Mapper
public interface TaskinfoLogsMapper extends BaseMapper<TaskinfoLogs> {

    @Select("select * from taskinfo_logs where status = 1 and execute_time <= #{nextHour}")
    List<TaskinfoLogs> selectGoal(Date nextHour);
}