import conf from '@/common/conf'
import request from '@/common/request'

var api = {
    // 登录
    login: function(data){
        let url = conf.urls.get('user_login')
        return request.postByEquipmentId(url,data)
    },
    // 微信验证码登录
    wechatLogin: function(data){
        let url = conf.urls.get('wechat_login')
        return request.postByEquipmentId(url,data)
    }
}

export default api
