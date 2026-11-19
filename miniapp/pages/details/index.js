var request = require('../../utils/request.js');
var util = require('../../utils/util.js');

Page({
  data: { orderId: '', order: {}, statusText: '', items: [], acting: false, housekeeperPhone: '', housekeeperName: '', countdown: '' },

  onShow: function() {
    this.setData({ acting: false });
    if (this.data.orderId) {
      this.loadOrderDetail(this.data.orderId);
    }
  },

  onLoad: function(options) {
    if (options && options.id) {
      this.setData({ orderId: options.id });
      this.loadOrderDetail(options.id);
    }
  },

  loadOrderDetail: function(orderId) {
    var that = this;
    util.showLoading('加载中...');
    request.get('/user/order/' + orderId).then(function(res) {
      util.hideLoading();
      var order = res.data;
      var items = order.orderDetailList || [];
      that.setData({
        order: order,
        statusText: util.getOrderStatusText(order.status),
        items: items
      });
      // 待付款订单显示支付倒计时
      that.startCountdown(order);
      // 如果有家政员id，查家政员电话
      for (var i = 0; i < items.length; i++) {
        if (items[i].housekeeperId) {
          that.loadHousekeeperPhone(items[i].housekeeperId);
          break;
        }
      }
    }).catch(function(err) {
      util.hideLoading();
      wx.showToast({ title: err.message || '加载失败', icon: 'none' });
    });
  },

  /**
   * 待付款订单的支付倒计时。
   * 截止时间 = 下单时间 + 15 分钟（与后端 OrderTask.UNPAID_TIMEOUT_MINUTES 保持一致）。
   * 归零后就停在 00:00，不再重新计时 —— 即使后端因为某些原因没自动取消，
   * 界面上也不会莫名其妙又从 15 分钟开始倒数。
   */
  startCountdown: function(order) {
    this.stopCountdown();
    if (!order || order.status !== 1 || !order.orderTime) {
      this.setData({ countdown: '' });
      return;
    }
    var PAY_TIMEOUT_MS = 15 * 60 * 1000;
    var deadline = new Date(String(order.orderTime).replace(/-/g, '/')).getTime() + PAY_TIMEOUT_MS;
    var that = this;
    var tick = function() {
      var left = deadline - Date.now();
      if (left < 0) left = 0;
      that.setData({ countdown: that.formatCountdown(left) });
    };
    tick();
    this.countdownTimer = setInterval(tick, 1000);
  },

  stopCountdown: function() {
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer);
      this.countdownTimer = null;
    }
  },

  formatCountdown: function(ms) {
    var total = Math.floor(ms / 1000);
    var m = Math.floor(total / 60);
    var s = total % 60;
    return (m < 10 ? '0' + m : m) + ':' + (s < 10 ? '0' + s : s);
  },

  onUnload: function() {
    this.stopCountdown();
  },

  loadHousekeeperPhone: function(housekeeperId) {
    var that = this;
    request.get('/user/housekeeper/' + housekeeperId + '/employee').then(function(res) {
      var emp = res.data;
      if (emp && emp.phone) {
        that.setData({ housekeeperPhone: emp.phone, housekeeperName: emp.name || '' });
      }
    }).catch(function() {});
  },

  handlePay: function() {
    if (this.data.acting) return;
    this.setData({ acting: true });
    wx.navigateTo({ url: '/pages/pay/index?orderId=' + this.data.orderId });
  },

  handleCancel: function() {
    if (this.data.acting) return;
    var that = this;
    this.setData({ acting: true });
    wx.showModal({
      title: '取消订单',
      content: '确定要取消该订单吗？',
      success: function(res) {
        if (res.confirm) {
          that.setData({ acting: false });
          wx.navigateTo({ url: '/pages/cancel-reason/index?orderId=' + that.data.orderId + '&from=detail' });
        } else {
          that.setData({ acting: false });
        }
      }
    });
  },

  handleReminder: function() {
    if (this.data.acting) return;
    var that = this;
    this.setData({ acting: true });
    request.post('/user/order/' + this.data.orderId + '/remind').then(function(res) {
      that.setData({ acting: false });
      wx.showToast({ title: '已催单', icon: 'success' });
    }).catch(function(err) {
      that.setData({ acting: false });
      wx.showToast({ title: err.message || '催单失败', icon: 'none' });
    });
  },

  /**
   * 再来一单：
   * - 服务项目/服务套餐订单：把明细逐条加入预约清单（后端会校验下架状态，并按名称去重）
   * - 家政员订单：明细没有服务 id，无法直接复购，跳到家政员详情页重新选择可约时段
   */
  oneMoreOrder: function() {
    if (this.data.acting) return;
    var that = this;
    var items = this.data.items || [];
    var serviceItems = [];
    var seen = {};
    var housekeeperId = (items.length > 0 && items[0].housekeeperId) || this.data.order.housekeeperId || null;

    // 订单明细 → 购物车入参（同一服务只加一次）
    for (var i = 0; i < items.length; i++) {
      var item = items[i];
      var dto = null;
      var key = null;
      if (item.serviceItemId) {
        dto = { serviceItemId: item.serviceItemId };
        key = 'item_' + item.serviceItemId;
      } else if (item.servicePackageId) {
        dto = { servicePackageId: item.servicePackageId };
        key = 'package_' + item.servicePackageId;
      } else {
        continue;
      }
      if (seen[key]) continue;
      seen[key] = true;
      if (item.requirement) {
        dto.requirement = item.requirement;
      }
      serviceItems.push({ dto: dto, name: item.name || '该服务' });
    }

    // 家政员订单：没有服务项目/套餐，去家政员页重新选时段
    if (serviceItems.length === 0) {
      if (housekeeperId) {
        wx.navigateTo({ url: '/pages/housekeeper-detail/housekeeper-detail?id=' + housekeeperId });
      } else {
        wx.showToast({ title: '该订单暂不支持再来一单', icon: 'none' });
      }
      return;
    }

    this.setData({ acting: true });
    util.showLoading('正在加入预约清单...');

    var index = 0;
    var added = 0;
    var exists = 0;
    var failMessages = [];
    var addNext = function() {
      if (index >= serviceItems.length) {
        util.hideLoading();
        that.setData({ acting: false });
        that.showReorderResult(added, exists, failMessages);
        return;
      }
      var current = serviceItems[index++];
      request.post('/user/shopping-cart/add', current.dto).then(function() {
        added++;
        addNext();
      }).catch(function(err) {
        var msg = (err && err.message) || '加入失败';
        // 清单里已有同名服务不算失败
        if (msg.indexOf('请勿重复') !== -1 || msg.indexOf('已加入相同') !== -1) {
          exists++;
        } else {
          failMessages.push('「' + current.name + '」' + msg);
        }
        addNext();
      });
    };
    addNext();
  },

  /**
   * 再来一单结果提示：成功/已存在都引导去预约清单，全部失败则只提示原因
   */
  showReorderResult: function(added, exists, failMessages) {
    var total = added + exists;
    if (total > 0) {
      var parts = [];
      if (added > 0) {
        parts.push('成功加入 ' + added + ' 项服务');
      }
      if (exists > 0) {
        parts.push(exists + ' 项已在清单中');
      }
      var content = parts.join('，');
      if (failMessages.length > 0) {
        content += '；未能加入：' + failMessages.join('；');
      }
      content += '。是否前往预约清单？';
      wx.showModal({
        title: '已加入预约清单',
        content: content,
        confirmText: '去清单',
        cancelText: '继续浏览',
        success: function(res) {
          if (res.confirm) {
            wx.switchTab({ url: '/pages/cart/cart' });
          }
        }
      });
      return;
    }
    if (failMessages.length > 0) {
      wx.showModal({ title: '无法再来一单', content: failMessages.join('；'), showCancel: false });
      return;
    }
    wx.showToast({ title: '该订单暂无可复购的服务', icon: 'none' });
  },

  handlePhone: function() {
    wx.makePhoneCall({ phoneNumber: '400-888-6688' });
  },

  handleCallHousekeeper: function() {
    wx.makePhoneCall({ phoneNumber: this.data.housekeeperPhone });
  }
});
