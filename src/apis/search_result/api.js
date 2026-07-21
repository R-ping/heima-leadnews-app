import conf from '@/common/conf'
import request from '@/common/request'

var api = {
    // 加载
    article_search: function(parms){
        let url = conf.urls.get('article_search')
        // 后端 UserSearchDto 使用驼峰字段 searchWords/pageNum/pageSize
        return request.postByEquipmentId(url,{
            searchWords:parms.keyword,
            pageNum:parms.pageNum,
            pageSize:20
        })
    }
}

export default api
