export const MenuData = [
  { title: '首页', path: '/creator/dashboard', icon: 'el-icon-s-home' },
  {
    title: '内容管理',
    icon: 'el-icon-document',
    children: [
      { title: '文章管理', path: '/creator/article/list' },
      { title: '专栏管理', path: '/creator/column/list' },
      { title: '沸点管理', path: '/creator/pins/list' },
      { title: '课程管理', path: '/creator/course/list' }
    ]
  },
  {
    title: '课程运营',
    icon: 'el-icon-s-marketing',
    children: [
      { title: '折扣码管理', path: '/creator/course/discount' },
      { title: '收入结算', path: '/creator/course/settlement' }
    ]
  },
  {
    title: '数据中心',
    icon: 'el-icon-s-data',
    children: [
      { title: '内容数据', path: '/creator/data' },
      { title: '粉丝数据', path: '/creator/fans' }
    ]
  },
  {
    title: '创作成长',
    icon: 'el-icon-s-promotion',
    children: [
      { title: '创作等级权益', path: '/creator/growth/grade' },
      { title: '创作任务', path: '/creator/growth/tasks' },
      { title: '创作灵感', path: '/creator/growth/inspiration' }
    ]
  },
  ]
