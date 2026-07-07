<template>
    <div class="bar_bg">
        <span class="bar-icon">&#xf0c9;</span>
        <Search type="search" @onClick="onClick" :icon="icon" left-width="25" rightWidth="20" placeholder="请输入搜索关键字..."/>
        <span class="bar-icon login-btn" v-if="!isLoggedIn" @click="showLogin">&#xf007;</span>
        <span class="bar-icon user-btn" v-if="isLoggedIn">&#xf007;</span>
    </div>
</template>

<script>
    import Search from '@/compoents/inputs/search_buttion';
    export default {
        name: "index",
        components: { Search},
        data:()=>{
            return {
                icon:'\uF002'
            }
        },
        computed: {
            isLoggedIn() {
                return this.$store.getters.isLoggedIn
            }
        },
        methods: {
            onClick : function(){
                this.$router.push('/search')
            },
            showLogin() {
                this.$store.dispatch('showLogin')
            }
        }
    };
</script>

<style lang="less" scoped>
    @import '../../styles/common';
    .bar_bg{
        width: 100%;
        display: flex;
        flex-direction: row;
        background-color: @mian-color;
        height: @top-height;
        padding: 7px 15px 10px;
        align-items: center;
        box-sizing: border-box;
    }
    .bar-icon{
        width: 25px;
        color: #ffffff;
        font-family: fontawesome;
        font-size: 32px;
        text-align: center;
        flex-shrink: 0;
    }
    .login-btn, .user-btn {
        cursor: pointer;
    }
</style>