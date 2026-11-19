var request = require('../../utils/request.js');

Page({
  data: {
    orderId: '',
    order: null,
    loading: true
  },

  onLoad: function (options) {
    if (!wx.getStorageSync('workerToken')) {
      wx.redirectTo({ url: '/pages/workerLogin/workerLogin' });
      return;
    }
    if (options && options.id) {
      this.setData({ orderId: options.id });
      this.loadDetail();
    } else {
      this.setData({ loading: false });
    }
  },

  loadDetail: function () {
    var that = this;
    this.setData({ loading: true });
    // 后端会校验这单是否指派给当前家政员，不是的话按「不存在」返回
    request.get('/worker/order/' + this.data.orderId).then(function (res) {
      that.setData({ order: res.data || null, loading: false });
    }).catch(function (err) {
      that.setData({ loading: false });
      wx.showToast({ title: (err && err.message) || '加载失败', icon: 'none' });
    });
  }
});
