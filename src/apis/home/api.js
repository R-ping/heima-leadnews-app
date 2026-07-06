import conf from '@/common/conf'
import request from '@/common/request'
import store from '@/stores/store'

function Api(){}
Api.prototype = {
    // 加载数据
    loaddata : function(params){
        let dir = params.loaddir
        let url = this.getLoadUrl(dir)
        return store.getEquipmentId().then(equipmentId=> {
            return new Promise((resolve, reject) => {
                request.post(url,params,{}).then((d)=>{
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
    // 保存展现行为数据
    saveShowBehavior : function(params){
        let ids = [];
        for(let k in params){
            if(params[k]){
                ids.push({id:k});
            }
        }
        if(ids.length>0){
            let url = conf.urls.get('show_behavior')
            return store.getEquipmentId().then(equipmentId=> {
                return new Promise((resolve, reject) => {
                    request.post(url, {
                        equipment_id: equipmentId,
                        article_ids: ids
                    }).then((d) => {
                        d.data = ids
                        resolve(d);
                    }).catch((e) => {
                        reject(e);
                    })
                })
            }).catch(e=>{
                return new Promise((resolve, reject) => {
                    reject(e);
                })
            })
        }
    },
    // 区别请求那个URL
    getLoadUrl : function(dir){
        let url = conf.urls.get('load')
        if(dir==0)
            url = conf.urls.get('loadnew')
        else if(dir==2)
            url = conf.urls.get('loadmore')
        return url;
    }
}

export default new Api()
