<template>
    <div class="art-page">
        <div class="art-top"><TopBar @onBlur="onBlur" @onInput="onInput"/></div>
        <div class="scroller" :style="{'height':scrollerHeight}" show-scrollbar="true">
            <template v-for="item in data.history">
                <SearchHistory @onClickText="doSearch" @onDeleteHistory="onDeleteHistory" :id="item.id" :title="item.keyword"/>
            </template>
            <a href="#" class="all-search">
                <span class="all-search-text">全部搜索记录</span>
            </a>
            <Title title="今日热点" :icon="icon.hot"/>
            <div class="hot-body">
                <template v-for="item in data.hot">
                    <div class="item">
                        <template v-for="k in item">
                            <HotCell @onClick="doSearch" :title="k.hot_words" type="k.type"/>
                        </template>
                    </div>
                </template>
            </div>
            <Title title="大家都在搜" :icon="icon.other"/>
            <div class="hot-body">
                <div class="item">
                    <HotCell title="长宁4.8级地震" tip="精"/>
                    <HotCell title="长宁4.8级地震"/>
                </div>
                <div class="item">
                    <HotCell title="长宁4.8级地震" tip="荐"/>
                    <HotCell title="长宁4.8级地震"/>
                </div>
                <div class="item">
                    <HotCell title="长宁4.8级地震"/>
                    <HotCell title="长宁4.8级地震" tip="热"/>
                </div>
            </div>
            <Title title="大家都在搜" :icon="icon.other"/>
            <div class="hot-body">
                <div class="item">
                    <HotCell title="长宁4.8级地震" tip="精"/>
                    <HotCell title="长宁4.8级地震"/>
                </div>
                <div class="item">
                    <HotCell title="长宁4.8级地震" tip="荐"/>
                    <HotCell title="长宁4.8级地震"/>
                </div>
                <div class="item">
                    <HotCell title="长宁4.8级地震"/>
                    <HotCell title="长宁4.8级地震" tip="热"/>
                </div>
            </div>
        </div>
        <div class="art-tip" v-if="showTip" ref="tip"><SearchTip @onSelect="doSearch" :search="data.keyword" :data="data.tip"/></div>
    </div>
</template>

<script>
    import TopBar from '@/compoents/bars/search_top'
    import SearchHistory from '@/compoents/cells/search_0'
    import SearchTip from '@/compoents/inputs/search_tip'
    import HotCell from '@/compoents/cells/search_1'
    import Title from '@/compoents/titles/title'
    import Api from '@/apis/search/api'
    import Utils from '@/utils/env'
    import { toast, confirmDialog } from "@/utils/toast"
    export default {
        name: "index",
        components:{TopBar,SearchHistory,Title,HotCell,SearchTip},
        data(){
            return {
                scrollerHeight:'500px',
                showTip:false,
                icon : {
                    hot : '\uf06d',
                    other:'\uf17d'
                },
                data : {
                    keyword:'',//当前输入的关键字
                    history : [],//搜索历史
                    tip : [],// 联想词
                    hot : []//热搜关键字
                }
            }
        },
        created(){
            Api.setVue(this)
        },
        mounted(){
            this.scrollerHeight=(Utils.getPageHeight()-180)+'px';
            this.load_search_history()
            this.load_hot_keywords()
        },
        methods:{
            doSearch : function(val){
                this.$router.push({name:'search_result',params:{'keyword':val}})
            },
            // 加载搜索历史
            load_search_history : function(){
                Api.load_search_history().then(data=>{
                    if(data.code==0){
                        this.data.history = data.data
                    }else{
                        toast( data.error_message, 3)
                    }
                }).catch((e)=>{
                    console.log(e)
                })
            },
            // 删除历史搜搜关键字
            onDeleteHistory : function(id){
                let _this = this;
                toast('确认要删除吗？'); confirmDialog('确认要删除吗？', function(button) {
                    if(button=='OK') {
                        Api.del_search(id).then(data => {
                            if (data.code == 0) {
                                toast('删除成功', 3)
                                _this.load_search_history()
                            } else {
                                toast(data.error_message, 3)
                            }
                        }).catch((e) => {
                            console.log(e)
                        })
                    }
                })
            },
            //用户输入时，提示联想词
            onInput : function(val){
                Api.associate_search(val).then(data => {
                    if (data.code == 0) {
                        this.data.keyword=val
                        this.showTip = true
                        this.data.tip=data.data
                    }
                })
            },
            // 加载热搜关键字
            load_hot_keywords : function(){
                Api.load_hot_keywords().then(data=>{
                    if(data.code==0){
                        // 需要转换数据格式
                        let newData=[]
                        let temp = []
                        for(var i=0;i<data.data.length;i++){
                            if(i>0&&i%2==0){
                                newData.push(temp)
                                temp = []
                            }
                            temp.push(data.data[i])
                        }
                        this.data.hot = newData
                    }else{
                        toast( data.error_message, 3)
                    }
                }).catch((e)=>{
                    console.log(e)
                })
            },
            // 失去焦点，关闭联想词
            onBlur : function(){
                this.showTip=false
            }
        }
    }
</script>

<style scoped>
    .art-page{
        width: 750px;
        display: flex;
        flex-direction: column;
        background-color: #ececec;
    }
    .art-tip{
        position: absolute;
        top: 100px;
        width: 750px;
        z-index: 999;
    }
    .art-top{
        top: 0px;
        z-index: 999;
        position: fixed;
        height: 120px;
        background-color: #ffffff;
    }
    .scroller{
        flex: 1;
        display: flex;
        flex-direction: column;
        width: 750px;
        margin-top: 120px;
    }
    .all-search{
        font-size: 36px;
        align-items: center;
        padding: 18px 20px;
        background-color: #ffffff;
    }
    .all-search-text{
        color: #bdbdbd;
    }
    .item{
        display: flex;
        flex-direction: row;
    }
</style>
