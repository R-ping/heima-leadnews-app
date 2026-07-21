<template>
    <div class="body">
        <div class="content">
            <template v-for=" item in data">
                <tip-cell @onClick="onClick" :keyword="item.associateWords || item.associate_words" :search="search"></tip-cell>
            </template>
        </div>
    </div>
</template>

<script>
    import TipCell from '@/compoents/cells/search_2'
    export default {
        components:{TipCell},
        props:{
            data:{
                type:Array,
                default:[]
            },
            search :{
                type:String,
                default:""
            }
        },
        methods:{
            onClick : function(e){
                this.$emit("onSelect",e)
            }
        }
    }
</script>

<style lang="less" scoped>
    .body{
        background-color: #ffffff;
    }
    .content{
        padding: 0px 20px 0px 48px;
    }

    @media screen and (min-width: 768px) {
        .body {
            max-width: 750PX;
            margin: 0 auto;
        }
        .content {
            padding: 0 20PX 0 48PX;
        }
    }
</style>
