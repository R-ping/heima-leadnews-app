import request from '@/common/wemedia_request'

const API_USERIMAGES_LIST = '/api/v1/material/list'
const API_USERIMAGES_ADD = '/api/v1/material/upload_picture'
const API_CHANNELS = '/api/v1/channel/channels'
const API_ARTICLES = '/api/v1/news/submit'
const API_MODIFYIMAGE_DELETE = '/api/v1/material/del_picture'
const API_MODIFYIMAGE_COL = '/api/v1/material/collect'
const API_MODIFYIMAGE_COL_CANCEL = '/api/v1/material/cancel_collect'

// 获取用户图片素材列表
export const getAllImgData = (data) => {
  return request.post(API_USERIMAGES_LIST, data)
}

// 上传图片
export const uploadImg = (data) => {
  return request.post(API_USERIMAGES_ADD, data)
}

// 获取文章频道
export const getChannels = () => {
  return request.get(API_CHANNELS)
}

// 发布文章
export const publishArticles = (params, data) => {
  delete data['id']
  return request.post(API_ARTICLES, data, { params })
}

// 修改文章
export const modifyArticles = (articleId, params, data) => {
  return request.post(API_ARTICLES, data, { params })
}

// 删除图片
export const delImg = (data) => {
  return request.delete(API_MODIFYIMAGE_DELETE, { data })
}

// 收藏或取消收藏素材
export const collectOrCancel = (data) => {
  return request.post(API_MODIFYIMAGE_COL, data)
}

// 获取OSS STS临时凭证
export const getStsToken = () => {
  return request({
    url: '/api/v1/media/oss/sts_token',
    method: 'get'
  })
}

// 保存素材记录（OSS直传后）
export const saveMaterial = (url) => {
  return request({
    url: '/api/v1/material/save',
    method: 'post',
    data: { url }
  })
}

// 获取OSS PostObject直传签名
export const getPostSignature = () => {
  return request({
    url: '/api/v1/media/oss/post_signature',
    method: 'get'
  })
}

// 获取标签列表
export const getTagList = (keyword) => {
  return request({
    url: '/api/v1/tag/list',
    method: 'get',
    params: { keyword }
  })
}

// 获取话题列表
export const getTopicList = (keyword) => {
  return request({
    url: '/api/v1/topic/list',
    method: 'get',
    params: { keyword }
  })
}

// 导入 Markdown 文档
export const importMarkdown = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return articleRequest({
    url: '/api/v1/article/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}