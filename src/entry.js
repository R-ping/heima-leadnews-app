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
router.push('/home');
