var request = require('../../utils/request.js');

Page({
  data: { list: [], loading: true },

  onShow: function() {
    this.loadList();
  },

  loadList: function() {
    var that = this;
    this.setData({ loading: true });
    // 返回该用户的全部券（含已使用），已使用的由页面置灰展示
    request.get('/user/voucher/mine').then(function(res) {
      that.setData({ list: res.data || [], loading: false });
    }).catch(function(err) {
      that.setData({ loading: false });
      wx.showToast({ title: (err && err.message) || '加载失败', icon: 'none' });
    });
  }
});
