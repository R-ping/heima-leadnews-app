<template>
    <div class="list-item">
        <span class="title" v-html="displayTitle"></span>
        <div class="item-image">
            <img class="image" v-for="img in data.image" :src="img" :key="img"/>
        </div>
        <div class="item-l">
            <div class="tags">
                <span class="tags-text tags-icon">{{data.icon}}</span>
                <span class="tags-text">{{data.source}}</span>
                <span class="meta-sep">·</span>
                <span class="tags-text meta-comment">{{data.comment}} 评论</span>
                <span class="tags-text date">{{formatDate(data.date)}}</span>
            </div>
        </div>
    </div>
</template>

<script>
    import { sanitizeHighlight } from '../../utils/sanitize.js'

    export default {
        name: "article_3",
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
            },
            formatTitle:function(title){
                if (!title) return ''
                if (title.indexOf('<') >= 0) {
                    return title;
                }
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
        padding: 0;
        margin: 8px 0 0;
        justify-content: space-between;
        gap: 8px;
    }
    .image{
        flex: 1;
        height: 100px;
        min-width: 0;
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