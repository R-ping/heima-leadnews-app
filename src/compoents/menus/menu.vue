<template>
    <div class="wxc-tab-page">
        <div class="tab-title-list"
             :style="{ height: '120px',paddingBottom:isIPhoneX?'78px':'0'}">
            <div class="title-item"
                 v-for="(v,index) in tabTitles"
                 :key="index"
                 :ref="'wxc-tab-title-'+index"
                 @click="setPage(index,v.url)"
                 :style="{ width: tabStyles.width +'px', height: tabStyles.height +'px', backgroundColor: currentPage == index ? tabStyles.activeBgColor : tabStyles.bgColor }"
                 :accessible="true"
                 :aria-label="`${v.title?v.title:'标签'+index}`">
                <span class="icon" :style="{color:getFillColor(index)}">{{v.icon}}</span>

                <span 
                        v-if="!titleUseSlot"
                        :style="{ fontSize: tabStyles.fontSize+'px', fontWeight: (currentPage == index && tabStyles.isActiveTitleBold)? 'bold' : 'normal', color: currentPage == index ? tabStyles.activeTitleColor : tabStyles.titleColor, paddingLeft:tabStyles.textPaddingLeft+'px', paddingRight:tabStyles.textPaddingRight+'px'}"
                        class="tab-text">{{v.title}}</span>
                <div class="desc-tag" v-if="v.badge && !titleUseSlot">
                    <span class="desc-text">{{v.badge}}</span>
                </div>
                <div v-if="v.dot && !v.badge && !titleUseSlot" class="dot"></div>
                <slot :name="`tab-title-${index}`" v-if="titleUseSlot"></slot>
            </div>
        </div>
    </div>
</template>

<style scoped>
    .icon{
        color: #666666;
        font-family: fontawesome;
        font-size: 48px;
        background-color: transparent;
    }
    .wxc-tab-page {
        width: 750px;
        position: absolute;
        left: 0;
        bottom: 0;
        background-color: #ffffff;
        border-top-color: #ebebeb;
        border-top-width: 1px;
    }
    .tab-title-list {
        display: flex;
        flex-direction: row;
        display: flex;
        justify-content: space-around;
    }
    .title-item {
        display: flex;
        justify-content: center;
        align-items: center;
        border-bottom-style: solid;
    }
    .tab-text {
        -webkit-line-clamp: 1; overflow: hidden; display: -webkit-box; -webkit-box-orient: vertical;
        text-overflow: ellipsis;
    }
    .desc-tag {
        position: absolute;
        top: 10px;
        right: 20px;
        border-bottom-right-radius: 14px;
        border-bottom-left-radius: 0;
        border-top-left-radius: 14px;
        border-top-right-radius: 14px;
        background-color: #FF5E00;
        height: 26px;
        align-items: center;
        display: flex;
        justify-content: center;
        padding-left: 6px;
        padding-right: 6px;
    }
    .dot {
        width: 12px;
        height: 12px;
        border-bottom-right-radius: 12px;
        border-bottom-left-radius: 12px;
        border-top-left-radius: 12px;
        border-top-right-radius: 12px;
        position: absolute;
        top: 10px;
        right: 40px;
        background-color: #FF5E00;
    }
    .desc-text {
        font-size: 18px;
        color: #ffffff;
    }
</style>

<script>
    import Utils from '@/utils/env';
    import config from './config';

    export default {
        props: {
            tabTitles: {
                type: Array,
                default: () => (config.tabTitles)
            },
            tabStyles:{
                type: Object,
                default: () => ({
                    bgColor: '#ffffff',
                    titleColor: '#757575',
                    activeTitleColor: '#3194FF',
                    activeBgColor: '#ffffff',
                    activeIconColor:'#3194FF',
                    iconColor:'#757575',
                    isActiveTitleBold: true,
                    iconWidth: 70,
                    iconHeight: 70,
                    width: 160,
                    height: 120,
                    fontSize: 24,
                    activeBottomColor: '#FFC900',
                    activeBottomWidth: 120,
                    activeBottomHeight: 6,
                    textPaddingLeft: 10,
                    textPaddingRight: 10
                })
            },
            titleUseSlot: {
                type: Boolean,
                default: false
            }
        },
        data: () => ({
            currentPage: 0
        }),
        created () {
            this.isIPhoneX = Utils.isIPhoneX();
        },
        methods: {
            getFillColor (index){
                if(this.currentPage==index){
                    return this.tabStyles.activeIconColor;
                }else{
                    return this.tabStyles.iconColor;
                }
            },
            setPage(page, url = null, animated = true) {
                this.currentPage = page;
                if(page>0){
                    this.$config.noAction();
                }
            }
        }
    };
</script>
