var request = require('../../utils/request.js');
var util = require('../../utils/util.js');

Page({
  data: { orderId: '', amount: '0.00', countdown: 900, countdownText: '15:00', payMethod: 1, isPaying: false },

  onLoad: function(options) {
    if (options && options.orderId) {
      this.setData({ orderId: options.orderId });
      this.loadOrderInfo(options.orderId);
    }
    this.startCountdown();
  },

  loadOrderInfo: function(orderId) {
    var that = this;
    request.get('/user/order/' + orderId).then(function(res) {
      var order = res.data;
      if (order && order.amount != null) {
        that.setData({ amount: order.amount.toFixed ? order.amount.toFixed(2) : String(order.amount) });
      }
      if (order && order.payMethod != null) {
        that.setData({ payMethod: order.payMethod });
      }
    }).catch(function() {});
  },

  startCountdown: function() {
    var that = this;
    this.timer = setInterval(function() {
      var c = that.data.countdown - 1;
      if (c <= 0) { clearInterval(that.timer); wx.showToast({ title: '支付超时', icon: 'none' }); setTimeout(function() { wx.navigateBack(); }, 1500); return; }
      var m = Math.floor(c / 60), s = c % 60;
      that.setData({ countdown: c, countdownText: (m < 10 ? '0' + m : m) + ':' + (s < 10 ? '0' + s : s) });
    }, 1000);
  },

  onUnload: function() { if (this.timer) clearInterval(this.timer); },

  selectPayMethod: function(e) {
    this.setData({ payMethod: parseInt(e.currentTarget.dataset.method) });
  },

  handlePay: function() {
    var that = this;
    if (this.data.isPaying) return;
    this.setData({ isPaying: true });
    wx.showLoading({ title: '支付中...' });
    request.put('/user/order/pay/' + this.data.orderId + '?payMethod=' + this.data.payMethod).then(function() {
      wx.hideLoading();
      that.setData({ isPaying: false });
      wx.removeStorageSync('cartList');
      wx.redirectTo({ url: '/pages/success/index?orderId=' + that.data.orderId });
    }).catch(function(err) {
      wx.hideLoading();
      that.setData({ isPaying: false });
      wx.showToast({ title: err.message || '支付失败', icon: 'none' });
    });
  }
});
