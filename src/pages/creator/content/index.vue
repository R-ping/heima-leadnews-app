<template>
  <div class="content-manage">
    <div class="page-title">文章管理</div>
    <div class="status-tabs">
      <el-tag
        v-for="tab in statusTabs"
        :key="tab.value"
        :type="activeStatus === tab.value ? 'primary' : 'info'"
        :class="{ 'active-tab': activeStatus === tab.value }"
        @click="changeStatus(tab.value)"
      >{{ tab.label }} ({{ tab.count }})</el-tag>
    </div>
    <search-tool
      v-if="!searchText"
      :changePage="searchArticle"
      :channel_list="channel_list"
    />
    <search-result
      ref='mySearchResult'
      :articleList="articleList"
      :host="host"
      :total="total"
      :changePage="searchArticle"
      :pageSize="params.size"
      :deleteArticlesById="deleteArticlesById"
      :upOrDown="upOrDown"
    />
  </div>
</template>

<script>
  import SearchTool from './components/SearchTool.vue'
  import SearchResult from './components/SearchResult.vue'
  import { deleteArticles , searchArticle,upDownArticle} from  '@/apis/creator/content'
  import { getChannels } from  '@/apis/creator/publish'
  export default {
    name: 'ContentManage',
    data() {
      return {
        channel_list:[],
        articleList:[],
        total:0,
        host:'',
        searchText:null,
        params:{
          page:1,
          size:10
        }, //查询参数  用于全局存储 因为分页时 需要在查询条件基础上分页
        tempParams : {},
        activeStatus: 'all',
        statusTabs: [
          { label: '全部', value: 'all', count: 0 },
          { label: '已发布', value: 'published', count: 0 },
          { label: '审核中', value: 'reviewing', count: 0 },
          { label: '未通过', value: 'rejected', count: 0 }
        ]
      }
    },
    created () {
      let { searchText } = this.$route.query //从路由中查找关键字参数
      this.searchText = searchText //存储当前值
      this.getChannels() //拉取频道列表数据
      // 如果搜索关键字有值 则直接调用搜索接口 否则 调用默认接口
      this.searchArticle();
    },
    components: {
      SearchTool,
      SearchResult
    },
    computed: {

    },
    methods: {
      changeStatus(status) {
        this.activeStatus = status
        let statusMap = {
          'all': null,
          'published': 9,
          'reviewing': 1,
          'rejected': 2
        }
        this.searchArticle({ status: statusMap[status] })
      },
      //搜索文章
      async searchArticle (newParams) {
        this.tempParams = newParams
        let result = await searchArticle({...this.params,key_word:this.searchText,...this.tempParams})
        /****需要重新将分页器的页码设置为1******/
        if(this.$refs.mySearchResult){
          this.$refs.mySearchResult.resetPage(); //重置
        }
        this.host = result.host
        this.total = result.total //总记录数
        this.articleList = result.data //当前的数组
      },
      //根据Id删除文章
      async deleteArticlesById (Id) {
        let temp = await deleteArticles(Id)
        if(temp.code==0) {
          this.$message({type: 'success', message: '删除成功!'});
          this.searchArticle();
        }else{
          this.$message({type: 'error', message: temp.error_message});
        }
      },
      //上下架
      async upOrDown (Id,enable) {
        let temp = await upDownArticle({id:Id,enable:enable})
        if(temp.code==0) {
          this.$message({type: 'success', message: '操作成功!'});
          this.searchArticle();
        }else{
          this.$message({type: 'error', message: temp.error_message});
        }
      },
      //拉取频道数据
      async getChannels () {
        let result = await getChannels()
        this.channel_list = result.data  //赋值数据
      }
    }
  }
</script>

<style lang="less" scoped>
@import '../layout/styles/variables.less';

.content-manage {
  min-height: calc(100vh - 50px);
  background-color: @bgGray;
  padding: 20px;

  .page-title {
    font-size: 20px;
    font-weight: 500;
    color: @textPrimary;
    margin-bottom: 20px;
  }

  .status-tabs {
    margin-bottom: 16px;
    el-tag {
      cursor: pointer;
      margin-right: 8px;
      padding: 4px 16px;
      &.active-tab {
        background-color: #1e80ff;
        border-color: #1e80ff;
        color: #fff;
      }
    }
  }
}
</style>
