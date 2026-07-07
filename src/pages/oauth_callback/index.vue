<template>
  <div class="callback-page">
    <div class="callback-card">
      <div class="status-icon" v-if="status === 'processing'">
        <span class="spinner"></span>
      </div>
      <div class="status-icon success" v-else-if="status === 'success'">
        <span class="checkmark">&#10003;</span>
      </div>
      <div class="status-icon error" v-else>
        <span class="cross">&#10005;</span>
      </div>
      <p class="status-text">{{ statusMessage }}</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'OAuthCallback',
  data() {
    return {
      status: 'processing',
      statusMessage: '正在登录...'
    }
  },
  created() {
    // Already logged in (e.g. page refresh) — redirect immediately
    if (this.$store.state.token) {
      this.$router.replace('/home')
      return
    }

    const token = this.$route.query.token
    if (token) {
      this.$store.dispatch('login', token)
      this.status = 'success'
      this.statusMessage = '登录成功'
      setTimeout(() => {
        this.$router.replace('/home')
      }, 1500)
    } else {
      this.status = 'error'
      this.statusMessage = '登录失败，请重试'
      setTimeout(() => {
        this.$router.replace('/home')
      }, 2000)
    }
  }
}
</script>

<style scoped>
.callback-page {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #ffffff;
}
.callback-card {
  text-align: center;
}
.status-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e0e0e0;
  border-top-color: #3194ff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
.checkmark,
.cross {
  font-size: 48px;
  font-weight: bold;
}
.status-icon.success .checkmark {
  color: #52c41a;
}
.status-icon.error .cross {
  color: #ff4d4f;
}
.status-text {
  font-size: 18px;
  color: #666666;
  margin: 0;
}
</style>
