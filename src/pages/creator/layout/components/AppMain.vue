<template>
  <section class="app-main">
    <transition name="fade-transform" mode="out-in">
      <div class="app-main-content" :key="key">
        <router-view/>
      </div>
    </transition>
  </section>
</template>

<script>
export default {
  name: 'AppMain',
  computed: {
    key() {
      return this.$route.fullPath
    }
  }
}
</script>

<style lang="less" scoped>
@import '../styles/variables.less';

.app-main {
  flex: 1;
  min-height: 0;
  width: 100%;
  position: relative;
  overflow-y: auto;
  background-color: @bgGray;
  -webkit-overflow-scrolling: touch;
}

.app-main-content {
  width: 100%;
  min-height: 100%;
  background-color: @bgGray;
  padding: 0;
}
</style>