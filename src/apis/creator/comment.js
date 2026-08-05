import request from '@/common/article_request'

const API_COMMENT_LIST = '/api/v1/comment/list'
const API_CLOSECOMMENTS = '/api/v1/comment/status'
const API_ADMIRECOMMENT = '/api/v1/comment/likings'
const API_CANCELADMIRECOMMENT = '/api/v1/comment/likings/'
const API_COMMENTS = '/api/v1/comment'

// 获取评论列表
export const getCommentList = (params) => {
  return request.get(API_COMMENT_LIST, { params })
}

// 关闭或打开评论
export const closeOrOpenComment = (data) => {
  return request.put(API_CLOSECOMMENTS, data)
}

// 点赞评论
export const admireComment = (data) => {
  return request.post(API_ADMIRECOMMENT, data)
}

// 取消点赞评论
export const cancleAdmire = (commentId) => {
  return request.delete(API_CANCELADMIRECOMMENT + commentId)
}

// 置顶评论
export const changeTop = (data) => {
  return request.put(API_COMMENTS, data)
}

// 新增评论/回复
export const addComments = (data) => {
  return request.post(API_COMMENTS, data)
}