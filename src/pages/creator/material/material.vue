<template>
 <div class="material-page">
   <div class="material-card">
     <header class="card-header">图片管理</header>
     <div class="card-body">
        <div class="toolbar">
          <el-radio-group size='small' @change="loadData" v-model="activeSelect">
             <el-radio-button  label="0">全部</el-radio-button>
             <el-radio-button  label="1">收藏</el-radio-button>
          </el-radio-group>
          <el-button @click="showPicDialog = true" class="upload_btn" type="primary" icon="el-icon-upload2">上传图片</el-button>
        </div>
        <div class="img_list">
            <div class="img_list_item" v-for="img in imgData" :key="img.id">
                <img :src="img.url" />
            </div>
        </div>
        <div class="pagination">
              <el-pagination
                    background
                    layout="prev, pager, next, jumper"
                    :total="imgPage.total"
                    :page-count="imgPage.pageCount"
                    :page-size="imgPage.pageSize"
                    :current-page="imgPage.currentPage"
                     @current-change="pageChange"
                     >
              </el-pagination>
        </div>
     </div>
   </div>
     <el-dialog
       :visible.sync="showPicDialog"
        width="50%"
       :show-close="false"
       :center="true"
       :before-close="closeModal"
       title="上传图片">
         <upload v-if="showPicDialog" :imgChange="imgChangeCall" />
          <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="closeModal">关闭</el-button>
       </span>
    </el-dialog>
 </div>
 </template>
<script>
import Upload from  '../components/Upload/upload.vue'
export default {
    name:'material',
    data () {
        return {
            imgPage:{
               total:0,
               currentPage:1,
               pageCount:0,
               pageSize:15
            },
            imgChange:false,//是否上传过图片导致图片数据变化 此状态用来控制是否在关闭后要进行重新加载
            showPicDialog:false,
            activeSelect:'0',
            imgData:[],//存储图片的数据 同时作为收藏数据和全部数据的引用
        }
    },
    components : {
        Upload
    },
    mounted () {
        this.loadData();
    },
    methods:{
      loadData : function(){
        //初始化时加载数据
        this.getImgData({
          page:this.imgPage.currentPage,
          size:this.imgPage.pageSize,
          is_collected:this.activeSelect
        })
      },
      //页面发生变化
      pageChange (newPage) {
        this.imgPage.currentPage = newPage
        this.loadData();
      },
      //获取图片素材
      async  getImgData (params) {
        // TODO: 素材管理 API 已废弃
        this.imgData = []
        this.imgPage.total = 0
        this.imgPage.pageCount = 0
      },
     imgChangeCall () {
         //图片变化了 记录改变的状态 用于关闭弹层时 重新加载数据
         this.imgChange = true
     },
     //关闭弹层时触发
     //注意 这里 为什么不在click用表达式赋值的方式去关掉弹层呢
     //因为发现在click="dialog = false" 模式下 不能触发关闭的回调 应该是实现机制的顺序问题
     closeModal () {
        if(this.imgChange){
           this.loadData()
           this.imgChange = false
        }
        this.showPicDialog = false
     }
  }
}
</script>
<style lang="less" scoped>
@import '../layout/styles/variables.less';
.material-page {
  min-height: calc(100vh - 70px);
  background-color: @bgGray;
  padding: 20px;
  .material-card {
    background-color: #ffffff;
    border-radius: @cardRadius;
    box-shadow: @cardShadow;
    .card-header {
      padding: 0 24px;
      height: 56px;
      line-height: 56px;
      font-size: 16px;
      color: @textPrimary;
      border-bottom: 1px solid #e8e8e8;
    }
    .card-body {
      padding: 24px;
    }
    .toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
      .upload_btn {
        position: static;
      }
    }
    .img_list {
      display: flex;
      flex-direction: row;
      flex-wrap: wrap;
      align-content: center;
      .img_list_item {
          margin: 16px;
          width: 180px;
          height: 180px;
          position:relative;
          img {
              width: 100%;
              height:100%;
              border-radius: 6px;
              object-fit: cover;
          }
          .operate {
              position: absolute;
              width: 100%;
              height: 36px;
              background: rgba(244, 245, 245, 0.95);
              bottom: 0;
              left:0;
              border-radius: 0 0 6px 6px;
              display: flex;
              flex-direction: row;
              justify-content: space-around;
              align-items: center;
              img {
                  width: 18px;
                  height:18px;
                  cursor: pointer;
              }
          }
      }
    }
    .pagination {
      width: 100%;
      text-align: center;
      margin-top: 24px;
    }
  }
}
</style>
