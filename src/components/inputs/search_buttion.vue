<template>
    <div class="search-body" @click="onClick" :style="getBodyStyle">
        <span class="search-icon" :style="getIconStyle">{{icon}}</span>
        <span class="search-text">{{ placeholder || '搜索' }}</span>
    </div>
</template>

<script>
    export default {
        props:{
            flex:{
                type:Number,
                default:1
            },
            height:{
                type:Number,
                default:56
            },
            icon:{
              type:String,
              default:'\uf002'
            },
            placeholder:{
                type:String,
                default:''
            },
            leftWidth:{
                type:Number,
                default:15
            },
            rightWidth:{
                type:Number,
                default:15
            }
        },
        computed:{
            getBodyStyle:function(){
                return {
                    marginLeft:this.leftWidth+'px',
                    marginRight:this.rightWidth+'px',
                    'flex':this.flex,
                    'height':this.height+'px'
                }
            },
            getIconStyle:function(){
                var iconSize = Math.max(20, Math.floor(this.height * 0.4));
                return {
                    "font-size": iconSize + "px"
                }
            }
        },
        methods:{
            onClick : function(){
                this.$emit('onClick', {});
            }
        }
    }
</script>

<style lang="less" scoped>
    @import '../../styles/common';

    .search-body{
        background-color: rgba(255,255,255,0.25);
        display: flex;
        flex-direction: row;
        justify-content: center;
        align-items: center;
        border-radius: 28px;
        overflow: hidden;
        box-sizing: border-box;
        transition: background-color 0.2s;
    }
    .search-body:active {
        background-color: rgba(255,255,255,0.35);
    }
    .search-icon{
        color: #ffffff;
        margin-right: 8px;
        opacity: 0.9;
    }
    .search-text{
        color: rgba(255,255,255,0.85);
        font-size: 26px;
    }
</style>
