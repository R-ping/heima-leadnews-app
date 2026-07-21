<template>
    <div class="circles-page">
        <div class="art-top"><HomeBar/></div>
        
        <div class="circles-content">
            <div class="circles-main">
                <!-- 我的圈子 -->
                <div class="section">
                    <h2 class="section-title">我的圈子</h2>
                    <div class="my-circles">
                        <div 
                            class="circle-card"
                            v-for="circle in myCircles"
                            :key="circle.id"
                        >
                            <div class="circle-icon">{{ circle.icon || '📌' }}</div>
                            <div class="circle-info">
                                <div class="circle-name">{{ circle.name }}</div>
                                <div class="circle-stats">{{ circle.memberCount }} 掘友 · {{ circle.pinsCount }} 沸点</div>
                            </div>
                            <div class="circle-status">已加入</div>
                        </div>
                    </div>
                </div>

                <!-- 圈子广场 -->
                <div class="section">
                    <h2 class="section-title">圈子广场</h2>
                    <div class="circles-tabs">
                        <div 
                            class="tab-item"
                            :class="{ 'active': activeTab === 'recommend' }"
                            @click="activeTab = 'recommend'"
                        >推荐圈子</div>
                        <div 
                            class="tab-item"
                            v-for="category in categories"
                            :key="category.id"
                            :class="{ 'active': activeTab === category.id }"
                            @click="activeTab = category.id"
                        >{{ category.name }}</div>
                        <div class="tab-item more-tab">更多</div>
                    </div>

                    <div class="circles-grid">
                        <div 
                            class="circle-card"
                            v-for="circle in filteredCircles"
                            :key="circle.id"
                        >
                            <div class="circle-icon">{{ circle.icon || '📌' }}</div>
                            <div class="circle-info">
                                <div class="circle-name">{{ circle.name }}</div>
                                <div class="circle-desc">{{ circle.description }}</div>
                                <div class="circle-stats">{{ circle.memberCount }} 掘友 · {{ circle.pinsCount }} 沸点</div>
                            </div>
                            <button 
                                class="join-btn"
                                :class="{ 'joined': isJoined(circle.id) }"
                                @click="toggleJoin(circle)"
                            >
                                {{ isJoined(circle.id) ? '已加入' : '+ 加入' }}
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 侧边栏 -->
            <div class="circles-sidebar">
                <div class="sidebar-section">
                    <h3 class="sidebar-title">人气圈子</h3>
                    <div class="popular-list">
                        <div 
                            class="popular-item"
                            v-for="circle in popularCircles"
                            :key="circle.id"
                        >
                            <div class="popular-icon">{{ circle.icon || '🔥' }}</div>
                            <div class="popular-info">
                                <div class="popular-name">{{ circle.name }}</div>
                                <div class="popular-desc">{{ circle.description }}</div>
                            </div>
                            <button 
                                class="popular-join-btn"
                                :class="{ 'joined': isJoined(circle.id) }"
                                @click="toggleJoin(circle)"
                            >
                                {{ isJoined(circle.id) ? '已加' : '加' }}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import HomeBar from '@/compoents/bars/home_bar'
import { toast } from '@/utils/toast'

export default {
    name: 'Circles',
    components: { HomeBar },
    data() {
        return {
            activeTab: 'recommend',
            
            // 我的圈子
            myCircles: [
                { id: 13, name: '今天学到了', description: '每日学习打卡', memberCount: 17000, pinsCount: 36000, icon: '💡' },
                { id: 15, name: '前端开发圈', description: '前端技术交流', memberCount: 7700, pinsCount: 19000, icon: '🎨' },
                { id: 16, name: '服务端与架构', description: '服务端开发', memberCount: 4400, pinsCount: 3200, icon: '⚙️' },
                { id: 13, name: '大模型生态圈', description: 'AI技术交流', memberCount: 3500, pinsCount: 2800, icon: '🤖' },
                { id: 21, name: 'VibeLaunch', description: '编程交流', memberCount: 2800, pinsCount: 4000, icon: '🚀' }
            ],

            // 一级圈子分类
            categories: [
                { id: 1, name: '技术' },
                { id: 2, name: '职场' },
                { id: 3, name: '吃喝玩乐' },
                { id: 4, name: '资讯' },
                { id: 5, name: '理财' },
                { id: 6, name: '互动交流' },
                { id: 7, name: '书影音' },
                { id: 8, name: '生活' },
                { id: 9, name: '搞笑' },
                { id: 10, name: '情感' },
                { id: 11, name: '游戏' },
                { id: 12, name: '数码' }
            ],

            // 所有二级圈子
            allCircles: [
                // 技术
                { id: 13, parentId: 1, name: '大模型生态圈', description: '大模型技术交流与应用', memberCount: 35000, pinsCount: 80000, icon: '🤖' },
                { id: 14, parentId: 1, name: '微服务生态圈', description: '微服务架构与实践', memberCount: 25000, pinsCount: 60000, icon: '🏗️' },
                { id: 15, parentId: 1, name: '前端开发圈', description: '前端技术与框架交流', memberCount: 40000, pinsCount: 120000, icon: '🎨' },
                { id: 16, parentId: 1, name: '服务端与架构', description: '服务端开发与系统架构', memberCount: 30000, pinsCount: 70000, icon: '⚙️' },
                { id: 17, parentId: 1, name: '技术交流圈', description: '综合技术交流', memberCount: 50000, pinsCount: 150000, icon: '💬' },
                // 职场
                { id: 18, parentId: 2, name: '上班摸鱼', description: '职场摸鱼日常', memberCount: 40000, pinsCount: 100000, icon: '🐟' },
                { id: 19, parentId: 2, name: '内推招聘广场', description: '内推与招聘信息', memberCount: 35000, pinsCount: 80000, icon: '📋' },
                { id: 20, parentId: 2, name: '程序员成长', description: '程序员职业发展', memberCount: 45000, pinsCount: 120000, icon: '📈' },
                // 吃喝玩乐
                { id: 21, parentId: 3, name: '美食探店', description: '美食分享与探店', memberCount: 50000, pinsCount: 150000, icon: '🍜' },
                { id: 22, parentId: 3, name: '旅行日记', description: '旅行经历分享', memberCount: 45000, pinsCount: 120000, icon: '✈️' },
                { id: 23, parentId: 3, name: '什么值得买', description: '好物推荐与评测', memberCount: 60000, pinsCount: 200000, icon: '🛒' },
                { id: 24, parentId: 3, name: '吃货日常', description: '日常美食分享', memberCount: 45000, pinsCount: 130000, icon: '🍕' },
                // 资讯
                { id: 25, parentId: 4, name: '今日新鲜事', description: '每日新鲜资讯', memberCount: 50000, pinsCount: 120000, icon: '📰' },
                { id: 26, parentId: 4, name: '科技前沿', description: '前沿科技资讯', memberCount: 45000, pinsCount: 100000, icon: '🔬' },
                { id: 27, parentId: 4, name: '互联网热点', description: '互联网行业热点', memberCount: 45000, pinsCount: 110000, icon: '🌐' },
                // 理财
                { id: 28, parentId: 5, name: '股票基金', description: '股票与基金投资', memberCount: 30000, pinsCount: 60000, icon: '📊' },
                { id: 29, parentId: 5, name: '投资理财', description: '综合投资理财', memberCount: 35000, pinsCount: 50000, icon: '💰' },
                { id: 30, parentId: 5, name: '省钱攻略', description: '省钱技巧分享', memberCount: 35000, pinsCount: 40000, icon: '💸' },
                // 互动交流
                { id: 31, parentId: 6, name: '新人报道', description: '新人自我介绍', memberCount: 20000, pinsCount: 30000, icon: '👋' },
                { id: 32, parentId: 6, name: '每日打卡', description: '每日打卡记录', memberCount: 25000, pinsCount: 50000, icon: '✅' },
                { id: 33, parentId: 6, name: '问答交流', description: '问题解答与交流', memberCount: 45000, pinsCount: 120000, icon: '❓' },
                // 书影音
                { id: 34, parentId: 7, name: '读书分享', description: '读书心得分享', memberCount: 30000, pinsCount: 70000, icon: '📚' },
                { id: 35, parentId: 7, name: '电影推荐', description: '电影推荐与影评', memberCount: 35000, pinsCount: 80000, icon: '🎬' },
                { id: 36, parentId: 7, name: '音乐分享', description: '音乐推荐与感悟', memberCount: 35000, pinsCount: 100000, icon: '🎵' },
                // 生活
                { id: 37, parentId: 8, name: '日常生活', description: '日常生活分享', memberCount: 40000, pinsCount: 100000, icon: '🏠' },
                { id: 38, parentId: 8, name: '健身打卡', description: '健身与运动', memberCount: 30000, pinsCount: 60000, icon: '💪' },
                { id: 39, parentId: 8, name: '宠物日常', description: '宠物萌宠分享', memberCount: 30000, pinsCount: 80000, icon: '🐱' },
                // 搞笑
                { id: 40, parentId: 9, name: '搞笑段子', description: '搞笑段子分享', memberCount: 50000, pinsCount: 150000, icon: '😂' },
                { id: 41, parentId: 9, name: '神回复', description: '神回复合集', memberCount: 40000, pinsCount: 120000, icon: '💯' },
                // 情感
                { id: 42, parentId: 10, name: '恋爱话题', description: '恋爱经验分享', memberCount: 35000, pinsCount: 80000, icon: '💕' },
                { id: 43, parentId: 10, name: '单身日记', description: '单身生活分享', memberCount: 30000, pinsCount: 60000, icon: '🐶' },
                // 游戏
                { id: 44, parentId: 11, name: '手游交流', description: '手机游戏交流', memberCount: 40000, pinsCount: 100000, icon: '🎮' },
                { id: 45, parentId: 11, name: '端游攻略', description: '端游攻略分享', memberCount: 35000, pinsCount: 80000, icon: '🖥️' },
                // 数码
                { id: 46, parentId: 12, name: '手机评测', description: '手机评测与推荐', memberCount: 30000, pinsCount: 50000, icon: '📱' },
                { id: 47, parentId: 12, name: '电脑配置', description: '电脑配置讨论', memberCount: 30000, pinsCount: 40000, icon: '💻' }
            ],

            // 人气圈子
            popularCircles: [
                { id: 48, name: '上班摸鱼', description: '来分享下你上班看到的好东西吧~', memberCount: 25000, pinsCount: 30000, icon: '🐟' },
                { id: 49, name: '青训营·快乐出发', description: '欢迎同学们，在这里尽情地分享校园...', memberCount: 12000, pinsCount: 15200, icon: '🎓' },
                { id: 50, name: '树洞一下', description: '匿名分享，让树洞倾听你的心事。', memberCount: 15000, pinsCount: 9700, icon: '🌲' },
                { id: 17, name: '技术交流圈', description: '开发者专属的技术交流圈，聊聊技术...', memberCount: 15000, pinsCount: 38000, icon: '💬' },
                { id: 51, name: '内推招聘广场', description: '人才招聘专属频道！欢迎大家发布招...', memberCount: 26000, pinsCount: 15000, icon: '📋' }
            ],

            // 已加入的圈子ID
            joinedCircleIds: [13, 15, 16, 17, 21]
        }
    },
    computed: {
        filteredCircles() {
            if (this.activeTab === 'recommend') {
                return this.allCircles.slice(0, 8)
            }
            return this.allCircles.filter(c => c.parentId === this.activeTab)
        }
    },
    methods: {
        isJoined(circleId) {
            return this.joinedCircleIds.includes(circleId)
        },
        toggleJoin(circle) {
            const index = this.joinedCircleIds.indexOf(circle.id)
            if (index > -1) {
                this.joinedCircleIds.splice(index, 1)
                toast('已退出圈子', 2)
            } else {
                this.joinedCircleIds.push(circle.id)
                toast('加入成功', 2)
            }
        }
    }
}
</script>

<style lang="less" scoped>
@import '../../styles/common';

.circles-page {
    min-height: 100vh;
    background: #f7f8fa;
}

.circles-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 24px;
    display: flex;
    gap: 24px;
}

.circles-main {
    flex: 1;
}

.section {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 16px;
    box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.section-title {
    font-size: 18px;
    font-weight: 600;
    color: #252933;
    margin-bottom: 16px;
}

/* 我的圈子 */
.my-circles {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 12px;
}

.circle-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    border-radius: 8px;
    background: #f7f8fa;
    transition: background-color 0.2s;
    &:hover {
        background: #eaf2ff;
    }
}

.circle-icon {
    font-size: 28px;
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fff;
    border-radius: 8px;
    border: 1px solid #e4e6eb;
}

.circle-info {
    flex: 1;
    min-width: 0;
}

.circle-name {
    font-size: 14px;
    font-weight: 500;
    color: #252933;
    margin-bottom: 2px;
}

.circle-desc {
    font-size: 12px;
    color: #8a919f;
    margin-bottom: 2px;
}

.circle-stats {
    font-size: 12px;
    color: #8a919f;
}

.circle-status {
    font-size: 12px;
    color: #8a919f;
    padding: 4px 12px;
    background: #f2f3f5;
    border-radius: 4px;
}

/* 圈子广场 */
.circles-tabs {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 16px;
}

.tab-item {
    padding: 8px 16px;
    border-radius: 4px;
    font-size: 14px;
    color: #515767;
    cursor: pointer;
    background: #f7f8fa;
    transition: all 0.2s;
    &:hover {
        background: #eaf2ff;
        color: #1e80ff;
    }
    &.active {
        background: #1e80ff;
        color: #fff;
    }
}

.more-tab {
    color: #1e80ff;
    &:hover {
        background: #eaf2ff;
    }
}

.circles-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 12px;
}

.join-btn {
    padding: 6px 16px;
    border: 1px solid #1e80ff;
    border-radius: 4px;
    background: #fff;
    color: #1e80ff;
    font-size: 13px;
    cursor: pointer;
    transition: all 0.2s;
    &:hover:not(.joined) {
        background: #eaf2ff;
    }
    &.joined {
        border-color: #8a919f;
        color: #8a919f;
    }
}

/* 侧边栏 */
.circles-sidebar {
    width: 280px;
    flex-shrink: 0;
}

.sidebar-section {
    background: #fff;
    border-radius: 8px;
    padding: 16px;
    box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.sidebar-title {
    font-size: 15px;
    font-weight: 600;
    color: #252933;
    margin-bottom: 12px;
}

.popular-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.popular-item {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    padding: 8px;
    border-radius: 8px;
    cursor: pointer;
    transition: background-color 0.2s;
    &:hover {
        background: #f7f8fa;
    }
}

.popular-icon {
    font-size: 20px;
    flex-shrink: 0;
}

.popular-info {
    flex: 1;
    min-width: 0;
}

.popular-name {
    font-size: 13px;
    font-weight: 500;
    color: #252933;
    margin-bottom: 2px;
}

.popular-desc {
    font-size: 12px;
    color: #8a919f;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.popular-join-btn {
    padding: 4px 8px;
    border: 1px solid #1e80ff;
    border-radius: 4px;
    background: #fff;
    color: #1e80ff;
    font-size: 12px;
    cursor: pointer;
    flex-shrink: 0;
    &:hover:not(.joined) {
        background: #eaf2ff;
    }
    &.joined {
        border-color: #8a919f;
        color: #8a919f;
    }
}

/* 响应式 */
@media screen and (max-width: 768px) {
    .circles-content {
        flex-direction: column;
        padding: 12px;
    }
    .circles-sidebar {
        width: 100%;
    }
    .my-circles, .circles-grid {
        grid-template-columns: 1fr;
    }
}
</style>