<template>
    <div class="list-item">
        <div class="list-lr">
            <div class="item-l">
                <span class="title">{{formatTitle(data.title)}}</span>
                <div class="tags">
                    <span class="tags-text tags-icon">{{data.icon}}</span>
                    <span class="tags-text">{{data.source}}</span>
                    <span class="tags-text">评论 {{data.commit}}</span>
                </div>
            </div>
            <div class="item-r">
                <img class="image" :src="data.image[0]"/>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        name: "article_1",
        props:{
            data:{
                type:Object
            }
        },
        methods : {
            formatDate:function(time){
                return this.$date.format13(time);
            },formatTitle:function(title){
                if(title.length>32){
                    return title.substring(0,31);
                }
                return title;
            }
        }
    }
</script>

<style lang="less" scoped>
    @import '../../styles/article';
    .list-item{
    }
    .list-lr{
        display: flex;
        flex-direction: row;
        display: flex;
        justify-content: space-around;
    }
    .item-l{
        width: 550px;
    }
    .item-r{
        width: 180px;
        margin: @list-tb-margin @list-lr-margin;
    }
</style>
