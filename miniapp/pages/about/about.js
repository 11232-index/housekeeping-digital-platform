var request = require('../../utils/request.js');

Page({
  data: { list: [], currentIndex: 0 },

  onLoad: function() {
    var that = this;
    request.get('/user/about/list').then(function(res) {
      that.setData({ list: res.data || [] });
    }).catch(function() {});
  },

  onTabTap: function(e) {
    this.setData({ currentIndex: e.currentTarget.dataset.index });
  }
});
