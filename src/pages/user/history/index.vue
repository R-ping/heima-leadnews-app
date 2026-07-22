<template>
    <div class="history-page">
        <div class="art-top"><HomeBar /></div>
        <div class="history-content">
            <div class="history-toolbar">
                <div class="toolbar-left">
                    <div class="search-box">
                        <i class="search-icon">&#xf002;</i>
                        <input
                            type="text"
                            class="search-input"
                            v-model="searchKeyword"
                            placeholder="搜索浏览记录"
                            @input="onSearchInput"
                        />
                        <i v-if="searchKeyword" class="clear-icon" @click="clearSearch">&#xf00d;</i>
                    </div>
                </div>
                <button class="clear-btn" @click="handleClearAll">清空记录</button>
            </div>

            <div class="history-list" v-loading="loading">
                <div v-if="groupedHistory.length === 0 && !loading" class="empty-state">
                    <span class="empty-icon">&#xf02d;</span>
                    <span class="empty-text">暂无浏览记录</span>
                </div>
                <div v-for="group in groupedHistory" :key="group.date" class="history-group">
                    <div class="date-header">{{ group.date }}</div>
                    <div class="article-list">
                        <div
                            v-for="item in group.items"
                            :key="item.id"
                            class="article-item"
                            @click="goToArticle(item.articleId)"
                        >
                            <div class="article-main">
                                <span class="type-badge">文章</span>
                                <div class="article-info">
                                    <div class="article-title">{{ item.articleTitle }}</div>
                                    <div class="article-meta">
                                        <span class="article-author">{{ item.authorName }}</span>
                                        <span class="meta-divider">·</span>
                                        <span class="article-stat">
                                            <i class="stat-icon">&#xf06e;</i>
                                            {{ formatCount(item.readCount) }} 阅读
                                        </span>
                                        <span class="meta-divider">·</span>
                                        <span class="article-stat">
                                            <i class="stat-icon">&#xf164;</i>
                                            {{ formatCount(item.likeCount) }}
                                        </span>
                                        <span class="meta-divider">·</span>
                                        <span class="article-stat">
                                            <i class="stat-icon">&#xf075;</i>
                                            {{ formatCount(item.commentCount) }}
                                        </span>
                                    </div>
                                </div>
                            </div>
                            <div class="article-time">{{ formatTime(item.browseTime) }}</div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="pagination-wrap" v-if="total > pageSize">
                <el-pagination
                    background
                    layout="prev, pager, next"
                    :total="total"
                    :page-size="pageSize"
                    :current-page.sync="currentPage"
                    @current-change="onPageChange"
                />
            </div>
        </div>
    </div>
</template>

<script>
import HomeBar from '@/compoents/bars/home_bar'
import { toast } from '@/utils/toast'
import { getBrowseHistory, clearBrowseHistory } from '@/apis/history'

export default {
    name: 'UserHistory',
    components: { HomeBar },
    data() {
        return {
            loading: false,
            searchKeyword: '',
            searchTimer: null,
            historyList: [],
            currentPage: 1,
            pageSize: 10,
            total: 0,
            mockHistory: [
                {
                    date: '2024-01-15',
                    items: [
                        { id: 1, articleId: 101, articleTitle: 'Vue3 组合式 API 入门指南', authorName: '程序员小站', readCount: 3200, likeCount: 15, commentCount: 8, browseTime: '2024-01-15 14:30' },
                        { id: 2, articleId: 102, articleTitle: 'TypeScript 高级类型技巧', authorName: '程序员小站', readCount: 2100, likeCount: 8, commentCount: 12, browseTime: '2024-01-15 10:15' }
                    ]
                },
                {
                    date: '2024-01-14',
                    items: [
                        { id: 3, articleId: 103, articleTitle: 'React Hooks 最佳实践', authorName: '前端达人', readCount: 1800, likeCount: 12, commentCount: 5, browseTime: '2024-01-14 16:45' }
                    ]
                }
            ]
        }
    },
    computed: {
        groupedHistory() {
            return this.historyList
        }
    },
    methods: {
        async loadHistory() {
            this.loading = true
            try {
                const params = {
                    page: this.currentPage,
                    size: this.pageSize
                }
                if (this.searchKeyword) {
                    params.keyword = this.searchKeyword
                }
                const res = await getBrowseHistory(params)
                if (res && res.code === 200 && res.data) {
                    this.historyList = res.data.list || []
                    this.total = res.data.total || 0
                } else {
                    this.useMockData()
                }
            } catch (e) {
                this.useMockData()
            } finally {
                this.loading = false
            }
        },
        useMockData() {
            if (this.searchKeyword) {
                const keyword = this.searchKeyword.toLowerCase()
                this.historyList = this.mockHistory
                    .map(group => ({
                        date: group.date,
                        items: group.items.filter(item =>
                            item.articleTitle.toLowerCase().includes(keyword) ||
                            item.authorName.toLowerCase().includes(keyword)
                        )
                    }))
                    .filter(group => group.items.length > 0)
                this.total = this.historyList.reduce((sum, g) => sum + g.items.length, 0)
            } else {
                this.historyList = this.mockHistory
                this.total = this.mockHistory.reduce((sum, g) => sum + g.items.length, 0)
            }
        },
        onSearchInput() {
            if (this.searchTimer) {
                clearTimeout(this.searchTimer)
            }
            this.searchTimer = setTimeout(() => {
                this.currentPage = 1
                this.loadHistory()
            }, 300)
        },
        clearSearch() {
            this.searchKeyword = ''
            this.currentPage = 1
            this.loadHistory()
        },
        handleClearAll() {
            this.$confirm('确定要清空所有浏览记录吗？', '提示', {
                type: 'warning'
            }).then(() => {
                this.doClearAll()
            }).catch(() => {})
        },
        async doClearAll() {
            try {
                const res = await clearBrowseHistory()
                if (res && res.code === 200) {
                    toast('已清空浏览记录', 2)
                } else {
                    toast('清空记录失败', 2)
                }
            } catch (e) {
                toast('清空记录失败', 2)
            }
            this.historyList = []
            this.total = 0
            this.currentPage = 1
        },
        onPageChange(page) {
            this.currentPage = page
            this.loadHistory()
            window.scrollTo({ top: 0, behavior: 'smooth' })
        },
        goToArticle(articleId) {
            this.$router.push('/article/' + articleId)
        },
        formatCount(count) {
            if (!count) return '0'
            if (count >= 10000) {
                return (count / 1000).toFixed(1) + 'k'
            }
            return count.toString()
        },
        formatTime(time) {
            if (!time) return ''
            const parts = time.split(' ')
            if (parts.length >= 2) {
                return parts[1].substring(0, 5)
            }
            return time
        }
    },
    mounted() {
        this.loadHistory()
    },
    beforeDestroy() {
        if (this.searchTimer) {
            clearTimeout(this.searchTimer)
        }
    }
}
</script>

<style lang="less" scoped>
@import '../../../styles/common';

.history-page {
    min-height: 100vh;
    background: #f7f8fa;
}

.history-content {
    max-width: 960px;
    margin: 0 auto;
    padding: 24px;
}

.history-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 16px 20px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.toolbar-left {
    flex: 1;
}

.search-box {
    position: relative;
    display: flex;
    align-items: center;
    width: 280px;
    height: 36px;
    background: #f7f8fa;
    border: 1px solid #e4e6eb;
    border-radius: 6px;
    padding: 0 12px;
    transition: border-color 0.2s;
    &:focus-within {
        border-color: #1e80ff;
        background: #fff;
    }
}

.search-icon {
    font-family: fontawesome;
    font-size: 14px;
    color: #8a919f;
    margin-right: 8px;
    flex-shrink: 0;
}

.search-input {
    flex: 1;
    border: none;
    background: transparent;
    font-size: 14px;
    color: #252933;
    outline: none;
    &::placeholder {
        color: #c4c9d1;
    }
}

.clear-icon {
    font-family: fontawesome;
    font-size: 12px;
    color: #8a919f;
    cursor: pointer;
    flex-shrink: 0;
    padding: 2px;
    &:hover {
        color: #515767;
    }
}

.clear-btn {
    padding: 8px 20px;
    border: 1px solid #ff4d4f;
    border-radius: 6px;
    background: #fff;
    color: #ff4d4f;
    font-size: 14px;
    cursor: pointer;
    white-space: nowrap;
    transition: all 0.2s;
    &:hover {
        background: #fff2f0;
        border-color: #ff7875;
    }
}

.history-list {
    min-height: 300px;
}

.history-group {
    margin-bottom: 16px;
}

.date-header {
    padding: 12px 20px;
    background: #f2f3f5;
    border-radius: 8px 8px 0 0;
    font-size: 14px;
    font-weight: 600;
    color: #515767;
}

.article-list {
    background: #fff;
    border-radius: 0 0 8px 8px;
    overflow: hidden;
}

.article-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    border-bottom: 1px solid #f2f3f5;
    cursor: pointer;
    transition: background-color 0.2s;
    &:last-child {
        border-bottom: none;
    }
    &:hover {
        background-color: #f7f8fa;
    }
}

.article-main {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    flex: 1;
    min-width: 0;
}

.type-badge {
    display: inline-block;
    padding: 2px 10px;
    background: #1e80ff;
    color: #fff;
    font-size: 12px;
    border-radius: 4px;
    white-space: nowrap;
    flex-shrink: 0;
    margin-top: 2px;
}

.article-info {
    flex: 1;
    min-width: 0;
}

.article-title {
    font-size: 15px;
    font-weight: 500;
    color: #252933;
    margin-bottom: 8px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    &:hover {
        color: #1e80ff;
    }
}

.article-meta {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 0;
    font-size: 13px;
    color: #8a919f;
}

.article-author {
    color: #515767;
}

.meta-divider {
    margin: 0 8px;
    color: #c4c9d1;
}

.article-stat {
    display: inline-flex;
    align-items: center;
    gap: 3px;
}

.stat-icon {
    font-family: fontawesome;
    font-size: 12px;
}

.article-time {
    font-size: 13px;
    color: #8a919f;
    flex-shrink: 0;
    margin-left: 16px;
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 0;
    background: #fff;
    border-radius: 8px;
}

.empty-icon {
    font-family: fontawesome;
    font-size: 48px;
    color: #c4c9d1;
    margin-bottom: 16px;
}

.empty-text {
    font-size: 14px;
    color: #8a919f;
}

.pagination-wrap {
    display: flex;
    justify-content: center;
    padding: 24px 0;
}

@media screen and (max-width: 768px) {
    .history-content {
        padding: 16px;
    }
    .history-toolbar {
        flex-direction: column;
        gap: 12px;
    }
    .search-box {
        width: 100%;
    }
    .clear-btn {
        width: 100%;
    }
    .article-item {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
    }
    .article-time {
        margin-left: 0;
    }
}
</style>