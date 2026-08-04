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
            keyword: {
                type: String,
                default: ''
            }
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
            // 优先从 $route.query 读取（路由跳转）; 次选 props（SSR / 直接挂载）
            this.params.keyword = this.$route.query.keyword || this.keyword || ''
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

<style lang="less" scoped src="./styles/search_result.less"></style>