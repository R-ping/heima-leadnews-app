// ============  主页路由MODEL  ==================
import Layout from '@/compoents/layouts/layout_main'
import Home from '@/pages/home/index'
import Article from '@/pages/article/index'
import Search from '@/pages/search/index'
import Screen from '@/pages/load_screen/index'
import SearchResult from '@/pages/search_result/index'
import OAuthCallback from '@/pages/oauth_callback/index'

let routes = [
    {
        path: '/',
        component: Layout,
        children:[
            {
                path:'/home',
                name:'Home',
                component: Home
            }
        ]
    },{
        path:'/screen',
        name: 'screen',
        component:Screen
    },{
        path:'/article',
        name:'article-info',
        component:Article,
        props:true
    },{
        path:'/search',
        name:'search',
        component:Search
    },{
        path:'/search_result',
        name:'search_result',
        component:SearchResult,
        props:true
    },{
        path:'/oauth/callback',
        name:'oauth-callback',
        component:OAuthCallback
    }
]

export default routes;
