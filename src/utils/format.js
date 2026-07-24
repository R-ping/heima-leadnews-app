/**
 * 格式化数字（k/w）
 * @param {number} count
 * @returns {string}
 */
export function formatCount(count) {
    if (!count && count !== 0) return '0'
    if (count >= 10000) {
        return (count / 10000).toFixed(1) + 'w'
    }
    if (count >= 1000) {
        return (count / 1000).toFixed(1) + 'k'
    }
    return count.toString()
}

/**
 * 格式化时间字符串为 HH:mm
 * @param {string|Date} time
 * @returns {string}
 */
export function formatTime(time) {
    if (!time) return ''
    try {
        const d = new Date(time)
        if (isNaN(d.getTime())) {
            const parts = time.split(' ')
            if (parts.length >= 2) return parts[1].substring(0, 5)
            return time
        }
        const h = String(d.getHours()).padStart(2, '0')
        const m = String(d.getMinutes()).padStart(2, '0')
        return `${h}:${m}`
    } catch {
        return time
    }
}

/**
 * 格式化时间字符串为 yyyy-MM-dd
 * @param {string|Date} time
 * @returns {string}
 */
export function formatDate(time) {
    if (!time) return ''
    try {
        const d = new Date(time)
        if (isNaN(d.getTime())) {
            const parts = time.split(' ')
            if (parts.length >= 1) return parts[0]
            return time
        }
        const y = d.getFullYear()
        const m = String(d.getMonth() + 1).padStart(2, '0')
        const day = String(d.getDate()).padStart(2, '0')
        return `${y}-${m}-${day}`
    } catch {
        return time
    }
}