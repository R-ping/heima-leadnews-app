import conf from '@/common/conf'
import request from '@/common/request'

var api = {
    /**
     * 统一登录接口
     * @param {Object} data - { phoneOrEmail, password, code, platform }
     *   - phoneOrEmail: 手机号或邮箱
     *   - password: 密码（手机号密码/邮箱密码登录时使用）
     *   - code: 验证码（手机号验证码登录时使用）
     *   - platform: 平台标识（如'app'）
     */
    loginAuth: function(data) {
        let url = conf.urls.get('user_login')
        return request.post(url, data)
    },

    /**
     * 获取验证码
     * @param {string} phone - 手机号
     * @param {string} platform - 平台标识（'app' 或 'github'/'weibo'等）
     * @param {string} tag - 场景标识（'login' 登录/注册, 'bind' 社交绑定）
     */
    getCode: function(phone, platform, tag) {
        let url = conf.urls.get('user_code')
        return request.post(url, {}, { phone: phone, platform: platform, tag: tag })
    },

    /**
     * 社交账号绑定
     * @param {Object} data - { platform, platformUid, phone, code }
     */
    socialBind: function(data) {
        let url = conf.urls.get('user_social_bind')
        return request.post(url, data)
    },

    /**
     * 刷新Token
     * @param {string} refreshToken - 刷新令牌
     */
    refreshToken: function(refreshToken) {
        let url = conf.urls.get('user_token_refresh')
        return request.post(url, { refreshToken: refreshToken })
    },

    /**
     * 兼容旧调用方式
     */
    login: function(data) {
        return api.loginAuth(data)
    }
}

export default api