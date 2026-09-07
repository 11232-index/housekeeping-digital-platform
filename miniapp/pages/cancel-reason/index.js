var request = require('../../utils/request.js');

Page({
  data: {
    orderId: '',
    from: 'list',
    reasons: ['临时有事，无法服务', '预约时间不合适', '想要更换服务/家政员', '想要更换服务时间', '价格太高', '其他原因'],
    selectedReason: '',
    customReason: ''
  },

  onLoad: function(options) {
    if (options && options.orderId) {
      this.setData({ orderId: options.orderId });
    }
    if (options && options.from) {
      this.setData({ from: options.from });
    }
  },

  onSelectReason: function(e) {
    var reason = e.currentTarget.dataset.reason;
    this.setData({ selectedReason: this.data.selectedReason === reason ? '' : reason });
  },

  onInput: function(e) {
    this.setData({ customReason: e.detail.value });
  },

  onCancel: function() {
    wx.navigateBack();
  },

  onConfirm: function() {
    var that = this;
    // 拼接取消原因：选中原因 + 自定义原因
    var parts = [];
    if (this.data.selectedReason) parts.push(this.data.selectedReason);
    if (this.data.customReason && this.data.customReason.trim()) parts.push(this.data.customReason.trim());
    var reason = parts.join('；');

    wx.showLoading({ title: '处理中...' });
    request.put('/user/order/cancel', {
      orderId: parseInt(this.data.orderId),
      cancelReason: reason || null
    }).then(function() {
      wx.hideLoading();
      wx.showToast({ title: reason ? '已提交取消' : '已取消', icon: 'success' });
      setTimeout(function() {
        if (that.data.from === 'detail') {
          wx.navigateBack();
        } else {
          wx.redirectTo({ url: '/pages/details/index?id=' + that.data.orderId });
        }
      }, 1200);
    }).catch(function(err) {
      wx.hideLoading();
      wx.showToast({ title: err.message || '操作失败', icon: 'none' });
    });
  }
});
