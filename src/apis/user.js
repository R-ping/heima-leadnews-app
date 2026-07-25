import request from '@/common/request'

export const getUserStatistics = () => {
  return request.get('/api/v1/user/statistics')
}

export const getUserProfile = () => {
  return request.get('/api/v1/user/profile')
}

export const updateUserProfile = (data) => {
  return request.put('/api/v1/user/profile', data)
}

export const uploadAvatar = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/v1/user/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 获取账号绑定状态
export const getBindings = () => {
  return request.get('/api/v1/user/bindings')
}

// 修改密码
export const updatePassword = (data) => {
  return request.put('/api/v1/user/password', data)
}

// 注销账号
export const deleteAccount = () => {
  return request.delete('/api/v1/user/account')
}

// 更新私信权限
export const updatePrivacyMessage = (data) => {
  return request.put('/api/v1/user/privacy/message', data)
}

// 获取屏蔽列表
export const getBlocks = (type, page = 1, size = 10) => {
  return request.get('/api/v1/user/blocks', { params: { type, page, size } })
}

// 解除屏蔽
export const removeBlock = (id) => {
  return request.delete(`/api/v1/user/blocks/${id}`)
}