<template>
    <div class="list-item">
        <div class="list-lr">
            <div class="item-l">
                <span class="title">{{formatTitle(data.title)}}</span>
                <div class="tags">
                    <span class="tags-text tags-icon">{{data.icon}}</span>
                    <span class="tags-text">{{data.source}}</span>
                    <span class="tags-text">评论 {{data.comment}}</span>
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
    .list-lr{
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        align-items: flex-start;
    }
    .item-l{
        flex: 1;
        min-width: 0;
    }
    .item-r{
        width: 180px;
        flex-shrink: 0;
        margin: @list-tb-margin @list-lr-margin;
    }
</style>