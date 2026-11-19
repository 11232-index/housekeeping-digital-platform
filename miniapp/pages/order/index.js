var app = getApp();
var request = require('../../utils/request.js');
var util = require('../../utils/util.js');

// 时间选项
var TIME_OPTIONS = [
  '8:00~9:00', '9:00~10:00', '10:00~11:00', '11:00~12:00',
  '14:00~15:00', '15:00~16:00', '16:00~17:00', '17:00~18:00'
];
var DURATION_OPTIONS = ['2小时', '4小时', '半天', '全天'];

Page({
  data: {
    address: null, cartList: [], totalPrice: 0, totalCount: 0,
    contactName: '', phone: '',
    scheduleDate: '', scheduleTime: '', serviceDuration: '',
    minDate: '', maxDate: '',
    remark: '', arrivalTime: '尽快',
    showRequirementPop: false, currentItemIndex: -1, currentRequirements: [],
    requirementsList: [], singleMode: false, hkMode: false,
    showTimePop: false, showDurationPop: false,
    submitting: false,
    // 优惠券（只有服务预约用得上；家政员预约不显示入口）
    showVoucherPop: false, usableVoucherList: [], selectedVoucher: null,
    originalPrice: '0.00', discountAmount: '0.00'
  },
  onShow: function() {
    if (!this.data.hkMode) {
      var dates = this.computeDateRange();
      this.setData({ minDate: dates.min, maxDate: dates.max });
      // 如果当前选中日期早于明天，重置为明天
      if (this.data.scheduleDate < dates.min) {
        this.setData({ scheduleDate: dates.min });
      }
    }
  },

  onLoad: function(options) {
    var dates = this.computeDateRange();
    // 非家政员模式默认选中明天
    if (!options || !options.housekeeperId) {
      this.setData({ minDate: dates.min, maxDate: dates.max, scheduleDate: dates.min });
    } else {
      this.setData({ minDate: dates.min, maxDate: dates.max });
    }
    if (options && options.address) {
      try { this.setData({ address: JSON.parse(decodeURIComponent(options.address)) }); } catch(e) {}
    }
    if (options && options.remark) { this.setData({ remark: decodeURIComponent(options.remark) }); }
    // 家政员预约模式
    if (options && options.housekeeperId) {
      this.setData({ hkMode: true });
      if (options.scheduleDate) this.setData({ scheduleDate: options.scheduleDate });
      if (options.scheduleTime) this.setData({ scheduleTime: decodeURIComponent(options.scheduleTime) });
      this.loadHousekeeper(options.housekeeperId, options.scheduleId);
    } else if (options && options.single === '1') {
      this.loadSingleItem();
      this.loadRequirementsFromStorage();
    } else {
      this.loadCart();
      this.loadRequirementsFromStorage();
    }
    this.loadDefaultAddress();
    // 服务预约才加载可用优惠券（家政员预约用不了券，入口也不显示）
    if (!this.data.hkMode) {
      this.loadUsableVouchers();
    }
  },
  onShow: function() {
    if (!this.data.singleMode && !this.data.hkMode) { this.loadCart(); }
    var addr = wx.getStorageSync('selectedAddress');
    if (addr) {
      this.setData({ address: addr });
      // 选地址后自动填充联系人和电话
      var update = { address: addr };
      if (addr.phone) update.phone = addr.phone;
      if (addr.consignee) update.contactName = addr.consignee;
      this.setData(update);
      wx.removeStorageSync('selectedAddress');
    }
    var remark = wx.getStorageSync('orderRemark');
    if (remark) { this.setData({ remark: remark }); wx.removeStorageSync('orderRemark'); }
  },

  // ========== 加载默认地址 ==========
  loadDefaultAddress: function() {
    var that = this;
    if (this.data.address) return; // 已有地址就不加载
    request.get('/user/address-book/default').then(function(res) {
      if (res.data) {
        that.setData({ address: res.data });
        if (res.data.phone) that.setData({ phone: res.data.phone });
        if (res.data.consignee) that.setData({ contactName: res.data.consignee });
      }
    }).catch(function() {});
  },

  // 家政员预约模式：加载家政员信息
  loadHousekeeper: function(housekeeperId, scheduleId) {
    var that = this;
    request.get('/user/housekeeper/' + housekeeperId).then(function(res) {
      var hk = res.data;
      that.setData({
        cartList: [{
          id: hk.id, cartKey: 'hk_' + hk.id, type: 'housekeeper',
          name: hk.name, image: hk.picture, price: hk.price, quantity: 1,
          requirement: '', hkScheduleId: scheduleId
        }],
        totalPrice: hk.price, totalCount: 1
      });
    }).catch(function() {});
  },

  // ========== 联系人/电话输入 ==========
  onContactInput: function(e) { this.setData({ contactName: e.detail.value }); },
  onPhoneInput: function(e) { this.setData({ phone: e.detail.value }); },

  // ========== picker弹窗（复用原生picker） ==========
  onPickDate: function(e) {
    var val = e.detail.value;
    // 兜底：确保不会选越界日期
    var dates = this.computeDateRange();
    if (val < dates.min) val = dates.min;
    if (val > dates.max) val = dates.max;
    this.setData({ scheduleDate: val });
  },
  onPickTime: function() { this.setData({ showTimePop: true }); },
  onPickDuration: function() { this.setData({ showDurationPop: true }); },
  closeTimePop: function() { this.setData({ showTimePop: false }); },
  closeDurationPop: function() { this.setData({ showDurationPop: false }); },
  onSelectTime: function(e) {
    var idx = e.currentTarget.dataset.index;
    this.setData({ scheduleTime: TIME_OPTIONS[idx], showTimePop: false });
  },
  onSelectDuration: function(e) {
    var idx = e.currentTarget.dataset.index;
    this.setData({ serviceDuration: DURATION_OPTIONS[idx], showDurationPop: false });
  },

  // ========== 优惠券（仅服务预约可用） ==========
  onPickVoucher: function() {
    if (this.data.usableVoucherList.length === 0) {
      wx.showToast({ title: '暂无可用优惠券', icon: 'none' });
      return;
    }
    this.setData({ showVoucherPop: true });
  },

  closeVoucherPop: function() { this.setData({ showVoucherPop: false }); },

  onSelectVoucher: function(e) {
    var idx = e.currentTarget.dataset.index;
    var list = this.data.usableVoucherList;
    if (!list || !list[idx]) return;
    // 一次只能选一张：直接替换
    this.setData({ selectedVoucher: list[idx], showVoucherPop: false });
    this.recalcTotal();
  },

  clearVoucher: function() {
    this.setData({ selectedVoucher: null, showVoucherPop: false });
    this.recalcTotal();
  },

  /**
   * 重算合计：选了券就按「原价 × 折扣率 ÷ 100」算折后价，并算出优惠了多少。
   * 算法与后端 OrderServiceImpl#applyVoucher 一致，都保留两位小数。
   */
  recalcTotal: function() {
    var original = parseFloat(this.data.originalPrice) || 0;
    var voucher = this.data.selectedVoucher;
    if (!voucher) {
      this.setData({ totalPrice: original.toFixed(2), discountAmount: '0.00' });
      return;
    }
    var rate = voucher.actualValue || 100;
    var finalAmount = Math.round(original * rate) / 100;
    this.setData({
      totalPrice: finalAmount.toFixed(2),
      discountAmount: (original - finalAmount).toFixed(2)
    });
  },

  /**
   * 加载「我的优惠券」里未使用的那部分供下单选择。
   * 已使用的券不在这里出现（用户要求选券时不给选已使用的）。
   */
  loadUsableVouchers: function() {
    var that = this;
    request.get('/user/voucher/mine').then(function(res) {
      var list = res.data || [];
      var usable = [];
      for (var i = 0; i < list.length; i++) {
        if (list[i].status === 0) {
          // 顺带把折扣率转成「8折」这种好显示的文案
          list[i].discountText = ((list[i].actualValue || 100) / 10) + '折';
          usable.push(list[i]);
        }
      }
      that.setData({ usableVoucherList: usable });
    }).catch(function() { /* 券拉不到不影响下单，按不用券处理 */ });
  },
  // ========== 加载购物车 ==========
  loadCart: function() {
    var that = this;
    request.get('/user/shopping-cart/list').then(function(res) {
      var backendCartList = res.data || [];
      var cartList = that.convertBackendCartToFrontend(backendCartList);
      wx.setStorageSync('cartList', cartList);
      var tp = 0, tc = 0;
      cartList.forEach(function(item) { tc += item.quantity || 0; tp += (item.price || 0) * (item.quantity || 0); });
      that.setData({ cartList: cartList, originalPrice: tp.toFixed(2), totalCount: tc });
      that.recalcTotal();
    }).catch(function(err) {
      console.log('加载预约清单失败，使用本地缓存', err);
      var cartList = wx.getStorageSync('cartList') || [];
      var tp = 0, tc = 0;
      cartList.forEach(function(item) { tc += item.quantity || 0; tp += (item.price || 0) * (item.quantity || 0); });
      that.setData({ cartList: cartList, originalPrice: tp.toFixed(2), totalCount: tc });
      that.recalcTotal();
    });
  },

  showRequirementPopByItem: function(index, item) {
    var reqs = item.requirements || [];
    if (reqs.length === 0) return;
    var existingSel = {};
    if (item.requirement) {
      var parts = item.requirement.split(',');
      parts.forEach(function(part) {
        var ci = part.indexOf(':');
        if (ci > 0) existingSel[part.substring(0, ci)] = part.substring(ci + 1);
      });
    }
    this.setData({
      showRequirementPop: true,
      currentItemIndex: index,
      currentRequirements: reqs.map(function(r) {
        var options = [];
        try { options = JSON.parse(r.value); } catch(er) { options = []; }
        if (!Array.isArray(options)) options = [];
        return { name: r.name, options: options, selected: existingSel[r.name] || '' };
      })
    });
  },

  convertBackendCartToFrontend: function(backendList) {
    var frontendList = [];
    backendList.forEach(function(item) {
      var type, serviceId;
      if (item.serviceItemId) { type = 'item'; serviceId = item.serviceItemId; }
      else if (item.servicePackageId) { type = 'package'; serviceId = item.servicePackageId; }
      else { return; }
      var selectedRequirements = [];
      if (item.requirement) {
        var parts = item.requirement.split(',');
        parts.forEach(function(part) {
          var colonIdx = part.indexOf(':');
          if (colonIdx > 0) { selectedRequirements.push({ name: part.substring(0, colonIdx), value: part.substring(colonIdx + 1) }); }
        });
      }
      var cartKey = type + '_' + serviceId + '_' + (item.requirement || '');
      var localRequirements = [];
      var localCart = wx.getStorageSync('cartList') || [];
      for (var j = 0; j < localCart.length; j++) {
        if (localCart[j].id === serviceId && localCart[j].type === type) { localRequirements = localCart[j].requirements || []; break; }
      }
      frontendList.push({ id: serviceId, cartKey: cartKey, type: type, name: item.name, image: item.image, price: item.amount, quantity: 1, requirement: item.requirement, selectedRequirements: selectedRequirements, requirements: localRequirements });
    });
    return frontendList;
  },

  loadSingleItem: function() {
    var raw = wx.getStorageSync('singleOrderItem');
    if (raw) {
      try {
        var item = JSON.parse(raw);
        item.quantity = 1;
        var cartList = [item];
        var tp = item.price || 0;
        this.setData({ singleMode: true, cartList: cartList, originalPrice: tp.toFixed(2), totalCount: 1 });
        this.recalcTotal();
      } catch(e) { this.loadCart(); }
      wx.removeStorageSync('singleOrderItem');
    } else { this.loadCart(); }
  },

  loadRequirementsFromStorage: function() {
    var raw = wx.getStorageSync('currentRequirements');
    if (raw) {
      try {
        var data = JSON.parse(raw);
        var list = data.requirements || [];
        var parsedList = list.map(function(r) {
          var options = [];
          try { options = JSON.parse(r.value); } catch(er) { options = []; }
          if (!Array.isArray(options)) options = [];
          return { name: r.name, options: options, selected: '' };
        });
        this.setData({ requirementsList: parsedList });
      } catch(e) { this.setData({ requirementsList: [] }); }
      wx.removeStorageSync('currentRequirements');
    } else { this.setData({ requirementsList: [] }); }
  },

  showRequirementPop: function(e) {
    var index = e.currentTarget.dataset.index;
    var item = this.data.cartList[index];
    this.showRequirementPopByItem(index, item);
  },

  closeRequirementPop: function() { this.setData({ showRequirementPop: false }); },

  onSelectRequirementOption: function(e) {
    var reqIndex = e.currentTarget.dataset.reqIndex;
    var optionIndex = e.currentTarget.dataset.optionIndex;
    var currentRequirements = this.data.currentRequirements;
    var req = currentRequirements[reqIndex];
    var opt = req.options[optionIndex];
    req.selected = req.selected === opt ? '' : opt;
    this.setData({ currentRequirements: currentRequirements });
  },

  onConfirmRequirement: function() {
    var currentRequirements = this.data.currentRequirements;
    var allSelected = currentRequirements.every(function(r) { return r.selected; });
    if (!allSelected) { wx.showToast({ title: '请选择全部服务需求', icon: 'none' }); return; }
    var reqStr = currentRequirements.map(function(r) { return r.name + ':' + r.selected; }).join(',');
    var cartList = this.data.cartList;
    var index = this.data.currentItemIndex;
    cartList[index].requirement = reqStr;
    cartList[index].selectedRequirements = currentRequirements.map(function(r) { return { name: r.name, value: r.selected }; });
    this.setData({ cartList: cartList, showRequirementPop: false });
  },

  onSelectRequirementOption2: function(e) {
    var parentIndex = e.currentTarget.dataset.parentIndex;
    var opt = e.currentTarget.dataset.option;
    var requirementsList = this.data.requirementsList;
    var group = requirementsList[parentIndex];
    group.selected = group.selected === opt ? '' : opt;
    this.setData({ requirementsList: requirementsList });
  },

  stopPropagation: function() {},

  // ========== 导航 ==========
  goAddress: function() { wx.navigateTo({ url: '/pages/address/address?from=order' }); },
  goRemark: function() { wx.navigateTo({ url: '/pages/remark/index?remark=' + encodeURIComponent(this.data.remark) }); },

  // ========== 提交 ==========
  submitOrder: function() {
    if (this.data.submitting) return;
    var that = this;
    if (!this.data.address) { wx.showToast({ title: '请选择服务地址', icon: 'none' }); return; }
    if (!this.data.contactName) { wx.showToast({ title: '请填写联系人', icon: 'none' }); return; }
    if (!this.data.phone) { wx.showToast({ title: '请填写联系电话', icon: 'none' }); return; }
    if (!this.data.scheduleDate) { wx.showToast({ title: '请选择预约日期', icon: 'none' }); return; }
    if (!this.data.scheduleTime) { wx.showToast({ title: '请选择预约时间', icon: 'none' }); return; }
    if (!this.data.serviceDuration) { wx.showToast({ title: '请选择服务时长', icon: 'none' }); return; }
    if (this.data.cartList.length === 0) { wx.showToast({ title: '预约清单为空', icon: 'none' }); return; }
    wx.showModal({
      title: '确认预约',
      content: '合计：¥' + this.data.totalPrice + '，确认提交？',
      success: function(res) {
        if (res.confirm) {
          that.setData({ submitting: true });
          util.showLoading('提交中...');
          var dto = {
            addressBookId: that.data.address.id,
            phone: that.data.phone,
            address: that.data.address ? (that.data.address.provinceName || '') + (that.data.address.cityName || '') + (that.data.address.districtName || '') + (that.data.address.detail || '') : '',
            contactName: that.data.contactName,
            scheduleDate: that.data.scheduleDate,
            scheduleTime: that.data.scheduleTime,
            serviceDuration: that.data.serviceDuration,
            remark: that.data.remark
          };
          // 服务预约且选了券才带上（家政员预约用不了券）
          if (!that.data.hkMode && that.data.selectedVoucher) {
            dto.voucherId = that.data.selectedVoucher.voucherId;
          }
          // 家政员模式：传 scheduleId
          if (that.data.hkMode && that.data.cartList.length === 1) {
            dto.housekeeperScheduleId = that.data.cartList[0].hkScheduleId;
          }
          // 单项模式：传 item；清单模式：不传
          var url = '/user/order/submit';
          if ((that.data.singleMode || that.data.hkMode) && that.data.cartList.length === 1) {
            var cartItem = that.data.cartList[0];
            dto.item = {
              housekeeperId: cartItem.type === 'housekeeper' ? cartItem.id : null,
              serviceItemId: cartItem.type === 'item' ? cartItem.id : null,
              servicePackageId: cartItem.type === 'package' ? cartItem.id : null,
              name: cartItem.name,
              image: cartItem.image,
              requirement: cartItem.requirement || '',
              amount: cartItem.price
            };
            url = '/user/order/submit-item';
          }
          request.post(url, dto).then(function(res) {
            util.hideLoading();
            wx.removeStorageSync('cartList');
            wx.redirectTo({ url: '/pages/pay/index?orderId=' + res.data.orderId });
          }).catch(function(err) {
            util.hideLoading();
            that.setData({ submitting: false });
            wx.showToast({ title: err.message || '提交失败', icon: 'none' });
          });
        }
      }
    });
  },

  computeDateRange: function() {
    var d = new Date();
    var y = d.getFullYear(), m = d.getMonth() + 1, day = d.getDate();
    // 明天
    var t = new Date(y, m - 1, day + 1);
    var min = t.getFullYear() + '-' + fmt(t.getMonth() + 1) + '-' + fmt(t.getDate());
    // 第7天
    var x = new Date(y, m - 1, day + 7);
    var max = x.getFullYear() + '-' + fmt(x.getMonth() + 1) + '-' + fmt(x.getDate());
    return { min: min, max: max };

    function fmt(n) { return n < 10 ? '0' + n : '' + n; }
  }
});
