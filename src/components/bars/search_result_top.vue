<template>
    <div class="bar_bg">
        <!-- 品牌标识 -->
        <div class="brand" @click="home">
            <img class="brand-logo" src="/static/images/logo-icon.svg" alt="logo" />
            <span class="brand-text">逐日Coding</span>
        </div>
        <span class="icon back-icon" @click="back">&#xf104;</span>
        <Search
                class="search-input"
                :icon="icon"
                @onSubmit="onSubmit"
                @onInput="onInput"
                :value="searchValue"
                :radius="62"
                :height="searchHeight"
                :placeholder="placeholder"
                right-width="30"
                left-width="30" />
        <span class="search-submit" @click="onSubmitClick">&#xf002;</span>
        <span class="icon close-icon" @click="home">&#xf00d;</span>
    </div>
</template>

<script>
    import Search from '@/components/inputs/search';
    import Utils from '@/utils/env';
    export default {
        name: "search_top_bar",
        components: {Search},
        props : {
            value : {
                type:String,
                default:'12'
            },
            placeholder:{
                type:String,
                default:'请输入...'
            }
        },
        data(){
            return {
                icon:'\uF002',
                searchValue: this.value || ''
            }
        },
        computed: {
            searchHeight: function () {
                return Utils.isDesktop() ? 40 : 70
            }
        },
        watch: {
            value: function (newVal) {
                this.searchValue = newVal || ''
            }
        },
        methods:{
            back : function(){
                this.$router.back()
            },
            home : function(){
                this.$router.push('/home');
            },
            onSubmit : function(val){
                this.searchValue = val || ''
                this.$emit('onSubmit', val);
            },
            onInput : function(val){
                this.searchValue = val || ''
            },
            onSubmitClick : function(){
                this.$emit('onSubmit', this.searchValue);
            }
        }
    }
</script>

<style lang="less" scoped>
    @import '../../styles/common';
    .bar_bg{
        width: @screen-width;
        display: flex;
        flex-direction: row;
        background-color: @mian-color;
        border-style: solid;
        height: @top-height;
        padding: 7px 15px 7px 20px;
        align-items: center;
        box-sizing: border-box;
    }
    .brand {
        display: none;
    }
    .icon{
        color: #ffffff;
        font-size: 42px;
        font-family: fontawesome;
    }
    .search-submit {
        display: none;
    }

    @media screen and (min-width: 768px) {
        .bar_bg {
            width: 100%;
            background-color: #FFFFFF;
            border-style: none;
            border-bottom: 1PX solid #F0F1F5;
            box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
            height: 60PX;
            padding: 10PX 24PX;
            box-sizing: border-box;
            justify-content: center;
            gap: 0;
        }
        .brand {
            display: flex;
            align-items: center;
            gap: 8PX;
            cursor: pointer;
            flex-shrink: 0;
            margin-right: 24PX;
            user-select: none;
        }
        .brand-logo {
            width: 28PX;
            height: 28PX;
        }
        .brand-text {
            font-size: 18PX;
            font-weight: 700;
            color: #1D2129;
            font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif;
            white-space: nowrap;
        }
        .icon {
            color: #515767;
            font-size: 20PX;
            cursor: pointer;
            flex-shrink: 0;
            font-family: fontawesome;
            transition: color 0.2s;
        }
        .icon:hover {
            color: #1E80FF;
        }
        .back-icon {
            margin-right: 12PX;
        }
        .close-icon {
            margin-left: 12PX;
        }
        .search-input {
            flex: 1;
            max-width: 520PX;
        }
        .search-submit {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 36PX;
            height: 36PX;
            margin-left: 12PX;
            font-family: fontawesome;
            font-size: 18PX;
            color: #FFFFFF;
            background-color: #1E80FF;
            border-radius: 50%;
            cursor: pointer;
            transition: background-color 0.2s;
            flex-shrink: 0;
        }
        .search-submit:hover {
            background-color: #1A6FD9;
        }
    }
</style>
