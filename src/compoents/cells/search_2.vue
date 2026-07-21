<template>
    <div class="cell-body">
        <span class="icon">{{icon}}</span>
        <div class="text-wrap" @click="onClick">
            <span class="text" v-html="highlightText()"></span>
        </div>
        <span class="skip"> </span>
    </div>
</template>

<script>
    import { sanitizeHighlight } from '../../utils/sanitize.js'

    export default {
        name: "search_2",
        props:{
            keyword:{
                type:String,
                default:""
            },
            search:{
                type:String,
                default:""
            },icon:{
                type:String,
                default:'\uf002'
            }
        },
        methods:{
            highlightText : function(){
                var kw = this.keyword
                var search = this.search
                if (!search || !kw) return kw
                try {
                    var reg = new RegExp('(' + search.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + ')', 'gi')
                    var highlighted = kw.replace(reg, '<em style="color:red;font-style:normal">$1</em>')
                    return sanitizeHighlight(highlighted)
                } catch (e) {
                    return kw
                }
            },
            onClick : function(){
                this.$emit("onClick",this.keyword)
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
        border-bottom-color: #ebebeb;
        border-bottom-width: 1px;
        border-bottom-style: solid;
        padding: 17px 20px;
        background-color: #ffffff;
    }
    .text-wrap {
        flex: 1;
        min-width: 0;
    }
    .text{
        color: #222222;
    }
    .skip{
        flex: 1;
    }
    .icon{
        width: 60px;
        font-size: 28px;
        text-align: center;
        color: #ababab;
        flex-shrink: 0;
    }

    @media screen and (min-width: 768px) {
        .cell-body {
            max-width: 750PX;
            margin: 0 auto;
            font-size: 16PX;
            padding: 12PX 20PX;
        }
        .icon {
            width: 28PX;
            font-size: 14PX;
        }
    }
</style>
