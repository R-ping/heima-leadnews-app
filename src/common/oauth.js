const config = {
  weibo: {
    clientId: '3770872274',
    redirectUri: 'https://78ae055a.r40.cpolar.top/user/oauth2/code/weibo',
    authorizeUrl: 'https://api.weibo.com/oauth2/authorize',
    responseType: 'code'
  },
  github: {
    clientId: 'Ov23liBlhpjPoc6XKPbf',
    redirectUri: 'https://78ae055a.r40.cpolar.top/user/oauth2/code/github',
    authorizeUrl: 'https://github.com/login/oauth/authorize',
    scope: 'user',
    responseType: 'code'
  }
}

export default config
