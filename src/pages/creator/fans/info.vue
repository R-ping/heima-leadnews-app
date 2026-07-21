<template>
  <div class="fans-page">
    <div class="fans-card">
      <header class="card-header">粉丝画像</header>
      <div class="tabBar">
        <router-link to="/fans/index">粉丝概况</router-link>
        <router-link to="/fans/info" class="active">粉丝画像</router-link>
        <router-link to="/fans/list">粉丝列表</router-link>
      </div>
      <div class="tabView">
        <div class="info-card">
          <Progress name="粉丝性别分布" :percentage="manPercent" :legend="['男', '女']"/>
        </div>
        <div class="info-card chart-table-card">
          <category-chart
            :ageRangeKey="ageRangeKey"
            :ageRangeValue="ageRangeValue"
            :width="width"
            :height="height"
            :autoResize="autoResize"
            :fansAgePerc="fansAgePerc"
          />
        </div>
        <div class="info-card">
          <Progress name="粉丝终端分布" :percentage="iosPercent" :legend="['Android', 'IOS']" />
        </div>
        <div class="info-card chart-card">
          <category-chart-big
            :chartData="readData"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Progress from './components/info/Progress.vue'
import CategoryChart from './components/info/CategoryChart.vue'
import CategoryChartBig from './components/info/CategoryChartBig.vue'
import { getFollowersPortrait }  from '@/apis/creator/fans'

export default {
  name: 'ContentManage',
  data() {
    return {
      gender: {
        male:0,
        female:0
      },
      mobile: {
        android: 0,
        ios:0
      },
      ageRangeValue:[],
      ageRangeKey:[],
      width: '60%',
      height: '350px',
      autoResize: true,
      fansAgePerc: [],
      readData: {
        key: [],
        value: []
      },
    }
  },
  components: {
    Progress,
    CategoryChart,
    CategoryChartBig
  },
  created () {
    this.getFansPortrait();
  },
  computed: {
     manPercent () {
       if((this.gender.male + this.gender.female) > 0){
          return Math.round(parseFloat(this.gender.male / (this.gender.male + this.gender.female)) * 100)
       }
       // 目前没有粉丝数据 所以 男粉丝+女粉丝是0  所以 模拟一下数据50
       return 50
     },
    iosPercent () {
       if ((this.mobile.android + this.mobile.ios) > 0) {
         return Math.round(parseFloat(this.mobile.android / (this.mobile.android + this.mobile.ios)) * 100);
       }
       return 50;
    }
  },
  methods: {
    // 获取粉丝性别分布
    async  getFansPortrait () {
       let result = await getFollowersPortrait();
       if (result.code == 0) {

         let portraits = result.data;
         let ageItems = [];
         let readItems = [];
         let totalFans = 0;
         portraits.forEach((item) => {
           var key = item.name.split(":")[1];
           var value = Number(item.value);
           // 提取性别
           if (item.name.startsWith("sex")) {
             this.gender[key] = value;
           }
           // 提取手机
           if (item.name.startsWith("mobile")) {
             this.mobile[key] = value;
           }
           // 年龄分布
           if (item.name.startsWith("age")) {
              ageItems.push(item);
              totalFans += value;
           }
           // 阅读统计
           if (item.name.startsWith("read")) {
             readItems.push(item);
           }
         });
         this.setAges(ageItems, totalFans)
         this.statisticRead(readItems)
       } else {
         this.$message({type: "error", message: result.error_message})
       }
    },
    setAges(ageItems, total) {
      // 排序年龄项
      ageItems.sort((a, b) => {
        if (a.name > b.name)
          return 1;
        else if (a.name < b.name)
          return -1;
        else
          return 0;
      });
      ageItems.forEach((item)=>{
        var key = item.name.split(":")[1];
        var value = Number(item.value);
        this.ageRangeKey.push(`${key}岁`);
        this.ageRangeValue.push(Number(value));
        let perc = Math.round(parseFloat((value / total) * 100));
        this.fansAgePerc.push({age: key, perc: `${perc}%`})
      })
    },
    statisticRead(readItems) {
      readItems.sort((a, b) => {
        if (a.name > b.name)
          return 1;
        else if (a.name < b.name)
          return -1;
        else
          return 0;
      });
      readItems.forEach((item) => {
        var key = item.name.split(":")[1];
        var value = Number(item.value);
        this.readData.key.push(key)
        this.readData.value.push(value)
      })
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
    .tabView {
      padding: 24px;
      .info-card {
        background-color: #ffffff;
        border-radius: @cardRadius;
        box-shadow: @cardShadow;
        border: 1px solid #e8e8e8;
        padding: 20px;
        margin-bottom: 20px;
      }
    }
  }
}
</style>
