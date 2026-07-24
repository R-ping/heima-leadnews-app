import Api from '@/apis/home/api'
import Config from '../config'

export default {
  data() {
    return {
      tabStates: [...Array(Config.tabTitles.length).keys()].map(() => ({
        loaded: false,
        loading: false,
        loadingMore: false,
        refreshing: false,
        noMore: false,
        error: false,
        errorMsg: ''
      })),
      params: {
        loaddir: 1,
        index: 0,
        tag: '__all__',
        size: 10,
        max_behot_time: 0,
        min_behot_time: 20000000000000
      }
    }
  },
  methods: {
    load(index, loaddir) {
      var idx = (index !== undefined) ? index : this.params.index
      var dir = (loaddir !== undefined) ? loaddir : this.params.loaddir
      var state = this.tabStates[idx]
      if (!state) return
      this.$set(state, 'loading', true)
      this.$set(state, 'error', false)
      var reqParams = {
        loaddir: dir,
        index: idx,
        tag: Config.tabTitles[idx].id,
        size: this.params.size || 10,
        max_behot_time: this.params.max_behot_time,
        min_behot_time: this.params.min_behot_time
      }
      var self = this
      Api.loaddata(reqParams).then((d) => {
        self.$set(state, 'loading', false)
        self.$set(state, 'loadingMore', false)
        self.$set(state, 'refreshing', false)
        self.$set(state, 'loaded', true)
        if (d && d.code === 200) {
          if (d.data && d.data.length > 0) {
            self.tanfer(d.data, idx, dir)
          } else {
            if (dir === 2) {
              self.$set(state, 'noMore', true)
            }
          }
        } else {
          self.$set(state, 'error', true)
          self.$set(state, 'errorMsg', (d && d.errorMessage) || '加载失败，请检查网络')
        }
      }).catch(() => {
        self.$set(state, 'loading', false)
        self.$set(state, 'loadingMore', false)
        self.$set(state, 'refreshing', false)
        self.$set(state, 'loaded', true)
        self.$set(state, 'error', true)
        self.$set(state, 'errorMsg', '网络请求失败，请检查网络连接')
      })
    },
    loadmore(index) {
      var state = this.tabStates[index]
      if (!state || state.loadingMore || state.noMore) return
      this.$set(state, 'loadingMore', true)
      this.load(index, 2)
    },
    loadnew(index) {
      var state = this.tabStates[index]
      if (!state || state.loading || state.refreshing) return
      this.$set(state, 'refreshing', true)
      this.load(index, 0)
    },
    tanfer(data, curIndex, loaddir) {
      if (!data || data.length === 0) {
        var state = this.tabStates[curIndex]
        if (state) this.$set(state, 'noMore', true)
        return
      }
      var arr = []
      for (var i = 0; i < data.length; i++) {
        try {
          var item = data[i]
          var ims = []
          if (item.images) {
            var imagesStr = item.images
            if (typeof imagesStr === 'string') {
              ims = imagesStr.replace(/[\[\]]/g, '').split(',').filter(function (s) { return s.trim() })
            } else if (Array.isArray(imagesStr)) {
              ims = imagesStr
            }
          }
          var pubTime = item.publishTime
          if (pubTime) {
            if (typeof pubTime === 'string') {
              pubTime = new Date(pubTime).getTime()
            }
            if (isNaN(pubTime)) pubTime = Date.now()
          } else {
            pubTime = Date.now()
          }
          var imgCount = ims.length
          var articleType = imgCount >= 3 ? 3 : (imgCount >= 1 ? 1 : 0)
          var tmp = {
            id: item.id,
            title: item.title || '',
            comment: item.comment || 0,
            authorId: item.authorId,
            source: item.authorName || '',
            date: pubTime,
            type: articleType,
            image: ims,
            icon: '\uf06d',
            staticUrl: item.staticUrl || ''
          }
          if (pubTime && this.params.max_behot_time < pubTime) {
            this.params.max_behot_time = pubTime
          }
          if (pubTime && this.params.min_behot_time > pubTime) {
            this.params.min_behot_time = pubTime
          }
          arr.push(tmp)
        } catch (e) {
          // ignore malformed items
        }
      }
      var newList = this.tabList.map(function (tab) { return tab.slice() })
      if (loaddir === 0) {
        newList[curIndex] = arr.concat(newList[curIndex])
      } else {
        newList[curIndex] = newList[curIndex].concat(arr)
      }
      this.tabList = newList
      this.showmore = false
      this.shownew = false
    },
    switchTab(index) {
      if (this.currentTab === index) return
      this.currentTab = index
      this.params.loaddir = 1
      this.params.index = index
      this.params.tag = Config.tabTitles[index].id
      this.params.max_behot_time = 0
      this.params.min_behot_time = 20000000000000
      this.$set(this.tabStates, index, {
        loaded: false, loading: false, loadingMore: false,
        refreshing: false, noMore: false, error: false, errorMsg: ''
      })
      var newList = this.tabList.map(function (tab) { return tab.slice() })
      newList[index] = []
      this.tabList = newList
      this.load(index, 1)
    },
    wxcTabPageCurrentTabSelected(e) {
      var index = e.page
      this.params.loaddir = 1
      this.params.index = index
      this.params.tag = Config.tabTitles[index].id
      this.params.max_behot_time = 0
      this.params.min_behot_time = 20000000000000
      this.$set(this.tabStates, index, {
        loaded: false, loading: false, loadingMore: false,
        refreshing: false, noMore: false, error: false, errorMsg: ''
      })
      var newList = this.tabList.map(function (tab) { return tab.slice() })
      newList[index] = []
      this.tabList = newList
      this.load(index, 1)
    },
    onScroll(e, index) {
      var el = e.target
      var scrollTop = el.scrollTop
      var scrollHeight = el.scrollHeight
      var clientHeight = el.clientHeight
      var state = this.tabStates[index]
      if (scrollHeight - scrollTop - clientHeight < 100) {
        if (state && !state.loadingMore && !state.noMore && !state.loading && state.loaded) {
          this.loadmore(index)
        }
      }
    },
    onDesktopScroll(e) {
      var el = e.target
      var scrollTop = el.scrollTop
      var scrollHeight = el.scrollHeight
      var clientHeight = el.clientHeight
      var state = this.currentState
      if (scrollHeight - scrollTop - clientHeight < 150) {
        if (state && !state.loadingMore && !state.noMore && !state.loading && state.loaded) {
          this.loadmore(this.currentTab)
        }
      }
    },
    resetTabState(index) {
      this.$set(this.tabStates, index, {
        loaded: false, loading: false, loadingMore: false,
        refreshing: false, noMore: false, error: false, errorMsg: ''
      })
    },
    clearTabList(index) {
      var newList = this.tabList.map(function (tab) { return tab.slice() })
      newList[index] = []
      this.tabList = newList
    }
  }
}