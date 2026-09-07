var request = require('../../utils/request.js');
var util = require('../../utils/util.js');

Page({
  data: { housekeeper: null, scheduleList: [], selectedSlot: null, booking: false },
  housekeeperId: null,

  onShow: function() {
    this.setData({ booking: false });
    // 每次显示页面时重新加载可用时段（防止已预约的时段仍显示）
    if (this.housekeeperId) {
      this.loadSchedules(this.housekeeperId);
    }
  },

  onLoad: function(options) {
    if (!options.id) return;
    this.housekeeperId = options.id;
    var that = this;
    request.get('/user/housekeeper/' + options.id).then(function(res) {
      var hk = res.data;
      if (hk) { hk.ratingText = util.formatRating(hk.rating); }
      that.setData({ housekeeper: hk });
      that.loadSchedules(options.id);
    }).catch(function() {});
  },

  loadSchedules: function(housekeeperId) {
    var that = this;
    request.get('/user/housekeeper-schedule/available', { housekeeperId: housekeeperId }).then(function(res) {
      that.setData({ scheduleList: res.data || [], selectedSlot: null });
    }).catch(function() {
      that.setData({ scheduleList: [], selectedSlot: null });
    });
  },

  onSelectSlot: function(e) {
    var index = e.currentTarget.dataset.index;
    this.setData({ selectedSlot: index });
  },

  onBookNow: function() {
    if (this.data.booking) return;
    if (this.data.scheduleList.length === 0) {
      wx.showToast({ title: '暂无可用时段，不可预约', icon: 'none' });
      return;
    }
    if (this.data.selectedSlot === null) {
      wx.showToast({ title: '您还未选择预约时间', icon: 'none' });
      return;
    }
    this.setData({ booking: true });
    var slot = this.data.scheduleList[this.data.selectedSlot];
    var params = 'housekeeperId=' + this.data.housekeeper.id +
      '&scheduleDate=' + slot.scheduleDate +
      '&scheduleTime=' + slot.startTime + '~' + slot.endTime +
      '&scheduleId=' + slot.id;
    wx.navigateTo({ url: '/pages/order/index?' + params });
  }
});
