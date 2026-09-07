var request = require('../../utils/request.js');

Page({
  data: {
    keyword: '',
    searchHistory: [],
    showHistory: false,
    list: [],
    page: 1,
    pageSize: 10,
    hasMore: true,
    loading: false
  },

  onLoad: function() {
    this.loadSearchHistory();
    this.loadList();
  },

  onShow: function() {
    this.setData({ list: [], page: 1, hasMore: true });
    this.loadList();
  },

  loadSearchHistory: function() {
    var that = this;
    request.get('/user/updates-search-history/list').then(function(res) {
      var history = res.data || [];
      that.setData({ searchHistory: history, showHistory: history.length > 0 && that.data.keyword === '' });
    }).catch(function() {});
  },

  onSearchFocus: function() {
    if (this.data.keyword === '' && this.data.searchHistory.length > 0) {
      this.setData({ showHistory: true });
    }
  },

  tapHistoryItem: function(e) {
    var keyword = e.currentTarget.dataset.keyword;
    this.setData({ keyword: keyword, showHistory: false, list: [], page: 1, hasMore: true });
    this.loadList();
  },

  clearSearchHistory: function() {
    var that = this;
    request.del('/user/updates-search-history/clean').then(function() {
      that.setData({ searchHistory: [], showHistory: false });
      wx.showToast({ title: '已清空', icon: 'success', duration: 1000 });
    }).catch(function() {});
  },

  onInput: function(e) {
    var val = e.detail.value;
    this.setData({ keyword: val, showHistory: val === '' && this.data.searchHistory.length > 0 });
  },

  onSearch: function(e) {
    var keyword = e.detail.value.trim();
    if (!keyword) {
      this.setData({ keyword: '', list: [], page: 1, hasMore: true, showHistory: true });
      this.loadList();
      return;
    }
    var that = this;
    this.setData({ keyword: keyword, list: [], page: 1, hasMore: true, showHistory: false });
    // 保存搜索记录
    request.post('/user/updates-search-history?keyword=' + encodeURIComponent(keyword)).then(function() {
      that.loadSearchHistory();
    }).catch(function() {});
    this.loadList();
  },

  onClear: function() {
    var that = this;
    this.setData({ keyword: '', list: [], page: 1, hasMore: true, showHistory: this.data.searchHistory.length > 0 });
    this.loadSearchHistory();
  },

  loadList: function() {
    var that = this;
    if (!that.data.hasMore || that.data.loading) return;
    that.setData({ loading: true });
    var params = { page: that.data.page, pageSize: that.data.pageSize };
    if (that.data.keyword) params.title = that.data.keyword;
    request.get('/user/updates/page', params).then(function(res) {
      var records = (res.data && res.data.records) ? res.data.records : [];
      // 格式化时间显示
      records.forEach(function(item) {
        if (item.createTime) {
          item.createTime = item.createTime.substring(0, 10); // yyyy-MM-dd
        }
      });
      var newList = that.data.list.concat(records);
      var hasMore = records.length >= that.data.pageSize;
      that.setData({
        list: newList,
        hasMore: hasMore,
        loading: false,
        page: that.data.page + 1
      });
    }).catch(function() {
      that.setData({ loading: false });
    });
  },

  onTapCard: function(e) {
    var id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: '/pages/updates-detail/updates-detail?id=' + id });
  },

  loadMore: function() {
    this.loadList();
  }
});
