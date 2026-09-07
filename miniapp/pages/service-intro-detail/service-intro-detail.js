var request = require('../../utils/request.js');

Page({
  data: { data: null },

  onLoad: function(options) {
    if (!options.id) return;
    var that = this;
    request.get('/user/service-intro/' + options.id).then(function(res) {
      var item = res.data;
      if (item && item.createTime) item.createTime = item.createTime.substring(0, 10);
      that.setData({ data: item });
    }).catch(function(err) {
      wx.showToast({ title: err.message || '加载失败', icon: 'none' });
    });
  }
});
