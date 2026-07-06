<template>
    <div class="bar_bg">
        <span class="icon" @click="back">&#xf104;</span>
        <Search
                :icon="icon"
                @onSubmit="onSubmit"
                :value="value"
                :radius="62"
                :placeholder="placeholder"
                right-width="30"
                left-width="30" />
        <span class="icon" @click="home">&#xf00d;</span>
    </div>
</template>

<script>
    import Search from '@/compoents/inputs/search';
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
                icon:'\uF002'
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
                this.$emit('onSubmit', val);
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
        }
    .icon{
        color: #ffffff;
        font-size: 42px;
    }
</style>
