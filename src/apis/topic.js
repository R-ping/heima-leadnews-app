import request from '@/common/article_request'

export const getRecommendTopics = () => {
  return request.get('/api/v1/topic/recommend')
}