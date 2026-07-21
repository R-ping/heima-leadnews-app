<template>
    <transition name="bind-fade">
        <div class="bind-overlay" :class="{ 'is-desktop': isDesktop }" v-if="visible" @click.self="close">
            <div class="bind-modal">
                <span class="close-btn" @click="close">&#10005;</span>
                <div class="bind-title">提示</div>
                <p class="bind-notice">
                    根据我国<em class="law-link">《网络安全法》</em>，您需要绑定手机号后才可在社区内发布内容。
                </p>
                <div class="bind-form">
                    <div class="input-row">
                        <div class="area-select">
                            <span class="area-code">+86</span>
                            <span class="area-arrow">&#x25BE;</span>
                        </div>
                        <input v-model="phone" type="tel" placeholder="请输入要绑定的手机号码" class="bind-input" maxlength="11" />
                    </div>
                    <div class="input-row code-row">
                        <input v-model="code" type="tel" placeholder="验证码" class="bind-input code-input" maxlength="6" />
                        <span class="code-btn" :class="{ disabled: countdown > 0 }" @click="getCode">
                            {{ countdown > 0 ? countdown + 's后重试' : '获取验证码' }}
                        </span>
                    </div>
                    <div class="bind-btn" @click="doBind">绑定手机</div>
                </div>
            </div>
        </div>
    </transition>
</template>

<script>
import Api from '@/apis/login/api'
import { toast } from "@/utils/toast"
import Utils from '@/utils/env'

export default {
    name: "SocialBindModal",
    data() {
        return {
            phone: '',
            code: '',
            countdown: 0,
            timer: null
        }
    },
    computed: {
        visible() {
            return this.$store.getters.showSocialBindModal
        },
        bindInfo() {
            return this.$store.getters.socialBindInfo
        },
        platform() {
            return (this.bindInfo && this.bindInfo.platform) || ''
        },
        platformUid() {
            return (this.bindInfo && this.bindInfo.platformUid) || ''
        },
        isDesktop() {
            return Utils.isDesktop()
        }
    },
    watch: {
        visible(val) {
            if (val) {
                this.phone = ''
                this.code = ''
                this.clearTimer()
            } else {
                this.clearTimer()
            }
        }
    },
    mounted() {
        this._resizeHandler = () => {
            this.$forceUpdate()
        }
        window.addEventListener('resize', this._resizeHandler)
    },
    beforeDestroy() {
        this.clearTimer()
        window.removeEventListener('resize', this._resizeHandler)
    },
    methods: {
        close() {
            this.$store.dispatch('hideSocialBind')
            this.$store.dispatch('clearSocialBindInfo')
        },
        getCode() {
            if (this.countdown > 0) return
            var phone = this.phone
            if (!phone || phone.length < 11) {
                toast('请输入正确的手机号', 3)
                return
            }
            var self = this
            Api.getCode(phone, this.platform, 'bind').then(function (d) {
                if (d && d.code === 200) {
                    toast('验证码已发送', 2)
                } else {
                    toast((d && d.errorMessage) || (d && d.message) || '获取验证码失败', 3)
                }
            }).catch(function () {
                toast('获取验证码失败', 3)
            })
            this.startCountdown()
        },
        startCountdown() {
            this.clearTimer()
            var self = this
            this.countdown = 60
            this.timer = setInterval(function () {
                self.countdown--
                if (self.countdown <= 0) {
                    self.clearTimer()
                }
            }, 1000)
        },
        clearTimer() {
            if (this.timer) {
                clearInterval(this.timer)
                this.timer = null
            }
            this.countdown = 0
        },
        doBind() {
            if (!this.phone || this.phone.length < 11) {
                toast('请输入正确的手机号', 3)
                return
            }
            if (!this.code) {
                toast('请输入验证码', 3)
                return
            }
            var self = this
            Api.socialBind({
                platform: this.platform,
                platformUid: this.platformUid,
                phone: this.phone,
                code: this.code
            }).then(function (d) {
                if (d && d.code === 200 && d.data) {
                    self.$store.dispatch('login', d.data)
                    self.$store.dispatch('clearSocialBindInfo')
                    self.$store.dispatch('hideSocialBind')
                    toast('绑定成功，已登录', 2)
                } else {
                    toast((d && d.errorMessage) || (d && d.message) || '绑定失败', 3)
                }
            }).catch(function () {
                toast('绑定失败，请重试', 3)
            })
        }
    }
}
</script>

<style lang="less" scoped>
.bind-fade-enter-active, .bind-fade-leave-active {
    transition: opacity 0.25s ease;
}
.bind-fade-enter, .bind-fade-leave-to {
    opacity: 0;
}

.bind-overlay {
    position: fixed;
    top: 0; left: 0; right: 0; bottom: 0;
    background-color: rgba(0, 0, 0, 0.55);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 10000;
    padding: 20px;
    box-sizing: border-box;
}

.bind-modal {
    position: relative;
    width: 100%;
    max-width: 480px;
    background-color: #ffffff;
    border-radius: 12px;
    padding: 36px 36px 32px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
    box-sizing: border-box;
}

.close-btn {
    position: absolute;
    top: 16px;
    right: 16px;
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    color: #999999;
    cursor: pointer;
    border-radius: 50%;
    transition: all 0.2s;
}
.close-btn:hover {
    background-color: #f5f5f5;
    color: #666666;
}

.bind-title {
    font-size: 26px;
    color: #333333;
    font-weight: 700;
    margin-bottom: 20px;
}

.bind-notice {
    font-size: 15px;
    color: #555555;
    line-height: 1.7;
    margin: 0 0 28px 0;
}
.law-link {
    color: #3194ff;
    font-style: normal;
}

.bind-form {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.input-row {
    display: flex;
    align-items: center;
    border: 1px solid #e0e0e0;
    border-radius: 6px;
    padding: 0 14px;
    height: 48px;
    box-sizing: border-box;
    transition: border-color 0.2s;
}
.input-row:focus-within {
    border-color: #3194ff;
}

.area-select {
    display: flex;
    align-items: center;
    gap: 6px;
    padding-right: 12px;
    border-right: 1px solid #e0e0e0;
    margin-right: 12px;
    flex-shrink: 0;
}
.area-code {
    font-size: 16px;
    color: #333333;
    font-weight: 500;
}
.area-arrow {
    font-size: 10px;
    color: #999999;
}

.bind-input {
    flex: 1;
    height: 100%;
    font-size: 15px;
    color: #333333;
    background-color: transparent;
    border: none;
    outline: none;
}
.bind-input::placeholder {
    color: #c0c4cc;
}

.code-row {
    padding-right: 8px;
}
.code-input {
    flex: 1;
}
.code-btn {
    font-size: 14px;
    color: #3194ff;
    cursor: pointer;
    white-space: nowrap;
    padding: 8px 12px;
    border-radius: 4px;
    transition: all 0.2s;
    font-weight: 500;
    flex-shrink: 0;
}
.code-btn:hover {
    background-color: #e8f4ff;
}
.code-btn.disabled {
    color: #c0c4cc;
    cursor: not-allowed;
}
.code-btn.disabled:hover {
    background-color: transparent;
}

.bind-btn {
    height: 48px;
    line-height: 48px;
    background-color: #3194ff;
    color: #ffffff;
    font-size: 17px;
    font-weight: 500;
    text-align: center;
    border-radius: 6px;
    margin-top: 8px;
    cursor: pointer;
    transition: background-color 0.2s;
}
.bind-btn:hover {
    background-color: #2684e8;
}
.bind-btn:active {
    background-color: #1a74d4;
}

/* ========== Web端适配：大写PX避免px2rem ========== */
.is-desktop.bind-overlay {
    padding: 20PX;
}
.is-desktop .bind-modal {
    width: 420PX;
    max-width: 90vw;
    padding: 36PX 36PX 32PX;
    border-radius: 12PX;
}
.is-desktop .close-btn {
    top: 14PX;
    right: 14PX;
    width: 30PX;
    height: 30PX;
    font-size: 18PX;
}
.is-desktop .bind-title {
    font-size: 22PX;
    margin-bottom: 16PX;
}
.is-desktop .bind-notice {
    font-size: 14PX;
    margin-bottom: 24PX;
}
.is-desktop .bind-form {
    gap: 14PX;
}
.is-desktop .input-row {
    height: 42PX;
    padding: 0 12PX;
    border-radius: 6PX;
}
.is-desktop .area-select {
    padding-right: 10PX;
    margin-right: 10PX;
}
.is-desktop .area-code {
    font-size: 14PX;
}
.is-desktop .area-arrow {
    font-size: 9PX;
}
.is-desktop .bind-input {
    font-size: 14PX;
}
.is-desktop .code-btn {
    font-size: 13PX;
    padding: 6PX 10PX;
}
.is-desktop .bind-btn {
    height: 42PX;
    line-height: 42PX;
    font-size: 15PX;
    border-radius: 6PX;
}

/* 移动端底部弹出 */
@media screen and (max-width: 560px) {
    .bind-overlay {
        padding: 0;
        align-items: flex-end;
    }
    .bind-modal {
        max-width: 100%;
        border-radius: 16px 16px 0 0;
        padding: 32px 24px 28px;
        animation: slideUp 0.3s ease;
    }
    @keyframes slideUp {
        from { transform: translateY(100%); }
        to { transform: translateY(0); }
    }
}
</style>
