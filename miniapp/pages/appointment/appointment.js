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
      that.setData({ scheduleList: that.filterBookableDates(res.data || []), selectedSlot: null });
    }).catch(function() {
      that.setData({ scheduleList: [], selectedSlot: null });
    });
  },

  /**
   * 只保留「明天起往后 3 天」的可预约时段。
   * 例：今天 9/21 → 只展示 9/22、9/23、9/24；其余日期无论是否可约都不展示。
   */
  filterBookableDates: function(list) {
    if (!list || list.length === 0) return [];
    var DAY = 24 * 60 * 60 * 1000;
    var todayStart = new Date();
    todayStart.setHours(0, 0, 0, 0);
    var start = new Date(todayStart.getTime() + DAY);   // 明天 00:00
    var end = new Date(start.getTime() + 3 * DAY);      // 明天 + 3 天（不含）
    return list.filter(function(slot) {
      if (!slot.scheduleDate) return false;
      var d = new Date(String(slot.scheduleDate).replace(/-/g, '/'));
      return d.getTime() >= start.getTime() && d.getTime() < end.getTime();
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
