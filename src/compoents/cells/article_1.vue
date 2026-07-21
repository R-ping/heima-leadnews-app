<template>
    <div class="list-item">
        <div class="list-lr">
            <div class="item-l">
                <span class="title" v-html="displayTitle"></span>
                <div class="tags">
                    <span class="tags-text tags-icon">{{data.icon}}</span>
                    <span class="tags-text">{{data.source}}</span>
                    <span class="meta-sep">·</span>
                    <span class="tags-text meta-comment">{{data.comment}} 评论</span>
                </div>
            </div>
            <div class="item-r">
                <img class="image" :src="data.image[0]"/>
            </div>
        </div>
    </div>
</template>

<script>
    import { sanitizeHighlight } from '../../utils/sanitize.js'

    export default {
        name: "article_1",
        props:{
            data:{
                type:Object
            }
        },
        computed: {
            displayTitle: function () {
                return sanitizeHighlight(this.formatTitle(this.data.title || ''))
            }
        },
        methods : {
            formatDate:function(time){
                return this.$date.format13(time);
            },formatTitle:function(title){
                if (!title) return ''
                if (title.indexOf('<') >= 0) {
                    return title;
                }
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
        gap: 16px;
    }
    .item-l{
        flex: 1;
        min-width: 0;
    }
    .item-r{
        width: 120px;
        flex-shrink: 0;
    }
    .item-r .image {
        width: 120px;
        height: 80px;
    }
    .title {
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        font-weight: 600;
    }
    .title /deep/ font {
        color: #ff0000;
    }
    .meta-sep {
        color: #c0c4cc;
        font-size: 12px;
        margin: 0 4px;
        user-select: none;
    }
    .meta-comment {
        color: #b0b5c0;
    }
</style>