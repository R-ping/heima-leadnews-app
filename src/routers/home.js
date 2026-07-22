// ============  主页路由MODEL  ==================
import Layout from '@/compoents/layouts/layout_main'
import Home from '@/pages/home/index'
import Article from '@/pages/article/index'
import Search from '@/pages/search/index'
import Screen from '@/pages/load_screen/index'
import SearchResult from '@/pages/search_result/index'
import OAuthCallback from '@/pages/oauth_callback/index'
import User from '@/pages/user/index'
import UserSettings from '@/pages/user/settings/index'
import UserGrowth from '@/pages/user/growth/index'
import UserCheckin from '@/pages/user/checkin/index'
import UserCourses from '@/pages/user/courses/index'
import UserHistory from '@/pages/user/history/index'
import Notification from '@/pages/notification/index'
import Pins from '@/pages/pins/index.vue'
import PinsCircles from '@/pages/pins/circles.vue'
import Course from '@/pages/course/index.vue'
import CourseDetail from '@/pages/course/detail.vue'
import CourseRead from '@/pages/course/read.vue'

let routes = [
    {
        path: '/',
        component: Layout,
        children:[
            {
                path:'/home',
                name:'Home',
                component: Home
            },
            {
                path:'/home/:category',
                name:'HomeCategory',
                component: Home
            }
        ]
    },{
        path:'/screen',
        name: 'screen',
        component:Screen
    },{
        path:'/article/:id',
        name:'article-info',
        component:Article
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
    },{
        path:'/user/:id',
        name:'user-profile',
        component:User
    },{
        path:'/user/settings',
        name:'user-settings',
        component:UserSettings
    },{
        path:'/user/growth',
        name:'user-growth',
        component:UserGrowth
    },{
        path:'/user/checkin',
        name:'user-checkin',
        component:UserCheckin
    },{
        path:'/user/courses',
        name:'user-courses',
        component:UserCourses
    },{
        path:'/user/history',
        name:'user-history',
        component:UserHistory
    },{
        path:'/notification',
        name:'notification',
        component:Notification
    },{
        path:'/pins',
        name:'pins',
        component:Pins
    },{
        path:'/pins/circles',
        name:'pins-circles',
        component:PinsCircles
    },{
        path:'/course',
        name:'course',
        component:Course
    },{
        path:'/course/:id',
        name:'course-detail',
        component:CourseDetail
    },{
        path:'/course/read/:id',
        name:'course-read',
        component:CourseRead
    }
]

export default routes;