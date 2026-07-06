import conf from '@/common/conf'
import request from '@/common/request'

var api = {
    // 加载搜索历史
    load_search_history: function(){
        let url = conf.urls.get('load_search_history')
        return request.postByEquipmentId(url,{page_size:5})
    },
    // 删除搜索词
    del_search: function(id){
        let url = conf.urls.get('del_search')
        return request.postByEquipmentId(url,{his_list:[{id:id}]})
    },
    // 输入联想
    associate_search: function(searchWords){
        let url = conf.urls.get('associate_search')
        return request.postByEquipmentId(url,{search_words:searchWords,page_size:10})
    },
    // 加载热词
    load_hot_keywords: function(){
        let url = conf.urls.get('load_hot_keywords')
        return request.postByEquipmentId(url,{page_size:6})
    }
}

export default api
