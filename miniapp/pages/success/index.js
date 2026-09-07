Page({
  data: { orderId: '', eta: '' },
  onLoad: function(options) {
    if (options && options.orderId) { this.setData({ orderId: options.orderId }); }
    var eta = new Date(Date.now() + 7200000);
    var h = eta.getHours(), m = eta.getMinutes();
    this.setData({ eta: (h < 10 ? '0' + h : h) + ':' + (m < 10 ? '0' + m : m) });
  },
  goIndex: function() { wx.switchTab({ url: '/pages/index/index' }); },
  goOrder: function() { wx.redirectTo({ url: '/pages/details/index?id=' + this.data.orderId }); }
});
