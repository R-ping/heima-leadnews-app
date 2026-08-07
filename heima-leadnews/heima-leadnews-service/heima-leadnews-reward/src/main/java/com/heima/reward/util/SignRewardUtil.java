package com.heima.reward.util;

/**
 * 签到奖励计算工具类
 * 基于连续天数 d 计算奖励，30天一个周期
 */
public class SignRewardUtil {

    private static final int CYCLE = 30;

    private static final int[] REWARD_TABLE = {
        100, 150, 512, 250, 300, 350, 1024, 450, 500, 550,
        600, 650, 700, 2048, 700, 700, 700, 700, 700, 700,
        4096, 700, 700, 700, 700, 700, 700, 700, 700, 5120
    };

    /**
     * 根据连续天数获取奖励矿石数
     * @param continuousDays 连续天数（从1开始）
     * @return 奖励矿石数
     */
    public static int getRewardByContinuousDays(int continuousDays) {
        if (continuousDays <= 0) return 0;
        int index = (continuousDays - 1) % CYCLE;
        return REWARD_TABLE[index];
    }

    /**
     * 判断指定连续天数是否为特殊奖励日（高额奖励）
     */
    public static boolean isSpecialDay(int continuousDays) {
        if (continuousDays <= 0) return false;
        int index = (continuousDays - 1) % CYCLE;
        int reward = REWARD_TABLE[index];
        return reward > 700;
    }
}