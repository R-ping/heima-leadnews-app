import request from '@/common/article_request'

function generateObjectKey(file) {
    const ext = file.name.substring(file.name.lastIndexOf('.'))
    const timestamp = Date.now()
    const random = Math.random().toString(36).substring(2, 8)
    return `${timestamp}_${random}${ext}`
}

export async function uploadFile(file, onProgress) {
    const res = await request({
        url: '/api/v1/media/oss/post_signature',
        method: 'get'
    })
    if (!res || res.code !== 200 || !res.data) {
        throw new Error(res?.errorMessage || '获取上传签名失败')
    }
    const data = res.data

    const objectKey = data.dir + generateObjectKey(file)
    const formData = new FormData()
    formData.append('name', file.name)
    formData.append('key', objectKey)
    formData.append('policy', data.policy)
    formData.append('OSSAccessKeyId', data.ossAccessKeyId)
    formData.append('success_action_status', '200')
    formData.append('signature', data.signature)
    formData.append('file', file)

    if (onProgress) {
        onProgress(50)
    }

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

    return data.host + '/' + objectKey
}

export function clearCache() {
    // 无缓存需要清理
}
