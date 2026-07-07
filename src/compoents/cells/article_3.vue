<template>
    <div class="list-item">
        <span class="title">{{formatTitle(data.title)}}</span>
        <div class="item-image">
            <img class="image" v-for="img in data.image" :src="img" :key="img"/>
        </div>
        <div class="item-l">
            <div class="tags">
                <span class="tags-text tags-icon">{{data.icon}}</span>
                <span class="tags-text">{{data.source}}</span>
                <span class="tags-text">评论 {{data.comment}}</span>
                <span class="tags-text">{{formatDate(data.date)}}</span>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        name: "article_3",
        props:{
            data:{
                type:Object
            }
        },
        methods : {
            formatDate:function(time){
                return this.$date.format13(time);
            },
            formatTitle:function(title){
                if(title.length>20){
                    return title.substring(0,19);
                }
                return title;
            }
        }
    }
</script>

<style lang="less" scoped>
    @import '../../styles/article';
    .item-image{
        display: flex;
        flex-direction: row;
        padding: 6px 10px 0px;
        justify-content: space-between;
        gap: 6px;
    }
    .image{
        flex: 1;
        height: 160px;
    }
</style>