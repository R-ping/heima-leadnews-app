import Vue from 'vue'
import lang from '@/langs/lang'
import conf from '@/common/conf'
import request from '@/common/request'
import store from '@/stores/store'
import date from '@/utils/date'

Vue.prototype.$date = date
Vue.prototype.$lang = lang
Vue.prototype.$config = conf
Vue.prototype.$store = store
request.setStore(store)
Vue.prototype.$request = request

import { router } from './router';
import App from '@/main.vue';
/* eslint-disable no-new */
new Vue(Vue.util.extend({el: '#root', router}, App));
// 仅根路径自动跳转首页，其他路径（如/creator、/oauth/callback）保持原路径
if (window.location.pathname === '/' || window.location.pathname === '') {
    router.push('/home');
}
// Token过期后重定向到主页，自动弹出登录框
if (sessionStorage.getItem('showLoginAfterRedirect') === '1') {
    sessionStorage.removeItem('showLoginAfterRedirect')
    store.dispatch('showLogin')
}