var request = require('../../utils/request.js');

Page({
  data: {
    data: null
  },

  goBack: function() {
    wx.navigateBack();
  },

  onLoad: function(options) {
    var id = options.id;
    if (!id) return;
    var that = this;
    request.get('/user/updates/' + id).then(function(res) {
      var item = res.data;
      if (item && item.createTime) {
        item.createTime = item.createTime.substring(0, 10);
      }
      that.setData({ data: item });
    }).catch(function(err) {
      wx.showToast({ title: err.message || '加载失败', icon: 'none' });
    });
  }
});
