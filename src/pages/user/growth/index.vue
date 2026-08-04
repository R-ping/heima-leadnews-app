<template>
    <div class="growth-page">
        <div class="art-top" v-if="!isDesktop"><HomeBar/></div>

        <div class="growth-content">
            <!-- 用户名称 -->
            <div class="user-name-row">
                <span class="user-name">{{ userName }}</span>
                <span class="user-level-tag">{{ levelInfo.dailyTitle || '新星掘友' }}</span>
            </div>

            <!-- 成长摘要卡片 -->
            <div class="summary-card">
                <div class="summary-left">
                    <div class="level-score-display">
                        <span class="score-number">{{ levelInfo.dailyScore }}</span>
                        <span class="score-unit">掘友分</span>
                    </div>
                    <div class="level-progress">
                        <div class="progress-bar">
                            <div
                                class="progress-fill"
                                :style="{ width: dailyProgressPercent + '%' }"
                            ></div>
                        </div>
                        <div class="progress-text">
                            {{ dailyProgressText }}
                        </div>
                    </div>
                    <!-- 等级里程碑节点 ZR1-ZR5 -->
                    <div class="milestone-track" v-if="dailyLevelConfigs.length > 0">
                        <div
                            v-for="(node, idx) in dailyLevelConfigs"
                            :key="node.levelValue"
                            class="milestone-item"
                        >
                            <div
                                class="milestone-dot"
                                :class="{ active: node.levelValue <= levelInfo.dailyLevel }"
                            ></div>
                            <div class="milestone-label">JY{{ node.levelValue }}</div>
                            <div class="milestone-score">{{ node.minScore }}</div>
                        </div>
                    </div>
                </div>
                <div class="summary-right">
                    <div class="function-grid">
                        <div class="func-item" @click="goCheckIn">
                            <span class="func-icon">📅</span>
                            <span class="func-text">每日签到</span>
                        </div>
                        <div class="func-item" @click="showDevTip('幸运抽奖')">
                            <span class="func-icon">🎁</span>
                            <span class="func-text">幸运抽奖</span>
                        </div>
                        <div class="func-item" @click="showDevTip('福利兑换')">
                            <span class="func-icon">🎁</span>
                            <span class="func-text">福利兑换</span>
                        </div>
                        <div class="func-item" @click="showDevTip('我的收获')">
                            <span class="func-icon">📦</span>
                            <span class="func-text">我的收获</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 逐力值卡片 -->
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
                        <div
                            class="progress-fill"
                            :style="{ width: powerProgress + '%' }"
                        ></div>
                    </div>
                    <div class="progress-text">
                        距离下一级还需 {{ powerNextLevelScore }} 分
                    </div>
                </div>
                <div class="level-title-name">{{ levelInfo.powerTitle }}</div>
            </div>

            <!-- 签到区域 -->
            <div class="check-in-section">
                <div class="check-in-card" @click="handleCheckIn">
                    <div class="check-in-left">
                        <div class="check-in-icon">📅</div>
                        <div class="check-in-info">
                            <div class="check-in-title">每日签到</div>
                            <div class="check-in-desc">签到获得 2 逐日分</div>
                        </div>
                    </div>
                    <button
                        class="check-in-btn"
                        :class="{ checked: hasCheckedIn }"
                    >
                        {{ hasCheckedIn ? '今日已签到' : '去签到' }}
                    </button>
                </div>
            </div>

            <!-- 权限区域 -->
            <div class="permissions-section" v-if="permissions.length > 0">
                <div class="section-header">
                    <div class="section-title">已解锁权限</div>
                    <div class="section-count">共 {{ permissions.length }} 项</div>
                </div>
                <div class="permissions-grid">
                    <div
                        v-for="perm in permissions"
                        :key="perm.code"
                        class="permission-item"
                    >
                        <span class="permission-icon">{{ perm.icon }}</span>
                        <span class="permission-name">{{ perm.name }}</span>
                    </div>
                </div>
            </div>

            <!-- 升级任务列表 -->
            <div class="tasks-section">
                <div class="tasks-section-header">
                    <div class="tasks-title">升级行为</div>
                    <div class="tasks-today-score">
                        今日掘友分 +{{ levelInfo.dailyScoreToday || 0 }}
                    </div>
                    <div class="jscore-link" @click="goToJScore">掘友分明细 ›</div>
                </div>
                <div
                    v-for="group in taskGroups"
                    :key="group.name"
                    class="task-group"
                >
                    <div class="group-title">
                        <span class="group-icon">{{ group.icon }}</span>
                        <span>{{ group.name }}</span>
                    </div>
                    <div class="group-tasks">
                        <div
                            v-for="task in group.tasks"
                            :key="task.actionType"
                            class="task-item"
                        >
                            <div class="task-icon-wrap">
                                <span class="task-icon">{{ task.icon }}</span>
                            </div>
                            <div class="task-info">
                                <div class="task-name">{{ task.name }}</div>
                            </div>
                            <div class="task-meta">
                                <div class="task-score">+{{ task.score }}</div>
                                <div class="task-progress">
                                    {{ task.completed ? '已完成' : task.current + '/' + task.max }}
                                </div>
                            </div>
                            <button
                                class="task-btn"
                                :class="{ completed: task.completed }"
                                :disabled="task.completed"
                                @click="handleTaskAction(task)"
                            >
                                {{ task.completed ? '已完成' : task.buttonText }}
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 等级规则 -->
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

            <!-- 底部信息 -->
            <div class="growth-footer">
                <span>用户协议</span>
                <span class="footer-sep">·</span>
                <span>法律声明</span>
                <span class="footer-sep">·</span>
                <span>&copy;2026 稀土掘金</span>
            </div>
        </div>
    </div>
</template>

<script>
import HomeBar from '@/components/bars/home_bar'
import Utils from '@/utils/env'
import { toast } from '@/utils/toast'

// 任务分组配置
const TASK_GROUP_CONFIG = {
    '社区基础': {
        icon: '🏠',
        actionTypes: ['upload_avatar', 'check_in', 'daily_login']
    },
    '社区活跃': {
        icon: '🔥',
        actionTypes: [
            'publish_article', 'publish_pin',
            'comment_article', 'comment_pin',
            'like_article', 'like_pin',
            'collect_article', 'follow_user'
        ]
    },
    '社区学习': {
        icon: '📚',
        actionTypes: ['browse_article', 'browse_course']
    },
    '社区影响力': {
        icon: '⭐',
        actionTypes: ['be_followed', 'pin_liked', 'article_liked']
    }
}

// 任务图标映射
const TASK_ICON_MAP = {
    'upload_avatar': '📷',
    'check_in': '📅',
    'daily_login': '📱',
    'publish_article': '✍️',
    'publish_pin': '💬',
    'comment_article': '💬',
    'comment_pin': '💬',
    'like_article': '👍',
    'like_pin': '👍',
    'collect_article': '⭐',
    'follow_user': '👤',
    'browse_article': '📖',
    'browse_course': '🎓',
    'be_followed': '👥',
    'pin_liked': '❤️',
    'article_liked': '❤️'
}

// 任务按钮文案映射
const TASK_BUTTON_MAP = {
    'upload_avatar': '去上传',
    'check_in': '去完成',
    'daily_login': '去完成',
    'publish_article': '去发布',
    'publish_pin': '去发布',
    'comment_article': '去评论',
    'comment_pin': '去评论',
    'like_article': '去点赞',
    'like_pin': '去点赞',
    'collect_article': '去收藏',
    'follow_user': '去关注',
    'browse_article': '去学习',
    'browse_course': '去学习',
    'be_followed': '去分享',
    'pin_liked': '去发布',
    'article_liked': '去创作'
}

export default {
    name: 'GrowthLevel',
    components: { HomeBar },
    data() {
        return {
            userId: 1,
            levelInfo: {
                dailyScore: 0,
                dailyLevel: 1,
                dailyTitle: '',
                dailyScoreToday: 0,
                powerValue: 0,
                powerLevel: 1,
                powerTitle: '',
                powerDescription: ''
            },
            dailyLevelConfigs: [],
            permissions: [],
            tasks: [],
            hasCheckedIn: false,
            dailyNextLevelScore: 0,
            powerNextLevelScore: 0
        }
    },
    computed: {
        isDesktop() {
            return Utils.isDesktop()
        },
        userName() {
            const userInfo = this.$store.state.user.userInfo
            return userInfo ? (userInfo.nickName || '用户') : '用户'
        },
        dailyProgressPercent() {
            const configs = this.dailyLevelConfigs
            if (!configs.length) return 0
            const currentLevel = this.levelInfo.dailyLevel
            const currentConfig = configs.find(c => c.levelValue === currentLevel)
            const nextConfig = configs.find(c => c.levelValue === currentLevel + 1)
            if (!currentConfig) return 0
            if (!nextConfig) {
                this.dailyNextLevelScore = 0
                return 100
            }
            const current = this.levelInfo.dailyScore
            const min = currentConfig.minScore
            const max = nextConfig.minScore
            this.dailyNextLevelScore = Math.max(max - current, 0)
            return Math.min(Math.max(((current - min) / (max - min)) * 100, 0), 100)
        },
        dailyProgressText() {
            if (this.dailyNextLevelScore <= 0) return '已达最高等级'
            const configs = this.dailyLevelConfigs
            const nextConfig = configs.find(c => c.levelValue === this.levelInfo.dailyLevel + 1)
            const nextTitle = nextConfig ? nextConfig.title : ''
            return '还需 ' + this.dailyNextLevelScore + ' 分可升至 ' + (nextTitle || ('逐日 ' + (this.levelInfo.dailyLevel + 1) + ' 级'))
        },
        powerProgress() {
            const levelConfig = this.getPowerLevelConfig()
            if (!levelConfig) return 0
            const min = levelConfig.minScore
            const max = levelConfig.maxScore
            const current = this.levelInfo.powerValue
            this.powerNextLevelScore = Math.max(max - current + 1, 0)
            return Math.min(((current - min) / (max - min)) * 100, 100)
        },
        taskGroups() {
            const groups = []
            const taskMap = {}
            this.tasks.forEach(t => { taskMap[t.actionType] = t })

            for (const [groupName, groupConfig] of Object.entries(TASK_GROUP_CONFIG)) {
                const groupTasks = []
                groupConfig.actionTypes.forEach(actionType => {
                    const task = taskMap[actionType]
                    if (task) {
                        groupTasks.push({
                            ...task,
                            icon: TASK_ICON_MAP[actionType] || '✨',
                            buttonText: TASK_BUTTON_MAP[actionType] || '去完成'
                        })
                    }
                })
                if (groupTasks.length > 0) {
                    groups.push({
                        name: groupName,
                        icon: groupConfig.icon,
                        tasks: groupTasks
                    })
                }
            }
            return groups
        }
    },
    created() {
        const userInfo = this.$store.state.user.userInfo
        if (userInfo && userInfo.userId) {
            this.userId = userInfo.userId
        }
    },
    mounted() {
        this.loadData()
    },
    methods: {
        goBack() {
            this.$router.back()
        },
        goToJScore() {
            this.$router.push('/user/growth/jscore')
        },
        async loadData() {
            await Promise.all([
                this.loadLevelInfo(),
                this.loadLevelConfigs(),
                this.loadPermissions(),
                this.loadTasks()
            ])
        },
        async loadLevelInfo() {
            try {
                const response = await this.$http.get('/api/v1/level/user/' + this.userId + '/info')
                if (response.data) {
                    this.levelInfo = {
                        ...this.levelInfo,
                        ...response.data
                    }
                }
            } catch (error) {
                // Keep default values when API fails
            }
        },
        async loadLevelConfigs() {
            try {
                const response = await this.$http.get('/api/v1/level/configs?levelType=1')
                const data = response.data
                if (Array.isArray(data)) {
                    this.dailyLevelConfigs = data
                } else if (data && Array.isArray(data.list)) {
                    this.dailyLevelConfigs = data.list
                } else if (data && Array.isArray(data.records)) {
                    this.dailyLevelConfigs = data.records
                }
            } catch (error) {
                // Keep empty configs when API fails
            }
        },
        async loadPermissions() {
            try {
                const response = await this.$http.get('/api/v1/level/user/' + this.userId + '/permissions')
                if (response.data) {
                    this.permissions = this.mapPermissions(response.data)
                }
            } catch (error) {
                // Keep empty permissions when API fails
            }
        },
        async loadTasks() {
            try {
                const response = await this.$http.get('/api/v1/level/user/' + this.userId + '/tasks')
                const data = response.data
                let taskList = []
                if (Array.isArray(data)) {
                    taskList = data
                } else if (data && Array.isArray(data.tasks)) {
                    taskList = data.tasks
                } else if (data && Array.isArray(data.list)) {
                    taskList = data.list
                }
                this.tasks = taskList.map(task => ({
                    ...task,
                    completed: task.completed || (task.current >= task.max)
                }))
            } catch (error) {
                // Keep empty tasks when API fails
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
        goCheckIn() {
            this.$router.push('/user/checkin')
        },
        showDevTip(name) {
            toast(name + '开发中', 2)
        },
        handleTaskAction(task) {
            if (task.completed) {
                toast('该任务已完成', 2)
                return
            }
            switch (task.actionType) {
                case 'publish_article':
                    this.$router.push('/creator/publish')
                    break
                case 'publish_pin':
                    this.$router.push('/pins')
                    break
                case 'browse_article':
                case 'browse_course':
                case 'comment_article':
                case 'comment_pin':
                case 'like_article':
                case 'like_pin':
                case 'follow_user':
                    this.$router.push('/')
                    break
                case 'upload_avatar':
                    this.$router.push('/user/settings')
                    break
                case 'check_in':
                    this.handleCheckIn()
                    break
                case 'collect_article':
                    this.$router.push('/')
                    break
                case 'be_followed':
                case 'pin_liked':
                case 'article_liked':
                    toast('快去创作优质内容吸引关注吧', 2)
                    break
                default:
                    toast('任务功能开发中', 2)
            }
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
            const codes = Array.isArray(permissionCodes) ? permissionCodes : []
            return codes.map(code => ({
                code,
                ...permissionMap[code] || { name: code, icon: '✨' }
            }))
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
    background: #F5F7FA;
}

.growth-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 24px;
}

// ===== 用户名称行 =====
.user-name-row {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
}

.user-name {
    font-size: 18px;
    font-weight: 600;
    color: #1A1A1A;
}

.user-level-tag {
    font-size: 13px;
    color: #1A73E8;
    background: #E8F0FE;
    padding: 2px 12px;
    border-radius: 12px;
}

// ===== 成长摘要卡片 =====
.summary-card {
    display: flex;
    background: #FFFFFF;
    border-radius: 16px;
    padding: 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    margin-bottom: 16px;
    gap: 32px;
}

.summary-left {
    flex: 1;
    min-width: 0;
}

.summary-right {
    flex-shrink: 0;
    display: flex;
    align-items: center;
}

.level-score-display {
    display: flex;
    align-items: baseline;
    gap: 8px;
    margin-bottom: 12px;
}

.score-number {
    font-size: 28px;
    font-weight: 700;
    color: #1A1A1A;
}

.score-unit {
    font-size: 14px;
    color: #8C8C8C;
}

.level-progress {
    margin-bottom: 16px;
}

.progress-bar {
    height: 6px;
    background: #E8E8E8;
    border-radius: 3px;
    overflow: hidden;
    margin-bottom: 6px;
}

.summary-left .progress-fill {
    height: 100%;
    border-radius: 3px;
    background: #1A73E8;
    transition: width 0.8s ease;
}

.progress-text {
    font-size: 13px;
    color: #666666;
}

// ===== 等级里程碑节点 =====
.milestone-track {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    padding: 8px 0 0;
    position: relative;
    margin-top: 4px;

    &::before {
        content: '';
        position: absolute;
        top: 28px;
        left: 10px;
        right: 10px;
        height: 2px;
        background: #D9D9D9;
        z-index: 0;
    }
}

.milestone-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    position: relative;
    z-index: 1;
    flex: 1;
}

.milestone-dot {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    border: 3px solid #D9D9D9;
    background: #FFFFFF;
    position: relative;
    z-index: 1;
    transition: all 0.3s ease;

    &.active {
        border-color: #1A73E8;
        background: #1A73E8;
    }
}

.milestone-label {
    font-size: 12px;
    color: #999999;
    margin-top: 6px;
    white-space: nowrap;
}

.milestone-score {
    font-size: 11px;
    color: #B0B0B0;
    margin-top: 2px;
    white-space: nowrap;
}

// ===== 功能入口网格 =====
.function-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
}

.func-item {
    width: 72px;
    height: 72px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: #F8FAFC;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
        transform: translateY(-2px);
    }
}

.func-icon {
    font-size: 24px;
    line-height: 1;
    margin-bottom: 4px;
}

.func-text {
    font-size: 12px;
    color: #666666;
    white-space: nowrap;
}

// ===== 等级卡片（逐力值） =====
.level-card {
    background: #fff;
    border-radius: 16px;
    padding: 20px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    margin-bottom: 16px;
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

.power-level .level-value {
    color: #6C5CE7;
}

.level-score {
    font-size: 14px;
    color: #999;
}

.power-level .progress-fill {
    height: 100%;
    border-radius: 3px;
    background: linear-gradient(90deg, #6C5CE7, #A29BFE);
    transition: width 0.3s ease;
}

.level-title-name {
    font-size: 13px;
    color: #666;
    font-weight: 500;
}

// ===== 签到区域 =====
.check-in-section {
    margin-bottom: 16px;
}

.check-in-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: linear-gradient(135deg, #FF6B35 0%, #FF8E53 100%);
    border-radius: 16px;
    padding: 20px;
    cursor: pointer;
    transition: transform 0.2s;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

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

// ===== 权限区域 =====
.permissions-section {
    background: #fff;
    border-radius: 16px;
    padding: 20px;
    margin-bottom: 16px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
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

// ===== 升级任务列表 =====
.tasks-section {
    background: #fff;
    border-radius: 16px;
    padding: 20px;
    margin-bottom: 16px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.tasks-section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 1px solid #F0F2F5;
}

.tasks-title {
    font-size: 16px;
    font-weight: 600;
    color: #1A1A1A;
}

.tasks-today-score {
    font-size: 14px;
    color: #52C41A;
}

.jscore-link {
    font-size: 13px;
    color: #1A73E8;
    cursor: pointer;
    margin-left: 12px;
    &:hover {
        color: #1557B0;
    }
}

// 任务分组
.task-group {
    margin-bottom: 24px;

    &:last-child {
        margin-bottom: 0;
    }
}

.group-title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 15px;
    font-weight: 500;
    color: #1A1A1A;
    margin-bottom: 8px;
}

.group-icon {
    font-size: 16px;
}

.group-tasks {
    display: flex;
    flex-direction: column;
}

.task-item {
    display: flex;
    align-items: center;
    padding: 14px 0;
    border-bottom: 1px solid #F0F2F5;

    &:last-child {
        border-bottom: none;
    }
}

.task-icon-wrap {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: #F0F5FF;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    margin-right: 12px;
}

.task-icon {
    font-size: 16px;
    line-height: 1;
}

.task-info {
    flex: 1;
    min-width: 0;
}

.task-name {
    font-size: 14px;
    color: #1A1A1A;
}

.task-meta {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-right: 16px;
    flex-shrink: 0;
}

.task-meta .task-score {
    font-size: 13px;
    color: #52C41A;
    font-weight: 500;
}

.task-meta .task-progress {
    font-size: 12px;
    color: #999999;
}

.task-btn {
    padding: 4px 16px;
    border: 1px solid #1A73E8;
    border-radius: 14px;
    background: transparent;
    color: #1A73E8;
    font-size: 13px;
    cursor: pointer;
    white-space: nowrap;
    flex-shrink: 0;
    transition: all 0.2s;

    &:hover:not(:disabled) {
        background: #E8F0FE;
    }

    &.completed,
    &:disabled {
        border-color: #D9D9D9;
        color: #999999;
        cursor: default;
        background: transparent;
    }
}

// ===== 等级规则 =====
.level-rules {
    background: #fff;
    border-radius: 16px;
    padding: 20px;
    margin-bottom: 16px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
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

// ===== 底部信息 =====
.growth-footer {
    text-align: center;
    padding: 24px 0;
    font-size: 12px;
    color: #B0B0B0;
}

.footer-sep {
    margin: 0 8px;
    color: #D9D9D9;
}

// ===== 响应式 =====
@media screen and (max-width: 1199px) {
    .growth-content {
        padding: 16px;
    }
}

@media screen and (max-width: 768px) {
    .growth-content {
        padding: 12px;
    }

    .summary-card {
        flex-direction: column;
        padding: 16px;
        gap: 16px;
    }

    .summary-right {
        justify-content: center;
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
        padding: 12px 0;
        flex-wrap: wrap;
    }

    .task-meta {
        margin-right: 8px;
    }

    .task-btn {
        padding: 4px 12px;
        font-size: 12px;
    }

    .milestone-label {
        font-size: 10px;
    }

    .milestone-score {
        display: none;
    }

    .func-item {
        width: 64px;
        height: 64px;
    }

    .func-icon {
        font-size: 20px;
    }

    .func-text {
        font-size: 11px;
    }
}
</style>