<template>
    <div class="checkin-modal-mask" v-if="visible" @click.self="handleClose">
        <div class="checkin-modal">
            <div class="modal-close" @click="handleClose">&#10005;</div>

            <div class="modal-header" v-if="mode === 'success'">
                <div class="reward-title">签到成功 <span class="ore-num">+{{ earnedOre }}</span> 矿石</div>
                <div class="reward-sub">矿石可用来抽奖或兑换礼物~</div>
            </div>

            <div class="modal-header" v-else>
                <div class="reward-title">每日签到</div>
                <div class="reward-sub">坚持连续签到可获得超额矿石奖励</div>
            </div>

            <div class="progress-section">
                <div class="progress-bar-wrap">
                    <div
                        class="progress-bar-fill"
                        :class="progressBarStateClass"
                        :style="{ width: progressPercent + '%' }"
                    ></div>
                </div>

                <div class="milestone-top-row">
                    <div
                        v-for="(ms, idx) in topRowMilestones"
                        :key="'top-' + ms.day"
                        class="milestone-item top-item"
                        :class="ms.stateClass"
                    >
                        <div
                            class="milestone-dot"
                            :class="{
                                'has-line': idx < topRowMilestones.length - 1,
                                'line-completed': ms.lineState === 'completed',
                                'line-active': ms.lineState === 'active'
                            }"
                        >
                            <span v-if="ms.state === 'achieved'" class="check-icon">&#10003;</span>
                        </div>
                        <div class="milestone-info">
                            <div class="milestone-day">{{ ms.day }}天</div>
                            <div class="milestone-ore">+{{ ms.ore }}</div>
                        </div>
                    </div>
                </div>

                <div
                    class="bottom-row-connector"
                    :class="connectorStateClass"
                    aria-hidden="true"
                ></div>

                <div class="milestone-bottom-row">
                    <template v-for="(ms, idx) in bottomRowMilestones">
                        <div
                            :key="'bot-' + ms.day"
                            class="milestone-item bottom-item"
                            :class="ms.stateClass"
                        >
                            <div
                                class="milestone-dot"
                                :class="{
                                    'has-line': idx < bottomRowMilestones.length - 1,
                                    'line-completed': ms.lineState === 'completed',
                                    'line-active': ms.lineState === 'active'
                                }"
                            >
                                <span v-if="ms.state === 'achieved'" class="check-icon">&#10003;</span>
                            </div>
                            <div class="milestone-info">
                                <div class="milestone-day">{{ ms.day }}天</div>
                                <div class="milestone-ore">+{{ ms.ore }}</div>
                            </div>
                        </div>
                        <div
                            v-if="idx < bottomRowMilestones.length - 1"
                            :key="'ellipsis-' + idx"
                            class="ellipsis-indicator"
                        >
                            <span></span><span></span><span></span>
                        </div>
                    </template>
                </div>
            </div>

            <div class="next-reward-info" v-if="computedNextSpecial && mode === 'entry'">
                <span class="next-label">下一个奖励：</span>
                <span class="next-day">第{{ computedNextSpecial.day }}天</span>
                <span class="next-ore">+{{ computedNextSpecial.ore }}矿石</span>
                <span class="next-days-left">再签{{ computedNextSpecial.daysLeft }}天可领取</span>
            </div>

            <div class="modal-actions">
                <button
                    class="btn btn-primary"
                    @click="handleCheckin"
                    v-if="mode === 'entry' && !isSigned"
                >
                    立即签到
                </button>
                <button
                    class="btn btn-primary"
                    @click="handleGoLottery"
                    v-if="mode === 'success'"
                >
                    去抽奖
                </button>
                <button
                    class="btn btn-secondary"
                    @click="handleClose"
                >
                    {{ mode === 'success' ? '关闭' : '签到日历' }}
                </button>
            </div>
        </div>
    </div>
</template>

<script>
import { doSignCheckin } from '@/apis/checkin'
import { toast } from '@/utils/toast'

const TOP_ROW_MILESTONES = [
    { day: 1, ore: 100 },
    { day: 2, ore: 150 },
    { day: 3, ore: 512 },
    { day: 4, ore: 250 },
    { day: 5, ore: 300 },
    { day: 6, ore: 350 },
    { day: 7, ore: 1024 }
]

const BOTTOM_ROW_MILESTONES = [
    { day: 7, ore: 1024 },
    { day: 14, ore: 2048 },
    { day: 21, ore: 4096 },
    { day: 30, ore: 5120 }
]

const KEY_NODES = [
    { day: 7, ore: 1024 },
    { day: 14, ore: 2048 },
    { day: 21, ore: 4096 },
    { day: 30, ore: 5120 }
]

function buildMilestone(cfg, currentDay, nextDay) {
    const achieved = currentDay >= cfg.day
    const isCurrent = currentDay === cfg.day
    let state = 'pending'
    if (achieved) state = 'achieved'
    if (isCurrent) state = 'current'

    let lineState = null
    if (nextDay !== undefined) {
        if (currentDay >= nextDay) {
            lineState = 'completed'
        } else if (achieved) {
            lineState = 'active'
        } else {
            lineState = 'pending'
        }
    }

    return {
        day: cfg.day,
        ore: cfg.ore,
        state: state,
        stateClass: {
            'achieved': state === 'achieved',
            'current': state === 'current',
            'pending': state === 'pending'
        },
        lineState: lineState
    }
}

export default {
    name: 'CheckinProgressModal',
    props: {
        visible: {
            type: Boolean,
            default: false
        },
        mode: {
            type: String,
            default: 'entry',
            validator: function(v) {
                return ['entry', 'success'].indexOf(v) !== -1
            }
        },
        earnedOre: {
            type: Number,
            default: 0
        },
        milestoneProgress: {
            type: Object,
            default: function() {
                return { current: 0, total: 30, percent: 0, specialDays: [] }
            }
        },
        nextSpecial: {
            type: Object,
            default: null
        },
        isSigned: {
            type: Boolean,
            default: false
        }
    },
    computed: {
        progressPercent() {
            return this.milestoneProgress.percent || 0
        },
        progressBarStateClass() {
            const current = this.milestoneProgress.current || 0
            if (current <= 0) return 'state-pending'
            if (current >= 30) return 'state-completed'
            return 'state-active'
        },
        connectorStateClass() {
            const current = this.milestoneProgress.current || 0
            if (current >= 7) return 'connector-completed'
            if (current >= 1) return 'connector-active'
            return 'connector-pending'
        },
        topRowMilestones() {
            const current = this.milestoneProgress.current || 0
            return TOP_ROW_MILESTONES.map((cfg, idx) => {
                const nextCfg = idx < TOP_ROW_MILESTONES.length - 1
                    ? TOP_ROW_MILESTONES[idx + 1]
                    : undefined
                return buildMilestone(cfg, current, nextCfg ? nextCfg.day : undefined)
            })
        },
        bottomRowMilestones() {
            const current = this.milestoneProgress.current || 0
            return BOTTOM_ROW_MILESTONES.map((cfg, idx) => {
                const nextCfg = idx < BOTTOM_ROW_MILESTONES.length - 1
                    ? BOTTOM_ROW_MILESTONES[idx + 1]
                    : undefined
                return buildMilestone(cfg, current, nextCfg ? nextCfg.day : undefined)
            })
        },
        computedNextSpecial() {
            if (this.nextSpecial) {
                return this.nextSpecial
            }
            const current = this.milestoneProgress.current || 0
            for (const node of KEY_NODES) {
                if (current < node.day) {
                    return {
                        day: node.day,
                        ore: node.ore,
                        daysLeft: node.day - current
                    }
                }
            }
            return null
        }
    },
    methods: {
        handleClose() {
            this.$emit('close')
        },
        async handleCheckin() {
            try {
                const res = await doSignCheckin()
                if (res && res.code === 200 && res.data) {
                    this.$emit('checkin-success', res.data)
                } else {
                    toast(res && res.message ? res.message : '签到失败', 2)
                }
            } catch (e) {
                toast('签到失败，请重试', 2)
            }
        },
        handleGoLottery() {
            this.$emit('go-lottery')
            this.$router.push('/user/center/lottery')
        }
    }
}
</script>

<style lang="less" scoped>
.checkin-modal-mask {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
    animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
    from { opacity: 0; }
    to { opacity: 1; }
}

.checkin-modal {
    width: 460px;
    max-height: 85vh;
    overflow-y: auto;
    background: #fff;
    border-radius: 16px;
    padding: 32px 28px 24px;
    position: relative;
    animation: slideUp 0.3s ease;
}

@keyframes slideUp {
    from { transform: translateY(20px); opacity: 0; }
    to { transform: translateY(0); opacity: 1; }
}

.modal-close {
    position: absolute;
    top: 16px;
    right: 20px;
    width: 28px;
    height: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #999;
    font-size: 18px;
    border-radius: 50%;
    transition: all 0.2s;

    &:hover {
        background: #f5f5f5;
        color: #333;
    }
}

.modal-header {
    text-align: center;
    margin-bottom: 24px;
}

.reward-title {
    font-size: 22px;
    font-weight: 600;
    color: #1a1a1a;
    margin-bottom: 8px;
}

.ore-num {
    color: #fa8c16;
}

.reward-sub {
    font-size: 13px;
    color: #999;
}

.progress-section {
    margin-bottom: 20px;
}

.progress-bar-wrap {
    height: 6px;
    background: #f0f2f5;
    border-radius: 3px;
    overflow: hidden;
    margin-bottom: 28px;
}

.progress-bar-fill {
    height: 100%;
    border-radius: 3px;
    transition: width 0.5s ease, background 0.5s ease;
    background: #e8e8e8;

    &.state-active {
        background: linear-gradient(90deg, #52c41a, #fa8c16);
    }

    &.state-completed {
        background: #52c41a;
    }

    &.state-pending {
        background: #e8e8e8;
    }
}

.milestone-top-row {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding: 0 4px;
    position: relative;
}

.milestone-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    flex-shrink: 0;
    position: relative;
    z-index: 1;
}

.milestone-dot {
    width: 14px;
    height: 14px;
    border-radius: 50%;
    background: #e8e8e8;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    transition: all 0.3s ease;

    .check-icon {
        color: #fff;
        font-size: 8px;
        font-weight: bold;
        line-height: 1;
    }

    &.has-line::after {
        content: '';
        position: absolute;
        top: 50%;
        left: calc(100% + 2px);
        width: calc(100% - 4px);
        height: 2px;
        background: #e8e8e8;
        transform: translateY(-50%);
        transition: background 0.5s ease;
    }

    &.line-completed::after {
        background: #52c41a;
    }

    &.line-active::after {
        background: linear-gradient(90deg, #52c41a, #fa8c16);
    }
}

.milestone-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-top: 6px;
    gap: 2px;
}

.milestone-day {
    font-size: 11px;
    color: #999;
    white-space: nowrap;
    transition: color 0.3s ease;
}

.milestone-ore {
    font-size: 10px;
    color: #bbb;
    white-space: nowrap;
    transition: color 0.3s ease;
}

.milestone-item.achieved {
    .milestone-dot {
        background: #52c41a;
    }
    .milestone-day {
        color: #52c41a;
        font-weight: 600;
    }
    .milestone-ore {
        color: #52c41a;
    }
}

.milestone-item.current {
    .milestone-dot {
        background: #fa8c16;
        box-shadow: 0 0 0 4px rgba(250, 140, 22, 0.2);
        animation: glowPulse 2s ease-in-out infinite;
    }
    .milestone-day {
        color: #fa8c16;
        font-weight: 600;
    }
    .milestone-ore {
        color: #fa8c16;
        font-weight: 600;
    }
}

@keyframes glowPulse {
    0%, 100% {
        box-shadow: 0 0 0 4px rgba(250, 140, 22, 0.2);
    }
    50% {
        box-shadow: 0 0 0 7px rgba(250, 140, 22, 0.35);
    }
}

.milestone-item.pending {
    .milestone-dot {
        background: #e8e8e8;
    }
}

.bottom-row-connector {
    width: 2px;
    height: 16px;
    background: #e8e8e8;
    margin: 6px auto 0;
    border-radius: 1px;
    transition: background 0.5s ease;

    &.connector-active {
        background: linear-gradient(180deg, #52c41a, #fa8c16);
    }

    &.connector-completed {
        background: #52c41a;
    }
}

.milestone-bottom-row {
    display: flex;
    justify-content: flex-start;
    align-items: flex-start;
    padding: 0 4px;
    margin-top: 4px;
    position: relative;
}

.ellipsis-indicator {
    display: flex;
    align-items: center;
    gap: 3px;
    padding: 0 4px;
    height: 14px;
    margin-top: 0;

    span {
        width: 4px;
        height: 4px;
        border-radius: 50%;
        background: #ccc;
        display: block;
    }
}

.bottom-item {
    margin: 0 2px;
}

.next-reward-info {
    background: linear-gradient(135deg, #fff7e6, #ffe7ba);
    border-radius: 10px;
    padding: 12px 16px;
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 20px;
    font-size: 13px;
}

.next-label {
    color: #666;
}

.next-day {
    color: #fa8c16;
    font-weight: 500;
}

.next-ore {
    color: #fa8c16;
    font-weight: 600;
}

.next-days-left {
    color: #999;
    margin-left: auto;
}

.modal-actions {
    display: flex;
    gap: 12px;
}

.btn {
    flex: 1;
    height: 44px;
    border-radius: 22px;
    font-size: 15px;
    font-weight: 500;
    cursor: pointer;
    border: none;
    transition: all 0.2s;
}

.btn-primary {
    background: linear-gradient(135deg, #1e80ff, #4096ff);
    color: #fff;

    &:hover {
        background: linear-gradient(135deg, #4096ff, #69b1ff);
        transform: translateY(-1px);
    }

    &:active {
        transform: translateY(0);
    }
}

.btn-secondary {
    background: #f5f7fa;
    color: #666;

    &:hover {
        background: #e8e8e8;
    }
}

@media screen and (max-width: 480px) {
    .checkin-modal {
        width: calc(100% - 32px);
        padding: 24px 16px 20px;
    }

    .milestone-top-row {
        padding: 0;
    }

    .milestone-dot {
        width: 12px;
        height: 12px;
    }

    .milestone-day {
        font-size: 10px;
    }

    .milestone-ore {
        font-size: 9px;
    }

    .ellipsis-indicator {
        span {
            width: 3px;
            height: 3px;
        }
    }
}
</style>
