<template>
    <div class="sidebar">
        <div class="sidebar-write-section">
            <div class="write-btn" @click="goPublish">
                <i class="el-icon-edit"></i> 写文章
            </div>
        </div>
        <el-menu class="sidebar-el-menu"
            :default-active="defaultRoute"
             background-color="#ffffff"
             text-color="#515767"
             active-text-color="#1e80ff"
             router
             :collapse="collapse"
            >
            <sidebar-item v-for="route in items" :item="route" :key="route.path"/>
        </el-menu>
    </div>
</template>

<script>
import SidebarItem from './SidebarItem.vue'
import { MenuData } from '../../constants/menus'
export default {
    props: ['collapse'],
    components: { SidebarItem },
    data() {
        return {
           items: MenuData // 利用配置的路由来进行菜单展示
        }
    },
    computed: {
        defaultRoute() {
          return this.$route.path  // 这里是为了active的菜单能够在刷新页面时也同样高亮显示
        }
    },
    methods: {
        goPublish() {
            window.open('/creator/publish', '_blank');
        }
    }
  }
</script>

<style lang="less" scoped>
@import '../styles/variables.less';

.sidebar-write-section {
    padding: 16px;
    .write-btn {
        width: 100%;
        height: 40px;
        line-height: 40px;
        text-align: center;
        background-color: #1e80ff;
        color: #fff;
        border-radius: 6px;
        font-size: 14px;
        font-weight: 500;
        cursor: pointer;
        transition: background-color 0.2s;
        &:hover {
            background-color: #1171ee;
        }
        i {
            margin-right: 6px;
        }
    }
}

.sidebar {
    background-color: @menuBg;
    height: 100%;
}
</style>
