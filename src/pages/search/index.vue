<template>
    <div class="art-page">
        <div class="art-top"><TopBar @onSubmit="doSearch" @onInput="onInput"/></div>
        <div class="scroller" :style="{'height':scrollerHeight}" show-scrollbar="true">
            <div v-if="data.history.length > 0" class="history-section">
                <div class="section-header">
                    <span class="section-title">搜索历史</span>
                    <span class="clear-btn" @click="onClearHistory">&#xf014; 清空</span>
                </div>
            </div>
            <template v-for="(item, idx) in data.history">
                <SearchHistory @onClickText="doSearch" @onDeleteHistory="onDeleteHistory" :id="idx" :title="item" :key="idx"/>
            </template>
            <!-- 搜索历史空状态 -->
            <div class="empty-state" v-if="data.history.length === 0">
                <span class="empty-icon">&#xf002;</span>
                <span class="empty-text">暂无搜索历史</span>
            </div>
        </div>
        <div class="art-tip" v-if="showTip" ref="tip"><SearchTip @onSelect="doSearch" :search="data.keyword" :data="data.tip"/></div>
    </div>
</template>

<script>
    import TopBar from '@/compoents/bars/search_top'
    import SearchHistory from '@/compoents/cells/search_0'
    import SearchTip from '@/compoents/inputs/search_tip'
    import Api from '@/apis/search/api'
    import Utils from '@/utils/env'
    import { toast, confirmDialog } from "@/utils/toast"

    var SEARCH_HISTORY_KEY = 'HEIMA_SEARCH_HISTORY'
    var MAX_HISTORY_COUNT = 6
    var MAX_SUGGESTION_COUNT = 10

    function getSearchHistory() {
        try {
            var str = localStorage.getItem(SEARCH_HISTORY_KEY)
            if (str) {
                var arr = JSON.parse(str)
                if (Array.isArray(arr)) {
                    return arr.slice(0, MAX_HISTORY_COUNT)
                }
            }
        } catch (e) {}
        return []
    }

    function saveSearchHistory(list) {
        try {
            localStorage.setItem(SEARCH_HISTORY_KEY, JSON.stringify(list.slice(0, MAX_HISTORY_COUNT)))
        } catch (e) {}
    }

    function addSearchHistory(keyword) {
        if (!keyword || !keyword.trim()) return
        keyword = keyword.trim()
        var list = getSearchHistory()
        var idx = list.indexOf(keyword)
        if (idx !== -1) {
            list.splice(idx, 1)
        }
        list.unshift(keyword)
        saveSearchHistory(list)
    }

    export default {
        name: "SearchPage",
        components: { TopBar, SearchHistory, SearchTip },
        data() {
            return {
                scrollerHeight: '500px',
                showTip: false,
                data: {
                    keyword: '',
                    history: [],
                    tip: []
                }
            }
        },
        created() {
            if (Utils.isDesktop()) {
                this.$router.replace('/home')
                return
            }
        },
        mounted() {
            if (Utils.isDesktop()) return
            var topBarHeight = 90
            if (Utils.isDesktop()) {
                topBarHeight = 60
            }
            this.scrollerHeight = (Utils.getPageHeight() - topBarHeight) + 'px'
            this.loadSearchHistory()
        },
        methods: {
            doSearch: function (val) {
                if (!val || !val.trim()) return
                val = val.trim()
                addSearchHistory(val)
                this.$router.push({ name: 'search_result', params: { 'keyword': val } })
            },
            loadSearchHistory: function () {
                this.data.history = getSearchHistory()
            },
            onDeleteHistory: function (index) {
                var _this = this
                confirmDialog('确认删除这条记录吗？', function (button) {
                    if (button === 'OK') {
                        var list = getSearchHistory()
                        if (index >= 0 && index < list.length) {
                            list.splice(index, 1)
                            saveSearchHistory(list)
                            _this.data.history = list
                            toast('已删除', 2)
                        }
                    }
                })
            },
            onClearHistory: function () {
                var _this = this
                confirmDialog('确认清空全部搜索历史吗？', function (button) {
                    if (button === 'OK') {
                        try {
                            localStorage.removeItem(SEARCH_HISTORY_KEY)
                        } catch (e) {}
                        _this.data.history = []
                        toast('已清空搜索历史', 2)
                    }
                })
            },
            onInput: function (val) {
                if (!val || val.trim() === '') {
                    this.showTip = false
                    this.data.tip = []
                    return
                }
                var self = this
                Api.associate_search(val.trim()).then(data => {
                    if (data && data.code === 200) {
                        self.data.keyword = val.trim()
                        self.showTip = true
                        self.data.tip = (data.data || []).slice(0, MAX_SUGGESTION_COUNT)
                    } else {
                        self.showTip = false
                        self.data.tip = []
                    }
                }).catch(function () {
                    self.showTip = false
                    self.data.tip = []
                })
            }
        }
    }
</script>

<style scoped>
    .art-page {
        width: 100%;
        display: flex;
        flex-direction: column;
        min-height: 100vh;
        background-color: #f5f5f5;
    }
    .art-tip {
        position: absolute;
        top: 90px;
        width: 100%;
        left: 0;
        z-index: 999;
    }
    .art-top {
        top: 0px;
        z-index: 999;
        position: fixed;
        left: 0;
        right: 0;
        height: 90px;
        background-color: #ffffff;
    }
    .scroller {
        flex: 1;
        display: flex;
        flex-direction: column;
        width: 100%;
        margin-top: 90px;
    }
    .history-section {
        background-color: #ffffff;
    }
    .section-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 15px 20px;
        border-bottom: 1px solid #f0f0f0;
        background-color: #ffffff;
    }
    .section-title {
        font-size: 28px;
        color: #666;
    }
    .clear-btn {
        font-family: fontawesome;
        font-size: 24px;
        color: #999;
        cursor: pointer;
        padding: 8px 12px;
    }
    .clear-btn:active {
        color: #3194ff;
    }
    .empty-state {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 80px 20px;
        gap: 16px;
        background-color: #ffffff;
        margin-top: 20px;
    }
    .empty-icon {
        font-family: fontawesome;
        font-size: 64px;
        color: #d0d0d0;
    }
    .empty-text {
        font-size: 28px;
        color: #999;
    }

    @media screen and (min-width: 768px) {
        .art-page {
            max-width: 750PX;
            margin: 0 auto;
            background-color: #ffffff;
        }
        .art-top {
            max-width: 750PX;
            margin: 0 auto;
            height: 60PX;
            position: sticky;
        }
        .art-tip {
            max-width: 750PX;
            margin: 0 auto;
            top: 60PX;
            left: 50%;
            transform: translateX(-50%);
        }
        .scroller {
            margin-top: 0;
        }
        .section-header {
            padding: 12PX 20PX;
        }
        .section-title {
            font-size: 14PX;
        }
        .clear-btn {
            font-size: 13PX;
        }
        .empty-state {
            margin-top: 0;
            padding: 60PX 20PX;
        }
        .empty-icon {
            font-size: 48PX;
        }
        .empty-text {
            font-size: 14PX;
        }
    }
</style>
