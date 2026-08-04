package com.heima.reward.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.reward.entity.UserAssets;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface UserAssetsMapper extends BaseMapper<UserAssets> {

    /**
     * 增加用户矿石余额
     * @param userId 用户ID
     * @param amount 增加数量（正数）
     */
    @Update("UPDATE user_assets SET ore_balance = ore_balance + #{amount}, updated_at = NOW() WHERE user_id = #{userId}")
    int addOreBalance(@Param("userId") Long userId, @Param("amount") int amount);

    /**
     * 扣减用户矿石余额（带余额检查）
     * @param userId 用户ID
     * @param amount 扣减数量
     * @return 影响行数（0表示余额不足）
     */
    @Update("UPDATE user_assets SET ore_balance = ore_balance - #{amount}, updated_at = NOW() WHERE user_id = #{userId} AND ore_balance >= #{amount}")
    int deductOreBalance(@Param("userId") Long userId, @Param("amount") int amount);
}
