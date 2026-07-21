<template>
    <div class="settings-page">
        <div class="art-top"><HomeBar/></div>
        <div class="settings-content">
            <div class="settings-sidebar">
                <div 
                    class="sidebar-item" 
                    :class="{ 'active': activeSection === 'profile' }"
                    @click="activeSection = 'profile'"
                >
                    <i class="el-icon-user"></i>
                    <span>个人资料</span>
                </div>
                <div 
                    class="sidebar-item" 
                    :class="{ 'active': activeSection === 'account' }"
                    @click="activeSection = 'account'"
                >
                    <i class="el-icon-user-solid"></i>
                    <span>账号设置</span>
                </div>
                <div 
                    class="sidebar-item" 
                    :class="{ 'active': activeSection === 'general' }"
                    @click="activeSection = 'general'"
                >
                    <i class="el-icon-setting"></i>
                    <span>通用设置</span>
                </div>
                <div 
                    class="sidebar-item" 
                    :class="{ 'active': activeSection === 'notification' }"
                    @click="activeSection = 'notification'"
                >
                    <i class="el-icon-bell"></i>
                    <span>消息设置</span>
                </div>
                <div 
                    class="sidebar-item" 
                    :class="{ 'active': activeSection === 'block' }"
                    @click="activeSection = 'block'"
                >
                    <i class="el-icon-circle-close"></i>
                    <span>屏蔽管理</span>
                </div>
                <div 
                    class="sidebar-item" 
                    :class="{ 'active': activeSection === 'tags' }"
                    @click="activeSection = 'tags'"
                >
                    <i class="el-icon-tags"></i>
                    <span>标签管理</span>
                </div>
            </div>

            <div class="settings-main">
                <div v-if="activeSection === 'profile'" class="section-content">
                    <div class="section-header">
                        <h2>个人资料</h2>
                    </div>
                    <div class="settings-card">
                        <div class="card-header">基本信息</div>
                        <div class="card-body">
                            <div class="avatar-upload-section">
                                <img :src="userInfo.avatar || defaultAvatar" class="current-avatar" alt="avatar">
                                <div class="avatar-actions">
                                    <button class="edit-btn primary" @click="showAvatarUpload = true">上传头像</button>
                                    <span class="avatar-hint">格式: 支持JPG、PNG、JPEG | 大小: 5M以内</span>
                                </div>
                            </div>
                            <el-form label-position="top" style="margin-top: 24px;">
                                <el-form-item label="用户名" required>
                                    <el-input placeholder="请输入用户名" v-model="profileForm.nickName"></el-input>
                                    <span class="input-hint">5/20</span>
                                </el-form-item>
                                <el-form-item label="开始工作" required>
                                    <el-date-picker v-model="profileForm.startWork" type="month" placeholder="选择年月"></el-date-picker>
                                </el-form-item>
                                <el-form-item label="职业方向" required>
                                    <el-select v-model="profileForm.jobDirection" placeholder="请选择">
                                        <el-option label="后端开发" value="backend"></el-option>
                                        <el-option label="前端开发" value="frontend"></el-option>
                                        <el-option label="移动端开发" value="mobile"></el-option>
                                        <el-option label="人工智能" value="ai"></el-option>
                                        <el-option label="产品经理" value="product"></el-option>
                                    </el-select>
                                </el-form-item>
                                <el-form-item label="职位">
                                    <el-input placeholder="请输入你的职位" v-model="profileForm.position"></el-input>
                                    <span class="input-hint">0/50</span>
                                </el-form-item>
                                <el-form-item label="公司">
                                    <el-input placeholder="请输入你的公司" v-model="profileForm.company"></el-input>
                                    <span class="input-hint">0/50</span>
                                </el-form-item>
                                <el-form-item label="个人主页">
                                    <el-input placeholder="请输入你的个人主页" v-model="profileForm.homepage"></el-input>
                                    <span class="input-hint">0/100</span>
                                </el-form-item>
                                <el-form-item label="个人介绍">
                                    <el-input type="textarea" placeholder="请填写职业技能、擅长的事情、兴趣爱好等" v-model="profileForm.intro" :rows="4"></el-input>
                                    <span class="input-hint">0/100</span>
                                </el-form-item>
                                <el-form-item label="兴趣标签管理">
                                    <div class="interest-tags">
                                        <span class="interest-tag" v-for="tag in profileForm.tags" :key="tag">
                                            {{ tag }}
                                            <span class="remove-tag" @click="removeInterestTag(tag)">×</span>
                                        </span>
                                        <input type="text" class="add-tag-input" placeholder="添加标签" v-model="newTag" @keyup.enter="addInterestTag">
                                    </div>
                                </el-form-item>
                                <el-form-item>
                                    <el-button type="primary" @click="saveProfile">保存</el-button>
                                </el-form-item>
                            </el-form>
                        </div>
                    </div>
                </div>

                <div v-if="activeSection === 'account'" class="section-content">
                    <div class="section-header">
                        <h2>账号设置</h2>
                    </div>
                    <div class="settings-card">
                        <div class="card-header">账号绑定</div>
                        <div class="card-body">
                            <div class="account-item">
                                <div class="account-info">
                                    <span class="account-icon">📱</span>
                                    <span class="account-label">手机</span>
                                </div>
                                <div class="account-right">
                                    <span class="account-value">13****1129</span>
                                    <button class="account-btn">换绑</button>
                                </div>
                            </div>
                            <div class="account-item">
                                <div class="account-info">
                                    <span class="account-icon">💬</span>
                                    <span class="account-label">微信</span>
                                </div>
                                <div class="account-right">
                                    <span class="account-value">未绑定</span>
                                    <button class="account-btn">绑定</button>
                                </div>
                            </div>
                            <div class="account-item">
                                <div class="account-info">
                                    <span class="account-icon">📢</span>
                                    <span class="account-label">新浪微博</span>
                                </div>
                                <div class="account-right">
                                    <span class="account-value">未绑定</span>
                                    <button class="account-btn">绑定</button>
                                </div>
                            </div>
                            <div class="account-item">
                                <div class="account-info">
                                    <span class="account-icon">💻</span>
                                    <span class="account-label">GitHub</span>
                                </div>
                                <div class="account-right">
                                    <span class="account-value">未绑定</span>
                                    <button class="account-btn">绑定</button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="settings-card">
                        <div class="card-header">安全设置</div>
                        <div class="card-body">
                            <div class="account-item">
                                <div class="account-info">
                                    <span class="account-icon">🔑</span>
                                    <span class="account-label">密码</span>
                                </div>
                                <button class="account-btn">重置</button>
                            </div>
                            <div class="account-item">
                                <div class="account-info">
                                    <span class="account-icon">🗑️</span>
                                    <span class="account-label">账号注销</span>
                                </div>
                                <button class="danger-btn" @click="showDeleteConfirm = true">注销</button>
                            </div>
                        </div>
                    </div>
                </div>

                <div v-if="activeSection === 'general'" class="section-content">
                    <div class="section-header">
                        <h2>通用设置</h2>
                    </div>
                    <div class="settings-card">
                        <div class="card-header">界面设置</div>
                        <div class="card-body">
                            <div class="general-item">
                                <div class="general-info">
                                    <span class="general-label">深色模式</span>
                                    <span class="general-desc">切换深色/浅色主题</span>
                                </div>
                                <el-switch v-model="generalSettings.darkMode" active-text="开启" inactive-text="关闭"></el-switch>
                            </div>
                            <div class="general-item">
                                <div class="general-info">
                                    <span class="general-label">字体大小</span>
                                    <span class="general-desc">调整页面字体大小</span>
                                </div>
                                <el-select v-model="generalSettings.fontSize" placeholder="请选择">
                                    <el-option label="小号" value="small"></el-option>
                                    <el-option label="默认" value="normal"></el-option>
                                    <el-option label="大号" value="large"></el-option>
                                </el-select>
                            </div>
                        </div>
                    </div>
                    <div class="settings-card">
                        <div class="card-header">浏览设置</div>
                        <div class="card-body">
                            <div class="general-item">
                                <div class="general-info">
                                    <span class="general-label">图片质量</span>
                                    <span class="general-desc">默认加载高清图片</span>
                                </div>
                                <el-select v-model="generalSettings.imageQuality" placeholder="请选择">
                                    <el-option label="高清" value="high"></el-option>
                                    <el-option label="标准" value="normal"></el-option>
                                    <el-option label="节省流量" value="low"></el-option>
                                </el-select>
                            </div>
                            <div class="general-item">
                                <div class="general-info">
                                    <span class="general-label">自动播放视频</span>
                                    <span class="general-desc">在Wi-Fi下自动播放视频</span>
                                </div>
                                <el-switch v-model="generalSettings.autoPlayVideo" active-text="开启" inactive-text="关闭"></el-switch>
                            </div>
                        </div>
                    </div>
                </div>

                <div v-if="activeSection === 'notification'" class="section-content">
                    <div class="section-header">
                        <h2>消息设置</h2>
                    </div>
                    <div class="settings-card">
                        <div class="card-header">互动通知</div>
                        <div class="card-body">
                            <div class="notification-item">
                                <div class="notification-info">
                                    <span class="notification-label">有人关注我</span>
                                    <span class="notification-desc">当有用户关注您时发送通知</span>
                                </div>
                                <el-switch v-model="notificationSettings.follow" active-text="开启" inactive-text="关闭"></el-switch>
                            </div>
                            <div class="notification-item">
                                <div class="notification-info">
                                    <span class="notification-label">有人评论我</span>
                                    <span class="notification-desc">当有用户评论您的文章时发送通知</span>
                                </div>
                                <el-switch v-model="notificationSettings.comment" active-text="开启" inactive-text="关闭"></el-switch>
                            </div>
                            <div class="notification-item">
                                <div class="notification-info">
                                    <span class="notification-label">有人点赞我</span>
                                    <span class="notification-desc">当有用户点赞您的内容时发送通知</span>
                                </div>
                                <el-switch v-model="notificationSettings.like" active-text="开启" inactive-text="关闭"></el-switch>
                            </div>
                            <div class="notification-item">
                                <div class="notification-info">
                                    <span class="notification-label">有人@我</span>
                                    <span class="notification-desc">当有用户@您时发送通知</span>
                                </div>
                                <el-switch v-model="notificationSettings.at" active-text="开启" inactive-text="关闭"></el-switch>
                            </div>
                        </div>
                    </div>

                    <div class="settings-card">
                        <div class="card-header">系统通知</div>
                        <div class="card-body">
                            <div class="notification-item">
                                <div class="notification-info">
                                    <span class="notification-label">系统公告</span>
                                    <span class="notification-desc">接收平台系统公告和重要通知</span>
                                </div>
                                <el-switch v-model="notificationSettings.system" active-text="开启" inactive-text="关闭"></el-switch>
                            </div>
                            <div class="notification-item">
                                <div class="notification-info">
                                    <span class="notification-label">热门内容推荐</span>
                                    <span class="notification-desc">接收平台推荐的热门内容</span>
                                </div>
                                <el-switch v-model="notificationSettings.recommend" active-text="开启" inactive-text="关闭"></el-switch>
                            </div>
                        </div>
                    </div>
                </div>

                <div v-if="activeSection === 'block'" class="section-content">
                    <div class="section-header">
                        <h2>屏蔽管理</h2>
                    </div>
                    <div class="settings-card">
                        <div class="card-header">屏蔽的用户</div>
                        <div class="card-body">
                            <div v-if="blockedUsers.length === 0" class="empty-block">
                                <span class="empty-icon">👤</span>
                                <span class="empty-text">暂无屏蔽的用户</span>
                            </div>
                            <div v-else class="blocked-list">
                                <div class="blocked-item" v-for="user in blockedUsers" :key="user.id">
                                    <img :src="user.avatar || defaultAvatar" class="blocked-avatar" alt="avatar">
                                    <div class="blocked-info">
                                        <div class="blocked-name">{{ user.name }}</div>
                                        <div class="blocked-time">{{ user.blockTime }}</div>
                                    </div>
                                    <button class="unblock-btn" @click="unblockUser(user.id)">取消屏蔽</button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="settings-card">
                        <div class="card-header">屏蔽的话题</div>
                        <div class="card-body">
                            <div v-if="blockedTopics.length === 0" class="empty-block">
                                <span class="empty-icon">📌</span>
                                <span class="empty-text">暂无屏蔽的话题</span>
                            </div>
                            <div v-else class="blocked-topics">
                                <span class="blocked-topic-item" v-for="topic in blockedTopics" :key="topic.id">
                                    {{ topic.name }}
                                    <span class="remove-topic" @click="unblockTopic(topic.id)">×</span>
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <div v-if="activeSection === 'tags'" class="section-content">
                    <div class="section-header">
                        <h2>标签管理</h2>
                    </div>
                    <div class="settings-card">
                        <div class="card-header">关注的标签</div>
                        <div class="card-body">
                            <div v-if="followedTags.length === 0" class="empty-tags">
                                <span class="empty-icon">🏷️</span>
                                <span class="empty-text">暂无关注的标签</span>
                                <button class="add-tag-btn" @click="showAddTag = true">添加标签</button>
                            </div>
                            <div v-else class="followed-tags-grid">
                                <div class="tag-card" v-for="tag in followedTags" :key="tag.id">
                                    <span class="tag-name">{{ tag.name }}</span>
                                    <span class="tag-count">{{ tag.articleCount }}篇文章</span>
                                    <button class="unfollow-tag-btn" @click="unfollowTag(tag.id)">取消关注</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <el-dialog 
            title="更换头像" 
            :visible.sync="showAvatarUpload" 
            width="480px"
        >
            <div class="avatar-upload-wrap">
                <img v-if="localAvatar" :src="localAvatar" class="avatar-preview">
                <el-upload 
                    class="avatar-uploader" 
                    action="#" 
                    :auto-upload="false"
                    :limit="1"
                    :on-change="handleAvatarChange"
                >
                    <i v-if="!localAvatar" class="el-icon-plus avatar-upload-icon"></i>
                </el-upload>
            </div>
            <span slot="footer" class="dialog-footer">
                <el-button @click="showAvatarUpload = false">取消</el-button>
                <el-button type="primary" @click="uploadAvatar">确定</el-button>
            </span>
        </el-dialog>

        <el-dialog 
            title="确认注销" 
            :visible.sync="showDeleteConfirm" 
            width="480px"
        >
            <div class="delete-warning">
                <i class="el-icon-warning"></i>
                <p>确认要注销您的账号吗？</p>
                <p class="warning-desc">注销后，您的所有数据将被永久删除且无法恢复，包括文章、动态、收藏等内容。</p>
            </div>
            <el-form>
                <el-form-item label="请输入密码确认">
                    <el-input type="password" placeholder="请输入登录密码" v-model="deleteForm.password"></el-input>
                </el-form-item>
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="showDeleteConfirm = false">取消</el-button>
                <el-button type="danger" @click="confirmDelete">确认注销</el-button>
            </span>
        </el-dialog>
    </div>
</template>

<script>
import HomeBar from '@/compoents/bars/home_bar'
import defaultAvatar from '@/static/images/creator/avatar.jpg'
import { toast } from '@/utils/toast'

export default {
    name: 'UserSettings',
    components: { HomeBar },
    data() {
        return {
            activeSection: 'profile',
            showAvatarUpload: false,
            showDeleteConfirm: false,
            showAddTag: false,
            localAvatar: null,
            newTag: '',
            deleteForm: {
                password: ''
            },
            userInfo: {
                id: '123456',
                nickName: '程序员小站',
                avatar: '',
                intro: '分享技术心得，记录成长历程',
                mobile: '138****8888',
                email: ''
            },
            profileForm: {
                nickName: '程序员小站',
                startWork: '2026-05',
                jobDirection: 'backend',
                position: '',
                company: '',
                homepage: '',
                intro: '',
                tags: ['Vue', 'React', 'JavaScript']
            },
            generalSettings: {
                darkMode: false,
                fontSize: 'normal',
                imageQuality: 'high',
                autoPlayVideo: false
            },
            notificationSettings: {
                follow: true,
                comment: true,
                like: true,
                at: true,
                system: true,
                recommend: true
            },
            blockedUsers: [
                { id: 1, name: '不良用户A', avatar: '', blockTime: '2025-10-20' },
                { id: 2, name: '垃圾广告号', avatar: '', blockTime: '2025-10-18' }
            ],
            blockedTopics: [
                { id: 1, name: '营销广告' },
                { id: 2, name: '八卦娱乐' }
            ],
            followedTags: [
                { id: 1, name: 'Vue.js', articleCount: 1256 },
                { id: 2, name: 'TypeScript', articleCount: 890 },
                { id: 3, name: 'Node.js', articleCount: 678 }
            ]
        }
    },
    computed: {
        defaultAvatar() {
            return defaultAvatar
        }
    },
    methods: {
        handleAvatarChange() {
            let file = document.querySelector('.el-upload .el-upload__input').files[0]
            if (file) {
                this.localAvatar = URL.createObjectURL(file)
            }
        },
        uploadAvatar() {
            if (!this.localAvatar) {
                toast('请选择一张图片', 2)
                return
            }
            this.showAvatarUpload = false
            this.localAvatar = null
            toast('头像上传成功', 2)
        },
        saveProfile() {
            if (!this.profileForm.nickName) {
                toast('请输入昵称', 2)
                return
            }
            this.userInfo.nickName = this.profileForm.nickName
            this.userInfo.intro = this.profileForm.intro
            toast('资料保存成功', 2)
        },
        confirmDelete() {
            if (!this.deleteForm.password) {
                toast('请输入密码', 2)
                return
            }
            this.showDeleteConfirm = false
            this.deleteForm.password = ''
            toast('账号注销成功', 2)
        },
        addInterestTag() {
            if (this.newTag && !this.profileForm.tags.includes(this.newTag)) {
                this.profileForm.tags.push(this.newTag)
                this.newTag = ''
            }
        },
        removeInterestTag(tag) {
            this.profileForm.tags = this.profileForm.tags.filter(t => t !== tag)
        },
        unblockUser(userId) {
            this.blockedUsers = this.blockedUsers.filter(u => u.id !== userId)
            toast('已取消屏蔽', 2)
        },
        unblockTopic(topicId) {
            this.blockedTopics = this.blockedTopics.filter(t => t.id !== topicId)
            toast('已取消屏蔽', 2)
        },
        unfollowTag(tagId) {
            this.followedTags = this.followedTags.filter(t => t.id !== tagId)
            toast('已取消关注', 2)
        }
    }
}
</script>

<style lang="less" scoped>
@import '../../../styles/common';

.settings-page {
    min-height: 100vh;
    background: #f7f8fa;
}

.settings-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 24px;
    display: flex;
    gap: 24px;
}

.settings-sidebar {
    width: 200px;
    flex-shrink: 0;
    background: #fff;
    border-radius: 8px;
    padding: 16px 0;
    box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.sidebar-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 24px;
    font-size: 14px;
    color: #515767;
    cursor: pointer;
    transition: all 0.2s;
    &:hover {
        background: #f7f8fa;
        color: #1e80ff;
    }
    &.active {
        background: #eaf2ff;
        color: #1e80ff;
        font-weight: 500;
    }
    i {
        font-size: 18px;
    }
}

.settings-main {
    flex: 1;
    min-width: 0;
}

.section-content {
    background: #fff;
    border-radius: 8px;
    padding: 24px;
    box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.section-header {
    margin-bottom: 24px;
    h2 {
        font-size: 20px;
        font-weight: 600;
        color: #252933;
        margin: 0;
    }
}

.settings-card {
    margin-bottom: 24px;
    border: 1px solid #f2f3f5;
    border-radius: 8px;
    overflow: hidden;
    &:last-child {
        margin-bottom: 0;
    }
}

.card-header {
    padding: 16px 20px;
    font-size: 14px;
    font-weight: 600;
    color: #252933;
    background: #f7f8fa;
    border-bottom: 1px solid #f2f3f5;
}

.card-body {
    padding: 20px;
}

.form-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f2f3f5;
    &:last-child {
        border-bottom: none;
    }
    label {
        font-size: 14px;
        color: #515767;
        flex-shrink: 0;
    }
}

.form-content {
    display: flex;
    align-items: center;
    gap: 12px;
}

.form-value {
    font-size: 14px;
    color: #252933;
    &.status-active {
        color: #52c41a;
    }
    &.status-pending {
        color: #faad14;
    }
}

.edit-btn {
    padding: 6px 16px;
    border: 1px solid #e4e6eb;
    border-radius: 4px;
    background: #fff;
    color: #515767;
    font-size: 13px;
    cursor: pointer;
    transition: all 0.2s;
    &:hover {
        border-color: #1e80ff;
        color: #1e80ff;
    }
    &.primary {
        background: #1e80ff;
        color: #fff;
        border-color: #1e80ff;
        &:hover {
            background: #4096ff;
        }
    }
}

.avatar-upload-section {
    display: flex;
    align-items: center;
    gap: 24px;
}

.current-avatar {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    object-fit: cover;
    border: 3px solid #1e80ff;
}

.avatar-actions {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.security-item, .notification-item, .privacy-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 0;
    border-bottom: 1px solid #f2f3f5;
    &:last-child {
        border-bottom: none;
    }
}

.security-info, .notification-info, .privacy-info {
    flex: 1;
}

.security-label, .notification-label, .privacy-label {
    display: block;
    font-size: 14px;
    color: #252933;
    margin-bottom: 4px;
}

.security-desc, .notification-desc, .privacy-desc {
    font-size: 13px;
    color: #8a919f;
}

.danger-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 0;
}

.danger-info {
    flex: 1;
}

.danger-label {
    display: block;
    font-size: 14px;
    color: #ff4d4f;
    font-weight: 500;
    margin-bottom: 4px;
}

.danger-desc {
    font-size: 13px;
    color: #8a919f;
}

.danger-btn {
    padding: 6px 16px;
    border: 1px solid #ff4d4f;
    border-radius: 4px;
    background: #fff;
    color: #ff4d4f;
    font-size: 13px;
    cursor: pointer;
    &:hover {
        background: #fff2f0;
    }
}

.avatar-upload-wrap {
    position: relative;
    display: flex;
    justify-content: center;
    padding: 20px 0;
}

.avatar-uploader {
    border: 1px dashed #d9d9d9;
    border-radius: 8px;
    cursor: pointer;
    position: relative;
    width: 200px;
    height: 200px;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
}

.avatar-preview {
    width: 200px;
    height: 200px;
    border-radius: 8px;
    object-fit: cover;
}

.avatar-upload-icon {
    font-size: 36px;
    color: #8c939d;
}

.delete-warning {
    text-align: center;
    padding: 20px 0;
    i {
        font-size: 48px;
        color: #faad14;
        margin-bottom: 16px;
    }
    p {
        font-size: 16px;
        color: #252933;
        margin: 0 0 8px 0;
    }
    .warning-desc {
        font-size: 13px;
        color: #8a919f;
        margin-top: 12px;
    }
}

.general-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 0;
    border-bottom: 1px solid #f2f3f5;
    &:last-child { border-bottom: none; }
}
.general-info { flex: 1; }
.general-label {
    display: block;
    font-size: 14px;
    color: #252933;
    margin-bottom: 4px;
}
.general-desc { font-size: 13px; color: #8a919f; }

.empty-block {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 40px 0;
}
.empty-icon { font-size: 32px; margin-bottom: 8px; }
.empty-text { font-size: 14px; color: #8a919f; }

.blocked-list { padding: 8px 0; }
.blocked-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 0;
    border-bottom: 1px solid #f2f3f5;
    &:last-child { border-bottom: none; }
}
.blocked-avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    object-fit: cover;
}
.blocked-info { flex: 1; }
.blocked-name { font-size: 14px; color: #252933; }
.blocked-time { font-size: 12px; color: #8a919f; }
.unblock-btn {
    padding: 6px 12px;
    border: 1px solid #e4e6eb;
    border-radius: 4px;
    background: #fff;
    color: #8a919f;
    font-size: 13px;
    cursor: pointer;
}

.blocked-topics {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}
.blocked-topic-item {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px 12px;
    background: #f7f8fa;
    border-radius: 4px;
    font-size: 13px;
    color: #515767;
}
.remove-topic {
    cursor: pointer;
    color: #8a919f;
    &:hover { color: #ff4d4f; }
}

.empty-tags {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 40px 0;
}
.add-tag-btn {
    margin-top: 12px;
    padding: 8px 24px;
    border: none;
    border-radius: 4px;
    background: #1e80ff;
    color: #fff;
    font-size: 14px;
    cursor: pointer;
}

.followed-tags-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 12px;
}
.tag-card {
    padding: 16px;
    border: 1px solid #f2f3f5;
    border-radius: 8px;
}
.tag-name {
    display: block;
    font-size: 14px;
    font-weight: 500;
    color: #252933;
    margin-bottom: 4px;
}
.tag-count {
    display: block;
    font-size: 12px;
    color: #8a919f;
    margin-bottom: 12px;
}
.unfollow-tag-btn {
    padding: 4px 12px;
    border: 1px solid #e4e6eb;
    border-radius: 4px;
    background: #fff;
    color: #8a919f;
    font-size: 12px;
    cursor: pointer;
}

.avatar-hint {
    display: block;
    font-size: 12px;
    color: #8a919f;
    margin-top: 8px;
}

.input-hint {
    font-size: 12px;
    color: #c4c9d1;
    margin-left: 8px;
}

.interest-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
}
.interest-tag {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    background: #eaf2ff;
    color: #1e80ff;
    border-radius: 4px;
    font-size: 13px;
}
.remove-tag {
    cursor: pointer;
    font-size: 14px;
    &:hover { color: #ff4d4f; }
}
.add-tag-input {
    padding: 4px 10px;
    border: 1px solid #e4e6eb;
    border-radius: 4px;
    font-size: 13px;
    outline: none;
    &:focus { border-color: #1e80ff; }
}

.account-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 0;
    border-bottom: 1px solid #f2f3f5;
    &:last-child { border-bottom: none; }
}
.account-info {
    display: flex;
    align-items: center;
    gap: 12px;
}
.account-icon { font-size: 20px; }
.account-label { font-size: 14px; color: #252933; }
.account-right {
    display: flex;
    align-items: center;
    gap: 12px;
}
.account-value { font-size: 14px; color: #8a919f; }
.account-btn {
    padding: 6px 16px;
    border: 1px solid #e4e6eb;
    border-radius: 4px;
    background: #fff;
    color: #1e80ff;
    font-size: 13px;
    cursor: pointer;
}

@media screen and (max-width: 768px) {
    .settings-content {
        flex-direction: column;
    }
    .settings-sidebar {
        width: 100%;
        display: flex;
        flex-wrap: wrap;
        padding: 0;
    }
    .sidebar-item {
        padding: 12px 16px;
        border-radius: 4px;
        margin: 8px;
    }
}
</style>