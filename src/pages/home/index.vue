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
        if (this.isDesktop && !this.tabStates[0].loaded && !this.recommendStates[0].loaded) {
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
          'recommend': 0, 'following': 1, 'comprehensive': 2, 'backend': 3,
          'frontend': 4, 'android': 5, 'ios': 6, 'ai': 7, 'devtools': 8,
          'coderslife': 9, 'reading': 10, 'ranking': 11
        }
        return categoryMap[category] !== undefined ? categoryMap[category] : 2
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

<style lang="less" scoped src="./styles/home.less"></style>