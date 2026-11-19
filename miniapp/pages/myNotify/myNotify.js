var request = require('../../utils/request.js');

Page({
  data: { list: [], loading: true },

  onShow: function() {
    this.loadList();
  },

  loadList: function() {
    var that = this;
    this.setData({ loading: true });
    request.get('/user/notify/list', { limit: 50 }).then(function(res) {
      that.setData({ list: res.data || [], loading: false });
    }).catch(function(err) {
      that.setData({ loading: false });
      wx.showToast({ title: (err && err.message) || '加载失败', icon: 'none' });
    });
  },

  // 点击消息：标记已读，有关联订单就跳到订单详情
  onTapItem: function(e) {
    var that = this;
    var idx = e.currentTarget.dataset.index;
    var item = this.data.list[idx];
    if (!item) return;

    var goDetail = function() {
      if (item.orderId) {
        wx.navigateTo({ url: '/pages/details/index?id=' + item.orderId });
      }
    };

    if (item.read) {
      goDetail();
      return;
    }
    // 未读 → 先标记已读（失败也不挡跳转）
    request.post('/user/notify/' + item.id + '/read').then(function() {
      var patch = {};
      patch['list[' + idx + '].read'] = true;
      that.setData(patch);
      goDetail();
    }).catch(function() {
      goDetail();
    });
  },

  markAllRead: function() {
    var that = this;
    request.post('/user/notify/read-all').then(function() {
      wx.showToast({ title: '已全部标记为已读', icon: 'success' });
      that.loadList();
    }).catch(function(err) {
      wx.showToast({ title: (err && err.message) || '操作失败', icon: 'none' });
    });
  }
});
