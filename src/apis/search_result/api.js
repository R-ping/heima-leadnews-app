import conf from '@/common/conf'
import request from '@/common/request'

var api = {
    // 加载
    article_search: function(parms){
        let url = conf.urls.get('article_search')
        return request.postByEquipmentId(url,{
            search_words:parms.keyword,
            page_num:parms.pageNum,
            tag:parms.tag,
            page_size:20
        })
    }
}

export default api
