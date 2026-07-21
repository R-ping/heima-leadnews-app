import conf from '@/common/conf'
import request from '@/common/request'

var api = {
    // 加载搜索历史
    load_search_history: function(){
        let url = conf.urls.get('load_search_history')
        return request.postByEquipmentId(url,{})
    },
    // 删除搜索词
    del_search: function(id){
        let url = conf.urls.get('del_search')
        // 后端 HistorySearchDto 使用驼峰字段 id
        return request.postByEquipmentId(url,{id:id})
    },
    // 输入联想
    associate_search: function(searchWords){
        let url = conf.urls.get('associate_search')
        // 后端 UserSearchDto 使用驼峰字段 searchWords/pageSize
        return request.postByEquipmentId(url,{searchWords:searchWords,pageSize:10})
    }
    // 后端未提供 load_hot_keywords 接口，已移除对应方法
}

export default api
