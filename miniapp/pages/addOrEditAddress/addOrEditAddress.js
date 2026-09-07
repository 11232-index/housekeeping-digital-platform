var request = require('../../utils/request.js');
var util = require('../../utils/util.js');

Page({
  data: {
    isEdit: false,
    editId: '',
    region: ['', '', ''],
    form: {
      consignee: '',
      phone: '',
      sex: '1',
      provinceCode: '',
      provinceName: '',
      cityCode: '',
      cityName: '',
      districtCode: '',
      districtName: '',
      detail: '',
      label: '2'
    },
    labels: [
      { value: '1', name: '公司' },
      { value: '2', name: '家' },
      { value: '3', name: '学校' }
    ],
    genders: [
      { value: '1', name: '先生' },
      { value: '0', name: '女士' }
    ],
    submitting: false
  },

  onLoad: function(options) {
    if (options && options.type === 'edit' && options.id) {
      this.setData({ isEdit: true, editId: options.id });
      this.loadAddress(options.id);
    }
  },

  // ========== 加载地址详情（编辑模式） ==========
  loadAddress: function(id) {
    var that = this;
    util.showLoading('加载中...');
    request.get('/user/address-book/' + id).then(function(res) {
      util.hideLoading();
      var addr = res.data;
      if (addr) {
        that.setData({
          form: {
            consignee: addr.consignee || '',
            phone: addr.phone || '',
            sex: addr.sex !== null && addr.sex !== undefined ? String(addr.sex) : '1',
            provinceCode: addr.provinceCode || '',
            provinceName: addr.provinceName || '',
            cityCode: addr.cityCode || '',
            cityName: addr.cityName || '',
            districtCode: addr.districtCode || '',
            districtName: addr.districtName || '',
            detail: addr.detail || '',
            label: addr.label || '2',
            id: addr.id
          },
          region: [
            addr.provinceName || '',
            addr.cityName || '',
            addr.districtName || ''
          ]
        });
      }
    }).catch(function(err) {
      util.hideLoading();
      wx.showToast({ title: '加载地址失败', icon: 'none' });
    });
  },

  // ========== 表单输入 ==========
  onInput: function(e) {
    var field = e.currentTarget.dataset.field;
    var form = this.data.form;
    form[field] = e.detail.value;
    this.setData({ form: form });
  },

  // ========== 选择性别 ==========
  selectGender: function(e) {
    var form = this.data.form;
    form.sex = e.currentTarget.dataset.value;
    this.setData({ form: form });
  },

  // ========== 选择标签 ==========
  selectLabel: function(e) {
    var form = this.data.form;
    form.label = e.currentTarget.dataset.value;
    this.setData({ form: form });
  },

  // ========== 省市区选择 ==========
  onRegionChange: function(e) {
    var value = e.detail.value;  // ['广东省', '广州市', '天河区']
    var code = e.detail.code;    // ['440000', '440100', '440106']
    var form = this.data.form;
    form.provinceName = value[0] || '';
    form.provinceCode = code[0] || '';
    form.cityName = value[1] || '';
    form.cityCode = code[1] || '';
    form.districtName = value[2] || '';
    form.districtCode = code[2] || '';
    this.setData({ form: form, region: value });
  },

  // ========== 表单校验 ==========
  validateForm: function() {
    var form = this.data.form;
    if (!form.consignee || form.consignee.trim().length < 2) {
      wx.showToast({ title: '请输入正确的联系人姓名', icon: 'none' });
      return false;
    }
    if (!util.validatePhone(form.phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' });
      return false;
    }
    if (!form.detail || form.detail.trim().length < 1) {
      wx.showToast({ title: '请输入详细地址', icon: 'none' });
      return false;
    }
    return true;
  },

  // ========== 保存地址 ==========
  saveAddress: function() {
    var that = this;
    if (!this.validateForm()) return;
    if (this.data.submitting) return;
    this.setData({ submitting: true });

    var form = JSON.parse(JSON.stringify(this.data.form));
    // 清理 id 字段（新增时不应传 id）
    var saveData = {
      consignee: form.consignee.trim(),
      phone: form.phone.trim(),
      sex: form.sex,
      provinceCode: form.provinceCode,
      provinceName: form.provinceName,
      cityCode: form.cityCode,
      cityName: form.cityName,
      districtCode: form.districtCode,
      districtName: form.districtName,
      detail: form.detail.trim(),
      label: form.label
    };

    if (this.data.isEdit) {
      // 修改地址
      saveData.id = parseInt(this.data.editId);
      util.showLoading('保存中...');
      request.put('/user/address-book', saveData).then(function() {
        util.hideLoading();
        that.setData({ submitting: false });
        wx.showToast({ title: '修改成功', icon: 'success' });
        setTimeout(function() { wx.navigateBack(); }, 1000);
      }).catch(function(err) {
        util.hideLoading();
        that.setData({ submitting: false });
        wx.showToast({ title: err.message || '修改失败', icon: 'none' });
      });
    } else {
      // 新增地址
      util.showLoading('保存中...');
      request.post('/user/address-book', saveData).then(function() {
        util.hideLoading();
        that.setData({ submitting: false });
        wx.showToast({ title: '添加成功', icon: 'success' });
        setTimeout(function() { wx.navigateBack(); }, 1000);
      }).catch(function(err) {
        util.hideLoading();
        that.setData({ submitting: false });
        wx.showToast({ title: err.message || '添加失败', icon: 'none' });
      });
    }
  },

  // ========== 删除地址（编辑模式下） ==========
  deleteAddress: function() {
    if (this.data.submitting) return;
    var that = this;
    wx.showModal({
      title: '确认删除',
      content: '确定要删除该地址吗？',
      success: function(res) {
        if (res.confirm) {
          that.setData({ submitting: true });
          util.showLoading('删除中...');
          request.del('/user/address-book/' + that.data.editId).then(function() {
            that.setData({ submitting: false });
            util.hideLoading();
            wx.showToast({ title: '已删除', icon: 'success' });
            setTimeout(function() { wx.navigateBack(); }, 1000);
          }).catch(function(err) {
            that.setData({ submitting: false });
            util.hideLoading();
            wx.showToast({ title: err.message || '删除失败', icon: 'none' });
          });
        }
      }
    });
  },

  // ========== 返回 ==========
  goBack: function() {
    wx.navigateBack();
  }
});
