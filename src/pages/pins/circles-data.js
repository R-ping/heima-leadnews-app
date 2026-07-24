// 圈子页面 mock 数据 — 待后端 API 替换
export const myCircles = [
  { id: 13, name: '今天学到了', description: '每日学习打卡', memberCount: 17000, pinsCount: 36000, icon: '💡' },
  { id: 15, name: '前端开发圈', description: '前端技术交流', memberCount: 7700, pinsCount: 19000, icon: '🎨' },
  { id: 16, name: '服务端与架构', description: '服务端开发', memberCount: 4400, pinsCount: 3200, icon: '⚙️' },
  { id: 13, name: '大模型生态圈', description: 'AI技术交流', memberCount: 3500, pinsCount: 2800, icon: '🤖' },
  { id: 21, name: 'VibeLaunch', description: '编程交流', memberCount: 2800, pinsCount: 4000, icon: '🚀' }
]

export const categories = [
  { id: 1, name: '技术' }, { id: 2, name: '职场' }, { id: 3, name: '吃喝玩乐' },
  { id: 4, name: '资讯' }, { id: 5, name: '理财' }, { id: 6, name: '互动交流' },
  { id: 7, name: '书影音' }, { id: 8, name: '生活' }, { id: 9, name: '搞笑' },
  { id: 10, name: '情感' }, { id: 11, name: '游戏' }, { id: 12, name: '数码' }
]

export const allCircles = [
  { id: 13, parentId: 1, name: '大模型生态圈', description: '大模型技术交流与应用', memberCount: 35000, pinsCount: 80000, icon: '🤖' },
  { id: 14, parentId: 1, name: '微服务生态圈', description: '微服务架构与实践', memberCount: 25000, pinsCount: 60000, icon: '🏗️' },
  { id: 15, parentId: 1, name: '前端开发圈', description: '前端技术与框架交流', memberCount: 40000, pinsCount: 120000, icon: '🎨' },
  { id: 16, parentId: 1, name: '服务端与架构', description: '服务端开发与系统架构', memberCount: 30000, pinsCount: 70000, icon: '⚙️' },
  { id: 17, parentId: 1, name: '技术交流圈', description: '综合技术交流', memberCount: 50000, pinsCount: 150000, icon: '💬' },
  { id: 18, parentId: 2, name: '上班摸鱼', description: '职场摸鱼日常', memberCount: 40000, pinsCount: 100000, icon: '🐟' },
  { id: 19, parentId: 2, name: '内推招聘广场', description: '内推与招聘信息', memberCount: 35000, pinsCount: 80000, icon: '📋' },
  { id: 20, parentId: 2, name: '程序员成长', description: '程序员职业发展', memberCount: 45000, pinsCount: 120000, icon: '📈' },
  { id: 21, parentId: 3, name: '美食探店', description: '美食分享与探店', memberCount: 50000, pinsCount: 150000, icon: '🍜' },
  { id: 22, parentId: 3, name: '旅行日记', description: '旅行经历分享', memberCount: 45000, pinsCount: 120000, icon: '✈️' },
  { id: 23, parentId: 3, name: '什么值得买', description: '好物推荐与评测', memberCount: 60000, pinsCount: 200000, icon: '🛒' },
  { id: 24, parentId: 3, name: '吃货日常', description: '日常美食分享', memberCount: 45000, pinsCount: 130000, icon: '🍕' },
  { id: 25, parentId: 4, name: '今日新鲜事', description: '每日新鲜资讯', memberCount: 50000, pinsCount: 120000, icon: '📰' },
  { id: 26, parentId: 4, name: '科技前沿', description: '前沿科技资讯', memberCount: 45000, pinsCount: 100000, icon: '🔬' },
  { id: 27, parentId: 4, name: '互联网热点', description: '互联网行业热点', memberCount: 45000, pinsCount: 110000, icon: '🌐' },
  { id: 28, parentId: 5, name: '股票基金', description: '股票与基金投资', memberCount: 30000, pinsCount: 60000, icon: '📊' },
  { id: 29, parentId: 5, name: '投资理财', description: '综合投资理财', memberCount: 35000, pinsCount: 50000, icon: '💰' },
  { id: 30, parentId: 5, name: '省钱攻略', description: '省钱技巧分享', memberCount: 35000, pinsCount: 40000, icon: '💸' },
  { id: 31, parentId: 6, name: '新人报道', description: '新人自我介绍', memberCount: 20000, pinsCount: 30000, icon: '👋' },
  { id: 32, parentId: 6, name: '每日打卡', description: '每日打卡记录', memberCount: 25000, pinsCount: 50000, icon: '✅' },
  { id: 33, parentId: 6, name: '问答交流', description: '问题解答与交流', memberCount: 45000, pinsCount: 120000, icon: '❓' },
  { id: 34, parentId: 7, name: '读书分享', description: '读书心得分享', memberCount: 30000, pinsCount: 70000, icon: '📚' },
  { id: 35, parentId: 7, name: '电影推荐', description: '电影推荐与影评', memberCount: 35000, pinsCount: 80000, icon: '🎬' },
  { id: 36, parentId: 7, name: '音乐分享', description: '音乐推荐与感悟', memberCount: 35000, pinsCount: 100000, icon: '🎵' },
  { id: 37, parentId: 8, name: '日常生活', description: '日常生活分享', memberCount: 40000, pinsCount: 100000, icon: '🏠' },
  { id: 38, parentId: 8, name: '健身打卡', description: '健身与运动', memberCount: 30000, pinsCount: 60000, icon: '💪' },
  { id: 39, parentId: 8, name: '宠物日常', description: '宠物萌宠分享', memberCount: 30000, pinsCount: 80000, icon: '🐱' },
  { id: 40, parentId: 9, name: '搞笑段子', description: '搞笑段子分享', memberCount: 50000, pinsCount: 150000, icon: '😂' },
  { id: 41, parentId: 9, name: '神回复', description: '神回复合集', memberCount: 40000, pinsCount: 120000, icon: '💯' },
  { id: 42, parentId: 10, name: '恋爱话题', description: '恋爱经验分享', memberCount: 35000, pinsCount: 80000, icon: '💕' },
  { id: 43, parentId: 10, name: '单身日记', description: '单身生活分享', memberCount: 30000, pinsCount: 60000, icon: '🐶' },
  { id: 44, parentId: 11, name: '手游交流', description: '手机游戏交流', memberCount: 40000, pinsCount: 100000, icon: '🎮' },
  { id: 45, parentId: 11, name: '端游攻略', description: '端游攻略分享', memberCount: 35000, pinsCount: 80000, icon: '🖥️' },
  { id: 46, parentId: 12, name: '手机评测', description: '手机评测与推荐', memberCount: 30000, pinsCount: 50000, icon: '📱' },
  { id: 47, parentId: 12, name: '电脑配置', description: '电脑配置讨论', memberCount: 30000, pinsCount: 40000, icon: '💻' }
]

export const popularCircles = [
  { id: 48, name: '上班摸鱼', description: '来分享下你上班看到的好东西吧~', memberCount: 25000, pinsCount: 30000, icon: '🐟' },
  { id: 49, name: '青训营·快乐出发', description: '欢迎同学们，在这里尽情地分享校园...', memberCount: 12000, pinsCount: 15200, icon: '🎓' },
  { id: 50, name: '树洞一下', description: '匿名分享，让树洞倾听你的心事。', memberCount: 15000, pinsCount: 9700, icon: '🌲' },
  { id: 17, name: '技术交流圈', description: '开发者专属的技术交流圈，聊聊技术...', memberCount: 15000, pinsCount: 38000, icon: '💬' },
  { id: 51, name: '内推招聘广场', description: '人才招聘专属频道！欢迎大家发布招...', memberCount: 26000, pinsCount: 15000, icon: '📋' }
]