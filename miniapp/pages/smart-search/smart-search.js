// 智能搜索是独立服务（端口 5001）
// 不写死 IP：跟随 app.js 的 aiBaseUrl（开发者工具用 127.0.0.1，真机用电脑热点 IP），只把端口换成 5001
var _app = getApp();
var request = require('../../utils/request.js');
var AI_BASE = (_app && _app.globalData && _app.globalData.aiBaseUrl) || 'http://127.0.0.1:5000';
var SMART_SEARCH_BASE_URL = AI_BASE.replace(/:\d+$/, ':5001');

Page({
  data: {
    query: '',
    searched: false,
    loading: false,
    error: '',
    answer: '',
    results: [],
    cartList: [],
    adding: false,
    showDetailPop: false,
    detailService: {},
    showSpecPop: false,
    currentService: {},
    currentRequirements: [],
    examples: [
      '适合养宠物的保洁',
      '会做饭的家政员',
      '家里有老人需要陪护',
      '深度清洁、除螨'
    ]
  },

  onLoad: function() {
    this.loadCartFromBackend();
  },

  onInput: function(e) {
    this.setData({
      query: e.detail.value || '',
      error: ''
    });
  },

  onExampleTap: function(e) {
    this.setData({
      query: e.currentTarget.dataset.text || '',
      searched: false,
      error: '',
      answer: '',
      results: []
    });
  },

  onSearch: function() {
    var that = this;
    var query = (this.data.query || '').trim();
    if (!query) {
      wx.showToast({ title: '请先描述你的需求', icon: 'none' });
      return;
    }
    if (this.data.loading) return;

    this.setData({
      searched: true,
      loading: true,
      error: '',
      answer: '',
      results: []
    });

    wx.request({
      url: SMART_SEARCH_BASE_URL + '/api/smart-search/search',
      method: 'POST',
      header: { 'Content-Type': 'application/json' },
      data: { query: query },
      timeout: 60000,
      success: function(res) {
        var data = res.data || {};
        if (res.statusCode === 200 && data.ok !== false) {
          var results = (data.results || []).map(function(item) {
            // 智能搜索的 service 对应购物车里的 item
            if (item.type === 'service') item.cartType = 'item';
            else if (item.type === 'package') item.cartType = 'package';
            return item;
          });
          that.setData({
            loading: false,
            answer: data.answer || '',
            results: results
          });
        } else {
          that.setData({
            loading: false,
            error: data.answer || data.error || '搜索失败，请稍后重试'
          });
        }
      },
      fail: function() {
        that.setData({
          loading: false,
          error: '无法连接智能搜索服务，请确认服务已启动'
        });
      }
    });
  },

  onResultTap: function(e) {
    var item = e.currentTarget.dataset.item || {};
    if (item.type === 'housekeeper') {
      wx.navigateTo({
        url: '/pages/housekeeper-detail/housekeeper-detail?id=' + item.id
      });
      return;
    }
    // 服务/套餐：和“服务预约”一样弹出详情窗
    this.setData({ showDetailPop: true, detailService: item });
  },

  closeDetail: function() { this.setData({ showDetailPop: false }); },
  closeSpecPop: function() { this.setData({ showSpecPop: false }); },
  stopPropagation: function() {},

  addToCart: function(e) {
    if (this.data.adding) return;
    var service = e.currentTarget.dataset.item || {};
    if (service.type === 'housekeeper') return;
    var that = this;
    that.setData({ adding: true });
    // 先检查营业状态
    request.get('/admin/shop/getStatus').then(function(res) {
      if (res.data === 0) {
        that.setData({ adding: false });
        wx.showToast({ title: '已经休息了哟，暂时无法预约', icon: 'none' });
        return;
      }
      // 检查是否已加入
      var cartList = that.data.cartList;
      for (var i = 0; i < cartList.length; i++) {
        if (cartList[i].name === service.name) {
          that.setData({ adding: false });
          wx.showToast({ title: '已加入相同的服务，请勿重复添加', icon: 'none' });
          return;
        }
      }
      // 有需求 → 弹窗选择；无需求 → 直接加入
      if (service.requirements && service.requirements.length > 0) {
        that.setData({ adding: false });
        var reqs = JSON.parse(JSON.stringify(service.requirements));
        reqs.forEach(function(r) {
          try { r.options = JSON.parse(r.value); } catch (err) { r.options = []; }
          r.selected = '';
        });
        that.setData({
          currentService: service,
          currentRequirements: reqs,
          showSpecPop: true,
          showDetailPop: false
        });
      } else {
        that.doAddToCart(service, '');
      }
    }).catch(function() {
      that.setData({ adding: false });
      wx.showToast({ title: '网络异常', icon: 'none' });
    });
  },

  doAddToCart: function(service, requirementStr) {
    var that = this;
    var cartType = service.cartType || (service.type === 'service' ? 'item' : service.type);
    var cartList = JSON.parse(JSON.stringify(this.data.cartList));
    var found = false;
    for (var i = 0; i < cartList.length; i++) {
      if (cartList[i].name === service.name) {
        found = true;
        break;
      }
    }
    if (found) {
      that.setData({ adding: false });
      wx.showToast({ title: '已加入相同的服务，请勿重复添加', icon: 'none' });
      return;
    }
    cartList.push({
      id: service.id,
      cartKey: cartType + '_' + service.id + '_' + (requirementStr || ''),
      type: cartType,
      name: service.name,
      image: service.image,
      price: service.price,
      quantity: 1,
      requirement: requirementStr || '',
      selectedRequirements: service.selectedRequirements || [],
      requirements: service.requirements || []
    });
    this.updateCart(cartList);
    this.setData({ showSpecPop: false, adding: false });

    var dto = {};
    if (cartType === 'item') {
      dto.serviceItemId = service.id;
    } else if (cartType === 'package') {
      dto.servicePackageId = service.id;
    }
    if (requirementStr) {
      dto.requirement = requirementStr;
    }

    request.post('/user/shopping-cart/add', dto).then(function() {
      wx.showToast({ title: '加入成功', icon: 'success', duration: 1000 });
      that.loadCartFromBackend();
    }).catch(function(err) {
      that.loadCartFromBackend();
      wx.showToast({ title: err.message || '加入预约清单失败', icon: 'none' });
    });
  },

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
    if (this.data.adding) return;
    var currentRequirements = this.data.currentRequirements;
    var allSelected = currentRequirements.every(function(r) { return r.selected; });
    if (!allSelected) {
      wx.showToast({ title: '请选择全部服务需求', icon: 'none' });
      return;
    }
    this.setData({ adding: true, showSpecPop: false });
    var service = JSON.parse(JSON.stringify(this.data.currentService));
    var requirementStr = currentRequirements.map(function(r) {
      return r.name + ':' + r.selected;
    }).join(',');
    this.doAddToCart(service, requirementStr);
  },

  updateCart: function(cartList) {
    this.setData({ cartList: cartList });
    wx.setStorageSync('cartList', cartList);
  },

  loadCartFromBackend: function() {
    var that = this;
    request.get('/user/shopping-cart/list').then(function(res) {
      var backendCartList = res.data || [];
      var cartList = that.convertBackendCartToFrontend(backendCartList);
      that.updateCart(cartList);
      if (cartList.length === 0) {
        wx.setStorageSync('cartList', []);
      }
    }).catch(function(err) {
      console.log('加载预约清单失败', err);
      that.loadCartFromStorage();
    });
  },

  convertBackendCartToFrontend: function(backendList) {
    var localCart = wx.getStorageSync('cartList') || [];
    var frontendList = [];
    backendList.forEach(function(item) {
      var type, serviceId;
      if (item.serviceItemId) {
        type = 'item';
        serviceId = item.serviceItemId;
      } else if (item.servicePackageId) {
        type = 'package';
        serviceId = item.servicePackageId;
      } else {
        return;
      }
      var selectedRequirements = [];
      if (item.requirement) {
        var parts = item.requirement.split(',');
        parts.forEach(function(part) {
          var colonIdx = part.indexOf(':');
          if (colonIdx > 0) {
            selectedRequirements.push({
              name: part.substring(0, colonIdx),
              value: part.substring(colonIdx + 1)
            });
          }
        });
      }
      var cartKey = type + '_' + serviceId + '_' + (item.requirement || '');
      var localRequirements = [];
      for (var j = 0; j < localCart.length; j++) {
        if (localCart[j].id === serviceId && localCart[j].type === type) {
          localRequirements = localCart[j].requirements || [];
          break;
        }
      }
      frontendList.push({
        id: serviceId,
        cartKey: cartKey,
        type: type,
        name: item.name,
        image: item.image,
        price: item.amount,
        quantity: 1,
        requirement: item.requirement,
        selectedRequirements: selectedRequirements,
        requirements: localRequirements
      });
    });
    return frontendList;
  },

  loadCartFromStorage: function() {
    var cartList = wx.getStorageSync('cartList') || [];
    this.setData({ cartList: cartList });
  }
});
