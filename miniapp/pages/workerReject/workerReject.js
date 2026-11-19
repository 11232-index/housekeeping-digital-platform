var request = require('../../utils/request.js');

// 两种模式共用本页：
//   reject       —— 待接单时「不接单」，订单退回待接单等重新指派
//   cannotServe  —— 已接单/服务中时「无法服务」，订单转待退款
var REASONS_REJECT = ['时间冲突，已有其他安排', '距离太远，无法上门', '技能不匹配', '身体状况不佳', '其他原因'];
var REASONS_CANNOT_SERVE = ['突发身体不适', '前一位客户延时', '交通原因无法准时到达', '工具/材料缺失', '其他原因'];

Page({
  data: {
    orderId: '',
    mode: 'reject',
    pageTitle: '不接单原因',
    confirmText: '确认不接单',
    reasonPlaceholder: '可填写更具体的原因',
    tipText: '提交后订单将退回待接单，由工作人员重新安排',
    reasons: REASONS_REJECT,
    selectedReason: '',
    customReason: '',
    submitting: false
  },

  onLoad: function(options) {
    if (options && options.orderId) {
      this.setData({ orderId: options.orderId });
    }
    if (options && options.mode === 'cannotServe') {
      this.setData({
        mode: 'cannotServe',
        pageTitle: '无法服务原因',
        confirmText: '确认无法服务',
        tipText: '提交后订单将进入待退款，由工作人员处理退款',
        reasons: REASONS_CANNOT_SERVE
      });
      wx.setNavigationBarTitle({ title: '无法服务原因' });
    }
  },

  onSelectReason: function(e) {
    var reason = e.currentTarget.dataset.reason;
    this.setData({ selectedReason: this.data.selectedReason === reason ? '' : reason });
  },

  onInput: function(e) {
    this.setData({ customReason: e.detail.value });
  },

  onCancel: function() {
    wx.navigateBack();
  },

  onConfirm: function() {
    if (this.data.submitting) {
      wx.showToast({ title: '正在提交，请稍候', icon: 'none' });
      return;
    }
    // 拼接原因：选中原因 + 自定义原因（后端要求必填）
    var parts = [];
    if (this.data.selectedReason) parts.push(this.data.selectedReason);
    if (this.data.customReason && this.data.customReason.trim()) parts.push(this.data.customReason.trim());
    var reason = parts.join('；');
    if (!reason) {
      wx.showToast({ title: '请选择或填写原因', icon: 'none' });
      return;
    }

    var that = this;
    var isCannotServe = this.data.mode === 'cannotServe';
    var url = '/worker/order/' + this.data.orderId + (isCannotServe ? '/cannot-serve' : '/reject');

    this.setData({ submitting: true });
    wx.showLoading({ title: '提交中...' });
    request.post(url, { reason: reason }).then(function() {
      wx.hideLoading();
      that.setData({ submitting: false });
      wx.showToast({ title: isCannotServe ? '已提交，订单转入待退款' : '已提交，等待重新安排', icon: 'none', duration: 1500 });
      setTimeout(function() {
        wx.navigateBack();
      }, 1500);
    }).catch(function(err) {
      wx.hideLoading();
      that.setData({ submitting: false });
      wx.showToast({ title: (err && err.message) || '提交失败', icon: 'none' });
    });
  }
});
