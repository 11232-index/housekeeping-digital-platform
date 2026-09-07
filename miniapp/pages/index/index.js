var app = getApp();
var request = require('../../utils/request.js');

Page({
  data: {
    shopStatus: 1,
    shopName: '智能家政服务',
    shopDesc: '专业家政服务，让生活更轻松',
    shopAddress: '北京市朝阳区建国路88号',
    loading: false,
    isLoggedIn: false,
    userInfo: {},
    authStep: 0,
    avatarUrl: '',
    nickName: '',
    bannerList: [],
    housekeeperList: [],
    feedList: [],
    couponList: []
  },

  onLoad: function() {
    this.loadBanner();
    this.loadHousekeeper();
    this.loadFeed();
    this.loadCoupon();
    this.loadShopStatus();
  },
  onReady: function() {
    this.checkLogin();
  },
  onShow: function() {
wx.hideTabBar();
    if (app.globalData.isLoggedIn) { this.loadBanner(); this.loadHousekeeper(); this.loadFeed(); this.loadCoupon(); }
    this.loadShopStatus();
    if (app.globalData.needReAuth) {
      app.globalData.needReAuth = false;
      app.globalData.isInAuthFlow = true;
      this.setData({ isLoggedIn: false });
      wx.hideTabBar();
      this.setData({ authStep: 1 });
    }
  },

  // 加载资讯中心
  loadFeed: function() {
    var that = this;
    request.get('/user/feed/list').then(function(res) {
      var list = res.data || [];
      list.forEach(function(item) {
        if (item.createTime) item.createTime = item.createTime.substring(0, 10);
      });
      that.setData({ feedList: list });
    }).catch(function() {});
  },

  // 点击资讯跳转详情
  onFeedTap: function(e) {
    var item = e.currentTarget.dataset.item;
    var detailId = item.sourceType === 'updates' ? item.updatesId : item.serviceIntroId;
    var url = item.sourceType === 'updates'
      ? '/pages/updates-detail/updates-detail?id=' + detailId
      : '/pages/service-intro-detail/service-intro-detail?id=' + detailId;
    wx.navigateTo({ url: url });
  },

  // 加载家政推荐
  loadHousekeeper: function() {
    var that = this;
    request.get('/user/housekeeper/top10').then(function(res) {
      that.setData({ housekeeperList: res.data || [] });
    }).catch(function() {});
  },

  // 加载轮播图
  loadBanner: function() {
    var that = this;
    request.get('/user/hot-picks/list').then(function(res) {
      that.setData({ bannerList: res.data || [] });
    }).catch(function() {});
  },

  // 加载优惠券
  loadCoupon: function() {
    var that = this;
    request.get('/user/voucher/list').then(function(res) {
      var list = res.data || [];
      list.forEach(function(item) {
        if (item.endTime) item.endTime = item.endTime.substring(0, 10);
        if (item.beginTime) item.beginTime = item.beginTime.substring(0, 10);
      });
      that.setData({ couponList: list });
    }).catch(function() {});
  },

  // 领取/秒杀优惠券
  onCouponClaim: function(e) {
    var that = this;
    var voucherId = e.currentTarget.dataset.id;
    var isSeckill = e.currentTarget.dataset.type === 1;
    wx.showModal({
      title: isSeckill ? '秒杀优惠券' : '领取优惠券',
      content: '确定领取该优惠券吗？',
      success: function(res) {
        if (!res.confirm) return;
        request.post('/user/voucher/seckill', { voucherId: voucherId }).then(function(data) {
          wx.showToast({ title: '领取成功', icon: 'success' });
          that.loadCoupon();
        }).catch(function(err) {
          wx.showToast({ title: err.message || '领取失败', icon: 'none' });
        });
      }
    });
  },

  // 快捷入口点击
  onHkTap: function(e) {
    wx.navigateTo({ url: '/pages/housekeeper-detail/housekeeper-detail?id=' + e.currentTarget.dataset.id });
  },

  onMoreHousekeeper: function() {
    var app = getApp();
    app.globalData.bookingTab = 'housekeeper';
    wx.switchTab({ url: '/pages/booking/booking' });
  },

  onNavTap: function(e) {
    var page = e.currentTarget.dataset.page;
    if (page === 'updates') {
      wx.navigateTo({ url: '/pages/updates/updates' });
    } else if (page === 'service-intro') {
      wx.navigateTo({ url: '/pages/service-intro/service-intro' });
    } else if (page === 'about') {
      wx.navigateTo({ url: '/pages/about/about' });
    } else if (page === 'housekeeper') {
      app.globalData.bookingTab = 'housekeeper';
      wx.switchTab({ url: '/pages/booking/booking' });
    } else {
      wx.showToast({ title: '敬请期待', icon: 'none' });
    }
  },

  loadShopStatus: function() {
    var that = this;
    request.get('/admin/shop/getStatus').then(function(res) {
      if (res.data !== undefined) that.setData({ shopStatus: res.data });
    }).catch(function() {});
  },

  // ========== 微信登录授权 ==========
  checkLogin: function() {
    var userInfo = wx.getStorageSync('userInfo');
    if (userInfo && userInfo.nickName) {
      app.globalData.userInfo = userInfo;
      app.globalData.isLoggedIn = true;
      this.setData({ isLoggedIn: true, userInfo: userInfo });
    } else {
      wx.hideTabBar();
      app.globalData.isInAuthFlow = true;
      this.setData({ authStep: 1 });
    }
  },

  onTipsConfirm: function() {
    wx.hideTabBar();
    this.setData({ authStep: 2, avatarUrl: '', nickName: '' });
  },

  onChooseAvatar: function(e) {
    this.setData({ avatarUrl: e.detail.avatarUrl });
  },

  onNicknameInput: function(e) { this.setData({ nickName: e.detail.value }); },
  onNicknameBlur: function(e) { this.setData({ nickName: e.detail.value }); },

  onAuthDeny: function() {
    this.setData({ authStep: 1, avatarUrl: '', nickName: '' });
  },

  onAuthAllow: function() {
    var that = this;
    if (!this.data.avatarUrl || !this.data.nickName) {
      wx.showToast({ title: '请先点击上方获取微信头像和昵称', icon: 'none' }); return;
    }
    wx.login({
      success: function(loginRes) {
        console.log('-=-=-=-=loginRes-=-=-=', loginRes);
        var userInfo = { avatarUrl: that.data.avatarUrl, nickName: that.data.nickName };
        request.post('/user/user/login', { code: loginRes.code }).then(function(res) {
          console.log('后端登录成功：', res);
          if (res.data && res.data.token) {
            wx.setStorageSync('token', res.data.token);
          }
          app.globalData.userInfo = userInfo;
          app.globalData.isLoggedIn = true;
          wx.setStorageSync('userInfo', userInfo);
          wx.setStorageSync('code', loginRes.code);
          that.setData({ authStep: 0, isLoggedIn: true, userInfo: userInfo });
          that.loadBanner();
          that.loadHousekeeper();
          that.loadFeed();
          that.loadCoupon();
          wx.showToast({ title: '登录成功', icon: 'success' });
          app.globalData.isInAuthFlow = false;
        }).catch(function(err) {
          console.log('后端登录失败：', err);
          wx.showToast({ title: '登录失败，请重试', icon: 'none' });
        });
      },
      fail: function(err) {
        console.log('wx.login失败：', err);
        wx.showToast({ title: '微信授权失败，请重试', icon: 'none' });
      }
    });
  }
});
