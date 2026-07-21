<template>
  <div class="wrapper">
    <router-view></router-view>
    <transition name="login-fade">
      <LoginModal v-if="showLoginModal" />
    </transition>
    <transition name="bind-fade">
      <SocialBindModal v-if="showSocialBindModal" />
    </transition>
  </div>
</template>

<script>
  import util from "@/utils/font"
  import LoginModal from "@/compoents/login_modal.vue"
  import SocialBindModal from "@/compoents/social_bind_modal.vue"

export default {
  name: 'HeiMa-App',
  components: { LoginModal, SocialBindModal },
  computed: {
    showLoginModal() {
      return this.$store.getters.showLoginModal
    },
    showSocialBindModal() {
      return this.$store.getters.showSocialBindModal
    }
  },
  created () {
    util.initIconFont();
    document.documentElement.style.fontSize = '75px';
  }
}
</script>

<style>
@import './styles/base.css';
@import './styles/common.less';

.wrapper {
  width: 100%;
  height: 100%;
}
.bind-fade-enter-active, .bind-fade-leave-active {
  transition: opacity 0.25s ease;
}
.bind-fade-enter, .bind-fade-leave-to {
  opacity: 0;
}
</style>