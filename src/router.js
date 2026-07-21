import Vue from 'vue'
import Router from 'vue-router'
import routes from '@/routers/index'
import navigator from '@/utils/navigator'
import store from '@/stores/store'
import { creatorGuard } from '@/routers/creator'

Vue.use(Router)
const router = new Router({
    mode: 'history',
    routes: routes
});

// 注册导航器属性
router.$navigator = navigator

// 注册返回方法
router.back = function(){
    var to = this.$navigator.back()
    if(to){
        this.push(to)
    }
}

// 路由之前记录路由处理
router.beforeResolve((to, from, next) => {
    router.$navigator.push(to, from, next)
})

// 路由守卫：需要登录的页面检查
router.beforeEach((to, from, next) => {
    // OAuth回调页面直接放行
    if (to.path === '/oauth/callback') {
        next()
        return
    }
    // 创作者中心登录鉴权
    creatorGuard(to, from, next)
})

export {router}