<template>
  <div class="wrapper" :class="{ 'is-desktop': isDesktop }">
    <div class="top-body" v-if="!isDesktop"><Home_Bar/></div>
    <div class="content-body">
      <!-- 移动端Tab -->
      <wxc-tab-page v-if="!isDesktop" ref="wxc-tab-page" :tab-titles="tabTitles" :tab-styles="tabStyles"
        title-type="text" :tab-page-height="tabPageHeight"
        @wxcTabPageCurrentTabSelected="wxcTabPageCurrentTabSelected">
        <div v-for="(v,index) in tabList" :key="index" class="item-container"
          :style="{ height: (tabPageHeight - tabStyles.height) + 'px' }"
          ref="scrollContainers" @scroll="onScroll($event, index)">
          <!-- 下拉刷新提示 -->
          <div class="pull-refresh" v-if="tabStates[index] && tabStates[index].refreshing">
            <span class="loading-spinner"></span>
            <span class="loading-text">{{ load_new_text }}</span>
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
          <!-- 没有更多数据 -->
          <div class="loading no-more" v-if="tabStates[index] && tabStates[index].noMore && v.length > 0">
            <span class="loading-text">— 没有更多了 —</span>
          </div>
          <!-- 空状态 -->
          <div class="empty-state" v-if="tabStates[index] && tabStates[index].loaded && v.length === 0 && !tabStates[index].loading">
            <span class="empty-icon">&#xf15c;</span>
            <span class="empty-text">暂无内容</span>
          </div>
          <!-- 加载错误 -->
          <div class="error-state" v-if="tabStates[index] && tabStates[index].error">
            <span class="error-text">{{ tabStates[index].errorMsg }}</span>
            <span class="retry-btn" @click="load()">点击重试</span>
          </div>
        </div>
      </wxc-tab-page>

      <!-- Web端列表 -->
      <div class="desktop-list" v-if="isDesktop">
        <div class="list-container" @scroll="onDesktopScroll">
          <!-- 下拉刷新提示 -->
          <div class="pull-refresh" v-if="currentState.refreshing">
            <span class="loading-spinner"></span>
            <span class="loading-text">{{ load_new_text }}</span>
          </div>
          <!-- 列表项 -->
          <div v-for="(item,key) in currentList" class="cell desktop-cell" :key="item.id || key"
            @click="wxcPanItemClicked(item)">
            <Item0 v-if="item.type === 0" :data="item"/>
            <Item1 v-if="item.type === 1" :data="item"/>
            <Item3 v-if="item.type === 3" :data="item"/>
          </div>
          <!-- 加载更多 -->
          <div class="loading" v-if="currentState.loadingMore">
            <span class="loading-spinner"></span>
            <span class="loading-text">{{ load_more_text }}</span>
          </div>
          <!-- 没有更多数据 -->
          <div class="loading no-more" v-if="currentState.noMore && currentList.length > 0">
            <span class="loading-text">— 没有更多了 —</span>
          </div>
          <!-- 空状态 -->
          <div class="empty-state" v-if="currentState.loaded && currentList.length === 0 && !currentState.loading">
            <span class="empty-icon">&#xf15c;</span>
            <span class="empty-text">暂无内容</span>
          </div>
          <!-- 加载错误 -->
          <div class="error-state" v-if="currentState.error">
            <span class="error-text">{{ currentState.errorMsg }}</span>
            <span class="retry-btn" @click="load(currentTab, 1)">点击重试</span>
          </div>
          <!-- 底部留白 -->
          <div class="list-bottom"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import Home_Bar from "@/components/bars/home_bar"
  import WxcTabPage from "@/components/tabs/home_tabs"
  import Utils from '@/utils/env'
  import Item0 from '../../components/cells/article_0.vue'
import Item1 from '../../components/cells/article_1.vue'
import Item3 from '../../components/cells/article_3.vue'
  import Config from './config'
  import Api from '@/apis/home/api'
  import { toast } from "@/utils/toast"

  export default {
    name: 'HeiMa-Home',
    components: { Home_Bar, WxcTabPage, Item0, Item1, Item3 },
    data: () => ({
      isDesktop: false,
      currentTab: 0,
      shownew: false,
      showmore: false,
      tabTitles: Config.tabTitles,
      tabStyles: Config.tabStyles,
      tabList: [...Array(Config.tabTitles.length).keys()].map(() => []),
      tabStates: [...Array(Config.tabTitles.length).keys()].map(() => ({
        loaded: false,
        loading: false,
        loadingMore: false,
        refreshing: false,
        noMore: false,
        error: false,
        errorMsg: ''
      })),
      tabPageHeight: 1334,
      params: {
        loaddir: 1,
        index: 0,
        tag: "__all__",
        size: 10,
        max_behot_time: 0,
        min_behot_time: 20000000000000
      }
    }),
    computed: {
      load_new_text: function () { return this.$lang.load_new_text },
      load_more_text: function () { return this.$lang.load_more_text },
      currentList: function() {
        return this.tabList[this.currentTab] || []
      },
      currentState: function() {
        return this.tabStates[this.currentTab] || {
          loaded: false, loading: false, loadingMore: false,
          refreshing: false, noMore: false, error: false, errorMsg: ''
        }
      }
    },
    mounted() {
      this.checkDevice()
      window.addEventListener('resize', this.checkDevice)
      this.$nextTick(() => {
        this.updateTabHeight()
        this.$nextTick(() => {
          if (!this.isDesktop && this.$refs['wxc-tab-page']) {
            this.$refs['wxc-tab-page'].setPage(0, null, true)
          }
        })
      })
      this.$nextTick(() => {
        if (this.isDesktop && !this.tabStates[0].loaded) {
          this.loadCategoryFromRoute()
        }
      })
      },
      watch: {
        '$route.params.category': function(newVal) {
          if (this.isDesktop) {
            this.loadCategoryFromRoute()
          }
        }
      },
    beforeDestroy() {
      window.removeEventListener('resize', this.checkDevice)
    },
    created() {
      this.tabPageHeight = Utils.getPageHeight()
    },
    methods: {
      updateTabHeight() {
        if (this.isDesktop) return
        const pageHeight = Utils.getPageHeight()
        const htmlFontSize = parseFloat(getComputedStyle(document.documentElement).fontSize)
        const topBarHeight = 90 / 75 * htmlFontSize
        this.tabPageHeight = Math.floor(pageHeight - topBarHeight)
      },
      checkDevice() {
        this.isDesktop = Utils.isDesktop()
        this.$nextTick(() => {
          this.updateTabHeight()
          this.$nextTick(() => {
            if (!this.isDesktop && this.$refs['wxc-tab-page']) {
              this.$refs['wxc-tab-page'].setPage(this.currentTab, null, false)
            }
          })
        })
      },
      loadCategoryFromRoute() {
        const category = this.$route.params.category || 'comprehensive'
        const tabIndex = this.getTabIndexByCategory(category)
        if (tabIndex !== -1) {
          if (!this.tabStates[tabIndex].loaded) {
            this.switchTab(tabIndex)
          } else {
            this.currentTab = tabIndex
          }
        }
      },
      getTabIndexByCategory(category) {
        const categoryMap = {
          'following': 0,
          'comprehensive': 1,
          'backend': 2,
          'frontend': 3,
          'android': 4,
          'ios': 5,
          'ai': 6,
          'devtools': 7,
          'coderslife': 8,
          'reading': 9,
          'ranking': 10
        }
        return categoryMap[category] !== undefined ? categoryMap[category] : 1
      },
      switchTab(index) {
        if (this.currentTab === index) return
        this.currentTab = index
        this.params.loaddir = 1
        this.params.index = index
        this.params.tag = Config.tabTitles[index].id
        this.params.max_behot_time = 0
        this.params.min_behot_time = 20000000000000
        this.$set(this.tabStates, index, {
          loaded: false, loading: false, loadingMore: false,
          refreshing: false, noMore: false, error: false, errorMsg: ''
        })
        var newList = this.tabList.map(function (tab) { return tab.slice() })
        newList[index] = []
        this.tabList = newList
        this.load(index, 1)
      },
      onDesktopScroll(e) {
        var el = e.target
        var scrollTop = el.scrollTop
        var scrollHeight = el.scrollHeight
        var clientHeight = el.clientHeight
        var state = this.currentState
        if (scrollHeight - scrollTop - clientHeight < 150) {
          if (state && !state.loadingMore && !state.noMore && !state.loading && state.loaded) {
            this.loadmore(this.currentTab)
          }
        }
      },
      onScroll: function (e, index) {
        var el = e.target
        var scrollTop = el.scrollTop
        var scrollHeight = el.scrollHeight
        var clientHeight = el.clientHeight
        var state = this.tabStates[index]
        if (scrollHeight - scrollTop - clientHeight < 100) {
          if (state && !state.loadingMore && !state.noMore && !state.loading && state.loaded) {
            this.loadmore(index)
          }
        }
      },
      loadmore: function (index) {
        var state = this.tabStates[index]
        if (!state || state.loadingMore || state.noMore) return
        this.$set(state, 'loadingMore', true)
        this.load(index, 2)
      },
      loadnew: function (index) {
        var state = this.tabStates[index]
        if (!state || state.loading || state.refreshing) return
        this.$set(state, 'refreshing', true)
        this.load(index, 0)
      },
      load: function (index, loaddir) {
        var idx = (index !== undefined) ? index : this.params.index
        var dir = (loaddir !== undefined) ? loaddir : this.params.loaddir
        var state = this.tabStates[idx]
        if (!state) return
        this.$set(state, 'loading', true)
        this.$set(state, 'error', false)
        var reqParams = {
          loaddir: dir,
          index: idx,
          tag: Config.tabTitles[idx].id,
          size: this.params.size || 10,
          max_behot_time: this.params.max_behot_time,
          min_behot_time: this.params.min_behot_time
        }
        var self = this
        Api.loaddata(reqParams).then((d) => {
          self.$set(state, 'loading', false)
          self.$set(state, 'loadingMore', false)
          self.$set(state, 'refreshing', false)
          self.$set(state, 'loaded', true)
          if (d && d.code === 200) {
            if (d.data && d.data.length > 0) {
              self.tanfer(d.data, idx, dir)
            } else {
              if (dir === 2) {
                self.$set(state, 'noMore', true)
              }
            }
          } else {
            self.$set(state, 'error', true)
            self.$set(state, 'errorMsg', (d && d.errorMessage) || '加载失败，请检查网络')
          }
        }).catch((e) => {
          self.$set(state, 'loading', false)
          self.$set(state, 'loadingMore', false)
          self.$set(state, 'refreshing', false)
          self.$set(state, 'loaded', true)
          self.$set(state, 'error', true)
          self.$set(state, 'errorMsg', '网络请求失败，请检查网络连接')
        })
      },
      tanfer: function (data, curIndex, loaddir) {
        if (!data || data.length === 0) {
          var state = this.tabStates[curIndex]
          if (state) this.$set(state, 'noMore', true)
          return
        }
        var arr = []
        for (var i = 0; i < data.length; i++) {
          try {
            var item = data[i]
            var ims = []
            if (item.images) {
              var imagesStr = item.images
              if (typeof imagesStr === 'string') {
                ims = imagesStr.replace(/[\[\]]/g, '').split(',').filter(function (s) { return s.trim() })
              } else if (Array.isArray(imagesStr)) {
                ims = imagesStr
              }
            }
            var pubTime = item.publishTime
            if (pubTime) {
              if (typeof pubTime === 'string') {
                pubTime = new Date(pubTime).getTime()
              }
              if (isNaN(pubTime)) pubTime = Date.now()
            } else {
              pubTime = Date.now()
            }
            var imgCount = ims.length
            var articleType = imgCount >= 3 ? 3 : (imgCount >= 1 ? 1 : 0)
            var tmp = {
              id: item.id,
              title: item.title || '',
              comment: item.comment || 0,
              authorId: item.authorId,
              source: item.authorName || '',
              date: pubTime,
              type: articleType,
              image: ims,
              icon: '\uf06d',
              staticUrl: item.staticUrl || ''
            }
            if (pubTime && this.params.max_behot_time < pubTime) {
              this.params.max_behot_time = pubTime
            }
            if (pubTime && this.params.min_behot_time > pubTime) {
              this.params.min_behot_time = pubTime
            }
            arr.push(tmp)
          } catch (e) {
          }
        }
        var newList = this.tabList.map(function (tab) { return tab.slice() })
        if (loaddir === 0) {
          newList[curIndex] = arr.concat(newList[curIndex])
        } else {
          newList[curIndex] = newList[curIndex].concat(arr)
        }
        this.tabList = newList
        this.showmore = false
        this.shownew = false
      },
      wxcTabPageCurrentTabSelected(e) {
        var index = e.page
        this.params.loaddir = 1
        this.params.index = index
        this.params.tag = Config.tabTitles[index].id
        this.params.max_behot_time = 0
        this.params.min_behot_time = 20000000000000
        this.$set(this.tabStates, index, {
          loaded: false, loading: false, loadingMore: false,
          refreshing: false, noMore: false, error: false, errorMsg: ''
        })
        var newList = this.tabList.map(function (tab) { return tab.slice() })
        newList[index] = []
        this.tabList = newList
        this.load(index, 1)
      },
      wxcPanItemClicked(item) {
        if (!item || !item.id) return
        if (this.isDesktop && item.staticUrl) {
          window.open(item.staticUrl, '_blank')
          return
        }
        this.$router.push({
          path: '/article/' + item.id
        })
      }
    }
  };
</script>

<style lang="less" scoped>
  @import '../../styles/article';
  .wrapper {
    background-color: @body-background;
    font-size: @font-size;
    font-family: @font-family;
    min-height: 100vh;
  }

  /* 移动端 */
  .top-body {
    position: fixed;
    left: 0;
    right: 0;
    top: 0;
    width: 100%;
    z-index: 100;
  }
  .content-body {
    width: 100%;
    padding-top: 90px;
    box-sizing: border-box;
    background-color: #f5f5f5;
  }
  .item-container {
    width: 100%;
    background-color: #f5f5f5;
    overflow-y: auto;
    -webkit-overflow-scrolling: touch;
    box-sizing: border-box;
    padding-bottom: 120px;
  }
  .cell {
    background-color: #ffffff;
    cursor: pointer;
    transition: background-color 0.15s;
  }
  .cell:hover {
    background-color: #fafafa;
  }
  .pull-refresh {
    display: flex;
    flex-direction: row;
    justify-content: center;
    align-items: center;
    height: 80px;
    gap: 10px;
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
  .loading {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 30px 20px;
    gap: 10px;
  }
  .loading-text {
    font-size: 24px;
    color: #999;
  }
  .no-more .loading-text {
    color: #ccc;
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
  .empty-text {
    font-size: 28px;
    color: #999;
  }
  .error-text {
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

  /* ========== Web端样式 ========== */
  @media screen and (min-width: 768px) {
    .is-desktop {
      background-color: transparent;
      font-size: 14PX;
    }
    .is-desktop .content-body {
      padding-top: 0;
      margin-top: 0;
      background-color: #F4F5F7;
      border-radius: 0;
      overflow: hidden;
    }

    /* Web端分类Tab */
    .desktop-tabs {
      background-color: #FFFFFF;
      border-bottom: 1PX solid #F0F1F5;
      padding: 0 24PX;
      position: sticky;
      top: 0;
      z-index: 10;
    }
    .tabs-scroll {
      display: flex;
      overflow-x: auto;
      padding: 0;
      -webkit-overflow-scrolling: touch;
    }
    .tabs-scroll::-webkit-scrollbar {
      display: none;
    }
    .desktop-tab {
      flex-shrink: 0;
      padding: 12PX 16PX;
      font-size: 15PX;
      font-weight: 500;
      color: #515767;
      cursor: pointer;
      position: relative;
      transition: color 0.2s, background-color 0.2s;
      white-space: nowrap;
      border-radius: 4PX 4PX 0 0;
    }
    .desktop-tab:hover {
      color: #1E80FF;
      background-color: #F4F5F7;
    }
    .desktop-tab.active {
      color: #1E80FF;
      font-weight: 500;
    }
    .desktop-tab.active::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 24PX;
      height: 2PX;
      background-color: #1E80FF;
      border-radius: 1PX;
    }

    /* Web端列表容器 */
    .desktop-list {
      width: 100%;
    }
    .list-container {
      overflow: visible;
    }
    .desktop-cell {
      border-bottom: 1PX solid #f5f5f5;
      margin: 0;
    }
    .desktop-cell:last-child {
      border-bottom: none;
    }
    .list-bottom {
      height: 20PX;
    }

    /* 覆盖article.less中的Web端字体和间距 */
    .desktop-cell /deep/ .list-item {
      padding: 16PX 20PX;
    }
    .desktop-cell /deep/ .title {
      font-size: 17PX;
      line-height: 1.4;
      margin: 0 0 8PX;
      padding: 0;
      color: #222;
    }
    .desktop-cell /deep/ .tags {
      margin: 8PX 0 0;
    }
    .desktop-cell /deep/ .tags-text {
      font-size: 12PX;
      color: #999;
      margin-right: 12PX;
    }
    .desktop-cell /deep/ .image {
      height: 100PX;
      border-radius: 4PX;
    }
    .desktop-cell /deep/ .image-error {
      font-size: 24PX;
    }
    .desktop-cell /deep/ .line {
      display: none;
    }

    /* 覆盖加载/空状态的Web端样式 */
    .pull-refresh {
      height: 40PX;
      gap: 8PX;
    }
    .loading-spinner {
      width: 20PX;
      height: 20PX;
      border-width: 2PX;
    }
    .loading-text {
      font-size: 13PX;
      color: #999;
    }
    .loading {
      padding: 20PX;
      gap: 8PX;
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
  }
</style>
