<template>
    <div class="growth-page">
        <div class="growth-header">
            <div class="header-left" @click="goBack">
                <span class="back-icon">&#xf060;</span>
            </div>
            <div class="header-title">成长等级</div>
            <div class="header-right"></div>
        </div>

        <div class="growth-content">
            <div class="level-cards">
                <div class="level-card daily-level">
                    <div class="level-header">
                        <div class="level-icon">☀️</div>
                        <div class="level-title">逐日等级</div>
                    </div>
                    <div class="level-main">
                        <div class="level-value">Lv.{{ levelInfo.dailyLevel }}</div>
                        <div class="level-score">{{ levelInfo.dailyScore }} 逐日分</div>
                    </div>
                    <div class="level-progress">
                        <div class="progress-bar">
                            <div class="progress-fill" :style="{ width: dailyProgress + '%' }"></div>
                        </div>
                        <div class="progress-text">距离下一级还需 {{ dailyNextLevelScore }} 分</div>
                    </div>
                    <div class="level-title-name">{{ levelInfo.dailyTitle }}</div>
                </div>

                <div class="level-card power-level">
                    <div class="level-header">
                        <div class="level-icon">💪</div>
                        <div class="level-title">逐力值</div>
                    </div>
                    <div class="level-main">
                        <div class="level-value">Lv.{{ levelInfo.powerLevel }}</div>
                        <div class="level-score">{{ levelInfo.powerValue }} 逐力值</div>
                    </div>
                    <div class="level-progress">
                        <div class="progress-bar">
                            <div class="progress-fill" :style="{ width: powerProgress + '%' }"></div>
                        </div>
                        <div class="progress-text">距离下一级还需 {{ powerNextLevelScore }} 分</div>
                    </div>
                    <div class="level-title-name">{{ levelInfo.powerTitle }}</div>
                </div>
            </div>

            <div class="check-in-section">
                <div class="check-in-card" @click="handleCheckIn">
                    <div class="check-in-left">
                        <div class="check-in-icon">📅</div>
                        <div class="check-in-info">
                            <div class="check-in-title">每日签到</div>
                            <div class="check-in-desc">签到获得 10 逐日分</div>
                        </div>
                    </div>
                    <button class="check-in-btn" :class="{ 'checked': hasCheckedIn }">
                        {{ hasCheckedIn ? '今日已签到' : '去签到' }}
                    </button>
                </div>
            </div>

            <div class="permissions-section" v-if="permissions.length > 0">
                <div class="section-header">
                    <div class="section-title">已解锁权限</div>
                    <div class="section-count">共 {{ permissions.length }} 项</div>
                </div>
                <div class="permissions-grid">
                    <div v-for="perm in permissions" :key="perm.code" class="permission-item">
                        <span class="permission-icon">{{ perm.icon }}</span>
                        <span class="permission-name">{{ perm.name }}</span>
                    </div>
                </div>
            </div>

            <div class="tasks-section">
                <div class="section-header">
                    <div class="section-title">升级任务</div>
                    <div class="section-tip">完成任务获取积分</div>
                </div>
                <div class="tasks-list">
                    <div v-for="task in tasks" :key="task.code" class="task-item">
                        <div class="task-icon">{{ task.icon }}</div>
                        <div class="task-info">
                            <div class="task-name">{{ task.name }}</div>
                            <div class="task-progress">
                                <span class="progress-current">{{ task.completedCount }}</span>
                                <span class="progress-separator">/</span>
                                <span class="progress-total">{{ task.totalCount }}</span>
                            </div>
                        </div>
                        <div class="task-right">
                            <div class="task-score">+{{ task.score }}</div>
                            <button 
                                class="task-btn" 
                                :class="{ 'completed': task.completedCount >= task.totalCount }"
                                @click="handleTaskAction(task)"
                            >
                                {{ task.completedCount >= task.totalCount ? '已完成' : '去完成' }}
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div class="level-rules">
                <div class="section-header">
                    <div class="section-title">等级规则</div>
                </div>
                <div class="rules-content">
                    <div class="rule-section">
                        <div class="rule-title">☀️ 逐日等级获取方式</div>
                        <ul class="rule-list">
                            <li>每日登录 +10分</li>
                            <li>阅读文章 +2分</li>
                            <li>发表评论 +5分</li>
                            <li>点赞 +1分</li>
                            <li>分享 +3分</li>
                            <li>关注 +2分</li>
                        </ul>
                    </div>
                    <div class="rule-section">
                        <div class="rule-title">💪 逐力值获取方式</div>
                        <ul class="rule-list">
                            <li>发布文章 +10分</li>
                            <li>文章被点赞 +2分</li>
                            <li>文章被评论 +3分</li>
                            <li>文章被收藏 +5分</li>
                            <li>文章被阅读 +1分</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { toast } from "@/utils/toast"

export default {
    name: 'GrowthLevel',
    data() {
        return {
            userId: 1,
            levelInfo: {
                dailyScore: 0,
                dailyLevel: 1,
                dailyTitle: '',
                dailyDescription: '',
                powerValue: 0,
                powerLevel: 1,
                powerTitle: '',
                powerDescription: ''
            },
            permissions: [],
            tasks: [],
            hasCheckedIn: false,
            dailyNextLevelScore: 0,
            powerNextLevelScore: 0
        }
    },
    computed: {
        dailyProgress() {
            const levelConfig = this.getDailyLevelConfig()
            if (!levelConfig) return 0
            const min = levelConfig.minScore
            const max = levelConfig.maxScore
            const current = this.levelInfo.dailyScore
            this.dailyNextLevelScore = max - current + 1
            return Math.min(((current - min) / (max - min)) * 100, 100)
        },
        powerProgress() {
            const levelConfig = this.getPowerLevelConfig()
            if (!levelConfig) return 0
            const min = levelConfig.minScore
            const max = levelConfig.maxScore
            const current = this.levelInfo.powerValue
            this.powerNextLevelScore = max - current + 1
            return Math.min(((current - min) / (max - min)) * 100, 100)
        }
    },
    mounted() {
        this.loadData()
    },
    methods: {
        goBack() {
            this.$router.back()
        },
        async loadData() {
            await this.loadLevelInfo()
            await this.loadPermissions()
            await this.loadTasks()
        },
        async loadLevelInfo() {
            try {
                const response = await this.$http.get(`/api/v1/level/user/${this.userId}/info`)
                if (response.data) {
                    this.levelInfo = response.data
                }
            } catch (error) {
                this.loadMockLevelInfo()
            }
        },
        async loadPermissions() {
            try {
                const response = await this.$http.get(`/api/v1/level/user/${this.userId}/permissions`)
                if (response.data) {
                    this.permissions = this.mapPermissions(response.data)
                }
            } catch (error) {
                this.permissions = this.getMockPermissions()
            }
        },
        async loadTasks() {
            try {
                const response = await this.$http.get(`/api/v1/level/user/${this.userId}/tasks`)
                if (response.data) {
                    this.tasks = this.mapTasks(response.data)
                }
            } catch (error) {
                this.tasks = this.getMockTasks()
            }
        },
        async handleCheckIn() {
            if (this.hasCheckedIn) {
                toast('今日已签到', 2)
                return
            }
            try {
                const response = await this.$http.post('/api/v1/level/check-in')
                if (response.code === 200) {
                    this.hasCheckedIn = true
                    toast('签到成功，获得10逐日分', 2)
                    this.loadLevelInfo()
                } else {
                    toast(response.message || '签到失败', 2)
                }
            } catch (error) {
                toast('签到失败，请稍后重试', 2)
            }
        },
        handleTaskAction(task) {
            if (task.completedCount >= task.totalCount) {
                toast('该任务已完成', 2)
                return
            }
            switch (task.code) {
                case 'publish_article':
                    this.$router.push('/creator/publish')
                    break
                case 'read_article':
                    this.$router.push('/')
                    break
                case 'comment_article':
                    this.$router.push('/')
                    break
                case 'like_article':
                    this.$router.push('/')
                    break
                case 'share_article':
                    toast('分享功能开发中', 2)
                    break
                case 'follow_user':
                    this.$router.push('/')
                    break
                default:
                    toast('任务功能开发中', 2)
            }
        },
        loadMockLevelInfo() {
            this.levelInfo = {
                dailyScore: 1250,
                dailyLevel: 3,
                dailyTitle: '中级掘友',
                dailyDescription: '社区的中坚力量',
                powerValue: 2300,
                powerLevel: 4,
                powerTitle: '高级创作者',
                powerDescription: '创作经验丰富'
            }
        },
        getMockPermissions() {
            return [
                { code: 'can_send_private_message', name: '私信权限', icon: '💬' },
                { code: 'can_set_comment_permission', name: '评论区权限设置', icon: '🔒' },
                { code: 'can_add_video', name: '添加视频', icon: '🎬' },
                { code: 'can_add_2_tags', name: '2个标签', icon: '🏷️' },
                { code: 'can_schedule_publish', name: '定时发布', icon: '⏰' },
                { code: 'can_add_3_tags', name: '3个标签', icon: '🏷️🏷️' }
            ]
        },
        getMockTasks() {
            return [
                { code: 'read_article', name: '阅读文章', icon: '📖', completedCount: 3, totalCount: 5, score: 2 },
                { code: 'comment_article', name: '发表评论', icon: '💬', completedCount: 1, totalCount: 3, score: 5 },
                { code: 'like_article', name: '点赞文章', icon: '👍', completedCount: 5, totalCount: 10, score: 1 },
                { code: 'share_article', name: '分享文章', icon: '🔗', completedCount: 0, totalCount: 2, score: 3 },
                { code: 'publish_article', name: '发布文章', icon: '✍️', completedCount: 0, totalCount: 1, score: 10 },
                { code: 'follow_user', name: '关注用户', icon: '👤', completedCount: 2, totalCount: 3, score: 2 }
            ]
        },
        mapPermissions(permissionCodes) {
            const permissionMap = {
                'can_send_private_message': { name: '私信权限', icon: '💬' },
                'can_set_comment_permission': { name: '评论区权限设置', icon: '🔒' },
                'can_create_poll': { name: '发起投票', icon: '📊' },
                'can_become_contributor': { name: '成为共建者', icon: '⭐' },
                'can_be_recommended': { name: '文章自动推荐', icon: '🔥' },
                'can_add_video': { name: '添加视频', icon: '🎬' },
                'can_add_2_tags': { name: '2个标签', icon: '🏷️' },
                'can_schedule_publish': { name: '定时发布', icon: '⏰' },
                'can_add_3_tags': { name: '3个标签', icon: '🏷️🏷️' },
                'can_add_4_tags': { name: '4个标签', icon: '🏷️🏷️🏷️' },
                'can_create_course': { name: '创作小册', icon: '📚' }
            }
            return permissionCodes.map(code => ({
                code,
                ...permissionMap[code] || { name: code, icon: '✨' }
            }))
        },
        mapTasks(taskData) {
            const taskMap = {
                'read_article': { name: '阅读文章', icon: '📖' },
                'comment_article': { name: '发表评论', icon: '💬' },
                'like_article': { name: '点赞文章', icon: '👍' },
                'share_article': { name: '分享文章', icon: '🔗' },
                'publish_article': { name: '发布文章', icon: '✍️' },
                'follow_user': { name: '关注用户', icon: '👤' },
                'check_in': { name: '每日签到', icon: '📅' }
            }
            return taskData.map(task => ({
                ...task,
                ...taskMap[task.code] || { name: task.code, icon: '✨' }
            }))
        },
        getDailyLevelConfig() {
            const level = this.levelInfo.dailyLevel
            const configs = [
                { level: 1, minScore: 0, maxScore: 99 },
                { level: 2, minScore: 100, maxScore: 499 },
                { level: 3, minScore: 500, maxScore: 1499 },
                { level: 4, minScore: 1500, maxScore: 2999 },
                { level: 5, minScore: 3000, maxScore: 4999 },
                { level: 6, minScore: 5000, maxScore: 7999 },
                { level: 7, minScore: 8000, maxScore: 11999 },
                { level: 8, minScore: 12000, maxScore: 19999 },
                { level: 9, minScore: 20000, maxScore: 29999 },
                { level: 10, minScore: 30000, maxScore: 999999 }
            ]
            return configs.find(c => c.level === level)
        },
        getPowerLevelConfig() {
            const level = this.levelInfo.powerLevel
            const configs = [
                { level: 1, minScore: 0, maxScore: 99 },
                { level: 2, minScore: 100, maxScore: 499 },
                { level: 3, minScore: 500, maxScore: 1499 },
                { level: 4, minScore: 1500, maxScore: 2999 },
                { level: 5, minScore: 3000, maxScore: 4999 },
                { level: 6, minScore: 5000, maxScore: 7999 },
                { level: 7, minScore: 8000, maxScore: 11999 },
                { level: 8, minScore: 12000, maxScore: 19999 },
                { level: 9, minScore: 20000, maxScore: 29999 },
                { level: 10, minScore: 30000, maxScore: 999999 }
            ]
            return configs.find(c => c.level === level)
        }
    }
}
</script>

<style lang="less" scoped>
@import '../../../styles/common';

.growth-page {
    min-height: 100vh;
    background: #f7f8fa;
}

.growth-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: @mian-color;
    height: @top-height;
    padding: 0 15px;
    box-sizing: border-box;
    position: sticky;
    top: 0;
    z-index: 100;
}

.header-left, .header-right {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.back-icon {
    font-family: fontawesome;
    font-size: 32px;
    color: #fff;
    cursor: pointer;
}

.header-title {
    font-size: 18px;
    font-weight: 600;
    color: #fff;
}

.growth-content {
    max-width: 800px;
    margin: 0 auto;
    padding: 16px;
}

.level-cards {
    display: flex;
    flex-direction: column;
    gap: 16px;
    margin-bottom: 16px;
}

.level-card {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.level-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
}

.level-icon {
    font-size: 20px;
}

.level-title {
    font-size: 14px;
    color: #666;
}

.level-main {
    display: flex;
    align-items: baseline;
    gap: 12px;
    margin-bottom: 12px;
}

.level-value {
    font-size: 28px;
    font-weight: 700;
    color: #1a1a1a;
}

.daily-level .level-value {
    color: #FF6B35;
}

.power-level .level-value {
    color: #6C5CE7;
}

.level-score {
    font-size: 14px;
    color: #999;
}

.level-progress {
    margin-bottom: 8px;
}

.progress-bar {
    height: 6px;
    background: #f0f0f0;
    border-radius: 3px;
    overflow: hidden;
    margin-bottom: 6px;
}

.progress-fill {
    height: 100%;
    border-radius: 3px;
    transition: width 0.3s ease;
}

.daily-level .progress-fill {
    background: linear-gradient(90deg, #FF6B35, #FF8E53);
}

.power-level .progress-fill {
    background: linear-gradient(90deg, #6C5CE7, #A29BFE);
}

.progress-text {
    font-size: 12px;
    color: #999;
}

.level-title-name {
    font-size: 13px;
    color: #666;
    font-weight: 500;
}

.check-in-section {
    margin-bottom: 16px;
}

.check-in-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: linear-gradient(135deg, #FF6B35 0%, #FF8E53 100%);
    border-radius: 12px;
    padding: 20px;
    cursor: pointer;
    transition: transform 0.2s;

    &:active {
        transform: scale(0.98);
    }
}

.check-in-left {
    display: flex;
    align-items: center;
    gap: 12px;
}

.check-in-icon {
    font-size: 32px;
}

.check-in-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.check-in-title {
    font-size: 18px;
    font-weight: 600;
    color: #fff;
}

.check-in-desc {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.8);
}

.check-in-btn {
    padding: 10px 24px;
    border: 2px solid #fff;
    border-radius: 20px;
    background: transparent;
    color: #fff;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
        background: rgba(255, 255, 255, 0.2);
    }

    &.checked {
        background: #fff;
        color: #FF6B35;
        cursor: default;
    }
}

.permissions-section, .tasks-section, .level-rules {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
}

.section-title {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
}

.section-count {
    font-size: 13px;
    color: #999;
}

.section-tip {
    font-size: 12px;
    color: #999;
}

.permissions-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
}

.permission-item {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    background: #f8f9fa;
    border-radius: 20px;
    font-size: 13px;
    color: #666;
}

.permission-icon {
    font-size: 14px;
}

.tasks-list {
    display: flex;
    flex-direction: column;
    gap: 0;
}

.task-item {
    display: flex;
    align-items: center;
    padding: 16px 0;
    border-bottom: 1px solid #f2f3f5;

    &:last-child {
        border-bottom: none;
    }
}

.task-icon {
    font-size: 24px;
    margin-right: 12px;
    width: 40px;
    text-align: center;
}

.task-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.task-name {
    font-size: 15px;
    color: #1a1a1a;
    font-weight: 500;
}

.task-progress {
    font-size: 13px;
    color: #999;
}

.progress-current {
    color: #FF6B35;
    font-weight: 600;
}

.progress-separator {
    margin: 0 4px;
}

.task-right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 8px;
}

.task-score {
    font-size: 13px;
    color: #FF6B35;
    font-weight: 500;
}

.task-btn {
    padding: 6px 16px;
    border: 1px solid #FF6B35;
    border-radius: 16px;
    background: transparent;
    color: #FF6B35;
    font-size: 13px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
        background: rgba(255, 107, 53, 0.1);
    }

    &.completed {
        border-color: #ccc;
        color: #999;
        cursor: default;
    }
}

.rules-content {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.rule-section {
    padding: 12px;
    background: #f8f9fa;
    border-radius: 8px;
}

.rule-title {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 8px;
    color: #333;
}

.rule-list {
    list-style: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
}

.rule-list li {
    font-size: 13px;
    color: #666;
    padding: 4px 10px;
    background: #fff;
    border-radius: 12px;
}

@media screen and (max-width: 768px) {
    .growth-content {
        padding: 12px;
    }

    .level-card {
        padding: 16px;
    }

    .check-in-card {
        padding: 16px;
    }

    .check-in-btn {
        padding: 8px 16px;
        font-size: 13px;
    }

    .task-item {
        padding: 14px 0;
    }

    .task-icon {
        font-size: 20px;
        width: 32px;
    }
}
</style>