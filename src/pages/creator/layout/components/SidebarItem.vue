<template>
  <div class="menu-wrapper">
      <template v-if="item.children && item.children.length > 0">
          <el-submenu :index="item.title" :key="item.title">
              <template slot="title">
                <div class="group-title">
                  <i :class="item.icon" style="margin-right: 8px; font-size: 15px;"></i>
                  {{ item.title }}
                </div>
              </template>
              <template v-for="subItem in item.children">
                  <el-menu-item :index="subItem.path" :key="subItem.path">
                    {{subItem.title}}
                  </el-menu-item>
              </template>
          </el-submenu>
      </template>
      <template v-else>
          <el-menu-item :index="item.path" :key="item.path">
              <i :class="item.icon"></i><span slot="title">{{item.title}}</span>
          </el-menu-item>
      </template>
  </div>

</template>

<script>
export default {
  name: 'SidebarItem',
  props: {
    item: {
      type: Object,
      required: true
    },
  },
  data() {
    return {
      onlyOneChild: null
    }
  }
}
</script>
<style lang="less" scoped>
@import '../styles/variables.less';

.menu-wrapper {
    overflow: visible;
}

.group-title {
  display: inline-block;
  font-size: 15px;
  font-weight: 600;
  color: #252933;
  padding: 0;
  letter-spacing: 0;
}

.menu-wrapper :deep(.el-menu-item) {
  font-size: 14px;
  color: #515767;
  padding-left: 48px !important;
}

.menu-wrapper :deep(.el-menu-item.is-active) {
  color: #1e80ff;
}
</style>
