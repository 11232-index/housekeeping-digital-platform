var request = require('../../utils/request.js');

/**
 * 延迟释放操作锁。
 * 不能立刻释放：列表刷新是异步的，刷新回来之前卡片上的按钮还在，
 * 用户再点一次就会把请求打到后端，报「该订单当前不可操作」这类状态错误。
 * 这里给刷新留一点时间再解锁，重复点击就会被 acting 拦下、弹出友好提示。
 */
function releaseActing(page) {
  setTimeout(function () {
    page.setData({ acting: false });
  }, 800);
}

Page({
  data: {
    tabs: ['待接单', '已接单', '服务中', '已完成'],
    statusMap: [2, 3, 4, 5],
    tabIndex: 0,
    orderList: [],
    page: 1, pageSize: 10, hasMore: true,
    loading: false,
    pendingCount: 0,
    // 操作防重复：同一时刻只允许一个订单操作在途
    acting: false,
    // 接单弹层（填写预计到达时间）
    acceptVisible: false,
    acceptOrderId: null,
    acceptDate: '',
    acceptTime: '',
    today: ''
  },

  onShow: function () {
    if (!wx.getStorageSync('workerToken')) {
      wx.redirectTo({ url: '/pages/workerLogin/workerLogin' });
      return;
    }
    this.setData({ today: this.formatDate(new Date()) });
    this.reload();
    this.startPolling();
  },

  onHide: function () { this.stopPolling(); },
  onUnload: function () { this.stopPolling(); },

  onPullDownRefresh: function () {
    this.reload(false, function () { wx.stopPullDownRefresh(); });
  },

  /**
   * 页面可见时每 30 秒轮询一次。
   * 小程序没有实时推送能力（退到后台约 5 秒 WebSocket 就被系统断掉），
   * 服务端也没有按家政员定向推送的通道，所以"收到新订单"只能靠主动拉取。
   */
  startPolling: function () {
    var that = this;
    this.stopPolling();
    this.pollTimer = setInterval(function () { that.reload(true); }, 30000);
  },

  stopPolling: function () {
    if (this.pollTimer) {
      clearInterval(this.pollTimer);
      this.pollTimer = null;
    }
  },

  reload: function (silent, cb) {
    this.setData({ page: 1, orderList: [], hasMore: true });
    this.loadOrders(false, silent, cb);
    this.loadPendingCount();
  },

  loadOrders: function (isLoadMore, silent, cb) {
    if (this.data.loading) return;
    var that = this;
    var page = isLoadMore ? this.data.page : 1;
    var params = { page: page, pageSize: this.data.pageSize };
    var s = this.data.statusMap[this.data.tabIndex];
    if (s != null) params.status = s;

    if (!silent) this.setData({ loading: true });
    request.get('/worker/order/page', params).then(function (res) {
      var newOrders = (res.data && res.data.length !== undefined) ? res.data : [];
      var orderList = isLoadMore ? that.data.orderList.concat(newOrders) : newOrders;
      that.setData({
        orderList: orderList,
        page: page + 1,
        hasMore: newOrders.length >= that.data.pageSize,
        loading: false
      });
      if (cb) cb();
    }).catch(function (err) {
      that.setData({ loading: false });
      if (err && err.message === 'Unauthorized') {
        wx.redirectTo({ url: '/pages/workerLogin/workerLogin' });
      }
      if (cb) cb();
    });
  },

  loadPendingCount: function () {
    var that = this;
    request.get('/worker/order/pending-count').then(function (res) {
      that.setData({ pendingCount: res.data || 0 });
    }).catch(function () { /* 角标拉取失败不影响列表 */ });
  },

  loadMore: function () {
    if (!this.data.hasMore) return;
    this.loadOrders(true);
  },

  changeTab: function (e) {
    var idx = e.currentTarget.dataset.index;
    this.setData({ tabIndex: idx, page: 1, orderList: [], hasMore: true });
    this.loadOrders();
  },

  goDetail: function (e) {
    var id = e.currentTarget.dataset.id;
    if (!id) return;
    wx.navigateTo({ url: '/pages/workerOrderDetail/workerOrderDetail?id=' + id });
  },

  // ========== 接单（需填写预计到达时间） ==========
  openAccept: function (e) {
    var id = e.currentTarget.dataset.id;
    var now = new Date();
    this.setData({
      acceptVisible: true,
      acceptOrderId: id,
      acceptDate: this.formatDate(now),
      acceptTime: this.formatTime(now),
      today: this.formatDate(now)
    });
  },

  closeAccept: function () {
    this.setData({ acceptVisible: false, acceptOrderId: null });
  },

  onAcceptDate: function (e) {
    this.setData({ acceptDate: e.detail.value });
  },

  onAcceptTime: function (e) {
    this.setData({ acceptTime: e.detail.value });
  },

  confirmAccept: function () {
    if (this.data.acting) {
      wx.showToast({ title: '正在接单，请稍候', icon: 'none' });
      return;
    }
    if (!this.data.acceptDate || !this.data.acceptTime) {
      wx.showToast({ title: '请选择预计到达时间', icon: 'none' });
      return;
    }
    var that = this;
    var estimatedArrivalTime = this.data.acceptDate + ' ' + this.data.acceptTime;

    this.setData({ acting: true });
    wx.showLoading({ title: '处理中...' });
    request.post('/worker/order/' + this.data.acceptOrderId + '/accept', {
      estimatedArrivalTime: estimatedArrivalTime
    }).then(function () {
      wx.hideLoading();
      that.setData({ acceptVisible: false, acceptOrderId: null });
      wx.showToast({ title: '接单成功', icon: 'success' });
      that.reload();
      releaseActing(that);
    }).catch(function (err) {
      wx.hideLoading();
      wx.showToast({ title: (err && err.message) || '接单失败', icon: 'none' });
      that.reload();
      releaseActing(that);
    });
  },

  // ========== 确认到达（已接单 → 服务中） ==========
  arriveOrder: function (e) {
    this.confirmThenPost(e, '确认到达', '确认已到达客户处并开始服务吗？', 'arrive', '已开始服务');
  },

  // ========== 完成服务（服务中 → 已完成） ==========
  completeOrder: function (e) {
    this.confirmThenPost(e, '完成服务', '确认本次服务已完成吗？', 'complete', '已完成');
  },

  /**
   * 「二次确认 → POST」的公共流程，三种订单操作共用。
   * acting 标志保证同一时刻只有一个操作在途，重复点击直接提示、不再发请求。
   */
  confirmThenPost: function (e, title, content, action, successText) {
    if (this.data.acting) {
      wx.showToast({ title: '正在' + title + '，请稍候', icon: 'none' });
      return;
    }
    var that = this;
    var id = e.currentTarget.dataset.id;
    wx.showModal({
      title: title,
      content: content,
      success: function (res) {
        if (!res.confirm) return;
        that.setData({ acting: true });
        wx.showLoading({ title: '处理中...' });
        request.post('/worker/order/' + id + '/' + action).then(function () {
          wx.hideLoading();
          wx.showToast({ title: successText, icon: 'success' });
          that.reload();
          releaseActing(that);
        }).catch(function (err) {
          wx.hideLoading();
          wx.showToast({ title: (err && err.message) || '操作失败', icon: 'none' });
          that.reload();
          releaseActing(that);
        });
      }
    });
  },

  // ========== 不接单（待接单） ==========
  rejectOrder: function (e) {
    var id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: '/pages/workerReject/workerReject?orderId=' + id });
  },

  // ========== 无法服务（已接单 / 服务中） ==========
  cannotServe: function (e) {
    var id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: '/pages/workerReject/workerReject?orderId=' + id + '&mode=cannotServe' });
  },

  // 阻止弹层内部点击穿透到遮罩
  noop: function () {},

  formatDate: function (d) {
    var m = d.getMonth() + 1, day = d.getDate();
    return d.getFullYear() + '-' + (m < 10 ? '0' + m : m) + '-' + (day < 10 ? '0' + day : day);
  },

  formatTime: function (d) {
    var h = d.getHours(), mi = d.getMinutes();
    return (h < 10 ? '0' + h : h) + ':' + (mi < 10 ? '0' + mi : mi);
  }
});
