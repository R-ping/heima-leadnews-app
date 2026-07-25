<template>
    <div class="settings-page">
        <div class="art-top" v-if="!isDesktop"><HomeBar/></div>
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
                            <!-- 头像上传 -->
                            <div class="avatar-upload-section">
                                <img :src="profileForm.avatarUrl || defaultAvatar" class="current-avatar" alt="avatar">
                                <div class="avatar-actions">
                                    <button class="edit-btn primary" @click="triggerAvatarUpload">上传头像</button>
                                    <input type="file" ref="avatarInput" accept="image/jpeg,image/png,image/webp" style="display:none" @change="handleAvatarFileChange">
                                    <span class="avatar-hint">格式: 支持JPG、PNG、WebP | 大小: 5M以内</span>
                                </div>
                            </div>
                            
                            <div class="profile-form">
                                <!-- 用户名 -->
                                <div class="form-item-row">
                                    <label class="form-label">用户名 <span class="required">*</span></label>
                                    <div class="form-input-wrap">
                                        <input class="form-input" v-model="profileForm.username" placeholder="请输入用户名" maxlength="20" />
                                        <span class="input-hint">{{ profileForm.username.length }}/20</span>
                                    </div>
                                </div>
                                
                                <!-- 开始工作 -->
                                <div class="form-item-row">
                                    <label class="form-label">开始工作</label>
                                    <div class="form-input-wrap">
                                        <input class="form-input" type="month" v-model="profileForm.careerStartDate" />
                                    </div>
                                </div>
                                
                                <!-- 职业方向 -->
                                <div class="form-item-row">
                                    <label class="form-label">职业方向 <span class="required">*</span></label>
                                    <div class="form-input-wrap">
                                        <select class="form-select" v-model="profileForm.careerDirection">
                                            <option value="">请选择</option>
                                            <option value="backend">后端开发</option>
                                            <option value="frontend">前端开发</option>
                                            <option value="mobile">移动端开发</option>
                                            <option value="ai">人工智能</option>
                                            <option value="product">产品经理</option>
                                            <option value="devops">运维</option>
                                            <option value="test">测试</option>
                                            <option value="data">数据分析</option>
                                            <option value="design">设计师</option>
                                            <option value="other">其他</option>
                                        </select>
                                    </div>
                                </div>
                                
                                <!-- 职位 -->
                                <div class="form-item-row">
                                    <label class="form-label">职位</label>
                                    <div class="form-input-wrap">
                                        <input class="form-input" v-model="profileForm.position" placeholder="请输入你的职位" maxlength="50" />
                                        <span class="input-hint">{{ (profileForm.position || '').length }}/50</span>
                                    </div>
                                </div>
                                
                                <!-- 公司 -->
                                <div class="form-item-row">
                                    <label class="form-label">公司</label>
                                    <div class="form-input-wrap">
                                        <input class="form-input" v-model="profileForm.company" placeholder="请输入你的公司" maxlength="50" />
                                        <span class="input-hint">{{ (profileForm.company || '').length }}/50</span>
                                    </div>
                                </div>
                                
                                <!-- 个人主页 -->
                                <div class="form-item-row">
                                    <label class="form-label">个人主页</label>
                                    <div class="form-input-wrap">
                                        <input class="form-input" v-model="profileForm.website" placeholder="请输入你的个人主页" maxlength="100" />
                                        <span class="input-hint">{{ (profileForm.website || '').length }}/100</span>
                                    </div>
                                </div>
                                
                                <!-- 个人介绍 -->
                                <div class="form-item-row">
                                    <label class="form-label">个人介绍</label>
                                    <div class="form-input-wrap">
                                        <textarea class="form-textarea" v-model="profileForm.bio" placeholder="请填写职业技能、擅长的事情、兴趣爱好等" maxlength="100" rows="4"></textarea>
                                        <span class="input-hint">{{ (profileForm.bio || '').length }}/100</span>
                                    </div>
                                </div>
                                
                                <!-- 兴趣标签 -->
                                <div class="form-item-row">
                                    <label class="form-label">兴趣标签 <span class="required">*</span></label>
                                    <div class="form-input-wrap">
                                        <div class="tag-selector">
                                            <!-- 分类导航 -->
                                            <div class="tag-categories">
                                                <span 
                                                    v-for="group in profileForm.tagGroups" 
                                                    :key="group.categoryCode"
                                                    class="tag-category"
                                                    :class="{ active: activeTagCategory === group.categoryCode }"
                                                    @click="activeTagCategory = group.categoryCode"
                                                >{{ group.categoryName }}</span>
                                            </div>
                                            <!-- 标签池 -->
                                            <div class="tag-pool">
                                                <span 
                                                    v-for="tag in currentCategoryTags" 
                                                    :key="tag.id"
                                                    class="tag-option"
                                                    :class="{ selected: profileForm.selectedTagIds.includes(tag.id) }"
                                                    @click="toggleTag(tag.id)"
                                                >{{ tag.tagName }}</span>
                                            </div>
                                        </div>
                                        <!-- 已选标签 -->
                                        <div class="selected-tags" v-if="profileForm.selectedTagIds.length > 0">
                                            <span class="selected-label">已选：</span>
                                            <span 
                                                v-for="tagId in profileForm.selectedTagIds" 
                                                :key="tagId"
                                                class="selected-tag"
                                            >{{ getTagName(tagId) }}<span class="remove-tag" @click="toggleTag(tagId)">×</span></span>
                                        </div>
                                    </div>
                                </div>
                                
                                <!-- 保存按钮 -->
                                <div class="form-item-row">
                                    <label class="form-label"></label>
                                    <div class="form-input-wrap">
                                        <button class="save-btn" @click="saveProfile" :disabled="profileSaving">
                                            {{ profileSaving ? '保存中...' : '保存修改' }}
                                        </button>
                                    </div>
                                </div>
                            </div>
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
                                    <span class="account-value">{{ bindings.phone || '未绑定' }}</span>
                                    <button class="account-btn">换绑</button>
                                </div>
                            </div>
                            <div class="account-item">
                                <div class="account-info">
                                    <span class="account-icon">💬</span>
                                    <span class="account-label">微信</span>
                                </div>
                                <div class="account-right">
                                    <span class="account-value">{{ bindings.wechat && bindings.wechat.bound ? (bindings.wechat.nickname || '已绑定') : '未绑定' }}</span>
                                    <button class="account-btn">{{ bindings.wechat && bindings.wechat.bound ? '解绑' : '绑定' }}</button>
                                </div>
                            </div>
                            <div class="account-item">
                                <div class="account-info">
                                    <span class="account-icon">📢</span>
                                    <span class="account-label">新浪微博</span>
                                </div>
                                <div class="account-right">
                                    <span class="account-value">{{ bindings.weibo && bindings.weibo.bound ? (bindings.weibo.nickname || '已绑定') : '未绑定' }}</span>
                                    <button class="account-btn">{{ bindings.weibo && bindings.weibo.bound ? '解绑' : '绑定' }}</button>
                                </div>
                            </div>
                            <div class="account-item">
                                <div class="account-info">
                                    <span class="account-icon">💻</span>
                                    <span class="account-label">GitHub</span>
                                </div>
                                <div class="account-right">
                                    <span class="account-value">{{ bindings.github && bindings.github.bound ? (bindings.github.nickname || '已绑定') : '未绑定' }}</span>
                                    <button class="account-btn">{{ bindings.github && bindings.github.bound ? '解绑' : '绑定' }}</button>
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
                                <button class="account-btn" @click="showPasswordDialog = true">重置</button>
                            </div>
                            <div class="account-item">
                                <div class="account-info">
                                    <span class="account-icon">🗑️</span>
                                    <span class="account-label">账号注销</span>
                                </div>
                                <button class="danger-btn" @click="handleDeleteAccount">注销</button>
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

        <!-- 密码修改弹窗 -->
        <el-dialog title="修改密码" :visible.sync="showPasswordDialog" width="420px" :close-on-click-modal="false">
            <div class="password-form">
                <div class="form-item">
                    <label>旧密码</label>
                    <input type="password" v-model="passwordForm.oldPassword" class="form-input" placeholder="请输入旧密码">
                </div>
                <div class="form-item">
                    <label>新密码</label>
                    <input type="password" v-model="passwordForm.newPassword" class="form-input" placeholder="请输入新密码（至少6位）">
                </div>
                <div class="form-item">
                    <label>确认密码</label>
                    <input type="password" v-model="passwordForm.confirmPassword" class="form-input" placeholder="请再次输入新密码">
                </div>
            </div>
            <span slot="footer" class="dialog-footer">
                <el-button @click="showPasswordDialog = false">取消</el-button>
                <el-button type="primary" @click="handleUpdatePassword" :disabled="passwordSaving">
                    {{ passwordSaving ? '保存中...' : '确定' }}
                </el-button>
            </span>
        </el-dialog>

        <!-- 注销确认弹窗 -->
        <el-dialog title="账号注销" :visible.sync="showDeleteAccountDialog" width="420px" :close-on-click-modal="false">
            <div class="delete-account-content">
                <p class="delete-warning">确认要注销您的账号吗？</p>
                <p class="delete-desc">注销后，您的所有数据将被永久删除且无法恢复，包括文章、动态、收藏等内容。</p>
            </div>
            <span slot="footer" class="dialog-footer">
                <el-button @click="showDeleteAccountDialog = false">取消</el-button>
                <el-button type="danger" @click="confirmDeleteAccount" :disabled="deleteAccountConfirming">
                    {{ deleteAccountConfirming ? '注销中...' : '确认注销' }}
                </el-button>
            </span>
        </el-dialog>

        </div>
</template>

<script>
import HomeBar from '@/components/bars/home_bar'
import Utils from '@/utils/env'
import defaultAvatar from '@/static/images/creator/avatar.jpg'
import { getUserProfile, updateUserProfile, uploadAvatar, getBindings, updatePassword, deleteAccount } from '@/apis/user'
import { toast } from '@/utils/toast'

export default {
    name: 'UserSettings',
    components: { HomeBar },
    data() {
        return {
            activeSection: 'profile',
            showAvatarUpload: false,
            showAddTag: false,
            localAvatar: null,
            profileForm: {
                username: '',
                avatarUrl: '',
                careerStartDate: '',
                careerDirection: '',
                position: '',
                company: '',
                website: '',
                bio: '',
                selectedTagIds: [],
                tagGroups: []
            },
            profileSaving: false,
            profileLoaded: false,
            activeTagCategory: '',
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
            ],
            bindings: {
                phone: '',
                wechat: { bound: false, nickname: '', avatar: '' },
                weibo: { bound: false, nickname: '', avatar: '' },
                github: { bound: false, nickname: '', avatar: '' }
            },
            showPasswordDialog: false,
            passwordForm: {
                oldPassword: '',
                newPassword: '',
                confirmPassword: ''
            },
            passwordSaving: false,
            showDeleteAccountDialog: false,
            deleteAccountConfirming: false
        }
    },
    computed: {
        isDesktop() {
            return Utils.isDesktop()
        },
        defaultAvatar() {
            return defaultAvatar
        },
        currentCategoryTags() {
            for (const group of this.profileForm.tagGroups) {
                if (group.categoryCode === this.activeTagCategory) {
                    return group.tags
                }
            }
            return []
        }
    },
    methods: {
        async loadProfile() {
            try {
                const res = await getUserProfile()
                if (res && res.code === 200 && res.data) {
                    const data = res.data
                    this.profileForm.username = data.username || ''
                    this.profileForm.avatarUrl = data.avatarUrl || ''
                    this.profileForm.careerStartDate = data.careerStartDate ? data.careerStartDate.substring(0, 7) : ''
                    this.profileForm.careerDirection = data.careerDirection || ''
                    this.profileForm.position = data.position || ''
                    this.profileForm.company = data.company || ''
                    this.profileForm.website = data.website || ''
                    this.profileForm.bio = data.bio || ''
                    this.profileForm.selectedTagIds = data.selectedTagIds || []
                    this.profileForm.tagGroups = data.tagGroups || []
                    // 默认选中第一个分类
                    if (this.profileForm.tagGroups.length > 0 && !this.activeTagCategory) {
                        this.activeTagCategory = this.profileForm.tagGroups[0].categoryCode
                    }
                    this.profileLoaded = true
                }
            } catch (e) {
                toast('加载个人资料失败', 2)
            }
        },
        async saveProfile() {
            // 校验
            if (!this.profileForm.username || this.profileForm.username.trim().length < 5) {
                toast('用户名至少需要5个字符', 2)
                return
            }
            if (this.profileForm.username.trim().length > 20) {
                toast('用户名不能超过20个字符', 2)
                return
            }
            if (!this.profileForm.careerDirection) {
                toast('请选择职业方向', 2)
                return
            }
            if (this.profileForm.selectedTagIds.length === 0) {
                toast('请至少选择一个兴趣标签', 2)
                return
            }
            
            this.profileSaving = true
            try {
                const params = {
                    username: this.profileForm.username.trim(),
                    careerStartDate: this.profileForm.careerStartDate ? this.profileForm.careerStartDate + '-01' : null,
                    careerDirection: this.profileForm.careerDirection,
                    position: this.profileForm.position || null,
                    company: this.profileForm.company || null,
                    website: this.profileForm.website || null,
                    bio: this.profileForm.bio || null,
                    tagIds: this.profileForm.selectedTagIds
                }
                const res = await updateUserProfile(params)
                if (res && res.code === 200) {
                    toast('资料保存成功', 2)
                } else {
                    toast(res.message || '保存失败', 2)
                }
            } catch (e) {
                toast(e.message || '保存失败', 2)
            } finally {
                this.profileSaving = false
            }
        },
        triggerAvatarUpload() {
            this.$refs.avatarInput.click()
        },
        async handleAvatarFileChange(e) {
            const file = e.target.files[0]
            if (!file) return
            
            // 校验类型
            const allowedTypes = ['image/jpeg', 'image/png', 'image/webp']
            if (!allowedTypes.includes(file.type)) {
                toast('仅支持JPG、PNG、WebP格式的图片', 2)
                return
            }
            
            // 校验大小
            if (file.size > 5 * 1024 * 1024) {
                toast('图片大小不能超过5MB', 2)
                return
            }
            
            try {
                const res = await uploadAvatar(file)
                if (res && res.code === 200 && res.data && res.data.url) {
                    this.profileForm.avatarUrl = res.data.url
                    toast('头像上传成功', 2)
                } else {
                    toast(res.message || '上传失败', 2)
                }
            } catch (e) {
                toast(e.message || '上传失败', 2)
            } finally {
                // 清除input以便重新选择同一文件
                e.target.value = ''
            }
        },
        toggleTag(tagId) {
            const index = this.profileForm.selectedTagIds.indexOf(tagId)
            if (index > -1) {
                this.profileForm.selectedTagIds.splice(index, 1)
            } else {
                this.profileForm.selectedTagIds.push(tagId)
            }
        },
        getTagName(tagId) {
            for (const group of this.profileForm.tagGroups) {
                for (const tag of group.tags) {
                    if (tag.id === tagId) return tag.tagName
                }
            }
            return ''
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
        },
        handleDeleteAccount() {
            this.showDeleteAccountDialog = true
        },
        async confirmDeleteAccount() {
            this.deleteAccountConfirming = true
            try {
                const res = await deleteAccount()
                if (res && res.code === 200) {
                    toast('账号已注销', 2)
                    this.$store.dispatch('logout')
                    this.$router.push('/home')
                } else {
                    toast(res.message || '注销失败', 2)
                }
            } catch (e) {
                toast(e.message || '注销失败', 2)
            } finally {
                this.deleteAccountConfirming = false
                this.showDeleteAccountDialog = false
            }
        },
        async loadBindings() {
            try {
                const res = await getBindings()
                if (res && res.code === 200 && res.data) {
                    this.bindings = res.data
                }
            } catch (e) {
                toast('加载绑定信息失败', 2)
            }
        },
        async handleUpdatePassword() {
            if (!this.passwordForm.oldPassword) {
                toast('请输入旧密码', 2)
                return
            }
            if (!this.passwordForm.newPassword || this.passwordForm.newPassword.length < 6) {
                toast('新密码至少6位', 2)
                return
            }
            if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
                toast('两次输入的密码不一致', 2)
                return
            }
            this.passwordSaving = true
            try {
                const res = await updatePassword({
                    oldPassword: this.passwordForm.oldPassword,
                    newPassword: this.passwordForm.newPassword
                })
                if (res && res.code === 200) {
                    toast('密码修改成功', 2)
                    this.showPasswordDialog = false
                    this.passwordForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
                } else {
                    toast(res.message || '修改失败', 2)
                }
            } catch (e) {
                toast(e.message || '修改失败', 2)
            } finally {
                this.passwordSaving = false
            }
        }
    },
    watch: {
        activeSection(newVal) {
            if (newVal === 'account') {
                this.loadBindings()
            }
        }
    },
    mounted() {
        this.loadProfile()
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

.profile-form {
    margin-top: 24px;
}

.form-item-row {
    display: flex;
    align-items: flex-start;
    padding: 14px 0;
    border-bottom: 1px solid #f2f3f5;
    &:last-child { border-bottom: none; }
}

.form-label {
    width: 100px;
    flex-shrink: 0;
    font-size: 14px;
    color: #515767;
    padding-top: 8px;
    .required { color: #ff4d4f; }
}

.form-input-wrap {
    flex: 1;
    min-width: 0;
}

.form-input {
    width: 100%;
    max-width: 400px;
    padding: 8px 12px;
    border: 1px solid #e4e6eb;
    border-radius: 4px;
    font-size: 14px;
    color: #333;
    outline: none;
    box-sizing: border-box;
    &:focus { border-color: #1e80ff; }
    &::placeholder { color: #c4c9d1; }
}

.form-select {
    width: 100%;
    max-width: 400px;
    padding: 8px 12px;
    border: 1px solid #e4e6eb;
    border-radius: 4px;
    font-size: 14px;
    color: #333;
    outline: none;
    background: #fff;
    &:focus { border-color: #1e80ff; }
}

.form-textarea {
    width: 100%;
    max-width: 400px;
    padding: 8px 12px;
    border: 1px solid #e4e6eb;
    border-radius: 4px;
    font-size: 14px;
    color: #333;
    outline: none;
    resize: vertical;
    font-family: inherit;
    &:focus { border-color: #1e80ff; }
    &::placeholder { color: #c4c9d1; }
}

.save-btn {
    padding: 8px 32px;
    border: none;
    border-radius: 4px;
    background: #1e80ff;
    color: #fff;
    font-size: 14px;
    cursor: pointer;
    transition: background 0.2s;
    &:hover { background: #4096ff; }
    &:disabled { background: #a0c4ff; cursor: not-allowed; }
}

.tag-selector {
    max-width: 500px;
}

.tag-categories {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 12px;
    padding-bottom: 12px;
    border-bottom: 1px solid #f2f3f5;
}

.tag-category {
    padding: 4px 12px;
    border-radius: 16px;
    font-size: 13px;
    color: #515767;
    cursor: pointer;
    transition: all 0.2s;
    &:hover { color: #1e80ff; }
    &.active {
        background: #1e80ff;
        color: #fff;
    }
}

.tag-pool {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.tag-option {
    padding: 4px 12px;
    border: 1px solid #e4e6eb;
    border-radius: 4px;
    font-size: 13px;
    color: #515767;
    cursor: pointer;
    transition: all 0.2s;
    &:hover { border-color: #1e80ff; color: #1e80ff; }
    &.selected {
        background: #eaf2ff;
        border-color: #1e80ff;
        color: #1e80ff;
    }
}

.selected-tags {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 6px;
    margin-top: 8px;
    padding-top: 8px;
    border-top: 1px solid #f2f3f5;
}

.selected-label {
    font-size: 12px;
    color: #999;
}

.selected-tag {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 2px 10px;
    background: #eaf2ff;
    color: #1e80ff;
    border-radius: 4px;
    font-size: 12px;
}

.remove-tag {
    cursor: pointer;
    font-size: 14px;
    &:hover { color: #ff4d4f; }
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

.password-form {
    padding: 10px 0;
    .form-item {
        margin-bottom: 16px;
        label {
            display: block;
            font-size: 14px;
            color: #515767;
            margin-bottom: 6px;
        }
        .form-input {
            width: 100%;
            padding: 8px 12px;
            border: 1px solid #e4e6eb;
            border-radius: 4px;
            font-size: 14px;
            box-sizing: border-box;
            &:focus { border-color: #1e80ff; outline: none; }
        }
    }
}

.delete-account-content {
    padding: 10px 0;
    .delete-warning {
        font-size: 16px;
        color: #ff4d4f;
        font-weight: 500;
        margin-bottom: 12px;
    }
    .delete-desc {
        font-size: 14px;
        color: #8a919f;
        line-height: 1.6;
    }
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