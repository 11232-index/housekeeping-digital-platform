var request = require('../../utils/request.js');
var util = require('../../utils/util.js');

Page({
  data: { orderId: '', order: {}, statusText: '', items: [], acting: false, housekeeperPhone: '', housekeeperName: '' },

  onShow: function() {
    this.setData({ acting: false });
    if (this.data.orderId) {
      this.loadOrderDetail(this.data.orderId);
    }
  },

  onLoad: function(options) {
    if (options && options.id) {
      this.setData({ orderId: options.id });
      this.loadOrderDetail(options.id);
    }
  },

  loadOrderDetail: function(orderId) {
    var that = this;
    util.showLoading('加载中...');
    request.get('/user/order/' + orderId).then(function(res) {
      util.hideLoading();
      var order = res.data;
      var items = order.orderDetailList || [];
      that.setData({
        order: order,
        statusText: util.getOrderStatusText(order.status),
        items: items
      });
      // 如果有家政员id，查家政员电话
      for (var i = 0; i < items.length; i++) {
        if (items[i].housekeeperId) {
          that.loadHousekeeperPhone(items[i].housekeeperId);
          break;
        }
      }
    }).catch(function(err) {
      util.hideLoading();
      wx.showToast({ title: err.message || '加载失败', icon: 'none' });
    });
  },

  loadHousekeeperPhone: function(housekeeperId) {
    var that = this;
    request.get('/user/housekeeper/' + housekeeperId + '/employee').then(function(res) {
      var emp = res.data;
      if (emp && emp.phone) {
        that.setData({ housekeeperPhone: emp.phone, housekeeperName: emp.name || '' });
      }
    }).catch(function() {});
  },

  handlePay: function() {
    if (this.data.acting) return;
    this.setData({ acting: true });
    wx.navigateTo({ url: '/pages/pay/index?orderId=' + this.data.orderId });
  },

  handleCancel: function() {
    if (this.data.acting) return;
    var that = this;
    this.setData({ acting: true });
    wx.showModal({
      title: '取消订单',
      content: '确定要取消该订单吗？',
      success: function(res) {
        if (res.confirm) {
          that.setData({ acting: false });
          wx.navigateTo({ url: '/pages/cancel-reason/index?orderId=' + that.data.orderId + '&from=detail' });
        } else {
          that.setData({ acting: false });
        }
      }
    });
  },

  handleReminder: function() {
    if (this.data.acting) return;
    this.setData({ acting: true });
    wx.showToast({ title: '已催单', icon: 'success' });
    setTimeout(function() { this.setData({ acting: false }); }.bind(this), 1500);
  },

  oneMoreOrder: function() {
    var items = this.data.items;
    if (items && items.length > 0) {
      wx.setStorageSync('cartList', items);
    }
    wx.switchTab({ url: '/pages/index/index' });
  },

  handlePhone: function() {
    wx.makePhoneCall({ phoneNumber: '400-888-6688' });
  },

  handleCallHousekeeper: function() {
    wx.makePhoneCall({ phoneNumber: this.data.housekeeperPhone });
  }
});
