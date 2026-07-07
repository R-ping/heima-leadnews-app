<template>
    <div class="list-item">
        <span class="title">{{data.title}}</span>
        <div class="tags">
            <span class="tags-text tags-icon">{{data.icon}}</span>
            <span class="tags-text">{{data.source}}</span>
            <span class="tags-text">评论 {{data.comment}}</span>
            <span class="tags-text date">{{formatDate(data.date)}}</span>
        </div>
    </div>
</template>

<script>
    export default {
        name: "article_0",
        props:{
            data:{
                type:Object
            }
        },
        methods : {
            formatDate:function(time){
                return this.$date.format13(time);
            }
        }
    }
</script>

<style lang="less" scoped>
    @import '../../styles/article';
</style>