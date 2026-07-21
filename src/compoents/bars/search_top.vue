<template>
    <div class="bar_bg">
        <Search type="search"
                autofocus="autofocus"
                @onSubmit="onSubmit"
                @onInput="onInput"
                @onBlur="onBlur"
                @onChange="onChange"
                :radius="62"
                :icon="icon"
                placeholder="请输入搜索关键字..."
                rightWidth="0" />
        <span class="icon" @click="back">取消</span>
    </div>
</template>

<script>
    import Search from '@/compoents/inputs/search';
    export default {
        name: "search_top_bar",
        components: {Search},
        data(){
            return {
                icon:'\uF002',
                autofocus:true
            }
        },
        methods:{
            back : function(){
                this.$router.back();
            },
            onSubmit : function(val){
                this.$emit('onSubmit',val)
            },
            onInput:function(val){
                this.$emit("onInput",val)
            },
            onChange:function(val){
                this.$emit("onChange",val)
            },
            onBlur:function(val){
                this.$emit("onBlur",val)
            }
        }
    }
</script>

<style lang="less" scoped>
    @import '../../styles/common';
    .bar_bg{
        width: 100%;
        display: flex;
        flex-direction: row;
        background-color: @mian-color;
        height: @top-height;
        align-items: center;
        padding: 5px 30px 8px;
        box-sizing: border-box;
    }
    .icon{
        color: #ffffff;
        font-size: 32px;
        padding:0px 0px 0px 20px ;
    }

    @media screen and (min-width: 768px) {
        .bar_bg {
            max-width: 750PX;
            margin: 0 auto;
            position: relative;
            left: auto;
            right: auto;
            border-radius: 0;
            background-color: #ffffff;
            padding: 10PX 20PX;
            height: 60PX;
            border-bottom: 1PX solid #f0f0f0;
        }
        .icon {
            color: @mian-color;
            font-size: 16PX;
            padding-left: 16PX;
        }
    }
</style>
