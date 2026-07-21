<template>
    <div class="cell-body">
        <span class="icon">{{icon}}</span>
        <span class="title" @click="onClickText">{{title}}</span>
        <span class="icon" @click="onDeleteHistory">&#xf00d;</span>
    </div>
</template>

<script>
    export default {
        name: "search_0",
        props:{
            id:{
                type:String,
                default:''
            },
            title:{
                type:String,
                default:'显示字'
            },
            icon:{
                type:String,
                default:'\uf017'
            }
        },
        methods : {
            onDeleteHistory : function(){
                this.$emit("onDeleteHistory",this.id)
            },
            onClickText :function(){
                this.$emit("onClickText",this.title)
            }
        }
    }
</script>

<style lang="less" scoped>
    @import '../../styles/common';
    .cell-body{
       display: flex;
        flex-direction: row;
        font-size: 36px;
        align-items: center;
        border-bottom-color: #e8e8e8;
        border-bottom-width: 1px;
        border-bottom-style: solid;
        padding: 17px 20px;
        background-color: #ffffff;
    }
    .title{
        flex: 1;
        color: #333333;
    }
    .icon{
        font-size: 40px;
        color: #ebebeb;
        width: 45px;
        margin-right: 10px;
    }

    @media screen and (min-width: 768px) {
        .cell-body {
            max-width: 750PX;
            margin: 0 auto;
            font-size: 16PX;
            padding: 12PX 20PX;
        }
        .icon {
            font-size: 18PX;
            width: 24PX;
            margin-right: 12PX;
        }
    }
</style>
