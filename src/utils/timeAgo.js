/**
 * 将时间转换为友好化展示
 * @param {string|number|Date} time - 时间戳或日期字符串
 * @returns {string} 友好化时间字符串
 */
export function timeAgo(time) {
  if (!time) return ''
  
  const now = Date.now()
  const date = new Date(time).getTime()
  const diff = now - date
  
  // 未来时间
  if (diff < 0) return '刚刚'
  
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  const months = Math.floor(days / 30)
  const years = Math.floor(months / 12)
  
  if (years >= 1) {
    return years + '年前'
  }
  if (months >= 1) {
    return months + '个月前'
  }
  if (days >= 1) {
    return days + '天前'
  }
  if (hours >= 1) {
    return hours + '小时前'
  }
  return '刚刚'
}

export default timeAgo