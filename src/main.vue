<template>
  <div class="wrapper">
    <router-view></router-view>
    <LoginModal/>
  </div>
</template>

<script>
  import util from "@/utils/font"
  import LoginModal from "@/compoents/login_modal"

export default {
  name: 'HeiMa-App',
  components: { LoginModal },
  created () {
    util.initIconFont();
  }
}
</script>

<style>
.wrapper {
  width: 100%;
  height: 100%;
}
</style>
