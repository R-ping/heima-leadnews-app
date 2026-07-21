<template>
    <transition name="login-fade">
        <div class="login-overlay" :class="{ 'is-desktop': isDesktop }" v-if="visible" @click.self="close">
            <div class="login-modal">
                <!-- 关闭按钮 -->
                <span class="close-btn" @click="close">&#10005;</span>

                <!-- 登录/注册 -->
                <div class="login-container">
                    <div class="modal-title">登录黑马头条</div>
                    <p class="modal-subtitle">
                        {{ isPasswordMode ? '手机号或邮箱登录' : '验证码登录' }}
                    </p>

                    <div class="form-wrapper">
                        <!-- 验证码登录 -->
                        <template v-if="!isPasswordMode">
                            <div class="input-group">
                                <span class="area-code">+86</span>
                                <input v-model="params.phone" type="tel" placeholder="请输入手机号" class="input" maxlength="11" />
                            </div>
                            <div class="input-group code-group">
                                <input v-model="params.code" type="tel" placeholder="请输入验证码" class="input" maxlength="6" />
                                <span class="code-btn" :class="{ disabled: codeCountdown > 0 }" @click="getCode">
                                    {{ codeCountdown > 0 ? codeCountdown + 's后重试' : '获取验证码' }}
                                </span>
                            </div>
                            <span class="login-btn" @click="loginByCode">登录/注册</span>
                        </template>

                        <!-- 密码登录（手机号/邮箱） -->
                        <template v-else>
                            <div class="input-group">
                                <input v-model="params.phoneOrEmail" type="text" placeholder="请输入手机号或邮箱" class="input" />
                            </div>
                            <div class="input-group">
                                <input v-model="params.password" type="password" placeholder="请输入密码" class="input" />
                            </div>
                            <span class="login-btn" @click="loginByPassword">登录</span>
                        </template>
                    </div>

                    <!-- 切换链接 -->
                    <div class="switch-row">
                        <template v-if="!isPasswordMode">
                            <span class="switch-link" @click="toggleMode">密码登录</span>
                            <span class="switch-link forget-link" @click="onForgetPassword">忘记密码?</span>
                        </template>
                        <template v-else>
                            <span class="switch-link" @click="toggleMode">验证码登录</span>
                            <span class="switch-link forget-link" @click="goRegister">注册新账号</span>
                        </template>
                    </div>

                    <!-- 分隔线 -->
                    <div class="divider">
                        <span class="divider-line"></span>
                        <span class="divider-text">其他登录方式</span>
                        <span class="divider-line"></span>
                    </div>

                    <!-- 社交登录 -->
                    <div class="social-login">
                        <div class="social-item" @click="weiboLogin" title="微博登录">
                            <span class="social-icon weibo-icon">&#xf18a;</span>
                            <span class="social-label">微博</span>
                        </div>
                        <div class="social-item" @click="githubLogin" title="GitHub登录">
                            <span class="social-icon github-icon">&#xf09b;</span>
                            <span class="social-label">GitHub</span>
                        </div>
                        <div class="social-item wechat-item" @click="toggleWechatQr" :class="{ active: showWechatQr }" title="微信公众号登录">
                            <span class="social-icon wechat-icon">&#xf1d7;</span>
                            <span class="social-label">微信</span>
                        </div>
                    </div>

                    <!-- 微信二维码 -->
                    <div class="wechat-qr-area" v-if="showWechatQr">
                        <div class="qr-box">
                            <img src="/static/images/gzh.jpeg" alt="微信公众号二维码" class="qr-img" />
                        </div>
                        <p class="qr-tip">请使用微信扫描二维码<br/>关注公众号后获取验证码登录</p>
                    </div>
                </div>

                <!-- 底部协议 -->
                <div class="agreement">
                    <span>注册登录即表示同意</span>
                    <span class="link">《用户协议》</span>
                    <span>和</span>
                    <span class="link">《隐私政策》</span>
                </div>
            </div>
        </div>
    </transition>
</template>

<script>
import Api from '@/apis/login/api'
import { toast } from "@/utils/toast"
import { getOAuthUrl } from '@/common/oauth'
import Utils from '@/utils/env'

export default {
    name: "LoginModal",
    data() {
        return {
            weiboIcon: '\uf18a',
            wechatIcon: '\uf1d7',
            githubIcon: '\uf09b',
            isPasswordMode: false,
            showWechatQr: false,
            codeCountdown: 0,
            codeTimer: null,
            params: {
                phone: '',
                phoneOrEmail: '',
                password: '',
                code: ''
            }
        }
    },
    computed: {
        visible() {
            return this.$store.getters.showLoginModal
        },
        isDesktop() {
            return Utils.isDesktop()
        }
    },
    watch: {
        visible: function (val) {
            if (val) {
                this.isPasswordMode = false
                this.showWechatQr = false
                this.resetParams()
            } else {
                this.showWechatQr = false
                this.clearCodeTimer()
            }
        }
    },
    beforeDestroy() {
        this.clearCodeTimer()
        if (this._resizeHandler) {
            window.removeEventListener('resize', this._resizeHandler)
        }
    },
    mounted() {
        var self = this
        this._resizeHandler = function () {
            self.$forceUpdate()
        }
        window.addEventListener('resize', this._resizeHandler)
    },
    methods: {
        close() {
            this.$store.dispatch('hideLogin')
            this.showWechatQr = false
        },
        resetParams() {
            this.params.phone = ''
            this.params.phoneOrEmail = ''
            this.params.password = ''
            this.params.code = ''
        },
        toggleMode() {
            this.isPasswordMode = !this.isPasswordMode
            this.showWechatQr = false
            this.resetParams()
        },
        goRegister() {
            this.isPasswordMode = false
            this.resetParams()
        },
        onForgetPassword() {
            toast('请使用验证码登录后在设置中重置密码', 3)
        },
        toggleWechatQr() {
            this.showWechatQr = !this.showWechatQr
        },

        getCode() {
            var phone = this.params.phone
            if (!phone || phone.length < 11) {
                toast('请输入正确的手机号', 3)
                return
            }
            var self = this
            Api.getCode(phone, 'app', 'login').then(function (d) {
                if (d && d.code === 200) {
                    toast('验证码已发送', 2)
                } else {
                    toast((d && d.errorMessage) || (d && d.message) || '获取验证码失败', 3)
                }
            }).catch(function () {
                toast('获取验证码失败', 3)
            })
            this.startCodeCountdown()
        },
        startCodeCountdown() {
            var self = this
            this.clearCodeTimer()
            this.codeCountdown = 60
            this.codeTimer = setInterval(function () {
                self.codeCountdown--
                if (self.codeCountdown <= 0) {
                    self.clearCodeTimer()
                }
            }, 1000)
        },
        clearCodeTimer() {
            if (this.codeTimer) {
                clearInterval(this.codeTimer)
                this.codeTimer = null
            }
            this.codeCountdown = 0
        },

        loginByCode() {
            var self = this
            var phone = this.params.phone
            if (!phone || phone.length < 11) {
                toast('请输入正确的手机号', 3)
                return
            }
            if (!this.params.code) {
                toast('请输入验证码', 3)
                return
            }
            Api.loginAuth({
                phoneOrEmail: phone,
                code: this.params.code,
                platform: 'app'
            }).then(function (d) {
                if (d && d.code === 200 && d.data) {
                    self.$store.dispatch('login', d.data)
                    self.close()
                    toast('登录成功', 2)
                } else {
                    toast((d && d.errorMessage) || (d && d.message) || '登录失败', 3)
                }
            }).catch(function () {
                toast('登录失败，请重试', 3)
            })
        },

        loginByPassword() {
            var self = this
            var account = this.params.phoneOrEmail
            var pwd = this.params.password
            if (!account) {
                toast('请输入手机号或邮箱', 3)
                return
            }
            if (!pwd) {
                toast('请输入密码', 3)
                return
            }
            var isPhone = /^1[3-9]\d{9}$/.test(account)
            var isEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(account)
            if (!isPhone && !isEmail) {
                toast('请输入正确的手机号或邮箱', 3)
                return
            }
            Api.loginAuth({
                phoneOrEmail: account,
                password: pwd,
                platform: 'app'
            }).then(function (d) {
                if (d && d.code === 200 && d.data) {
                    self.$store.dispatch('login', d.data)
                    self.close()
                    toast('登录成功', 2)
                } else {
                    toast((d && d.errorMessage) || (d && d.message) || '账号或密码错误', 3)
                }
            }).catch(function () {
                toast('登录失败，请重试', 3)
            })
        },

        weiboLogin() {
            window.location.href = getOAuthUrl('weibo')
        },
        githubLogin() {
            window.location.href = getOAuthUrl('github')
        }
    }
}
</script>

<style lang="less" scoped>
.login-fade-enter-active, .login-fade-leave-active {
    transition: opacity 0.25s ease;
}
.login-fade-enter, .login-fade-leave-to {
    opacity: 0;
}

.login-overlay {
    position: fixed;
    top: 0; left: 0; right: 0; bottom: 0;
    background-color: rgba(0, 0, 0, 0.55);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
    padding: 20px;
    box-sizing: border-box;
}

.login-modal {
    position: relative;
    width: 100%;
    max-width: 480px;
    background-color: #ffffff;
    border-radius: 12px;
    padding: 48px 48px 36px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
    box-sizing: border-box;
}

.close-btn {
    position: absolute;
    top: 20px;
    right: 20px;
    width: 36px;
    height: 36px;
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

.modal-title {
    font-size: 28px;
    color: #333333;
    font-weight: 600;
    text-align: center;
    margin-bottom: 8px;
}
.modal-subtitle {
    font-size: 16px;
    color: #999999;
    text-align: center;
    margin: 0 0 32px 0;
}

.form-wrapper {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.input-group {
    display: flex;
    align-items: center;
    background-color: #f7f8fa;
    border-radius: 8px;
    padding: 0 20px;
    border: 1px solid transparent;
    transition: all 0.2s;
    height: 56px;
    box-sizing: border-box;
}
.input-group:focus-within {
    border-color: #3194ff;
    background-color: #ffffff;
}
.area-code {
    font-size: 17px;
    color: #333333;
    padding-right: 16px;
    border-right: 1px solid #e0e0e0;
    margin-right: 16px;
    font-weight: 500;
}
.input {
    flex: 1;
    height: 100%;
    font-size: 16px;
    color: #333333;
    background-color: transparent;
    border: none;
    outline: none;
}
.input::placeholder {
    color: #c0c4cc;
}

.code-group {
    padding-right: 12px;
}
.code-btn {
    font-size: 15px;
    color: #3194ff;
    cursor: pointer;
    white-space: nowrap;
    padding: 8px 14px;
    border-radius: 4px;
    transition: all 0.2s;
    font-weight: 500;
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

.login-btn {
    height: 52px;
    line-height: 52px;
    background-color: #3194ff;
    color: #ffffff;
    font-size: 18px;
    font-weight: 500;
    text-align: center;
    border-radius: 8px;
    margin-top: 8px;
    cursor: pointer;
    transition: background-color 0.2s;
}
.login-btn:hover {
    background-color: #2684e8;
}
.login-btn:active {
    background-color: #1a74d4;
}

.switch-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 20px;
}
.switch-link {
    font-size: 15px;
    color: #3194ff;
    cursor: pointer;
    transition: color 0.2s;
}
.switch-link:hover {
    color: #1a7de8;
    text-decoration: underline;
}
.forget-link {
    color: #999999;
}
.forget-link:hover {
    color: #3194ff;
}

.divider {
    display: flex;
    align-items: center;
    margin: 36px 0 24px;
    gap: 16px;
}
.divider-line {
    flex: 1;
    height: 1px;
    background-color: #eeeeee;
}
.divider-text {
    font-size: 13px;
    color: #c0c4cc;
}

.social-login {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 40px;
}
.social-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    transition: transform 0.2s;
}
.social-item:hover {
    transform: translateY(-2px);
}
.social-icon {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    font-family: fontawesome;
    border-radius: 50%;
    transition: all 0.2s;
}
.weibo-icon {
    background-color: #fff3f3;
    color: #e6162d;
}
.social-item:hover .weibo-icon {
    background-color: #e6162d;
    color: #ffffff;
}
.github-icon {
    background-color: #f5f5f5;
    color: #333333;
}
.social-item:hover .github-icon {
    background-color: #333333;
    color: #ffffff;
}
.wechat-icon {
    background-color: #f0f9eb;
    color: #07c160;
}
.wechat-item.active .wechat-icon,
.social-item:hover .wechat-icon {
    background-color: #07c160;
    color: #ffffff;
}
.social-label {
    font-size: 13px;
    color: #999999;
}

.wechat-qr-area {
    margin-top: 28px;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 24px;
    background-color: #fafafa;
    border-radius: 8px;
}
.qr-box {
    width: 180px;
    height: 180px;
    background-color: #ffffff;
    border: 1px solid #eeeeee;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 10px;
    box-sizing: border-box;
}
.qr-img {
    width: 100%;
    height: 100%;
    object-fit: contain;
}
.qr-tip {
    font-size: 13px;
    color: #999999;
    text-align: center;
    margin: 16px 0 0 0;
    line-height: 1.6;
}

.agreement {
    margin-top: 28px;
    text-align: center;
    font-size: 12px;
    color: #c0c4cc;
    line-height: 1.6;
}
.agreement .link {
    color: #999999;
    cursor: pointer;
}
.agreement .link:hover {
    color: #3194ff;
}

/* ========== Web端适配：通过.is-desktop类控制，大写PX避免px2rem转换 ========== */
.is-desktop.login-overlay {
    padding: 20PX;
    align-items: center;
}
.is-desktop .login-modal {
    width: 440PX;
    max-width: 90vw;
    padding: 48PX 48PX 36PX;
    border-radius: 12PX;
    box-shadow: 0 20PX 60PX rgba(0, 0, 0, 0.15);
}
.is-desktop .close-btn {
    top: 16PX;
    right: 16PX;
    width: 32PX;
    height: 32PX;
    font-size: 18PX;
}
.is-desktop .modal-title {
    font-size: 24PX;
    margin-bottom: 8PX;
}
.is-desktop .modal-subtitle {
    font-size: 14PX;
    margin-bottom: 28PX;
}
.is-desktop .form-wrapper {
    gap: 16PX;
}
.is-desktop .input-group {
    height: 44PX;
    padding: 0 16PX;
    border-radius: 6PX;
}
.is-desktop .area-code {
    font-size: 15PX;
    padding-right: 12PX;
    margin-right: 12PX;
}
.is-desktop .input {
    font-size: 14PX;
}
.is-desktop .code-group {
    padding-right: 8PX;
}
.is-desktop .code-btn {
    font-size: 13PX;
    padding: 6PX 12PX;
}
.is-desktop .login-btn {
    height: 44PX;
    line-height: 44PX;
    font-size: 16PX;
    border-radius: 6PX;
    margin-top: 4PX;
}
.is-desktop .switch-row {
    margin-top: 16PX;
}
.is-desktop .switch-link {
    font-size: 13PX;
}
.is-desktop .divider {
    margin: 28PX 0 20PX;
    gap: 12PX;
}
.is-desktop .divider-text {
    font-size: 12PX;
}
.is-desktop .social-login {
    gap: 36PX;
}
.is-desktop .social-item {
    gap: 6PX;
}
.is-desktop .social-icon {
    width: 40PX;
    height: 40PX;
    font-size: 20PX;
}
.is-desktop .social-label {
    font-size: 12PX;
}
.is-desktop .wechat-qr-area {
    margin-top: 20PX;
    padding: 20PX;
    border-radius: 8PX;
}
.is-desktop .qr-box {
    width: 140PX;
    height: 140PX;
}
.is-desktop .qr-tip {
    font-size: 12PX;
    margin-top: 12PX;
}
.is-desktop .agreement {
    margin-top: 24PX;
    font-size: 12PX;
}

/* 移动端适配 */
@media screen and (max-width: 560px) {
    .login-overlay {
        padding: 0;
        align-items: flex-end;
    }
    .login-modal {
        max-width: 100%;
        border-radius: 16px 16px 0 0;
        padding: 36px 28px 30px;
        animation: slideUp 0.3s ease;
    }
    @keyframes slideUp {
        from { transform: translateY(100%); }
        to { transform: translateY(0); }
    }
    .close-btn {
        top: 16px;
        right: 16px;
    }
    .modal-title {
        font-size: 24px;
    }
    .social-login {
        gap: 32px;
    }
    .social-icon {
        width: 44px;
        height: 44px;
        font-size: 22px;
    }
}
</style>
