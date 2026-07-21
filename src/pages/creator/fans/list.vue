<template>
  <div class="fans-page">
    <div class="fans-card">
      <header class="card-header">粉丝列表</header>
      <div class="tabBar">
        <router-link to="/fans/index">粉丝概况</router-link>
        <router-link to="/fans/info">粉丝画像</router-link>
        <router-link to="/fans/list" class="active">粉丝列表</router-link>
      </div>
      <div class="card-body">
        <List
          :fanList="fansList"
          :total="total"
          :pageSize="params.size"
          :changePage="getFollowers"
          :followOperate="followOperate"
        />
      </div>
    </div>
  </div>
</template>

<script>
import List from './components/list/List.vue'
import { getFollowers, getFollowersAvatar, changeFollowState } from '@/apis/creator/fans'
export default {
  name: 'FansList',
  data() {
    return {
        fansList:[],
        params:{
          page:1,
          size:10
        },
        total: 0
    }
  },
  created () {
     this.getFollowers() // 粉丝列表数据
  },
  components: {
    List
  },
  computed: {

  },
  methods: {
      // 获取粉丝列表
      async getFollowers (newParams) {
       let result = await getFollowers({...this.params, ...newParams}) // 获取粉丝列表
       var datas = result.data // 粉丝列表数据
       // 获取粉丝头像
        for (let i = 0; i < datas.length; i++) {
          let avatarRes = await getFollowersAvatar({id: datas[i].fans_id});
          datas[i].photo = avatarRes;
       }
        this.total = result.total;
        this.fansList = datas;
    },
      async followOperate(data) {
        let result = await changeFollowState(data);
        if (data.switch_state) {
          if (result.code == 0) {
            this.$message({ type: 'success',  message: '关注成功!'  });
          } else {
            this.$message({ type: 'error',  message: result.error_message  });
          }
        } else {
          this.$message({ type: 'success',  message: '取消关注成功!'  });
        }
        this.getFollowers()
      }
  }
}
</script>

<style rel="stylesheet/less" lang="less" scoped>
@import '../layout/styles/variables.less';
.fans-page {
  min-height: calc(100vh - 70px);
  background-color: @bgGray;
  padding: 20px;
  .fans-card {
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
    .tabBar {
      font-size: 14px;
      padding: 0 24px;
      height: 55px;
      line-height: 55px;
      border-bottom: 1px solid #e8e8e8;
      a {
        margin-right: 35px;
        color: @textSecondary;
        &.active {
          color: @brandBlue;
        }
      }
    }
    .card-body {
      padding: 24px;
    }
  }
}
</style>
