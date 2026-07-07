<template>
  <div class="wrapper">
    <div class="top-body"><Home_Bar/></div>
    <div class="content-body">
      <wxc-tab-page ref="wxc-tab-page" :tab-titles="tabTitles" :tab-styles="tabStyles" title-type="text" :tab-page-height="tabPageHeight" @wxcTabPageCurrentTabSelected="wxcTabPageCurrentTabSelected">
        <div v-for="(v,index) in tabList"  :key="index" class="item-container" :style="{ height: (tabPageHeight - tabStyles.height) + 'px' }">
          <!-- 下来刷新最新 -->
          <div @refresh='loadnew'  :display="shownew?'show':'hide'" class="loading">
            <span class="loading-text">{{load_new_text}}</span>
          </div>
          <!-- 列表项，并绑定显示事件 -->
          <div v-for="(item,key) in v" class="cell" @appear="show(item.id)" :key="key">
            <div @click="wxcPanItemClicked(item)">
              <Item0 v-if="item.type==0" :data="item"/>
              <Item1 v-if="item.type==1" :data="item"/>
              <Item3 v-if="item.type==2" :data="item"/>
              <Item3 v-if="item.type==3" :data="item"/>
            </div>
          </div>
          <!-- 上来加载更多 -->
          <div @loading="loadmore" :display="showmore?'show':'hide'" class="loading">
            <span class="loading-text">{{load_more_text}}</span>
          </div>
        </div>
      </wxc-tab-page>
    </div>
  </div>
</template>

<script>
  import Home_Bar from "@/compoents/bars/home_bar"
  import WxcTabPage from "@/compoents/tabs/home_tabs"
  import Utils from '@/utils/env'
  import Item0 from '../../compoents/cells/article_0.vue'
  import Item1 from '../../compoents/cells/article_1.vue'
  import Item3 from '../../compoents/cells/article_3.vue'
  import Config from './config'
  import Api from '@/apis/home/api'

  import { toast, confirmDialog } from "@/utils/toast"

  export default {
    name: 'HeiMa-Home',
    components: {Home_Bar,WxcTabPage, Item0,Item1,Item3},
    data: () => ({
      api:null,
      shownew:true,
      showmore:false,
      tabTitles: Config.tabTitles,
      tabStyles: Config.tabStyles,
      tabList: [...Array(Config.tabTitles.length).keys()].map(i => []),
      tabPageHeight: 1334,
      params:{
        loaddir:1,
        index:0,
        tag:"__all__",
        size:10,
        max_behot_time:0,
        min_behot_time:20000000000000
      },
      ashow : {},
      timer : null
    }),
    computed:{
      load_new_text:function(){return this.$lang.load_new_text},
      load_more_text:function(){return this.$lang.load_more_text}
    },
    mounted(){
      this.$refs['wxc-tab-page'].setPage(1,null,true);
    },
    destroyed(){
      clearInterval(this.timer)
    },
    created () {
      this.tabPageHeight = Utils.getPageHeight()-120;
      let _this = this;
      this.timer = setInterval(function(){
        let result = Api.saveShowBehavior(_this.ashow);
        if(result){
          result.then((d)=>{
            let ids=d.data;
            for(let i=0;i<ids.length;i++){
              _this.ashow[ids[i].id]=false;
            }
          });
        }
      },5000);
    },
    methods: {
      show:function(id){
        if(this.ashow[id]==undefined){
          this.ashow[id]=true;
        }
      },
      loadmore:function(){
        this.showmore=true;
        this.params.loaddir=2
        this.load();
      },
      loadnew:function(){
        this.shownew=true;
        this.params.loaddir=0
        this.load();
      },
      load : function(){
        Api.loaddata(this.params).then((d)=>{
          this.tanfer(d.data);
        }).catch((e)=>{
          console.log(e)
        })
      },
      tanfer : function(data){
        if(data.length==0){
          this.showmore=false;
          this.shownew=false;
          toast('没有数据了...', 3)
          return ;
        }
        let arr = []
        for(let i=0;i<data.length;i++){
          let ims = []
          if(data[i].images){
            ims = data[i].images.replace(/[\[\]]/ig,'').split(',')
          }
          let tmp = {
            id:data[i].id,
            title:data[i].title,
            comment:data[i].comment,
            authorId:data[i].author_id,
            source:data[i].author_name,
            date:data[i].publish_time,
            type:ims.length==2?1:ims.length,
            image:ims,
            icon:'\uf06d'
          }
          let time = data[i].publish_time;
          if(this.params.max_behot_time<time){
            this.params.max_behot_time=time;
          }
          if(this.params.min_behot_time>time){
            this.params.min_behot_time=time;
          }
          arr.push(tmp);
        }
        let newList = [...Array(this.tabTitles.length).keys()].map(i => []);
        if(this.params.loaddir!=0){
          arr = this.tabList[this.params.index].concat(arr);
        }else{
          arr=arr.concat(this.tabList[this.params.index]);
        }
        newList[this.params.index] = arr;
        this.tabList = newList;
        this.showmore=false;
        this.shownew=false;
      },
      wxcTabPageCurrentTabSelected (e) {
        this.params.loaddir=1
        this.params.index=e.page
        this.params.tag = Config.tabTitles[e.page]['id'];
        this.params.max_behot_time=0
        this.params.min_behot_time=20000000000000
        this.shownew=true
        this.load();
      },
      wxcPanItemPan (e) {
      },
      wxcPanItemClicked(item){
        this.$router.push({
          name:'article-info',
          params:item
        })
      }
    }
  };
</script>

<style lang="less" scoped>
  @import '../../styles/article';
  .wrapper{
    background-color: @body-background;
    font-size: @font-size;
    font-family: @font-family;
    display: flex;
    flex-direction: column;
    min-height: 100vh;
  }
  .top-body{
    position: fixed;
    left: 0;
    right: 0;
    top: 0;
    z-index: 100;
  }
  .content-body{
    flex: 1;
    display: flex;
    flex-direction: column;
    margin-top: 90px;
    width: 100%;
  }
  .item-container {
    width: 100%;
    background-color: #f5f5f5;
    overflow-y: auto;
    -webkit-overflow-scrolling: touch;
  }
  .cell {
    background-color: #ffffff;
    cursor: pointer;
  }
</style>