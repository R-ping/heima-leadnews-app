<template>
  <div class="wxc-tab-page"
       :style="{ height: (tabPageHeight)+'px', backgroundColor:wrapBgColor }">
    <div class="tab-body">
      <div class="tab-title-list"
           ref="tab-title-list">

        <div class="title-item"
             v-for="(v,index) in tabTitles"
             :key="index"
             :ref="'wxc-tab-title-'+index"
             @click="setPage(index,v.url, clickAnimation)"
             :style="{
               backgroundColor: currentPage === index ? tabStyles.activeBgColor : tabStyles.bgColor
             }"
             :accessible="true"
             :aria-label="`${v.title?v.title:'标签'+index}`">

          <span
            v-if="!titleUseSlot"
            :style="{ fontSize: tabStyles.fontSize+'px', fontWeight: (currentPage === index && tabStyles.isActiveTitleBold)? 'bold' : 'normal', color: currentPage === index ? tabStyles.activeTitleColor : tabStyles.titleColor, paddingLeft:(tabStyles.textPaddingLeft?tabStyles.textPaddingLeft:10)+'px', paddingRight:(tabStyles.textPaddingRight?tabStyles.textPaddingRight:10)+'px'}"
            class="tab-text">{{v.title}}</span>
          <div class="border-bottom"
               v-if="tabStyles.hasActiveBottom && !titleUseSlot"
               :style="{ width: tabStyles.activeBottomWidth+'px', height: tabStyles.activeBottomHeight+'px', backgroundColor: currentPage === index ? tabStyles.activeBottomColor : 'transparent' }"></div>
          <slot :name="`tab-title-${index}`" v-if="titleUseSlot"></slot>
        </div>
      </div>
      <span v-if="showMore" class="more-icon" @click="noAction">&#xf0c9;</span>
    </div>
    <div class="tab-page-wrap"
         ref="tab-page-wrap"
         :style="{ height: (tabPageHeight-tabStyles.height)+'px' }">
      <div ref="tab-container"
           class="tab-container">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
  .wxc-tab-page {
    width: 100%;
  }

  .tab-body{
    display: flex;
    flex-direction: row;
    justify-content: center;
    align-items: center;
    border-bottom: 1px solid #ebebeb;
  }

  .tab-title-list {
    display: flex;
    flex-direction: row;
    flex: 1;
    overflow-x: auto;
    overflow-y: hidden;
    -webkit-overflow-scrolling: touch;
    &::-webkit-scrollbar {
      display: none;
    }
  }

  .title-item {
    display: flex;
    justify-content: center;
    align-items: center;
    flex-shrink: 0;
    position: relative;
    cursor: pointer;
    border-right: 1px solid #f5f5f5;
  }

  .border-bottom {
    position: absolute;
    bottom: 0;
    border-radius: 3px;
    left: 50%;
    transform: translateX(-50%);
  }

  .tab-page-wrap {
    width: 100%;
    background-color: #ffffff;
    overflow: hidden;
  }

  .tab-container {
    display: flex;
    flex-direction: row;
    width: 100%;
  }

  .tab-text {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .more-icon{
    width: 45px;
    flex-shrink: 0;
    font-family: fontawesome;
    font-size: 28px;
    color: #999;
    text-align: center;
    cursor: pointer;
  }
</style>

<script>
  import Utils from '@/utils/env'

  export default {
    props: {
      tabTitles: {
        type: Array,
        default: () => ([])
      },
      panDist: {
        type: Number,
        default: 200
      },
      showMore: {
        type: Boolean,
        default: false
      },
      spmC: {
        type: [String, Number],
        default: ''
      },
      titleUseSlot: {
        type: Boolean,
        default: false
      },
      tabStyles: {
        type: Object,
        default: () => ({
          bgColor: '#FFFFFF',
          titleColor: '#9b9b9b',
          activeTitleColor: '#3D3D3D',
          activeBgColor: '#FFFFFF',
          isActiveTitleBold: true,
          width: 120,
          height: 80,
          fontSize: 24,
          hasActiveBottom: true,
          activeBottomColor: '#3194ff',
          activeBottomHeight: 6,
          activeBottomWidth: 36,
          textPaddingLeft: 10,
          textPaddingRight: 10
        })
      },
      titleType: {
        type: String,
        default: 'icon'
      },
      tabPageHeight: {
        type: [String, Number],
        default: 1334
      },
      needSlider: {
        type: Boolean,
        default: true
      },
      isTabView: {
        type: Boolean,
        default: true
      },
      duration: {
        type: [Number, String],
        default: 300
      },
      timingFunction: {
        type: String,
        default: 'cubic-bezier(0.25, 0.46, 0.45, 0.94)'
      },
      wrapBgColor: {
        type: String,
        default: '#ffffff'
      },
      clickAnimation: {
        type: Boolean,
        default: true
      },
      rightIconStyle: {
        type: Object,
        default: () => ({
          top: 0,
          right: 0,
          paddingLeft: 20,
          paddingRight: 20
        })
      }
    },
    data: () => ({
      currentPage: 0,
      isMoving: false
    }),
    methods: {
      noAction(){
        this.$config.noAction()
      },
      next () {
        let page = this.currentPage;
        if (page < this.tabTitles.length - 1) {
          page++;
        }
        this.setPage(page);
      },
      prev () {
        let page = this.currentPage;
        if (page > 0) {
          page--;
        }
        this.setPage(page);
      },
      setPage (page, url = null, animated = true) {
        if (!this.isTabView) {
          this.jumpOut(url);
          return;
        }
        if (this.isMoving === true) {
          return;
        }
        this.isMoving = true;
        const currentTabEl = this.$refs[`wxc-tab-title-${page}`];
        const tabEl = Array.isArray(currentTabEl) ? currentTabEl[0] : currentTabEl;

        if (tabEl) {
          tabEl.scrollIntoView({ behavior: animated ? 'smooth' : 'auto', inline: 'nearest', block: 'nearest' });
        }

        this.isMoving = false;
        this.currentPage = page;
        this._animateTransformX(page, animated);
        this.$emit('wxcTabPageCurrentTabSelected', { page });
      },
      jumpOut (url) {
        url && Utils.goToH5Page(url)
      },
      _animateTransformX (page, animated) {
        const { duration, timingFunction } = this;
        const computedDur = animated ? duration : 0.00001;
        const containerEl = this.$refs['tab-container'];
        // Get the actual width of the container parent
        const parentWidth = containerEl.parentElement.offsetWidth;
        const dist = page * parentWidth;
        containerEl.style.transition = 'transform ' + computedDur + 'ms ' + timingFunction;
        containerEl.style.transform = 'translateX(' + (-dist) + 'px)';
        // Set each child to full width
        const children = containerEl.children;
        for (let i = 0; i < children.length; i++) {
          children[i].style.width = parentWidth + 'px';
        }
      }
    }
  };
</script>