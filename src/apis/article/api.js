import conf from '@/common/conf'
import request from '@/common/request'
import store from '@/stores/store'

function Api(){}
Api.prototype = {
    // 保存展现行为数据
    loadinfo : function(articleId){
        let url = conf.urls.get('load_article_info')
        return new Promise((resolve, reject) => {
            request.post(url,{article_id:articleId}).then((d)=>{
                resolve(d);
            }).catch((e)=>{
                reject(e);
            })
        })
    },
    // 加载文章关系信息
    loadbehavior: function(articleId,authorId){
        let url = conf.urls.get('load_article_behavior')
        return store.getEquipmentId().then(equipmentId=>{
            return new Promise((resolve, reject) => {
                request.post(url,{equipment_id:equipmentId,article_id:articleId,author_id:authorId}).then((d)=>{
                    resolve(d);
                }).catch((e)=>{
                    reject(e);
                })
            })
        }).catch(e=>{
            return new Promise((resolve, reject) => {
                reject(e);
            })
        })
    },
    // 喜欢、点赞
    like : function(data){
        let url = conf.urls.get('like_behavior')
        return store.getEquipmentId().then(equipmentId=>{
            return new Promise((resolve, reject) => {
                request.post(url,{equipment_id:equipmentId,entry_id:data.articleId,type:0,operation:data.operation}).then((d)=>{
                    resolve(d);
                }).catch((e)=>{
                    reject(e);
                })
            })
        }).catch(e=>{
            return new Promise((resolve, reject) => {
                reject(e);
            })
        })
    },
    // 不喜欢
    unlike : function(data){
        let url = conf.urls.get('unlike_behavior')
        return store.getEquipmentId().then(equipmentId=>{
            return new Promise((resolve, reject) => {
                request.post(url,{equipment_id:equipmentId,article_id:data.articleId,type:data.type}).then((d)=>{
                    resolve(d);
                }).catch((e)=>{
                    reject(e);
                })
            })
        }).catch(e=>{
            return new Promise((resolve, reject) => {
                reject(e);
            })
        })
    },
    // 阅读
    read : function(data){
        let url = conf.urls.get('read_behavior')
        return store.getEquipmentId().then(equipmentId=>{
            return new Promise((resolve, reject) => {
                request.post(url,{
                    equipment_id:equipmentId,
                    article_id:data.articleId,
                    count:1,
                    read_duration:data.readDuration,
                    percentage:data.percentage,
                    load_duration:data.loadDuration
                }).then((d)=>{
                    resolve(d);
                }).catch((e)=>{
                    reject(e);
                })
            })
        }).catch(e=>{
            return new Promise((resolve, reject) => {
                reject(e);
            })
        })
    },
    // 收藏
    collection : function(data){
        let url = conf.urls.get('collection_behavior')
        return store.getEquipmentId().then(equipmentId=>{
            return new Promise((resolve, reject) => {
                request.post(url,{
                    equipment_id:equipmentId,
                    entry_id:data.articleId,
                    published_time:data.publishedTime,
                    type:0,
                    operation:data.operation
                }).then((d)=>{
                    resolve(d);
                }).catch((e)=>{
                    reject(e);
                })
            })
        }).catch(e=>{
            return new Promise((resolve, reject) => {
                reject(e);
            })
        })
    },
    // 转发
    forward : function(data){
        let url = conf.urls.get('forward_behavior')
        return store.getEquipmentId().then(equipmentId=>{
            return new Promise((resolve, reject) => {
                request.post(url,{
                    equipment_id:equipmentId,
                    article_id:data.articleId
                }).then((d)=>{
                    resolve(d);
                }).catch((e)=>{
                    reject(e);
                })
            })
        }).catch(e=>{
            return new Promise((resolve, reject) => {
                reject(e);
            })
        })
    },
    // 分享
    share : function(data){
        let url = conf.urls.get('share_behavior')
        return store.getEquipmentId().then(equipmentId=>{
            return new Promise((resolve, reject) => {
                request.post(url,{
                    equipment_id:equipmentId,
                    article_id:data.articleId,
                    type:data.type
                }).then((d)=>{
                    resolve(d);
                }).catch((e)=>{
                    reject(e);
                })
            })
        }).catch(e=>{
            return new Promise((resolve, reject) => {
                reject(e);
            })
        })
    },
    // 关注
    follow : function(data){
        let url = conf.urls.get('user_follow')
        return new Promise((resolve, reject) => {
            request.post(url,{
                author_id:data.authorId,
                operation:data.operation,
                article_id:data.articleId
            }).then((d)=>{
                resolve(d);
            }).catch((e)=>{
                reject(e);
            })
        })
    }
}

export default new Api()
