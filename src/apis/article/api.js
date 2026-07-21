import conf from '@/common/conf'
import request from '@/common/request'

function Api(){}
Api.prototype = {
    // 喜欢/点赞
    like : function(data){
        let url = conf.urls.get('like_behavior')
        return new Promise((resolve, reject) => {
            request.post(url, {
                articleId: data.articleId,
                type: 0,
                operation: data.operation
            }).then((d) => {
                resolve(d)
            }).catch((e) => {
                reject(e)
            })
        })
    },
    // 不喜欢
    unlike : function(data){
        let url = conf.urls.get('unlike_behavior')
        return new Promise((resolve, reject) => {
            request.post(url, {
                articleId: data.articleId,
                type: data.type
            }).then((d) => {
                resolve(d)
            }).catch((e) => {
                reject(e)
            })
        })
    },
    // 阅读行为
    read : function(data){
        let url = conf.urls.get('read_behavior')
        return new Promise((resolve, reject) => {
            request.post(url, {
                articleId: data.articleId,
                count: 1,
                readDuration: data.readDuration || 0,
                percentage: data.percentage || 0,
                loadDuration: data.loadDuration || 0
            }).then((d) => {
                resolve(d)
            }).catch((e) => {
                reject(e)
            })
        })
    },
    // 获取文章元数据
    getInfo: function (articleId) {
        let url = conf.urls.get('article_info')
        return new Promise((resolve, reject) => {
            request.get(url, { articleId: articleId }).then((d) => {
                resolve(d)
            }).catch((e) => {
                reject(e)
            })
        })
    },
    // 获取文章内容
    getContent: function (articleId) {
        let url = conf.urls.get('article_content')
        return new Promise((resolve, reject) => {
            request.get(url, { articleId: articleId }).then((d) => {
                resolve(d)
            }).catch((e) => {
                reject(e)
            })
        })
    },
    // 获取评论列表
    getCommentList: function (articleId, page, size) {
        let url = conf.urls.get('comment_list')
        return new Promise((resolve, reject) => {
            request.post(url, {
                articleId: articleId,
                page: page || 1,
                size: size || 3
            }).then((d) => {
                resolve(d)
            }).catch((e) => {
                reject(e)
            })
        })
    },
    // 发表评论
    addComment: function (data) {
        let url = conf.urls.get('comment_add')
        return new Promise((resolve, reject) => {
            request.post(url, {
                articleId: data.articleId,
                parentId: data.parentId || null,
                content: data.content
            }).then((d) => {
                resolve(d)
            }).catch((e) => {
                reject(e)
            })
        })
    },
    // 点赞评论
    likeComment: function (commentId) {
        let url = conf.urls.get('comment_like')
        return new Promise((resolve, reject) => {
            request.post(url, {
                commentId: commentId
            }).then((d) => {
                resolve(d)
            }).catch((e) => {
                reject(e)
            })
        })
    },
    // 收藏
    collect: function (data) {
        let url = conf.urls.get('collection_behavior')
        return new Promise((resolve, reject) => {
            request.post(url, {
                articleId: data.articleId,
                operation: data.operation
            }).then((d) => {
                resolve(d)
            }).catch((e) => {
                reject(e)
            })
        })
    },
    // 关注
    follow: function (data) {
        let url = conf.urls.get('follow_behavior')
        return new Promise((resolve, reject) => {
            request.post(url, {
                articleId: data.articleId,
                operation: data.operation
            }).then((d) => {
                resolve(d)
            }).catch((e) => {
                reject(e)
            })
        })
    }
}

export default new Api()