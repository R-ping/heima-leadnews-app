<template>
    <div class="courses-page">
        <div class="art-top"><HomeBar/></div>
        <div class="courses-content">
            <!-- 标题行 -->
            <div class="title-row">
                <span class="title">我的小册</span>
                <span class="history-link" @click="$router.push('/user/history')">浏览记录</span>
            </div>

            <!-- 筛选选项卡 -->
            <div class="filter-tabs">
                <div
                    v-for="tab in filterTabs"
                    :key="tab.key"
                    class="filter-tab"
                    :class="{ active: activeFilter === tab.key }"
                    @click="switchFilter(tab.key)"
                >
                    {{ tab.label }}
                </div>
            </div>

            <!-- 视图切换 -->
            <div class="toolbar">
                <div class="view-toggle">
                    <span
                        class="toggle-btn"
                        :class="{ active: viewMode === 'grid' }"
                        @click="viewMode = 'grid'"
                    >
                        &#xf00a;
                    </span>
                    <span
                        class="toggle-btn"
                        :class="{ active: viewMode === 'list' }"
                        @click="viewMode = 'list'"
                    >
                        &#xf03a;
                    </span>
                </div>
            </div>

            <!-- 空状态：VIP借阅无数据 -->
            <div v-if="activeFilter === 'vip' && courses.length === 0 && !loading" class="empty-state">
                <p class="empty-text">你还未借阅过任何课程～快去课程首页看看吧～！</p>
                <button class="empty-btn" @click="$router.push('/course')">前往课程首页</button>
            </div>

            <!-- 网格视图 -->
            <div v-else-if="viewMode === 'grid' && courses.length > 0" class="course-grid">
                <div
                    v-for="course in courses"
                    :key="course.id"
                    class="course-card"
                    @click="goToDetail(course.id)"
                >
                    <div class="card-cover">
                        <img :src="course.coverImage || defaultCover" alt="封面" />
                    </div>
                    <div class="card-info">
                        <div class="card-title">{{ course.title }}</div>
                        <div class="card-author">{{ course.authorName }}</div>
                        <div class="card-progress" v-if="course.progress !== undefined">
                            <div class="progress-bar">
                                <div class="progress-fill" :style="{ width: course.progress + '%' }"></div>
                            </div>
                            <span class="progress-text">{{ course.progress }}%</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 列表视图 -->
            <div v-else-if="viewMode === 'list' && courses.length > 0" class="course-list">
                <div
                    v-for="course in courses"
                    :key="course.id"
                    class="list-item"
                    @click="goToDetail(course.id)"
                >
                    <div class="list-cover">
                        <img :src="course.coverImage || defaultCover" alt="封面" />
                    </div>
                    <div class="list-info">
                        <div class="list-title">{{ course.title }}</div>
                        <div class="list-author">{{ course.authorName }}</div>
                        <div class="list-progress" v-if="course.progress !== undefined">
                            <div class="progress-bar">
                                <div class="progress-fill" :style="{ width: course.progress + '%' }"></div>
                            </div>
                            <span class="progress-text">{{ course.progress }}%</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 其他筛选无数据 -->
            <div v-else-if="courses.length === 0 && !loading" class="empty-state">
                <p class="empty-text">暂无课程数据</p>
            </div>
        </div>
    </div>
</template>

<script>
import HomeBar from '@/compoents/bars/home_bar'
import { toast } from '@/utils/toast'

export default {
    name: 'UserCourses',
    components: { HomeBar },
    data() {
        return {
            activeFilter: 'all',
            viewMode: 'grid',
            courses: [],
            loading: false,
            filterTabs: [
                { key: 'all', label: '全部' },
                { key: 'purchased', label: '已购' },
                { key: 'vip', label: 'VIP借阅' }
            ],
            defaultCover: '/static/images/avatar_head_1.png'
        }
    },
    mounted() {
        this.loadCourses()
    },
    methods: {
        switchFilter(key) {
            this.activeFilter = key
            this.courses = []
            this.loadCourses()
        },
        async loadCourses() {
            this.loading = true
            try {
                const res = await this.$http.get('/api/v1/course/my', {
                    params: { filter: this.activeFilter }
                })
                if (res && res.code === 200 && res.data) {
                    this.courses = res.data.list || []
                }
            } catch (e) {
                // Keep empty courses when API fails
            } finally {
                this.loading = false
            }
        },
        goToDetail(courseId) {
            this.$router.push('/course/' + courseId)
        }
    }
}
</script>

<style lang="less" scoped>
@import '../../../styles/common';

.courses-page {
    min-height: 100vh;
    background: #f4f5f7;
}

.courses-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 24px;
}

.title-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #fff;
    padding: 16px 24px;
    border-radius: 16px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.06);
    margin-bottom: 0;
    border-bottom-left-radius: 0;
    border-bottom-right-radius: 0;
    border-bottom: 1px solid #f0f2f5;

    .title {
        font-size: 20px;
        font-weight: 600;
        color: #1a1a1a;
    }

    .history-link {
        font-size: 14px;
        color: #1e80ff;
        cursor: pointer;
        &:hover {
            opacity: 0.8;
        }
    }
}

.filter-tabs {
    display: flex;
    background: #fff;
    padding: 0 24px;
    border-bottom: 1px solid #f0f2f5;
}

.filter-tab {
    padding: 12px 0;
    margin-right: 24px;
    font-size: 15px;
    color: #666;
    cursor: pointer;
    position: relative;
    transition: color 0.2s;

    &:hover {
        color: #1e80ff;
    }

    &.active {
        color: #1e80ff;
        font-weight: 600;

        &::after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 0;
            right: 0;
            height: 2px;
            background: #1e80ff;
            border-radius: 1px;
        }
    }
}

.toolbar {
    display: flex;
    justify-content: flex-end;
    background: #fff;
    padding: 12px 24px;
    border-radius: 0 0 16px 16px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.06);
    margin-bottom: 24px;
}

.view-toggle {
    display: flex;
    gap: 4px;
    background: #f4f5f7;
    border-radius: 6px;
    padding: 3px;
}

.toggle-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 28px;
    font-family: fontawesome;
    font-size: 16px;
    color: #8a919f;
    cursor: pointer;
    border-radius: 4px;
    transition: all 0.2s;

    &:hover {
        color: #1e80ff;
    }

    &.active {
        background: #fff;
        color: #1e80ff;
        box-shadow: 0 1px 3px rgba(0,0,0,0.1);
    }
}

.course-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
}

.course-card {
    background: #fff;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    cursor: pointer;
    transition: transform 0.2s, box-shadow 0.2s;

    &:hover {
        transform: translateY(-4px);
        box-shadow: 0 4px 16px rgba(0,0,0,0.1);
    }
}

.card-cover {
    width: 100%;
    height: 160px;
    background: #f5f7fa;
    overflow: hidden;

    img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
}

.card-info {
    padding: 12px 16px;
}

.card-title {
    font-size: 16px;
    font-weight: 500;
    color: #1a1a1a;
    line-height: 1.4;
    margin-bottom: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
}

.card-author {
    font-size: 13px;
    color: #8c8c8c;
    margin-bottom: 8px;
}

.card-progress {
    display: flex;
    align-items: center;
    gap: 8px;
}

.progress-bar {
    flex: 1;
    height: 4px;
    background: #e8e8e8;
    border-radius: 2px;
    overflow: hidden;
}

.progress-fill {
    height: 100%;
    background: #1e80ff;
    border-radius: 2px;
    transition: width 0.3s;
}

.progress-text {
    font-size: 12px;
    color: #666;
    flex-shrink: 0;
}

.course-list {
    display: flex;
    flex-direction: column;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    overflow: hidden;
}

.list-item {
    display: flex;
    align-items: center;
    padding: 16px 24px;
    cursor: pointer;
    border-bottom: 1px solid #f5f5f5;
    transition: background-color 0.2s;

    &:last-child {
        border-bottom: none;
    }

    &:hover {
        background: #f8fafc;
    }
}

.list-cover {
    width: 80px;
    height: 56px;
    border-radius: 6px;
    overflow: hidden;
    flex-shrink: 0;
    background: #f0f2f5;
    margin-right: 16px;

    img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
}

.list-info {
    flex: 1;
    min-width: 0;
}

.list-title {
    font-size: 15px;
    font-weight: 500;
    color: #1a1a1a;
    margin-bottom: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.list-author {
    font-size: 13px;
    color: #8c8c8c;
    margin-bottom: 6px;
}

.list-progress {
    display: flex;
    align-items: center;
    gap: 8px;
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 0;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.empty-text {
    font-size: 15px;
    color: #999;
    text-align: center;
    margin-bottom: 24px;
}

.empty-btn {
    padding: 8px 32px;
    border: none;
    border-radius: 20px;
    background: #1e80ff;
    color: #fff;
    font-size: 14px;
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover {
        background: #4096ff;
    }
}

@media screen and (max-width: 1199px) {
    .course-grid {
        grid-template-columns: repeat(2, 1fr);
    }
}

@media screen and (max-width: 767px) {
    .courses-content {
        padding: 16px;
    }

    .course-grid {
        grid-template-columns: 1fr;
        gap: 16px;
    }

    .title-row {
        padding: 12px 16px;
        border-radius: 12px 12px 0 0;

        .title {
            font-size: 18px;
        }
    }

    .filter-tabs {
        padding: 0 16px;
    }

    .toolbar {
        padding: 10px 16px;
        border-radius: 0 0 12px 12px;
    }

    .list-cover {
        width: 60px;
        height: 42px;
    }

    .empty-state {
        padding: 60px 0;
    }
}
</style>