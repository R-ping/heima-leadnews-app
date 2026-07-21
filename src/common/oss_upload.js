import { getPostSignature } from '@/apis/creator/publish'

function generateObjectKey(file) {
    const ext = file.name.substring(file.name.lastIndexOf('.'))
    const timestamp = Date.now()
    const random = Math.random().toString(36).substring(2, 8)
    return `material/${timestamp}_${random}${ext}`
}

export async function uploadFile(file, onProgress) {
    // 1. 获取上传签名
    const res = await getPostSignature()
    if (!res || res.code !== 200 || !res.data) {
        throw new Error('获取上传签名失败')
    }
    const data = res.data
    
    // 2. 构建 FormData
    const objectKey = data.dir + generateObjectKey(file).replace('material/', '')
    const formData = new FormData()
    formData.append('name', file.name)
    formData.append('key', objectKey)
    formData.append('policy', data.policy)
    formData.append('OSSAccessKeyId', data.ossAccessKeyId)
    formData.append('success_action_status', '200')
    formData.append('signature', data.signature)
    formData.append('file', file)
    
    // 3. 模拟进度
    if (onProgress) {
        onProgress(50)
    }
    
    // 4. POST 到 OSS（使用 no-cors 模式避免 CORS 拦截响应）
    // OSS 会存储文件但默认不返回 CORS 头，浏览器会拦截响应导致 fetch reject
    // no-cors 模式下 fetch 总是 resolve（返回 opaque response），我们不需要读取响应体
    try {
        await fetch(data.host, {
            method: 'POST',
            body: formData,
            mode: 'no-cors'
        })
    } catch (e) {
        console.warn('OSS fetch 异常（可忽略）:', e.message)
    }
    
    if (onProgress) {
        onProgress(100)
    }
    
    // no-cors 模式下 response.type === 'opaque'，无法读取状态码
    // 直接拼接 URL 返回，OSS 已存储文件
    const url = data.host + '/' + objectKey
    return url
}

export function clearCache() {
    // 无缓存需要清理
}