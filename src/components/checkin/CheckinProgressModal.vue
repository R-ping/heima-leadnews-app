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
                    <div class="progress-bar" :style="{ width: progressPercent + '%' }"></div>
                </div>
                
                <div class="milestones">
                    <div 
                        v-for="ms in milestoneList" 
                        :key="ms.day" 
                        class="milestone"
                        :class="{ 
                            'achieved': ms.achieved, 
                            'current': ms.isCurrent,
                            'special': ms.day === 3 || ms.day === 7 || ms.day === 14 || ms.day === 21 || ms.day === 30
                        }"
                    >
                        <div class="milestone-dot"></div>
                        <div class="milestone-label">
                            <span class="milestone-day">{{ ms.day }}天</span>
                            <span class="milestone-ore" v-if="ms.ore">{{ ms.ore }}矿石</span>
                            <span class="milestone-status" v-if="ms.achieved && !ms.isCurrent">已完成</span>
                            <span class="milestone-status current-label" v-else-if="ms.isCurrent">当前</span>
                            <span class="milestone-status pending" v-else>{{ ms.ore }}矿石</span>
                        </div>
                    </div>
                </div>
            </div>

            <div class="next-reward-info" v-if="nextSpecial">
                <span class="next-label">下一个奖励：</span>
                <span class="next-day">第{{ nextSpecial.day }}天</span>
                <span class="next-ore">+{{ nextSpecial.ore }}矿石</span>
                <span class="next-days-left">再签{{ nextSpecial.daysLeft }}天可领取</span>
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
                    @click="handleGoCheckinPage"
                    v-if="mode === 'entry' || (mode === 'success' && !isSigned)"
                >
                    签到日历
                </button>
            </div>
        </div>
    </div>
</template>

<script>
import { doCheckIn } from '@/apis/checkin'
import { toast } from '@/utils/toast'

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
        milestoneList() {
            return this.milestoneProgress.specialDays || []
        }
    },
    methods: {
        handleClose() {
            this.$emit('close')
        },
        async handleCheckin() {
            try {
                const res = await doCheckIn()
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
            this.$router.push('/user/lottery')
        },
        handleGoCheckinPage() {
            this.$emit('go-checkin-page')
            this.$router.push('/user/checkin')
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
    width: 380px;
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
    height: 8px;
    background: #f0f2f5;
    border-radius: 4px;
    overflow: hidden;
    margin-bottom: 20px;
}

.progress-bar {
    height: 100%;
    background: linear-gradient(90deg, #52c41a, #fa8c16);
    border-radius: 4px;
    transition: width 0.5s ease;
}

.milestones {
    display: flex;
    justify-content: space-between;
    gap: 8px;
}

.milestone {
    display: flex;
    flex-direction: column;
    align-items: center;
    flex: 1;
    position: relative;
}

.milestone-dot {
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: #e8e8e8;
    margin-bottom: 8px;
    transition: all 0.3s;
    
    .achieved & {
        background: #52c41a;
    }
    
    .current & {
        background: #fa8c16;
        box-shadow: 0 0 0 4px rgba(250, 140, 22, 0.2);
    }
    
    .special & {
        width: 16px;
        height: 16px;
    }
}

.milestone-label {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
    text-align: center;
}

.milestone-day {
    font-size: 12px;
    color: #666;
    font-weight: 500;
}

.milestone-ore {
    font-size: 11px;
    color: #fa8c16;
}

.milestone-status {
    font-size: 11px;
    color: #999;
    
    &.current-label {
        color: #fa8c16;
        font-weight: 500;
    }
    
    &.pending {
        color: #fa8c16;
    }
}

.milestone.achieved {
    .milestone-day { color: #52c41a; }
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
</style>