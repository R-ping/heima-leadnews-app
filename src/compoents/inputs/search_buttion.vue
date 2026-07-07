<template>
    <div class="body" @click="onClick" :style="getBodyStyle">
        <span class="icon" :style="getIconStyle">{{icon}}</span>
        <span class="text">搜索</span>
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
                default:70
            },
            icon:{
              type:String,
              default:'\uf044'
            },
            leftWidth:{
                type:Number,
                default:25
            },
            rightWidth:{
                type:Number,
                default:20
            }
        },
        computed:{
            getBodyStyle:function(){
                return {
                    marginLeft:this.leftWidth+'px',
                    marginRight:this.rightWidth+'px',
                    borderColor:this.color,
                    'flex':this.flex,
                    'height':this.height+'px'
                }
            },
            getIconStyle:function(){
                return {
                    width:(this.height-30)+"px",
                    height:(this.height-10)+"px",
                    lineHeight:(this.height-10)+"px",
                    fontSize:(this.height/70)*32+"px"
                }
            }
        },
        methods:{
            onClick : function(){
                if(this.inpValue!=''){
                    this.$emit('onClick', {});
                }
            }
        }
    }
</script>

<style lang="less" scoped>
    @import '../../styles/common';
    .body{
        background-color: #5aadff;
        display: flex;
        flex-direction: row;
        justify-content: center;
        align-items: center;
        border-radius: @top-height - 30;
    }
    .icon{
        color: #ffffff;
        font-size: 28px;
    }
    .text{
        color: #ffffff;
        font-size: 28px;
    }
</style>