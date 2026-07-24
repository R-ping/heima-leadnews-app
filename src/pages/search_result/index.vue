<template>
    <div class="wrapper">
        <div class="top-body"><Home_Bar @onSubmit="onSubmit" :value="keyword"/></div>
        <div class="content-body">
            <wxc-tab-page ref="wxc-tab-page" :showMore="false" :tab-titles="tabTitles"
                :tab-styles="tabStyles" title-type="text" :tab-page-height="tabPageHeight"
                @wxcTabPageCurrentTabSelected="wxcTabPageCurrentTabSelected">
                <div v-for="(v,index) in tabList" :key="index" class="item-container"
                    :style="{ height: (tabPageHeight - tabStyles.height) + 'px' }"
                    @scroll="onScroll($event, index)">
                    <!-- 骨架屏加载 -->
                    <div v-if="tabStates[index] && tabStates[index].loading && v.length === 0" class="skeleton-list">
                        <div class="skeleton-card" v-for="n in 5" :key="n">
                            <div class="sk-line sk-title"></div>
                            <div class="sk-line sk-sub"></div>
                            <div class="sk-line sk-meta"></div>
                        </div>
                    </div>
                    <!-- 列表项 -->
                    <div v-for="(item,key) in v" class="cell" :key="item.id || key"
                        @click="wxcPanItemClicked(item)">
                        <Item0 v-if="item.type === 0" :data="item"/>
                        <Item1 v-if="item.type === 1" :data="item"/>
                        <Item3 v-if="item.type === 3" :data="item"/>
                    </div>
                    <!-- 加载更多 -->
                    <div class="loading" v-if="tabStates[index] && tabStates[index].loadingMore">
                        <span class="loading-spinner"></span>
                        <span class="loading-text">{{ load_more_text }}</span>
                    </div>
                    <!-- 没有更多 -->
                    <div class="loading" v-if="tabStates[index] && tabStates[index].noMore && v.length > 0">
                        <span class="loading-text">— 没有更多了 —</span>
                    </div>
                    <!-- 空状态 -->
                    <div class="empty-state" v-if="tabStates[index] && tabStates[index].loaded && v.length === 0 && !tabStates[index].loading">
                        <span class="empty-icon">&#xf002;</span>
                        <span class="empty-text">未找到相关结果</span>
                    </div>
                    <!-- 加载错误 -->
                    <div class="error-state" v-if="tabStates[index] && tabStates[index].error">
                        <span class="error-text">{{ tabStates[index].errorMsg }}</span>
                        <span class="retry-btn" @click="load()">点击重试</span>
                    </div>
                </div>
            </wxc-tab-page>
        </div>
        <!-- 开发调试面板 -->
        <div v-if="showDebug" class="debug-panel">
            <span class="debug-title">DEV</span>
            <span class="debug-info">结果: {{ totalCount }} 条</span>
            <span class="debug-info">耗时: {{ lastApiTime }}ms</span>
        </div>
    </div>
</template>

<script>
    import Home_Bar from "@/components/bars/search_result_top"
    import WxcTabPage from "@/components/tabs/home_tabs"
    import Utils from '@/utils/env'
    import { toast } from "@/utils/toast"
    import Item0 from '../../components/cells/article_0.vue'
import Item1 from '../../components/cells/article_1.vue'
import Item3 from '../../components/cells/article_3.vue'
    import Config from './config'
    import Api from '@/apis/search_result/api'

    export default {
        name: 'SearchResult',
        components: { Home_Bar, WxcTabPage, Item0, Item1, Item3 },
        props: {
            keyword: ''
        },
        data: () => ({
            showmore: false,
            tabTitles: Config.tabTitles,
            tabStyles: Config.tabStyles,
            tabList: [...Array(Config.tabTitles.length).keys()].map(() => []),
            tabStates: [...Array(Config.tabTitles.length).keys()].map(() => ({
                loaded: false,
                loading: false,
                loadingMore: false,
                noMore: false,
                error: false,
                errorMsg: ''
            })),
            tabPageHeight: 1334,
            params: {
                tag: "__all__",
                keyword: '',
                pageNum: 1,
                pageSize: 20,
                index: 0
            },
            showDebug: process.env.NODE_ENV === 'development',
            totalCount: 0,
            lastApiTime: 0,
            apiStartTime: 0
        }),
        computed: {
            load_more_text: function () { return this.$lang.load_more_text }
        },
        mounted() {
            this.$refs['wxc-tab-page'].setPage(0, null, true)
        },
        created() {
            this.tabPageHeight = Utils.getPageHeight() - 110
            this.params.keyword = this.keyword
        },
        methods: {
            // 滚动加载更多
            onScroll: function (e, index) {
                var el = e.target
                var scrollHeight = el.scrollHeight
                var scrollTop = el.scrollTop
                var clientHeight = el.clientHeight
                var state = this.tabStates[index]
                if (scrollHeight - scrollTop - clientHeight < 100) {
                    if (state && !state.loadingMore && !state.noMore && !state.loading && state.loaded) {
                        this.params.pageNum = this.params.pageNum + 1
                        this.params.index = index
                        this.params.tag = Config.tabTitles[index].id
                        this.loadmore()
                    }
                }
            },
            loadmore: function () {
                var state = this.tabStates[this.params.index]
                if (!state || state.loadingMore || state.noMore) return
                this.$set(state, 'loadingMore', true)
                this.load()
            },
            load: function () {
                var state = this.tabStates[this.params.index]
                if (!state) return
                this.$set(state, 'loading', true)
                this.$set(state, 'error', false)
                this.apiStartTime = Date.now()
                Api.article_search(this.params).then((d) => {
                    this.lastApiTime = Date.now() - this.apiStartTime
                    this.$set(state, 'loading', false)
                    this.$set(state, 'loadingMore', false)
                    this.$set(state, 'loaded', true)
                    if (d && d.code === 200) {
                        if (d.data && d.data.length > 0) {
                            this.tanfer(d.data)
                        } else {
                            this.$set(state, 'noMore', true)
                        }
                    } else {
                        this.$set(state, 'error', true)
                        this.$set(state, 'errorMsg', (d && d.errorMessage) || '搜索失败')
                    }
                }).catch(() => {
                    this.lastApiTime = Date.now() - this.apiStartTime
                    this.$set(state, 'loading', false)
                    this.$set(state, 'loadingMore', false)
                    this.$set(state, 'loaded', true)
                    this.$set(state, 'error', true)
                    this.$set(state, 'errorMsg', '网络请求失败，请检查网络连接')
                })
            },
            tanfer: function (data) {
                if (!data || data.length === 0) {
                    var state = this.tabStates[this.params.index]
                    if (state) this.$set(state, 'noMore', true)
                    return
                }
                var arr = []
                for (var i = 0; i < data.length; i++) {
                    try {
                        var item = data[i]
                        var ims = []
                        if (item.images) {
                            if (typeof item.images === 'string') {
                                ims = item.images.replace(/[\[\]]/g, '').split(',').filter(function (s) { return s.trim() })
                            } else if (Array.isArray(item.images)) {
                                ims = item.images
                            }
                        }
                        var imgCount = ims.length
                        var articleType = imgCount >= 3 ? 3 : (imgCount >= 1 ? 1 : 0)
                        var tmp = {
                            id: item.id,
                            title: item.h_title || item.title || '',
                            comment: item.comment || 0,
                            authorId: item.authorId,
                            source: item.authorName || '',
                            date: item.publishTime,
                            type: articleType,
                            image: ims,
                            icon: '\uf06d',
                            staticUrl: item.staticUrl || ''
                        }
                        arr.push(tmp)
                    } catch (e) {
                        // 跳过异常数据
                    }
                }
                var newList = this.tabList.map(function (tab) { return tab.slice() })
                var curIndex = this.params.index
                if (this.params.pageNum !== 1) {
                    newList[curIndex] = newList[curIndex].concat(arr)
                } else {
                    newList[curIndex] = arr
                }
                this.tabList = newList
                this.showmore = false
                this.totalCount = newList[curIndex].length
            },
            wxcTabPageCurrentTabSelected(e) {
                this.params.pageNum = 1
                this.params.index = e.page
                this.params.tag = Config.tabTitles[e.page].id
                // 重置Tab状态
                var state = this.tabStates[e.page]
                if (!state) {
                    this.$set(this.tabStates, e.page, {
                        loaded: false, loading: false, loadingMore: false,
                        noMore: false, error: false, errorMsg: ''
                    })
                }
                if (!this.tabStates[e.page].loaded && !this.tabStates[e.page].loading) {
                    this.load()
                }
            },
            wxcPanItemClicked(item) {
                if (!item || !item.id) return
                if (Utils.isDesktop() && item.staticUrl) {
                    window.open(item.staticUrl, '_blank')
                    return
                }
                this.$router.push({
                    path: '/article',
                    query: { id: item.id }
                })
            },
            onSubmit: function (val) {
                this.params.keyword = val
                this.params.pageNum = 1
                this.tabList = [...Array(this.tabTitles.length).keys()].map(() => [])
                this.tabStates = [...Array(this.tabTitles.length).keys()].map(() => ({
                    loaded: false, loading: false, loadingMore: false,
                    noMore: false, error: false, errorMsg: ''
                }))
                this.load()
            }
        }
    }
</script>

<style lang="less" scoped>
    @import '../../styles/article';
    .wrapper {
        background-color: @body-background;
        font-size: @font-size;
        font-family: @font-family;
        display: flex;
        flex-direction: column;
        min-height: 100vh;
        width: 100%;
    }
    .top-body {
        position: fixed;
        left: 0;
        right: 0;
        top: 0;
        z-index: 100;
    }
    .content-body {
        flex: 1;
        display: flex;
        flex-direction: column;
        margin-top: 90px;
        width: 100%;
    }
    .item-container {
        width: 100%;
        background-color: #ffffff;
        overflow-y: auto;
        -webkit-overflow-scrolling: touch;
    }
    .cell {
        background-color: #ffffff;
        cursor: pointer;
        transition: background-color 0.15s;
    }
    .cell:hover {
        background-color: #fafafa;
    }
    .loading-spinner {
        width: 28px;
        height: 28px;
        border: 3px solid #e0e0e0;
        border-top-color: #3194ff;
        border-radius: 50%;
        animation: spin 0.8s linear infinite;
    }
    @keyframes spin {
        to { transform: rotate(360deg); }
    }
    .empty-state, .error-state {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 80px 20px;
        gap: 16px;
    }
    .empty-icon {
        font-family: fontawesome;
        font-size: 64px;
        color: #d0d0d0;
    }
    .empty-text, .error-text {
        font-size: 28px;
        color: #999;
        text-align: center;
    }
    .retry-btn {
        font-size: 28px;
        color: #3194ff;
        cursor: pointer;
        padding: 10px 30px;
        border: 1px solid #3194ff;
        border-radius: 6px;
        transition: all 0.2s;
    }
    .retry-btn:hover {
        background-color: #3194ff;
        color: #fff;
    }

    /* 骨架屏 */
    .skeleton-list {
        padding: 0 4PX;
    }
    .skeleton-card {
        background: #FFFFFF;
        border-radius: 8PX;
        padding: 20PX 24PX;
        margin-bottom: 16PX;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
    }
    .sk-line {
        height: 14PX;
        background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
        background-size: 200% 100%;
        animation: shimmer 1.5s infinite;
        border-radius: 4PX;
        margin-bottom: 12PX;
    }
    .sk-title {
        width: 70%;
        height: 18PX;
    }
    .sk-sub {
        width: 50%;
    }
    .sk-meta {
        width: 35%;
        margin-bottom: 0;
    }
    @keyframes shimmer {
        0% { background-position: 200% 0; }
        100% { background-position: -200% 0; }
    }

    /* 调试面板 */
    .debug-panel {
        position: fixed;
        right: 8PX;
        bottom: 8PX;
        background: rgba(0, 0, 0, 0.75);
        color: #fff;
        border-radius: 6PX;
        padding: 6PX 12PX;
        font-size: 12PX;
        font-family: monospace;
        z-index: 9999;
        display: flex;
        align-items: center;
        gap: 10PX;
        backdrop-filter: blur(4px);
        user-select: none;
        pointer-events: none;
    }
    .debug-title {
        background: #1E80FF;
        color: #fff;
        padding: 2PX 6PX;
        border-radius: 3PX;
        font-weight: 700;
        font-size: 10PX;
    }
    .debug-info {
        opacity: 0.85;
    }

    /* ========== Web端样式 ========== */
    @media screen and (min-width: 768px) {
        .wrapper {
            background-color: #F4F5F7;
            font-size: 14PX;
        }
        .top-body {
            background-color: #FFFFFF;
            border-bottom: 1PX solid #F0F1F5;
            box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
        }
        .content-body {
            margin-top: 60PX;
            padding: 20PX;
            max-width: 960PX;
            margin-left: auto;
            margin-right: auto;
            width: 100%;
            box-sizing: border-box;
        }
        .item-container {
            background-color: transparent;
            padding: 0 4PX;
        }
        .cell {
            background-color: transparent;
            margin-bottom: 0;
            padding: 0;
        }
        .cell:hover {
            background-color: transparent;
        }
        .cell /deep/ .list-item {
            background-color: #FFFFFF;
            border-radius: 8PX;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
            margin-bottom: 16PX;
            padding: 20PX 24PX;
            transition: box-shadow 0.25s ease, transform 0.25s ease;
            border-bottom: 1PX solid #F2F3F5;
        }
        .cell /deep/ .list-item:hover {
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.10);
            transform: translateY(-2px);
        }
        .cell /deep/ .title {
            font-size: 17PX;
            line-height: 1.4;
            color: #222222;
        }
        .cell /deep/ .title:hover {
            color: #1E80FF;
        }
        .cell /deep/ .tags-text {
            font-size: 14PX;
            color: #8A93A6;
        }
        .cell /deep/ .image {
            height: 100PX;
            border-radius: 4PX;
        }

        /* 标签栏 */
        .content-body /deep/ .tab-body {
            background-color: #FFFFFF;
            border-bottom: 1PX solid #F0F1F5;
            border-radius: 8PX 8PX 0 0;
            margin-bottom: 16PX;
            height: 52PX;
            padding-left: 4PX;
        }
        .content-body /deep/ .title-item {
            padding: 0 20PX;
            transition: color 0.2s;
            border-right: none;
        }
        .content-body /deep/ .title-item:hover .tab-text {
            color: #1E80FF;
        }
        .content-body /deep/ .tab-text {
            font-size: 15PX;
            color: #515767;
            font-weight: 400 !important;
        }
        .content-body /deep/ .border-bottom {
            background-color: #1E80FF !important;
            height: 3PX !important;
            width: 24PX !important;
            border-radius: 2PX;
        }
        /* 已选中Tab放大加粗 */
        .content-body /deep/ .title-item[style*="background-color: rgb(49, 148, 255)"] .tab-text,
        .content-body /deep/ .title-item [style*="font-weight: bold"] {
            font-weight: 600 !important;
            color: #1D2129 !important;
        }

        /* 加载与空状态 */
        .loading {
            height: 60PX;
            gap: 8PX;
        }
        .loading-spinner {
            width: 20PX;
            height: 20PX;
            border-width: 2PX;
        }
        .loading-text {
            font-size: 14PX;
            color: #999999;
        }
        .empty-state, .error-state {
            padding: 60PX 20PX;
            gap: 12PX;
        }
        .empty-icon {
            font-size: 48PX;
        }
        .empty-text, .error-text {
            font-size: 14PX;
        }
        .retry-btn {
            font-size: 14PX;
            padding: 8PX 24PX;
        }

        /* 骨架屏 */
        .skeleton-list {
            padding: 0;
        }
        .skeleton-card {
            padding: 24PX;
            margin-bottom: 16PX;
        }
        .sk-line {
            height: 16PX;
            margin-bottom: 14PX;
        }
        .sk-title {
            height: 20PX;
            width: 60%;
        }
        .sk-meta {
            width: 30%;
            margin-bottom: 0;
        }

        /* 调试面板 */
        .debug-panel {
            right: 16PX;
            bottom: 16PX;
            padding: 8PX 16PX;
            font-size: 13PX;
            gap: 14PX;
        }
    }
</style>