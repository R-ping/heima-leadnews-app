// ============  主页路由MODEL  ==================
// 布局组件（app shell）保持静态导入，避免首屏闪烁
import Layout from '@/components/layouts/layout_main'
// 路由组件全部使用动态导入，实现按需加载
const Home = () => import('@/pages/home/index')
const Article = () => import('@/pages/article/index')
const Search = () => import('@/pages/search/index')
const Screen = () => import('@/pages/load_screen/index')
const SearchResult = () => import('@/pages/search_result/index')
const OAuthCallback = () => import('@/pages/oauth_callback/index')
const User = () => import('@/pages/user/index')
const UserSettings = () => import('@/pages/user/settings/index')
const UserGrowth = () => import('@/pages/user/growth/index')
const UserCheckin = () => import('@/pages/user/checkin/index')
const UserCourses = () => import('@/pages/user/courses/index')
const UserHistory = () => import('@/pages/user/history/index')
const Notification = () => import('@/pages/notification/index')
const Pins = () => import('@/pages/pins/index.vue')
const PinsCircles = () => import('@/pages/pins/circles.vue')
const Course = () => import('@/pages/course/index.vue')
const CourseDetail = () => import('@/pages/course/detail.vue')
const CourseRead = () => import('@/pages/course/read.vue')
const Hot = () => import('@/pages/hot/index.vue')

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
            },
            {
                path:'/hot',
                name:'hot',
                component: Hot
            },
            {
                path:'/user/settings',
                name:'user-settings',
                component:UserSettings
            },
            {
                path:'/user/growth',
                name:'user-growth',
                component:UserGrowth
            },
            {
                path:'/user/checkin',
                name:'user-checkin',
                component:UserCheckin
            },
            {
                path:'/user/courses',
                name:'user-courses',
                component:UserCourses
            },
            {
                path:'/user/history',
                name:'user-history',
                component:UserHistory
            },
            {
                path:'/user/:id',
                name:'user-profile',
                component:User
            },
            {
                path:'/notification',
                name:'notification',
                component:Notification
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