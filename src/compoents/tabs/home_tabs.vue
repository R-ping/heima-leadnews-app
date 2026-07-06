<!-- CopyRight (C) 2017-2022 Alibaba Group Holding Limited. -->
<!-- Created by Tw93 on 17/07/28. -->
<!-- Updated by Tw93 on 17/11/16.-->

<template>
  <div class="wxc-tab-page"
       :style="{ height: (tabPageHeight)+'px', backgroundColor:wrapBgColor }">
    <div class="tab-body">
      <div class="tab-title-list"
                ref="tab-title-list"
                :show-scrollbar="false"
                scroll-direction="horizontal"
                :data-spm="spmC"
                :style="{
                  marginRight:(showMore?'15px':'0px'),
                  backgroundColor: tabStyles.bgColor,
                  height: (tabStyles.height)+'px'
                }">

        <div class="title-item"
             v-for="(v,index) in tabTitles"
             :key="index"
             :ref="'wxc-tab-title-'+index"
             @click="setPage(index,v.url, clickAnimation)"
             :style="{
               width: tabStyles.width +'px',
               height: tabStyles.height +'px',
               backgroundColor: currentPage === index ? tabStyles.activeBgColor : tabStyles.bgColor
             }"
             :accessible="true"
             :aria-label="`${v.title?v.title:'标签'+index}`">

          <img :src="currentPage === index ? v.activeIcon : v.icon"
                 v-if="titleType === 'icon' && !titleUseSlot"
                 :style="{ width: tabStyles.iconWidth + 'px', height:tabStyles.iconHeight+'px'}"></img>

          <span class="icon-font"
                v-if="titleType === 'iconFont' && v.codePoint && !titleUseSlot"
                :style="{fontFamily: 'wxcIconFont',fontSize: tabStyles.iconFontSize+'px', color: currentPage === index ? tabStyles.activeIconFontColor : tabStyles.iconFontColor}">{{v.codePoint}}</span>

          <span 
            v-if="!titleUseSlot"
            :style="{ fontSize: tabStyles.fontSize+'px', fontWeight: (currentPage === index && tabStyles.isActiveTitleBold)? 'bold' : 'normal', color: currentPage === index ? tabStyles.activeTitleColor : tabStyles.titleColor, paddingLeft:(tabStyles.textPaddingLeft?tabStyles.textPaddingLeft:10)+'px', paddingRight:(tabStyles.textPaddingRight?tabStyles.textPaddingRight:10)+'px'}"
            class="tab-text">{{v.title}}</span>
          <div class="border-bottom"
               v-if="tabStyles.hasActiveBottom && !titleUseSlot"
               :style="{ width: tabStyles.activeBottomWidth+'px', left: (tabStyles.width-tabStyles.activeBottomWidth)/2+'px', height: tabStyles.activeBottomHeight+'px', backgroundColor: currentPage === index ? tabStyles.activeBottomColor : 'transparent' }"></div>
          <slot :name="`tab-title-${index}`" v-if="titleUseSlot"></slot>
        </div>
      </div>
      <span v-if="showMore" class="icon" @click="noAction"
            :style="{ fontSize: tabStyles.fontSize*1.6+'px', color: tabStyles.titleColor}">&#xf0c9;</span>
    </div>
    <div class="tab-page-wrap"
         ref="tab-page-wrap"
         @horizontalpan="startHandler"
         :style="{ height: (tabPageHeight-tabStyles.height)+'px' }">
      <div ref="tab-container"
           class="tab-container">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
  @import '../../styles/common';
  .wxc-tab-page {
    width: 750px;
  }

  .tab-body{
    display: flex;
        flex-direction: row;
    display: flex;
        justify-content: center;
    align-items: center;
  }

  .tab-title-list {
    display: flex;
        flex-direction: row;
    flex: 1;
    border-right: 2px solid #ebebeb;
  }

  .title-item {
    display: flex;
        justify-content: center;
    align-items: center;
    border-right-style: solid;
    border-right: 1px solid #ebebeb;
  }

  .border-bottom {
    position: absolute;
    bottom: 13px;
    border-radius: 5px;
  }

  .tab-page-wrap {
    border-bottom: 1px solid #ebebeb;
    border-top: 1px solid #ebebeb;
    padding-top: 10px;
    padding-bottom: 10px;
    width: 750px;
    background-color: #ffffff;
    /* overflow: hidden; */
  }

  .tab-container {
    flex: 1;
    display: flex;
        flex-direction: row;
    position: absolute;
  }

  .tab-text {
    -webkit-line-clamp: 1; overflow: hidden; display: -webkit-box; -webkit-box-orient: vertical;
    text-overflow: ellipsis;
  }
  .icon{
    width: 45px;
    font-family: fontawesome;
    font-size: 48px;
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
        default: true
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
          titleColor: '#666666',
          activeTitleColor: '#3D3D3D',
          activeBgColor: '#FFFFFF',
          isActiveTitleBold: true,
          iconWidth: 70,
          iconHeight: 70,
          width: 160,
          height: 120,
          fontSize: 24,
          hasActiveBottom: true,
          activeBottomColor: '#FFC900',
          activeBottomWidth: 120,
          activeBottomHeight: 6,
          textPaddingLeft: 10,
          textPaddingRight: 10,
          leftOffset: 0,
          rightOffset: 0,
          normalBottomColor: '#F2F2F2',
          normalBottomHeight: 0,
          hasRightIcon: false
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
      gesToken: 0,
      isMoving: false,
      startTime: 0,
      deltaX: 0,
      translateX: 0
    }),
    created () {
      const { titleType, tabStyles } = this;
      if (titleType === 'iconFont' && tabStyles.iconFontUrl) {
        // Inject @font-face via style element
        var style = document.createElement('style');
        style.textContent = '@font-face { font-family: "wxcIconFont"; src: url(' + tabStyles.iconFontUrl + '); }';
        document.head.appendChild(style);
      }
    },
    mounted () {
      // swipeBack not available in browser — skip
      // BindEnv/Binding not available in browser — skip
    },
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
      startHandler () {
        // Weex expression binding not available in browser
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
        const previousPage = this.currentPage;
        const currentTabEl = this.$refs[`wxc-tab-title-${page}`];
        // $refs returns array in old weex, single element in Vue 2
        const tabEl = Array.isArray(currentTabEl) ? currentTabEl[0] : currentTabEl;
        const { width } = this.tabStyles;
        const appearNum = parseInt(750 / width);
        const tabsNum = this.tabTitles.length;

        if (appearNum < tabsNum && tabEl) {
          var offset = page > appearNum ? -(750 - width) / 2 : -width * 2;
          if (page <= 1 && previousPage > page) {
            offset = -width * page;
          }
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
        const dist = page * 750;
        // Use CSS transition instead of weex animation module
        containerEl.style.transition = 'transform ' + computedDur + 'ms ' + timingFunction;
        containerEl.style.transform = 'translateX(' + (-dist) + 'px)';
      }
    }
  };
</script>
