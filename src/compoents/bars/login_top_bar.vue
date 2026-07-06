<template>
    <div class="minibar" :style="{backgroundColor: backgroundColor}">
        <span class="icon left-icon" @click="minibarLeftButtonClick"></span>
        <span class="title-text" :style="{color: '#FFFFFF'}">登 录</span>
        <span class="icon right-icon"></span>
    </div>
</template>

<script>

    export default {
        name: "login_top_bar",
        props:{
          text:{
              type:String,
              default:'文章标题'
          }
        },
        data:()=>{
            return {
                backgroundColor:'#3296fa',
                useDefaultReturn:false
            }
        },
        mounted(){
            this.backgroundColor = this.$config.style.main_bg;
        },
        computed:{
            getText:function(){
                let temp = this.text;
                if(this.text.length>12){
                    temp = this.text.substring(0,11)+'...';
                }
                return temp;
            }
        },
        methods: {
            noAction:function () {
                this.$config.noAction()
            },
            minibarLeftButtonClick () {
                this.$router.back()
            },
            minibarRightButtonClick () {
            }
        }
    }
</script>

<style lang="less" scoped>
    @import '../../styles/common';
    .minibar {
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: space-between;
        width: 750px;
        height: 90px;
        padding: 0 20px;
    }
    .icon{
        font-size: 36px;
        color: #ffffff;
        width: 60px;
    }
    .left-icon {
        text-align: left;
    }
    .right-icon {
        text-align: right;
    }
    .title-text {
        flex: 1;
        text-align: center;
        font-size: 32px;
    }
</style>
