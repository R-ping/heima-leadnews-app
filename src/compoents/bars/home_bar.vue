<template>
    <div class="bar_bg">
        <span class="icon"> </span>
        <Search type="search" @onClick="onClick" :icon="icon" left-width="25" rightWidth="20" placeholder="请输入搜索关键字..."/>
        <span class="icon login-btn" v-if="!isLoggedIn" @click="showLogin">{{loginIcon}}</span>
        <span class="icon user-btn" v-if="isLoggedIn">{{userIcon}}</span>
    </div>
</template>

<script>
    import Search from '@/compoents/inputs/search_buttion';
    export default {
        name: "index",
        components: { Search},
        data:()=>{
            return {
                icon:'\uF002',
                loginIcon:'\uf007',
                userIcon:'\uf007'
            }
        },
        computed: {
            isLoggedIn() {
                return this.$store.getters.isLoggedIn
            }
        },
        mounted(){
            this.backgroundColor = this.$config.style.main_bg;
        },
        methods: {
            onClick : function(){
                this.$router.push('/search')
            },
            showLogin() {
                this.$store.dispatch('showLogin')
            },
            minibarLeftButtonClick () {
            },
            minibarRightButtonClick () {
            }
        }
    };
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
        padding: 7px 15px 10px;
        align-items: center;
    }
    .icon{
        width: 25px;
        color: #ffffff;
        font-family: fontawesome;
        font-size: 32px;
    }
    .login-btn, .user-btn {
        cursor: pointer;
    }
</style>
