import request from '@/common/reward_request'

/** 获取签到状态与日历数据（新接口） */
export const getSignStatus = () => {
  return request.get('/api/v1/sign/status')
}

/** 执行每日签到 */
export const doSignCheckin = () => {
  return request.post('/api/v1/sign/checkin')
}

/** 执行补签操作 */
export const doSignExtra = (date) => {
  return request.post('/api/v1/sign/extra', { date })
}

/** 获取今日签到状态（侧边栏用） */
export const getTodayStatus = () => {
  return request.get('/api/v1/sign/today')
}