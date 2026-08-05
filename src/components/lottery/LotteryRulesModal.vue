<template>
    <div class="rules-modal-mask" v-if="visible" @click.self="handleClose">
        <div class="rules-modal">
            <div class="modal-close" @click="handleClose">&#10005;</div>
            <div class="rules-title">抽奖规则</div>
            <div class="rules-content">
                <div class="rule-section">
                    <div class="rule-section-title">抽奖说明</div>
                    <ul class="rule-list">
                        <li>同一用户每人每天首次访问稀土掘金，完成每日签到后，即可免费获得 <strong>1次</strong> 抽奖机会。</li>
                        <li>免费抽奖次数用尽后，每抽奖1次需要消耗 <strong>200矿石</strong>；当您账户中矿石数量小于200时，将无法参与抽奖；当您账户矿石数量小于2000时，将无法使用十连抽功能。</li>
                        <li>幸运值积累到 <strong>6000</strong> 时，必出抽奖转盘中实物奖励；任何一次抽中实物奖励后，幸运值会重新积累（溢出值会加进下次初始值中）。</li>
                    </ul>
                </div>
                <div class="rule-section">
                    <div class="rule-section-title">奖品发放</div>
                    <ul class="rule-list">
                        <li>用户抽奖奖品为系统随机抽取。</li>
                        <li>抽奖活动奖品包括但不限于：任天堂（Nintendo）Switch 掌上游戏机、Pico Neo3等数码设备，马克杯等稀土掘金周边，各类会员卡，稀土掘金矿石以及稀土掘金bug彩蛋。其中，部分大奖需当日完成2-3次抽奖后才可解锁。具体奖品以抽奖活动页面展示为准。</li>
                        <li>用户可在稀土掘金网页端"个人头像-经验条/成长福利-再切换到我的收获"查看中奖奖品并填写中奖信息。</li>
                        <li>如抽中稀土掘金矿石奖励，奖励将直接发放至用户当前稀土掘金账户，您可通过"每日签到页面-当前矿石数"查看矿石余额。如抽中稀土掘金彩蛋道具，暂时无需填写收货地址，您可在"我的收获-我的道具页面"查看已获得的彩蛋。</li>
                        <li>如用户抽中任天堂掌上游戏机以及稀土掘金周边等实物奖品的，需线上填写收货地址，用于奖品邮寄。如您在收到平台获奖通知后 <strong>30个工作日</strong> 内未填写收货信息的，逾期将视为您放弃领取奖品。</li>
                    </ul>
                </div>
            </div>
            <div class="rules-actions">
                <button class="btn btn-primary" @click="handleClose">我知道了</button>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'LotteryRulesModal',
    props: {
        visible: { type: Boolean, default: false }
    },
    methods: {
        handleClose() {
            this.$emit('close')
        }
    }
}
</script>

<style lang="less" scoped>
.rules-modal-mask {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.6);
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

.rules-modal {
    width: 520px;
    max-height: 80vh;
    background: #fff;
    border-radius: 16px;
    padding: 32px 28px 24px;
    position: relative;
    animation: slideUp 0.3s ease;
    overflow-y: auto;
}

@keyframes slideUp {
    from { transform: translateY(30px); opacity: 0; }
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

.rules-title {
    font-size: 20px;
    font-weight: 700;
    color: #1a1a1a;
    text-align: center;
    margin-bottom: 24px;
}

.rules-content {
    margin-bottom: 24px;
}

.rule-section {
    margin-bottom: 20px;

    &:last-child {
        margin-bottom: 0;
    }
}

.rule-section-title {
    font-size: 15px;
    font-weight: 600;
    color: #fa8c16;
    margin-bottom: 12px;
    padding-left: 10px;
    border-left: 3px solid #fa8c16;
}

.rule-list {
    padding-left: 20px;
    margin: 0;

    li {
        font-size: 13px;
        color: #666;
        line-height: 1.8;
        margin-bottom: 8px;

        strong {
            color: #fa8c16;
        }
    }
}

.rules-actions {
    text-align: center;
}

.btn {
    width: 200px;
    height: 44px;
    border-radius: 22px;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    border: none;
    transition: all 0.2s;
}

.btn-primary {
    background: linear-gradient(135deg, #fa8c16, #ffc069);
    color: #fff;

    &:hover {
        opacity: 0.9;
        transform: translateY(-1px);
    }
}
</style>
