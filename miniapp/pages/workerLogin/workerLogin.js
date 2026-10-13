var request = require('../../utils/request.js');
var util = require('../../utils/util.js');

Page({
  data: { username: '', password: '', submitting: false },

  onInput: function(e) {
    var field = e.currentTarget.dataset.field;
    var data = {};
    data[field] = e.detail.value;
    this.setData(data);
  },

  doLogin: function() {
    var that = this;
    var username = (this.data.username || '').trim();
    var password = this.data.password || '';

    if (!username) {
      wx.showToast({ title: '请输入账号', icon: 'none' });
      return;
    }
    if (!password) {
      wx.showToast({ title: '请输入密码', icon: 'none' });
      return;
    }
    if (this.data.submitting) return;
    this.setData({ submitting: true });
    util.showLoading('登录中...');

    request.post('/worker/auth/login', { username: username, password: password }).then(function(res) {
      util.hideLoading();
      that.setData({ submitting: false });
      var data = res.data;
      wx.setStorageSync('workerToken', data.token);
      wx.setStorageSync('workerInfo', {
        employeeId: data.employeeId,
        housekeeperId: data.housekeeperId,
        name: data.name,
        picture: data.picture
      });
      wx.showToast({ title: '登录成功', icon: 'success' });
      setTimeout(function() {
        wx.redirectTo({ url: '/pages/workerHome/workerHome' });
      }, 800);
    }).catch(function(err) {
      util.hideLoading();
      that.setData({ submitting: false });
      wx.showToast({ title: err.message || '登录失败', icon: 'none' });
    });
  }
});
