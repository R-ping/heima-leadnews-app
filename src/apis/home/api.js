import conf from '@/common/conf'
import request from '@/common/request'
import store from '@/stores/store'
import articleRequest from '@/common/article_request'

function Api(){}
Api.prototype = {
    // 加载数据
    loaddata : function(params){
        let dir = params.loaddir
        let url = this.getLoadUrl(dir)
        // 后端 ArticleHomeDto 使用驼峰字段
        let body = {
            tag: params.tag,
            size: params.size || 10
        }
        // maxBehotTime/minBehotTime 为数字时间戳，发送给后端 Date 类型
        if (params.max_behot_time && params.max_behot_time > 0) {
            body.maxBehotTime = params.max_behot_time
        }
        if (params.min_behot_time && params.min_behot_time > 0 && params.min_behot_time < 20000000000000) {
            body.minBehotTime = params.min_behot_time
        }
        return store.getEquipmentId().then(equipmentId => {
            body.equipmentId = equipmentId
            return new Promise((resolve, reject) => {
                request.post(url, body, {}).then((d) => {
                    resolve(d)
                }).catch((e) => {
                    reject(e)
                })
            })
        }).catch(e => {
            return new Promise((resolve, reject) => {
                reject(e)
            })
        })
    },
    // 加载推荐数据（基于种子随机洗牌）
    recommendLoad: function(params) {
        var body = {
            channel: params.channel || '__all__',
            size: params.size || 10
        }
        if (params.seed) {
            body.seed = params.seed
        }
        if (params.page !== undefined && params.page !== null) {
            body.page = params.page
        }
        return store.getEquipmentId().then(function(equipmentId) {
            body.equipmentId = equipmentId
            return new Promise(function(resolve, reject) {
                articleRequest.post('/api/v1/content/recommend', body, {}).then(function(d) {
                    resolve(d)
                }).catch(function(e) {
                    reject(e)
                })
            })
        }).catch(function(e) {
            return new Promise(function(resolve, reject) {
                reject(e)
            })
        })
    },
    // 区别请求哪个URL
    getLoadUrl : function(dir){
        let url = conf.urls.get('load')
        if (dir === 0)
            url = conf.urls.get('loadnew')
        else if (dir === 2)
            url = conf.urls.get('loadmore')
        return url
    }
}

export default new Api()