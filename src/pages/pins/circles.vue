<template>
    <div class="circles-page">
        <div class="art-top"><HomeBar/></div>
        
        <div class="circles-content">
            <div class="circles-main">
                <!-- 我的圈子 -->
                <div class="section">
                    <h2 class="section-title">我的圈子</h2>
                    <div class="my-circles">
                        <CircleCard
                            v-for="circle in myCircles"
                            :key="circle.id"
                            :circle="circle"
                            mode="my"
                        />
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
                        <CircleCard
                            v-for="circle in filteredCircles"
                            :key="circle.id"
                            :circle="circle"
                            :joined="isJoined(circle.id)"
                            :show-desc="true"
                            @toggle-join="toggleJoin"
                        />
                    </div>
                </div>
            </div>

            <!-- 侧边栏 -->
            <div class="circles-sidebar">
                <div class="sidebar-section">
                    <h3 class="sidebar-title">人气圈子</h3>
                    <div class="popular-list">
                        <PopularCircleItem
                            v-for="circle in popularCircles"
                            :key="circle.id"
                            :circle="circle"
                            :joined="isJoined(circle.id)"
                            @toggle-join="toggleJoin"
                        />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import HomeBar from '@/components/bars/home_bar'
import { toast } from '@/utils/toast'
import CircleCard from './components/CircleCard.vue'
import PopularCircleItem from './components/PopularCircleItem.vue'
import { myCircles, categories, allCircles, popularCircles } from './circles-data.js'

export default {
    name: 'Circles',
    components: { HomeBar, CircleCard, PopularCircleItem },
    data() {
        return {
            activeTab: 'recommend',
            myCircles,
            categories,
            allCircles,
            popularCircles,

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