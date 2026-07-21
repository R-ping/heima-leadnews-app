<template>
    <div class="menu-bar">
        <div class="menu-inner">
            <div class="menu-item"
                 v-for="(item, index) in tabTitles"
                 :key="index"
                 :class="{ active: currentPage === index }"
                 @click="handleClick(item, index)">
                <span class="menu-icon" :class="{ 'dot-icon': item.dot }">{{ item.icon }}</span>
                <span class="menu-text">{{ item.title }}</span>
                <span class="menu-badge" v-if="item.badge">{{ item.badge }}</span>
                <span class="menu-dot" v-if="item.dot && !item.badge"></span>
            </div>
        </div>
        <div class="menu-safe-area" v-if="isIPhoneX"></div>
    </div>
</template>

<script>
    import Utils from '@/utils/env';
    import config from './config';

    export default {
        props: {
            tabTitles: {
                type: Array,
                default: () => (config.tabTitles)
            }
        },
        data: () => ({
            currentPage: 0,
            isIPhoneX: false
        }),
        created() {
            this.isIPhoneX = Utils.isIPhoneX();
        },
        methods: {
            handleClick(item, index) {
                this.currentPage = index;
                switch(index) {
                    case 0:
                        this.$router.push('/home');
                        break;
                    case 1:
                    case 2:
                        this.$config && this.$config.noAction && this.$config.noAction();
                        break;
                    case 3:
                        var isLoggedIn = this.$store.getters.isLoggedIn;
                        if (!isLoggedIn) {
                            this.$store.dispatch('showLogin');
                        } else {
                            this.$config && this.$config.noAction && this.$config.noAction();
                        }
                        break;
                    default:
                        this.$router.push('/home');
                }
            }
        }
    };
</script>

<style lang="less" scoped>
    @import '../../styles/common';

    .menu-bar {
        width: 100%;
        background-color: #ffffff;
        border-top: 1px solid #ebebeb;
        box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
    }
    .menu-inner {
        display: flex;
        flex-direction: row;
        justify-content: space-around;
        align-items: center;
        height: 120px;
        padding: 8px 0;
        box-sizing: border-box;
    }
    .menu-item {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 6px;
        cursor: pointer;
        position: relative;
        padding: 8px 0;
        transition: all 0.2s;
    }
    .menu-icon {
        font-family: fontawesome;
        font-size: 44px;
        color: #999999;
        transition: color 0.2s;
        line-height: 1;
    }
    .menu-item.active .menu-icon {
        color: @mian-color;
    }
    .menu-text {
        font-size: 22px;
        color: #999999;
        transition: color 0.2s;
    }
    .menu-item.active .menu-text {
        color: @mian-color;
        font-weight: 500;
    }
    .menu-badge {
        position: absolute;
        top: 2px;
        right: 50%;
        margin-right: -40px;
        background-color: #ff5e00;
        color: #ffffff;
        font-size: 18px;
        padding: 2px 10px;
        border-radius: 14px;
        min-width: 24px;
        text-align: center;
        line-height: 1.4;
    }
    .menu-dot {
        position: absolute;
        top: 8px;
        right: 50%;
        margin-right: -32px;
        width: 14px;
        height: 14px;
        background-color: #ff5e00;
        border-radius: 50%;
    }
    .menu-safe-area {
        height: 68px;
        background-color: #ffffff;
    }
</style>
