// 智能家政服务 - 小程序入口
App({
  onLaunch: function () {
    console.log('智能家政小程序启动');
    this.globalData._isColdStart = true;
    const systemInfo = wx.getSystemInfoSync();
    this.globalData.systemInfo = systemInfo;
    this.globalData.statusBarHeight = systemInfo.statusBarHeight;
    const menuButtonInfo = wx.getMenuButtonBoundingClientRect();
    this.globalData.navBarHeight = (menuButtonInfo.top - systemInfo.statusBarHeight) * 2 + menuButtonInfo.height;
  },

  onShow: function () {
    console.log('App Show');
    // 冷启动跳过（首页会自动弹授权），仅暖启动时强制跳首页
    if (this.globalData._isColdStart) {
      this.globalData._isColdStart = false;
      return;
    }
    // 如果正在授权流程中（选择头像/昵称），不要打断
    if (this.globalData.isInAuthFlow) return;
    var userInfo = wx.getStorageSync('userInfo');
    if ((!userInfo || !userInfo.nickName) && !this.globalData._redirecting) {
      this.globalData._redirecting = true;
      var app = this;
      wx.reLaunch({ url: '/pages/index/index' });
      setTimeout(function() { app.globalData._redirecting = false; }, 2000);
    }
  },

  onHide: function () {
    console.log('App Hide')
  },

  globalData: {
    baseUrl: 'http://localhost:8080',
    systemInfo: null,
    statusBarHeight: 0,
    navBarHeight: 44,
    token: '',
    userInfo: null,
    shopStatus: 1,
    isInAuthFlow: false,
    needReAuth: false,
    _isColdStart: false,
    _redirecting: false
  }
})
