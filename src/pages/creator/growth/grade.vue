<template>
  <div class="grade-page">
    <div class="grade-header">
      <div class="header-content">
        <div class="level-overview">
          <div class="level-icon-wrap">
            <div class="level-icon">💪</div>
            <div class="level-badge">Lv.{{ levelInfo.powerLevel }}</div>
          </div>
          <div class="level-info">
            <div class="level-title">{{ levelInfo.powerTitle }}</div>
            <div class="level-value">{{ levelInfo.powerValue }} 逐力值</div>
          </div>
        </div>
        <div class="level-progress-section">
          <div class="progress-bar-wrap">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: powerProgress + '%' }"></div>
            </div>
            <div class="progress-text">
              <span>距离 Lv.{{ nextLevel }} 还需 {{ powerNextLevelScore }} 逐力值</span>
            </div>
          </div>
          <div class="level-stats">
            <div class="stat-item">
              <span class="stat-value">{{ unlockedCount }}</span>
              <span class="stat-label">已解锁权益</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-value">{{ nextLevelPermissionsCount }}</span>
              <span class="stat-label">下一级解锁</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="grade-content">
      <div class="level-tabs-section">
        <div class="section-header">
          <div class="section-title">等级权益</div>
          <div class="section-tip">提升等级解锁更多创作权限</div>
        </div>
        <div class="level-tabs">
          <div
            v-for="level in levelConfigs"
            :key="level.level"
            class="level-tab"
            :class="{
              'current': level.level === levelInfo.powerLevel,
              'unlocked': level.level <= levelInfo.powerLevel,
              'locked': level.level > levelInfo.powerLevel
            }"
            @click="selectLevel(level)"
          >
            <div class="level-num">Lv.{{ level.level }}</div>
            <div class="level-icon-small">{{ getLevelIcon(level.level) }}</div>
            <div v-if="level.level > levelInfo.powerLevel" class="level-lock">🔒</div>
            <div v-if="level.level <= levelInfo.powerLevel && level.level === levelInfo.powerLevel" class="level-current-badge">当前</div>
          </div>
        </div>
        <div class="level-details">
          <div class="detail-header">
            <div class="detail-title">{{ selectedLevelTitle }}</div>
            <div class="detail-score" v-if="selectedLevel && selectedLevel.level > levelInfo.powerLevel">
              需 {{ selectedLevel.minScore }} 逐力值
            </div>
          </div>
          <div class="detail-permissions">
            <div v-if="selectedLevelPermissions.length > 0" class="permissions-list">
              <div
                v-for="perm in selectedLevelPermissions"
                :key="perm.code"
                class="permission-tag"
                :class="{ 'locked': !isPermissionUnlocked(perm.code) }"
              >
                <span class="tag-icon">{{ perm.icon }}</span>
                <span class="tag-name">{{ perm.name }}</span>
                <span v-if="!isPermissionUnlocked(perm.code)" class="tag-lock">🔒</span>
              </div>
            </div>
            <div v-else class="empty-permissions">该等级暂无权益</div>
          </div>
        </div>
      </div>

      <div class="unlocked-section">
        <div class="section-header">
          <div class="section-title">已解锁权益</div>
          <div class="section-count">共 {{ unlockedPermissions.length }} 项</div>
        </div>
        <div class="unlocked-grid">
          <div v-for="perm in unlockedPermissions" :key="perm.code" class="unlocked-card">
            <div class="card-icon">{{ perm.icon }}</div>
            <div class="card-name">{{ perm.name }}</div>
            <div class="card-desc">{{ getPermissionDesc(perm.code) }}</div>
          </div>
        </div>
      </div>

      <div class="tasks-section">
        <div class="section-header">
          <div class="section-title">升级任务</div>
          <div class="section-tip">完成任务提升等级</div>
        </div>
        <div class="tasks-list">
          <div v-for="task in tasks" :key="task.code" class="task-item">
            <div class="task-icon">{{ task.icon }}</div>
            <div class="task-info">
              <div class="task-name">{{ task.name }}</div>
              <div class="task-desc">{{ task.desc }}</div>
            </div>
            <div class="task-right">
              <div class="task-score">+{{ task.score }} 逐力值</div>
              <div class="task-limit" v-if="task.limit">{{ task.limit }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { toast } from "@/utils/toast"

export default {
  name: 'CreatorGrade',
  data() {
    return {
      userId: 1,
      levelInfo: {
        powerValue: 0,
        powerLevel: 1,
        powerTitle: '',
        powerDescription: ''
      },
      permissions: [],
      selectedLevel: null,
      powerNextLevelScore: 0,
      tasks: [
        { code: 'publish_article', name: '发布文章', icon: '✍️', desc: '发布原创技术文章', score: 10, limit: '每日上限2篇' },
        { code: 'article_like', name: '文章获得赞', icon: '👍', desc: '文章被用户点赞', score: 2, limit: '无上限' },
        { code: 'article_comment', name: '文章获得评论', icon: '💬', desc: '文章收到用户评论', score: 3, limit: '无上限' },
        { code: 'article_collect', name: '文章获得收藏', icon: '⭐', desc: '文章被用户收藏', score: 5, limit: '无上限' }
      ]
    }
  },
  computed: {
    levelConfigs() {
      return [
        { level: 1, minScore: 0, maxScore: 99, title: '新手创作者' },
        { level: 2, minScore: 100, maxScore: 499, title: '初级创作者' },
        { level: 3, minScore: 500, maxScore: 1499, title: '中级创作者' },
        { level: 4, minScore: 1500, maxScore: 2999, title: '高级创作者' },
        { level: 5, minScore: 3000, maxScore: 4999, title: '资深创作者' },
        { level: 6, minScore: 5000, maxScore: 7999, title: '优秀创作者' },
        { level: 7, minScore: 8000, maxScore: 11999, title: '杰出创作者' },
        { level: 8, minScore: 12000, maxScore: 19999, title: '卓越创作者' },
        { level: 9, minScore: 20000, maxScore: 29999, title: '顶级创作者' },
        { level: 10, minScore: 30000, maxScore: 999999, title: '传奇创作者' }
      ]
    },
    nextLevel() {
      const maxLevel = this.levelConfigs.length
      return Math.min(this.levelInfo.powerLevel + 1, maxLevel)
    },
    powerProgress() {
      const levelConfig = this.getPowerLevelConfig()
      if (!levelConfig) return 0
      const min = levelConfig.minScore
      const max = levelConfig.maxScore
      const current = this.levelInfo.powerValue
      this.powerNextLevelScore = Math.max(0, max - current + 1)
      return Math.min(((current - min) / (max - min)) * 100, 100)
    },
    unlockedCount() {
      return this.unlockedPermissions.length
    },
    nextLevelPermissionsCount() {
      const nextConfig = this.levelConfigs.find(c => c.level === this.nextLevel)
      if (!nextConfig) return 0
      return this.getLevelPermissions(nextConfig.level).length
    },
    selectedLevelTitle() {
      if (!this.selectedLevel) return ''
      return this.selectedLevel.title
    },
    selectedLevelPermissions() {
      if (!this.selectedLevel) return []
      return this.getLevelPermissions(this.selectedLevel.level)
    },
    unlockedPermissions() {
      return this.permissions.filter(p => this.isPermissionUnlocked(p.code))
    }
  },
  mounted() {
    this.loadData()
    this.selectedLevel = this.levelConfigs.find(c => c.level === this.levelInfo.powerLevel) || this.levelConfigs[0]
  },
  methods: {
    async loadData() {
      await this.loadLevelInfo()
      await this.loadPermissions()
    },
    async loadLevelInfo() {
      try {
        const response = await this.$http.get(`/api/v1/level/user/${this.userId}/info`)
        if (response.data) {
          this.levelInfo = {
            powerValue: response.data.powerValue || 0,
            powerLevel: response.data.powerLevel || 1,
            powerTitle: response.data.powerTitle || '',
            powerDescription: response.data.powerDescription || ''
          }
        }
      } catch (error) {
        this.loadMockLevelInfo()
      }
    },
    async loadPermissions() {
      try {
        const response = await this.$http.get(`/api/v1/level/user/${this.userId}/permissions`)
        if (response.data) {
          this.permissions = this.mapPermissions(response.data)
        }
      } catch (error) {
        this.permissions = this.getMockPermissions()
      }
    },
    loadMockLevelInfo() {
      this.levelInfo = {
        powerValue: 2300,
        powerLevel: 4,
        powerTitle: '高级创作者',
        powerDescription: '创作经验丰富'
      }
    },
    getMockPermissions() {
      return [
        { code: 'can_add_video', name: '添加视频', icon: '🎬', level: 2 },
        { code: 'can_add_2_tags', name: '2个标签', icon: '🏷️', level: 1 },
        { code: 'can_schedule_publish', name: '定时发布', icon: '⏰', level: 3 },
        { code: 'can_add_3_tags', name: '3个标签', icon: '🏷️🏷️', level: 4 },
        { code: 'can_send_private_message', name: '私信权限', icon: '💬', level: 2 },
        { code: 'can_set_comment_permission', name: '评论区权限设置', icon: '🔒', level: 3 },
        { code: 'can_create_poll', name: '发起投票', icon: '📊', level: 5 },
        { code: 'can_become_contributor', name: '成为共建者', icon: '⭐', level: 6 },
        { code: 'can_be_recommended', name: '文章自动推荐', icon: '🔥', level: 7 },
        { code: 'can_add_4_tags', name: '4个标签', icon: '🏷️🏷️🏷️', level: 8 },
        { code: 'can_create_course', name: '创作小册', icon: '📚', level: 9 }
      ]
    },
    mapPermissions(permissionCodes) {
      const permissionMap = {
        'can_add_video': { name: '添加视频', icon: '🎬', level: 2 },
        'can_add_2_tags': { name: '2个标签', icon: '🏷️', level: 1 },
        'can_schedule_publish': { name: '定时发布', icon: '⏰', level: 3 },
        'can_add_3_tags': { name: '3个标签', icon: '🏷️🏷️', level: 4 },
        'can_send_private_message': { name: '私信权限', icon: '💬', level: 2 },
        'can_set_comment_permission': { name: '评论区权限设置', icon: '🔒', level: 3 },
        'can_create_poll': { name: '发起投票', icon: '📊', level: 5 },
        'can_become_contributor': { name: '成为共建者', icon: '⭐', level: 6 },
        'can_be_recommended': { name: '文章自动推荐', icon: '🔥', level: 7 },
        'can_add_4_tags': { name: '4个标签', icon: '🏷️🏷️🏷️', level: 8 },
        'can_create_course': { name: '创作小册', icon: '📚', level: 9 }
      }
      return permissionCodes.map(code => ({
        code,
        ...permissionMap[code] || { name: code, icon: '✨', level: 1 }
      }))
    },
    getPowerLevelConfig() {
      return this.levelConfigs.find(c => c.level === this.levelInfo.powerLevel)
    },
    getLevelIcon(level) {
      const icons = ['🌱', '🌿', '🌳', '🌲', '🏔️', '⭐', '💫', '🌟', '👑', '🏆']
      return icons[level - 1] || '⭐'
    },
    selectLevel(level) {
      this.selectedLevel = level
    },
    getLevelPermissions(level) {
      const allPermissions = [
        { code: 'can_add_2_tags', name: '2个标签', icon: '🏷️', level: 1 },
        { code: 'can_add_video', name: '添加视频', icon: '🎬', level: 2 },
        { code: 'can_send_private_message', name: '私信权限', icon: '💬', level: 2 },
        { code: 'can_schedule_publish', name: '定时发布', icon: '⏰', level: 3 },
        { code: 'can_set_comment_permission', name: '评论区权限设置', icon: '🔒', level: 3 },
        { code: 'can_add_3_tags', name: '3个标签', icon: '🏷️🏷️', level: 4 },
        { code: 'can_create_poll', name: '发起投票', icon: '📊', level: 5 },
        { code: 'can_become_contributor', name: '成为共建者', icon: '⭐', level: 6 },
        { code: 'can_be_recommended', name: '文章自动推荐', icon: '🔥', level: 7 },
        { code: 'can_add_4_tags', name: '4个标签', icon: '🏷️🏷️🏷️', level: 8 },
        { code: 'can_create_course', name: '创作小册', icon: '📚', level: 9 }
      ]
      return allPermissions.filter(p => p.level === level)
    },
    isPermissionUnlocked(code) {
      return this.permissions.some(p => p.code === code)
    },
    getPermissionDesc(code) {
      const descMap = {
        'can_add_video': '在文章中插入视频内容',
        'can_add_2_tags': '发布文章时可添加2个标签',
        'can_schedule_publish': '设置文章定时发布时间',
        'can_add_3_tags': '发布文章时可添加3个标签',
        'can_send_private_message': '与粉丝私信交流',
        'can_set_comment_permission': '管理评论区权限设置',
        'can_create_poll': '在文章中发起投票互动',
        'can_become_contributor': '成为社区内容共建者',
        'can_be_recommended': '文章获得平台自动推荐',
        'can_add_4_tags': '发布文章时可添加4个标签',
        'can_create_course': '创建和发布付费小册'
      }
      return descMap[code] || ''
    }
  }
}
</script>

<style lang="less" scoped>
@import '../layout/styles/variables.less';

.grade-page {
  min-height: 100vh;
  background: @bgGray;
}

.grade-header {
  background: linear-gradient(135deg, #6C5CE7 0%, #A29BFE 100%);
  padding: 32px 24px;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
}

.level-overview {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.level-icon-wrap {
  position: relative;
}

.level-icon {
  width: 64px;
  height: 64px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}

.level-badge {
  position: absolute;
  bottom: -4px;
  right: -4px;
  background: #fff;
  color: #6C5CE7;
  font-size: 12px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.level-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.level-title {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
}

.level-value {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.level-progress-section {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  padding: 16px 20px;
}

.progress-bar-wrap {
  margin-bottom: 16px;
}

.progress-bar {
  height: 8px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: #fff;
  border-radius: 4px;
  transition: width 0.5s ease;
}

.progress-text {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
}

.level-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 32px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
}

.stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.stat-divider {
  width: 1px;
  height: 32px;
  background: rgba(255, 255, 255, 0.3);
}

.grade-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  margin-top: -20px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: @textPrimary;
}

.section-tip {
  font-size: 13px;
  color: @textMuted;
}

.section-count {
  font-size: 13px;
  color: @textMuted;
}

.level-tabs-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: @cardShadow;
}

.level-tabs {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 16px;
  border-bottom: 1px solid @borderLight;
  margin-bottom: 20px;

  &::-webkit-scrollbar {
    display: none;
  }
}

.level-tab {
  flex-shrink: 0;
  position: relative;
  width: 72px;
  padding: 12px 8px;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;

  &:hover {
    background: #f0f0f0;
  }

  &.current {
    background: linear-gradient(135deg, #6C5CE7 0%, #A29BFE 100%);

    .level-num,
    .level-icon-small {
      color: #fff;
    }
  }

  &.unlocked:not(.current) {
    background: #fff;
    border: 1px solid #e8e8e8;
  }

  &.locked {
    opacity: 0.6;
    background: #fafafa;
    border: 1px dashed #e8e8e8;
  }
}

.level-num {
  font-size: 14px;
  font-weight: 600;
  color: @textPrimary;
}

.level-icon-small {
  font-size: 20px;
}

.level-lock {
  position: absolute;
  top: 4px;
  right: 4px;
  font-size: 12px;
}

.level-current-badge {
  position: absolute;
  top: -8px;
  left: 50%;
  transform: translateX(-50%);
  background: #FF6B35;
  color: #fff;
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 8px;
}

.level-details {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.detail-title {
  font-size: 16px;
  font-weight: 600;
  color: @textPrimary;
}

.detail-score {
  font-size: 13px;
  color: #FF6B35;
  font-weight: 500;
}

.detail-permissions {
  min-height: 60px;
}

.permissions-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.permission-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: #fff;
  border-radius: 20px;
  font-size: 13px;
  color: @textSecondary;
  border: 1px solid #e8e8e8;

  &.locked {
    opacity: 0.5;
    background: #f5f5f5;
  }
}

.tag-icon {
  font-size: 14px;
}

.tag-name {
  font-weight: 500;
}

.tag-lock {
  font-size: 12px;
}

.empty-permissions {
  font-size: 14px;
  color: @textMuted;
  text-align: center;
  padding: 20px;
}

.unlocked-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: @cardShadow;
}

.unlocked-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.unlocked-card {
  padding: 20px;
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f2ff 100%);
  border-radius: 10px;
  border-left: 4px solid #6C5CE7;
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-2px);
  }
}

.card-icon {
  font-size: 28px;
  margin-bottom: 10px;
}

.card-name {
  font-size: 15px;
  font-weight: 600;
  color: @textPrimary;
  margin-bottom: 6px;
}

.card-desc {
  font-size: 12px;
  color: @textMuted;
  line-height: 1.5;
}

.tasks-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: @cardShadow;
}

.tasks-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.task-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid @borderLight;

  &:last-child {
    border-bottom: none;
  }
}

.task-icon {
  font-size: 28px;
  margin-right: 16px;
  width: 48px;
  text-align: center;
}

.task-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.task-name {
  font-size: 15px;
  font-weight: 500;
  color: @textPrimary;
}

.task-desc {
  font-size: 13px;
  color: @textMuted;
}

.task-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.task-score {
  font-size: 14px;
  font-weight: 600;
  color: #6C5CE7;
}

.task-limit {
  font-size: 12px;
  color: @textMuted;
}

@media screen and (max-width: 768px) {
  .grade-header {
    padding: 24px 16px;
  }

  .level-icon {
    width: 52px;
    height: 52px;
    font-size: 26px;
  }

  .level-title {
    font-size: 18px;
  }

  .grade-content {
    padding: 16px;
  }

  .level-tabs-section,
  .unlocked-section,
  .tasks-section {
    padding: 16px;
  }

  .unlocked-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 12px;
  }

  .unlocked-card {
    padding: 16px;
  }

  .card-icon {
    font-size: 24px;
  }

  .task-item {
    padding: 14px 0;
  }

  .task-icon {
    font-size: 24px;
    width: 40px;
    margin-right: 12px;
  }

  .level-stats {
    gap: 20px;
  }

  .stat-value {
    font-size: 20px;
  }
}
</style>