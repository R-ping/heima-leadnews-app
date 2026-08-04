<template>
    <div class="checkin-page">
        <div class="art-top" v-if="!isDesktop"><HomeBar/></div>

        <div class="checkin-content">
            <!-- User Info Bar -->
            <div class="user-info-bar">
                <div class="user-info-section">
                    <span class="user-nickname">{{ dashboard.user.name || '用户' }}</span>
                    <span class="user-level-badge">{{ dashboard.user.level || 'JY.1' }}</span>
                </div>
                <span class="info-separator">|</span>
                <div class="user-info-section">
                    <span class="info-value">{{ dashboard.stats.consecutive || 0 }}</span>
                    <span class="info-label">连续签到天数</span>
                </div>
                <span class="info-separator">|</span>
                <div class="user-info-section">
                    <span class="info-value">{{ dashboard.stats.total || 0 }}</span>
                    <span class="info-label">累计签到天数</span>
                </div>
                <span class="info-separator">|</span>
                <div class="user-info-section ore-section">
                    <span class="info-value ore-value">{{ dashboard.stats.ore || 0 }}</span>
                    <span class="ore-icon">&#xf06d;</span>
                    <span class="ore-question">&#xf059;</span>
                </div>
            </div>

            <!-- Sub Tabs -->
            <div class="sub-tabs">
                <div
                    v-for="tab in subTabs"
                    :key="tab.key"
                    class="tab-item"
                    :class="{ active: activeTab === tab.key }"
                    @click="switchTab(tab)"
                >
                    {{ tab.label }}
                </div>
            </div>

            <!-- Calendar Section -->
            <div class="calendar-section">
                <div class="calendar-left">
                    <div class="encourage-text">
                        <div class="encourage-line">今日签到</div>
                        <div class="encourage-line">领取矿石</div>
                        <div class="encourage-line">加速升级</div>
                    </div>
                </div>

                <div class="calendar-center">
                    <div class="calendar-grid">
                        <div class="calendar-header-row">
                            <div
                                v-for="day in weekDays"
                                :key="day"
                                class="calendar-header-cell"
                            >
                                {{ day }}
                            </div>
                        </div>
                        <div class="calendar-body">
                            <div
                                v-for="(cell, index) in calendarCells"
                                :key="index"
                                class="calendar-cell"
                                :class="cellClass(cell)"
                                @click="handleCellClick(cell)"
                            >
                                <template v-if="!cell.isEmpty">
                                    <span class="cell-date">{{ cell.day }}</span>
                                    <span v-if="cell.status === 'SIGNED' && cell.reward" class="cell-reward">
                                        +{{ cell.reward }}
                                    </span>
                                    <span v-if="cell.status === 'MISSED'" class="cell-missed-label">补签</span>
                                    <span v-if="cell.extraLabel" class="cell-extra-label">{{ cell.extraLabel }}</span>
                                </template>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="calendar-right">
                    <div class="month-nav">
                        <span class="month-arrow" @click="prevMonth">&#xf104;</span>
                        <span class="month-label">{{ currentYear }}年{{ currentMonth }}月</span>
                        <span class="month-arrow" @click="nextMonth">&#xf105;</span>
                    </div>
                    <div class="today-btn" @click="goToday">今天</div>
                    <div class="patch-card-info">
                        <span class="patch-label">补签卡</span>
                        <span class="patch-count">{{ dashboard.cards.retroactive || 0 }}张</span>
                    </div>
                    <div class="qr-code-area">
                        <div class="qr-placeholder">&#xf029;</div>
                        <div class="qr-text">扫描右侧二维码</div>
                        <div class="qr-text">分享给好友</div>
                    </div>
                </div>
            </div>

            <!-- Bottom Sections -->
            <div class="bottom-sections">
                <div class="bottom-section section-notice">
                    <div class="section-title">掘金公告</div>
                    <div class="notice-list">
                        <div class="notice-item">
                            <span class="notice-name">公益计划</span>
                            <span class="notice-link">了解详情 &#xf105;</span>
                        </div>
                        <div class="notice-item">
                            <span class="notice-name">游戏入口调整</span>
                            <span class="notice-link">了解详情 &#xf105;</span>
                        </div>
                        <div class="notice-item">
                            <span class="notice-link">捐赠详情 &#xf105;</span>
                        </div>
                    </div>
                </div>
                <div class="bottom-section section-exchange">
                    <div class="section-title">点石成金</div>
                    <div class="exchange-content">
                        <div class="exchange-desc">用矿石兑换公益基金</div>
                        <div class="exchange-btn">去兑换</div>
                    </div>
                </div>
            </div>

            <!-- My Tasks Section -->
            <div class="tasks-section">
                <div class="tasks-header">
                    <span class="tasks-title">我的任务</span>
                    <span class="tasks-question-icon">&#xf059;</span>
                </div>
                <div class="tasks-list">
                    <div
                        v-for="task in dashboard.tasks"
                        :key="task.id"
                        class="task-item"
                    >
                        <div class="task-info">
                            <div class="task-name">{{ task.name }}</div>
                            <div class="task-desc">{{ task.description }}</div>
                        </div>
                        <div class="task-reward">
                            +{{ task.reward }} 矿石
                        </div>
                        <div
                            class="task-status"
                            :class="{ completed: task.status === 'completed' }"
                        >
                            {{ task.status === 'completed' ? '已完成' : '去完成' }}
                        </div>
                    </div>
                </div>
            </div>

            <!-- Footer -->
            <div class="checkin-footer">
                用户协议 · 法律声明 &copy;{{ new Date().getFullYear() }} 稀土掘金
            </div>
        </div>
    </div>
</template>

<script>
import HomeBar from '@/components/bars/home_bar'
import Utils from '@/utils/env'
import { getDashboard, doCheckIn, doRetroactive, getCheckInRecords } from '@/apis/checkin'
import { toast } from '@/utils/toast'

export default {
    name: 'UserCheckin',
    components: { HomeBar },
    data() {
        return {
            activeTab: 'checkin',
            subTabs: [
                { key: 'checkin', label: '每日签到' },
                { key: 'growth', label: '成长等级' },
                { key: 'lottery', label: '幸运抽奖' },
                { key: 'exchange', label: '福利兑换' },
                { key: 'harvest', label: '我的收获' }
            ],
            weekDays: ['日', '一', '二', '三', '四', '五', '六'],
            currentYear: new Date().getFullYear(),
            currentMonth: new Date().getMonth() + 1,
            todayYear: new Date().getFullYear(),
            todayMonth: new Date().getMonth() + 1,
            today: new Date().getDate(),
            dashboard: {
                user: { name: '', level: '', userId: 0 },
                todayStatus: 'NORMAL',
                stats: { consecutive: 0, total: 0, ore: 0 },
                cards: { retroactive: 0 },
                calendar: [],
                tasks: []
            }
        }
    },
    computed: {
        isDesktop() {
            return Utils.isDesktop()
        },
        calendarCells() {
            const cells = []
            const firstDay = new Date(this.currentYear, this.currentMonth - 1, 1).getDay()
            const totalDays = new Date(this.currentYear, this.currentMonth, 0).getDate()

            // 填充前置空白格
            for (let i = 0; i < firstDay; i++) {
                cells.push({ isEmpty: true })
            }

            // 填充日期格
            for (let d = 1; d <= totalDays; d++) {
                const dateStr = this.formatDateStr(d)
                const match = this.dashboard.calendar.find(
                    item => item.date === dateStr
                )

                cells.push({
                    isEmpty: false,
                    day: d,
                    date: dateStr,
                    status: match ? match.status : this.getDefaultStatus(d),
                    reward: match ? match.reward : null,
                    extraLabel: match ? match.extra_label : null
                })
            }

            return cells
        }
    },
    mounted() {
        this.loadDashboard()
    },
    methods: {
        goBack() {
            this.$router.back()
        },
        switchTab(tab) {
            if (tab.key === 'checkin') {
                this.activeTab = tab.key
                return
            }
            if (tab.key === 'growth') {
                this.$router.push('/user/growth')
                return
            }
            if (tab.key === 'lottery') {
                this.$router.push('/user/lottery')
                return
            }
            if (tab.key === 'exchange') {
                this.$router.push('/user/welfare')
                return
            }
            if (tab.key === 'harvest') {
                toast('我的收获功能开发中', 2)
                return
            }
        },
        getDefaultStatus(d) {
            const isCurrentMonth =
                this.currentYear === this.todayYear &&
                this.currentMonth === this.todayMonth
            if (isCurrentMonth && d === this.today) {
                return 'NORMAL'
            }
            const cellDate = new Date(this.currentYear, this.currentMonth - 1, d)
            const todayDate = new Date(this.todayYear, this.todayMonth - 1, this.today)
            if (cellDate > todayDate) {
                return 'FUTURE'
            }
            return 'MISSED'
        },
        cellClass(cell) {
            if (cell.isEmpty) return 'is-empty'
            const map = {
                SIGNED: 'is-signed',
                MISSED: 'is-missed',
                NORMAL: 'is-today',
                FUTURE: 'is-future'
            }
            return map[cell.status] || ''
        },
        async loadDashboard() {
            try {
                const res = await getDashboard()
                if (res && res.code === 200 && res.data) {
                    const data = res.data
                    this.dashboard.user = {
                        name: data.userInfo && data.userInfo.nickname ? data.userInfo.nickname : '用户',
                        level: data.userInfo && data.userInfo.level ? data.userInfo.level : 'JY.1'
                    }
                    const stats = data.checkinStats || {}
                    this.dashboard.todayStatus = stats.todaySigned ? 'SIGNED' : 'NORMAL'
                    this.dashboard.stats = {
                        consecutive: stats.continuousDays != null ? stats.continuousDays : 0,
                        total: stats.totalDays != null ? stats.totalDays : 0,
                        ore: stats.oreBalance != null ? stats.oreBalance : 0
                    }
                    this.dashboard.cards = {
                        retroactive: data.patchCardCount != null ? data.patchCardCount : 0
                    }
                    // 标准化日历数据
                    if (data.calendar) {
                        this.dashboard.calendar = data.calendar.map(item => {
                            const statusMap = {
                                'signed': 'SIGNED',
                                'repaired': 'SIGNED',
                                'miss': 'MISSED',
                                'today': 'NORMAL',
                                'future': 'FUTURE'
                            }
                            return {
                                date: item.date,
                                status: statusMap[item.status] || 'FUTURE',
                                reward: item.oreReward || null,
                                extra_label: item.isSpecial ? '特殊' : null
                            }
                        })
                    } else {
                        this.dashboard.calendar = []
                    }
                    this.dashboard.tasks = data.tasks || []
                }
            } catch (error) {
                toast('加载数据失败，请稍后重试', 2)
            }
        },
        async loadRecords() {
            // 重新加载仪表盘数据以获取日历（后端已包含近2个月数据）
            await this.loadDashboard()
        },
        async handleCellClick(cell) {
            if (cell.isEmpty) return
            if (cell.status === 'FUTURE') return

            if (cell.status === 'SIGNED') {
                toast('该日期已签到', 2)
                return
            }

            if (cell.status === 'MISSED') {
                if (this.dashboard.cards.retroactive <= 0) {
                    toast('没有补签卡了', 2)
                    return
                }
                try {
                    const res = await doRetroactive(cell.date)
                    if (res && res.code === 200) {
                        toast('补签成功', 2)
                        await this.loadDashboard()
                    } else {
                        toast(res && res.message ? res.message : '补签失败', 2)
                    }
                } catch (error) {
                    toast('补签失败，请稍后重试', 2)
                }
                return
            }

            if (cell.status === 'NORMAL') {
                try {
                    const res = await doCheckIn()
                    if (res && res.code === 200) {
                        const reward = res.data && res.data.reward ? res.data.reward : 10
                        toast('签到成功！+' + reward + ' 矿石', 2)
                        await this.loadDashboard()
                    } else {
                        toast(res && res.message ? res.message : '签到失败', 2)
                    }
                } catch (error) {
                    toast('签到失败，请稍后重试', 2)
                }
            }
        },
        prevMonth() {
            if (this.currentMonth === 1) {
                this.currentYear -= 1
                this.currentMonth = 12
            } else {
                this.currentMonth -= 1
            }
            this.loadRecords()
        },
        nextMonth() {
            if (this.currentMonth === 12) {
                this.currentYear += 1
                this.currentMonth = 1
            } else {
                this.currentMonth += 1
            }
            this.loadRecords()
        },
        goToday() {
            this.currentYear = this.todayYear
            this.currentMonth = this.todayMonth
            this.loadRecords()
        },
        formatDateStr(day) {
            const y = this.currentYear
            const m = String(this.currentMonth).padStart(2, '0')
            const d = String(day).padStart(2, '0')
            return y + '-' + m + '-' + d
        }
    }
}
</script>

<style lang="less" scoped>
@import '../../../styles/common';

.checkin-page {
    min-height: 100vh;
    background: #f5f7fa;
}

.checkin-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 24px 24px;
}

// User Info Bar
.user-info-bar {
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fff;
    border-radius: 0 0 12px 12px;
    padding: 16px 24px;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    flex-wrap: wrap;
    gap: 12px;
}

.user-info-section {
    display: flex;
    align-items: center;
    gap: 6px;
}

.user-nickname {
    font-size: 15px;
    color: #1a1a1a;
    font-weight: 500;
}

.user-level-badge {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 10px;
    background: linear-gradient(135deg, #fa8c16, #ffc069);
    color: #fff;
    font-size: 12px;
    font-weight: 600;
    line-height: 1.5;
}

.info-separator {
    color: #e8e8e8;
    font-size: 14px;
}

.info-value {
    font-size: 18px;
    font-weight: 700;
    color: #fa8c16;
}

.info-label {
    font-size: 13px;
    color: #999;
}

.ore-section {
    .ore-value {
        font-size: 18px;
    }

    .ore-icon {
        font-family: fontawesome;
        font-size: 16px;
        color: #fa8c16;
    }

    .ore-question {
        font-family: fontawesome;
        font-size: 14px;
        color: #ccc;
        cursor: pointer;
        margin-left: 2px;
    }
}

// Sub Tabs
.sub-tabs {
    display: flex;
    background: #fff;
    border-radius: 12px;
    padding: 0 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    margin-bottom: 16px;
}

.tab-item {
    padding: 16px 20px;
    font-size: 15px;
    color: #666;
    cursor: pointer;
    position: relative;
    transition: color 0.2s;

    &:hover {
        color: #1e80ff;
    }

    &.active {
        color: #1e80ff;
        font-weight: 600;

        &::after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 50%;
            transform: translateX(-50%);
            width: 28px;
            height: 3px;
            background: #1e80ff;
            border-radius: 2px;
        }
    }
}

// Calendar Section
.calendar-section {
    display: flex;
    gap: 16px;
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    margin-bottom: 16px;
}

.calendar-left {
    width: 120px;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
}

.encourage-text {
    background: linear-gradient(135deg, #1e80ff, #69b1ff);
    border-radius: 12px;
    padding: 20px 16px;
    text-align: center;
}

.encourage-line {
    font-size: 15px;
    color: #fff;
    font-weight: 500;
    line-height: 1.8;
}

.calendar-center {
    flex: 1;
    min-width: 0;
}

.calendar-grid {
    width: 100%;
}

.calendar-header-row {
    display: flex;
    border-bottom: 1px solid #f0f2f5;
    padding-bottom: 8px;
    margin-bottom: 8px;
}

.calendar-header-cell {
    flex: 1;
    text-align: center;
    font-size: 13px;
    color: #999;
    height: 32px;
    line-height: 32px;
}

.calendar-body {
    display: flex;
    flex-wrap: wrap;
}

.calendar-cell {
    width: calc(100% / 7);
    aspect-ratio: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    cursor: pointer;
    transition: background-color 0.2s;
    position: relative;

    &.is-empty {
        cursor: default;
    }

    &.is-signed {
        background: #52c41a;

        .cell-date {
            color: #fff;
            font-weight: 600;
        }

        .cell-reward {
            color: #fff;
        }

        .cell-extra-label {
            color: rgba(255, 255, 255, 0.8);
        }
    }

    &.is-missed {
        background: #f5f5f5;

        .cell-date {
            color: #999;
        }

        .cell-missed-label {
            color: #1e80ff;
        }
    }

    &.is-today {
        background: #1e80ff;

        .cell-date {
            color: #fff;
            font-weight: 600;
        }

        .cell-extra-label {
            color: rgba(255, 255, 255, 0.8);
        }

        &:hover {
            background: #4096ff;
        }
    }

    &.is-future {
        cursor: default;

        .cell-date {
            color: #ccc;
        }
    }

    &:hover:not(.is-empty):not(.is-future):not(.is-missed) {
        opacity: 0.85;
    }

    &.is-missed:hover {
        background: #e8e8e8;
    }
}

.cell-date {
    font-size: 15px;
    color: #1a1a1a;
    line-height: 1;
}

.cell-reward {
    font-size: 11px;
    color: #52c41a;
    margin-top: 2px;
    font-weight: 500;
}

.cell-missed-label {
    font-size: 11px;
    color: #1e80ff;
    margin-top: 2px;
    font-weight: 500;
}

.cell-extra-label {
    font-size: 10px;
    margin-top: 1px;
}

// Calendar Right
.calendar-right {
    width: 120px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
    padding-top: 8px;
}

.month-nav {
    display: flex;
    align-items: center;
    gap: 8px;
}

.month-arrow {
    font-family: fontawesome;
    font-size: 18px;
    color: #666;
    cursor: pointer;
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 4px;

    &:hover {
        background: #f0f2f5;
        color: #1e80ff;
    }
}

.month-label {
    font-size: 14px;
    color: #1a1a1a;
    font-weight: 500;
    white-space: nowrap;
}

.today-btn {
    padding: 6px 20px;
    border: 1px solid #1e80ff;
    border-radius: 16px;
    color: #1e80ff;
    font-size: 13px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
        background: #1e80ff;
        color: #fff;
    }
}

.patch-card-info {
    text-align: center;
}

.patch-label {
    display: block;
    font-size: 12px;
    color: #999;
    margin-bottom: 4px;
}

.patch-count {
    font-size: 14px;
    color: #1e80ff;
    font-weight: 500;
}

.qr-code-area {
    text-align: center;
    padding: 8px;
    border: 1px dashed #e8e8e8;
    border-radius: 8px;
    width: 100%;
}

.qr-placeholder {
    font-family: fontawesome;
    font-size: 40px;
    color: #ccc;
    margin-bottom: 6px;
}

.qr-text {
    font-size: 11px;
    color: #999;
    line-height: 1.5;
}

// Bottom Sections
.bottom-sections {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
    margin-bottom: 16px;
}

.bottom-section {
    background: #fff;
    border-radius: 12px;
    padding: 20px 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-title {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
    margin-bottom: 16px;
}

.notice-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.notice-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
    border-bottom: 1px solid #f2f3f5;

    &:last-child {
        border-bottom: none;
    }
}

.notice-name {
    font-size: 14px;
    color: #1a1a1a;
}

.notice-link {
    font-size: 13px;
    color: #1e80ff;
    cursor: pointer;
    font-family: fontawesome;

    &:hover {
        opacity: 0.8;
    }
}

.exchange-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
    padding: 20px 0;
}

.exchange-desc {
    font-size: 14px;
    color: #666;
}

.exchange-btn {
    padding: 10px 32px;
    background: linear-gradient(135deg, #fa8c16, #ffc069);
    color: #fff;
    border-radius: 24px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: opacity 0.2s;

    &:hover {
        opacity: 0.85;
    }
}

// Tasks Section
.tasks-section {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    margin-bottom: 16px;
}

.tasks-header {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
}

.tasks-title {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
}

.tasks-question-icon {
    font-family: fontawesome;
    font-size: 16px;
    color: #ccc;
    margin-left: 8px;
    cursor: pointer;
}

.tasks-list {
    display: flex;
    flex-direction: column;
}

.task-item {
    display: flex;
    align-items: center;
    padding: 14px 0;
    border-bottom: 1px solid #f2f3f5;
    gap: 12px;

    &:last-child {
        border-bottom: none;
    }
}

.task-info {
    flex: 1;
}

.task-name {
    font-size: 15px;
    color: #1a1a1a;
    font-weight: 500;
    margin-bottom: 4px;
}

.task-desc {
    font-size: 13px;
    color: #999;
}

.task-reward {
    font-size: 13px;
    color: #52c41a;
    font-weight: 500;
    white-space: nowrap;
}

.task-status {
    padding: 6px 16px;
    border-radius: 16px;
    font-size: 13px;
    white-space: nowrap;
    border: 1px solid #1e80ff;
    color: #1e80ff;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
        background: rgba(30, 128, 255, 0.1);
    }

    &.completed {
        border-color: #ccc;
        color: #999;
        cursor: default;
        background: #f5f5f5;

        &:hover {
            background: #f5f5f5;
        }
    }
}

// Footer
.checkin-footer {
    text-align: center;
    font-size: 12px;
    color: #ccc;
    padding: 16px 0;
}

// Responsive
@media screen and (max-width: 768px) {
    .checkin-content {
        padding: 0 12px 12px;
    }

    .user-info-bar {
        padding: 12px 16px;
        gap: 8px;
    }

    .user-info-section {
        gap: 4px;
    }

    .info-value {
        font-size: 16px;
    }

    .info-label {
        font-size: 12px;
    }

    .sub-tabs {
        padding: 0 12px;
        overflow-x: auto;
    }

    .tab-item {
        padding: 14px 12px;
        font-size: 13px;
        white-space: nowrap;
    }

    .calendar-section {
        flex-direction: column;
        padding: 16px;
    }

    .calendar-left {
        width: 100%;
    }

    .encourage-text {
        display: flex;
        gap: 12px;
        padding: 12px 16px;
    }

    .encourage-line {
        font-size: 14px;
    }

    .calendar-right {
        width: 100%;
        flex-direction: row;
        justify-content: space-between;
        flex-wrap: wrap;
    }

    .calendar-cell {
        border-radius: 6px;
    }

    .cell-date {
        font-size: 13px;
    }

    .cell-reward,
    .cell-missed-label {
        font-size: 10px;
    }

    .bottom-sections {
        grid-template-columns: 1fr;
    }

    .task-item {
        flex-wrap: wrap;
        padding: 12px 0;
    }
}
</style>