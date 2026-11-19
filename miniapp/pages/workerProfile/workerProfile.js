var request = require('../../utils/request.js');

// 星期、身价、性别的展示映射（与后端 JSON / 数据库取值对应）
var WEEK_LABELS = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
var RANK_LABELS = { 0: '无牌', 1: '金牌', 2: '银牌', 3: '铜牌' };
var SEX_LABELS = { '1': '男', '2': '女' };

Page({
  data: {
    loading: true,
    profile: null,
    // 以下都是派生出来的展示文本：WXML 里不能 JSON.parse，统一在 JS 里算好
    sexText: '暂无',
    priceText: '暂无',
    ratingText: '暂无',
    rankText: '暂无',
    educationText: '暂无',
    experienceText: '暂无',
    scheduleRows: [],
    slotList: []
  },

  onLoad: function() {
    this.loadProfile();
  },

  onShow: function() {
    // 与 workerHome 一致的鉴权：没有家政员令牌就回登录页
    if (!wx.getStorageSync('workerToken')) {
      wx.redirectTo({ url: '/pages/workerLogin/workerLogin' });
    }
  },

  loadProfile: function() {
    var that = this;
    this.setData({ loading: true });

    request.get('/worker/profile').then(function(res) {
      var p = res.data || {};
      that.setData({
        loading: false,
        profile: p,
        sexText: SEX_LABELS[String(p.sex)] || '暂无',
        priceText: that.buildPriceText(p.price),
        ratingText: that.buildRatingText(p.rating),
        rankText: RANK_LABELS[p.rank] || '暂无',
        educationText: that.buildEducationText(p.education),
        experienceText: that.buildExperienceText(p.pastExperience),
        scheduleRows: that.buildScheduleRows(p.schedule),
        slotList: p.availableSlots || []
      });
    }).catch(function(err) {
      that.setData({ loading: false, profile: null });
      var unauthorized = err && err.message === 'Unauthorized';
      wx.showToast({ title: unauthorized ? '登录已过期，请重新登录' : '加载失败', icon: 'none' });
      if (unauthorized) {
        setTimeout(function() {
          wx.redirectTo({ url: '/pages/workerLogin/workerLogin' });
        }, 1200);
      }
    });
  },

  // 单价 → "¥120/次"
  buildPriceText: function(price) {
    if (!price || Number(price) <= 0) return '暂无';
    return '¥' + price + '/次';
  },

  // 评分 → "4.8 分"
  buildRatingText: function(rating) {
    if (!rating || Number(rating) <= 0) return '暂无';
    return Number(rating).toFixed(1) + ' 分';
  },

  // 学历 JSON → "学校（学历）、学校（学历）"
  buildEducationText: function(raw) {
    if (!raw) return '暂无';
    var list;
    try {
      list = JSON.parse(raw);
    } catch (e) {
      return raw; // 不是 JSON 就原样显示
    }
    if (!list || !list.length) return '暂无';
    var parts = [];
    for (var i = 0; i < list.length; i++) {
      var e = list[i] || {};
      if (e.school && e.degree) parts.push(e.school + '（' + e.degree + '）');
      else if (e.school) parts.push(e.school);
      else if (e.degree) parts.push(e.degree);
    }
    return parts.length ? parts.join('、') : '暂无';
  },

  // 经历 JSON → "描述（起 至 止）；描述"
  buildExperienceText: function(raw) {
    if (!raw) return '暂无';
    var list;
    try {
      list = JSON.parse(raw);
    } catch (e) {
      return raw;
    }
    if (!list || !list.length) return '暂无';
    var parts = [];
    for (var i = 0; i < list.length; i++) {
      var it = list[i] || {};
      var range = it.dateRange || [];
      var rangeText = (range.length === 2 && range[0] && range[1])
        ? '（' + range[0] + ' 至 ' + range[1] + '）'
        : '';
      var text = (it.description || '') + rangeText;
      if (text) parts.push(text);
    }
    return parts.length ? parts.join('；') : '暂无';
  },

  // 排班 JSON → 每个星期几一行的展示数据（slots 是数组，由 WXML 渲染成一排小标签）
  // 入库结构：{"1":[{"start":"09:00","end":"11:00"}],"3":[{"start":"14:00","end":"16:00"}]}
  // 返回：[{ day: '周一', slots: ['09:00-11:00', '14:00-16:00'] }, { day: '周三', slots: ['14:00-16:00'] }]
  buildScheduleRows: function(raw) {
    if (!raw) return [];
    var sch;
    try {
      sch = JSON.parse(raw);
    } catch (e) {
      return [];
    }
    if (!sch) return [];

    var rows = [];
    var keys = Object.keys(sch);
    for (var i = 0; i < keys.length; i++) {
      var day = Number(keys[i]);
      if (!(day >= 1 && day <= 7)) continue;
      var list = sch[keys[i]];
      if (!list || !list.length) continue;
      var slots = list
        .filter(function(s) { return s && s.start && s.end; })
        .map(function(s) { return s.start + '-' + s.end; });
      if (slots.length) rows.push({ day: WEEK_LABELS[day - 1], slots: slots, sort: day });
    }
    rows.sort(function(a, b) { return a.sort - b.sort; });
    return rows;
  }
});
