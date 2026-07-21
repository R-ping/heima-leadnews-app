import Home from './home'
import Creator from './creator'

let routes = []

let concat = (router) => {
    routes = routes.concat(router)
}
// 合并'主页'相关路由
routes = routes.concat(Home)
// 合并'创作中心'相关路由
routes = routes.concat(Creator)

export default  routes;
