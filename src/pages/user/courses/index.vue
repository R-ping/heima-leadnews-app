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

            <!-- 空状态：VIP借阅无数据 -->
            <div v-if="activeFilter === 'vip' && courses.length === 0 && !loading" class="empty-state">
                <p class="empty-text">你还未借阅过任何课程～快去课程首页看看吧～！</p>
                <button class="empty-btn" @click="$router.push('/course')">前往课程首页</button>
            </div>

            <!-- 全部 tab：网格视图 -->
            <div v-else-if="activeFilter === 'all' && courses.length > 0" class="course-grid">
                <CourseGridCard
                    v-for="course in courses"
                    :key="course.id"
                    :course="course"
                    @click="goToDetail(course.id)"
                />
            </div>

            <!-- 已购 / VIP借阅 tab：列表视图 -->
            <div v-else-if="activeFilter !== 'all' && courses.length > 0" class="course-list">
                <CourseListItem
                    v-for="course in courses"
                    :key="course.id"
                    :course="course"
                    @click="goToDetail(course.id)"
                />
            </div>

            <!-- 其他筛选无数据 -->
            <div v-else-if="courses.length === 0 && !loading" class="empty-state">
                <p class="empty-text">暂无课程数据</p>
            </div>
        </div>
    </div>
</template>

<script>
import HomeBar from '@/components/bars/home_bar'
import CourseGridCard from './components/CourseGridCard.vue'
import CourseListItem from './components/CourseListItem.vue'
import request from '@/common/article_request'

export default {
    name: 'UserCourses',
    components: { HomeBar, CourseGridCard, CourseListItem },
    data() {
        return {
            activeFilter: 'all',
            courses: [],
            loading: false,
            filterTabs: [
                { key: 'all', label: '全部' },
                { key: 'purchased', label: '已购' },
                { key: 'vip', label: 'VIP借阅' }
            ]
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
                const res = await request.get('/api/v1/course/my', {
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
    border-radius: 0 0 16px 16px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.06);
    margin-bottom: 24px;
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

.course-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.course-list {
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  overflow: hidden;
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
        border-radius: 0 0 12px 12px;
    }

    .empty-state {
        padding: 60px 0;
    }
}
</style>