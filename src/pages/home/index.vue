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
          <div class="pull-refresh" v-if="tabStates[index] && tabStates[index].refreshing">
            <span class="loading-spinner"></span>
            <span class="loading-text">{{ load_new_text }}</span>
          </div>
          <div v-for="(item,key) in v" class="cell" :key="item.id || key"
            @click="wxcPanItemClicked(item)">
            <Item0 v-if="item.type === 0" :data="item"/>
            <Item1 v-if="item.type === 1" :data="item"/>
            <Item3 v-if="item.type === 3" :data="item"/>
          </div>
          <div class="loading" v-if="tabStates[index] && tabStates[index].loadingMore">
            <span class="loading-spinner"></span>
            <span class="loading-text">{{ load_more_text }}</span>
          </div>
          <div class="loading no-more" v-if="tabStates[index] && tabStates[index].noMore && v.length > 0">
            <span class="loading-text">— 没有更多了 —</span>
          </div>
          <div class="empty-state" v-if="tabStates[index] && tabStates[index].loaded && v.length === 0 && !tabStates[index].loading">
            <span class="empty-icon">&#xf15c;</span>
            <span class="empty-text">暂无内容</span>
          </div>
          <div class="error-state" v-if="tabStates[index] && tabStates[index].error">
            <span class="error-text">{{ tabStates[index].errorMsg }}</span>
            <span class="retry-btn" @click="load()">点击重试</span>
          </div>
        </div>
      </wxc-tab-page>

      <!-- Web端列表 -->
      <div class="desktop-list" v-if="isDesktop">
        <div class="list-container" @scroll="onDesktopScroll">
          <div class="pull-refresh" v-if="currentState.refreshing">
            <span class="loading-spinner"></span>
            <span class="loading-text">{{ load_new_text }}</span>
          </div>
          <div v-for="(item,key) in currentList" class="cell desktop-cell" :key="item.id || key"
            @click="wxcPanItemClicked(item)">
            <Item0 v-if="item.type === 0" :data="item"/>
            <Item1 v-if="item.type === 1" :data="item"/>
            <Item3 v-if="item.type === 3" :data="item"/>
          </div>
          <div class="loading" v-if="currentState.loadingMore">
            <span class="loading-spinner"></span>
            <span class="loading-text">{{ load_more_text }}</span>
          </div>
          <div class="loading no-more" v-if="currentState.noMore && currentList.length > 0">
            <span class="loading-text">— 没有更多了 —</span>
          </div>
          <div class="empty-state" v-if="currentState.loaded && currentList.length === 0 && !currentState.loading">
            <span class="empty-icon">&#xf15c;</span>
            <span class="empty-text">暂无内容</span>
          </div>
          <div class="error-state" v-if="currentState.error">
            <span class="error-text">{{ currentState.errorMsg }}</span>
            <span class="retry-btn" @click="load(currentTab, 1)">点击重试</span>
          </div>
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
  import feedMixin from './mixins/feedMixin'

  export default {
    name: 'HeiMa-Home',
    components: { Home_Bar, WxcTabPage, Item0, Item1, Item3 },
    mixins: [feedMixin],
    data: () => ({
      isDesktop: false,
      currentTab: 0,
      shownew: false,
      showmore: false,
      tabTitles: Config.tabTitles,
      tabStyles: Config.tabStyles,
      tabList: [...Array(Config.tabTitles.length).keys()].map(() => []),
      tabPageHeight: 1334
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
      '$route.params.category': function() {
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
          'following': 0, 'comprehensive': 1, 'backend': 2, 'frontend': 3,
          'android': 4, 'ios': 5, 'ai': 6, 'devtools': 7, 'coderslife': 8,
          'reading': 9, 'ranking': 10
        }
        return categoryMap[category] !== undefined ? categoryMap[category] : 1
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