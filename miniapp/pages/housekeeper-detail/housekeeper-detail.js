var request = require('../../utils/request.js');
var util = require('../../utils/util.js');

Page({
  data: { data: null, carouselList: [], employee: null, booking: false },

  onShow: function() {
    this.setData({ booking: false });
  },

  onLoad: function(options) {
    if (!options.id) return;
    var that = this;
    request.get('/user/housekeeper/' + options.id).then(function(res) {
      var item = res.data;
      // 解析轮播图：分号切割，过滤空串
      var carouselList = (item.carousel || '').split(';').filter(function(u) { return u.trim(); });
      if (item) { item.ratingText = util.formatRating(item.rating); }
      that.setData({ data: item, carouselList: carouselList });
    }).catch(function(err) {
      wx.showToast({ title: err.message || '加载失败', icon: 'none' });
    });
    // 查询关联员工信息
    request.get('/user/housekeeper/' + options.id + '/employee').then(function(res) {
      that.setData({ employee: res.data });
    }).catch(function() {});
  },
  onBookNow: function() {
    if (this.data.booking) return;
    this.setData({ booking: true });
    var that = this;
    request.get('/admin/shop/getStatus').then(function(res) {
      if (res.data === 0) {
        that.setData({ booking: false });
        wx.showToast({ title: '已经休息了哟，暂时无法预约', icon: 'none' });
        return;
      }
      if (res.data === 0) {
        that.setData({ booking: false });
        wx.showToast({ title: '已经休息了哟，暂时无法预约', icon: 'none' });
        return;
      }
      // 查该家政员是否有可预约时段
      request.get('/user/housekeeper-schedule/available', { housekeeperId: that.data.data.id }).then(function(sRes) {
        if (!sRes.data || sRes.data.length === 0) {
          that.setData({ booking: false });
          wx.showToast({ title: '该家政员暂无可预约时间', icon: 'none' });
          return;
        }
        wx.navigateTo({ url: '/pages/appointment/appointment?id=' + that.data.data.id });
      }).catch(function() {
        wx.navigateTo({ url: '/pages/appointment/appointment?id=' + that.data.data.id });
      });
    }).catch(function() {
      that.setData({ booking: false });
      wx.navigateTo({ url: '/pages/appointment/appointment?id=' + that.data.data.id });
    });
  }
});
