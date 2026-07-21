<template>
  <section class="result">
    <div v-if="articleList.length === 0" class="empty-state">
      <div class="empty-icon">
        <i class="fa fa-file-text-o"></i>
      </div>
      <div class="empty-text">暂无文章</div>
    </div>
    <div v-else class="article-list">
      <div v-for="article in articleList" :key="article.id" class="article-item">
        <div class="article-main">
          <div class="article-info">
            <div class="article-author">
              <img :src="article.authorImage || defaultAvatar" class="author-avatar" />
              <span class="author-name">{{ article.authorName || '未知作者' }}</span>
            </div>
            <h3 
              class="article-title"
              :class="{ 'clickable': article.status == '9' && article.static_url }"
              @click="openDetail(article)"
            >{{ article.title }}</h3>
            <div class="article-meta">
              <span class="meta-item">{{ dateFormat(article.publish_time) }}</span>
              <span class="meta-divider">·</span>
              <span class="meta-item">0 展现</span>
              <span class="meta-divider">·</span>
              <span class="meta-item">{{ article.views || 0 }} 阅读</span>
              <span class="meta-divider">·</span>
              <span class="meta-item">{{ article.likes || 0 }} 点赞</span>
              <span class="meta-divider">·</span>
              <span class="meta-item">{{ article.comment || 0 }} 评论</span>
              <span class="meta-divider">·</span>
              <span class="meta-item">0 收藏</span>
            </div>
          </div>
          <div v-if="getImage(article)" class="article-cover">
            <img :src="getImage(article)" alt="封面" />
          </div>
        </div>
        <div class="article-actions">
          <el-dropdown trigger="click" @command="(cmd) => operateBtn(article.id, cmd)">
            <span class="el-dropdown-link">
              <i class="el-icon-more"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="modify">编辑</el-dropdown-item>
              <el-dropdown-item command="del">删除</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
    </div>
    <div class="pagination" v-if="total > 0">
      <el-pagination
        layout="total, prev, pager, next"
        @current-change='pageChange'
        :current-page.sync='listPage.currentPage'
        :page-size="pageSize"
        :total="total">
      </el-pagination>
    </div>
  </section>
</template>

<script>
import DateUtil from '../../utils/date'
export default {
  props: ['host','articleList','pageSize','total','changePage','deleteArticlesById','upOrDown'],
  data() {
    return {
       listPage:{
          currentPage:1
       },
       defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
    }
  },
  methods: {
    getImage (item){
      if(item.images){
        let temp = item.images.split(",")
        if(temp.length>0 && temp[0]){
          return this.host+temp[0];
        }
      }
      return null
    },
    pageChange (newPage) {
        this.changePage && this.changePage({page:newPage})
    },
    resetPage(){
      this.listPage.currentPage = 1
    },
    dateFormat (time) {
      return DateUtil.format13HH(time)
    },
    openDetail(row) {
      if (row.status == '9' && row.static_url) {
        window.open(this.host + row.static_url, '_blank')
      }
    },
    operateBtn (Id, type) {
      switch(type){
        case 'modify':
          this.$router.push({path:'/creator/publish',query:{articleId:Id}})
          break
        case 'del':
          this.$confirm('此操作将永久删除该文章, 是否继续?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
             this.deleteArticlesById && this.deleteArticlesById(Id)
          }).catch(() => {
            this.$message({
              type: 'info',
              message: '已取消删除'
            });
          });
          break
        default:
      }
    }
  }
}
</script>
<style lang="less" scoped>
@import '../../layout/styles/variables.less';

.result {
  background-color: #ffffff;
  border-radius: @cardRadius;
  box-shadow: @cardShadow;
  padding: 0;

  .empty-state {
    padding: 80px 0;
    text-align: center;
    .empty-icon {
      font-size: 48px;
      color: #c0c4cc;
      margin-bottom: 16px;
    }
    .empty-text {
      color: #909399;
      font-size: 14px;
    }
  }

  .article-list {
    padding: 0;
  }

  .article-item {
    display: flex;
    align-items: center;
    padding: 20px;
    border-bottom: 1px solid #f2f3f5;
    &:last-child {
      border-bottom: none;
    }
    &:hover {
      background-color: #fafafa;
    }
  }

  .article-main {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-width: 0;
  }

  .article-info {
    flex: 1;
    min-width: 0;
    margin-right: 20px;
  }

  .article-author {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
  }

  .author-avatar {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    margin-right: 8px;
    object-fit: cover;
  }

  .author-name {
    font-size: 13px;
    color: #515767;
  }

  .article-title {
    font-size: 16px;
    color: @textPrimary;
    font-weight: 500;
    margin-bottom: 8px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    &.clickable {
      color: #1e80ff;
      cursor: pointer;
      &:hover {
        text-decoration: underline;
      }
    }
  }

  .article-meta {
    font-size: 13px;
    color: #909399;
    display: flex;
    align-items: center;
    flex-wrap: wrap;
  }

  .meta-item {
    margin-right: 4px;
  }

  .meta-divider {
    margin: 0 4px;
    color: #dcdfe6;
  }

  .article-cover {
    flex-shrink: 0;
    width: 150px;
    height: 100px;
    border-radius: 4px;
    overflow: hidden;
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .article-actions {
    flex-shrink: 0;
    margin-left: 20px;
    .el-dropdown-link {
      cursor: pointer;
      color: #909399;
      font-size: 18px;
      &:hover {
        color: #1e80ff;
      }
    }
  }

  .pagination {
    text-align: right;
    padding: 20px;
    border-top: 1px solid #f2f3f5;
  }
}
</style>