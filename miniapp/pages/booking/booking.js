var app = getApp();
var request = require('../../utils/request.js');
var util = require('../../utils/util.js');

Page({
  data: {
    categoryList: [],
    categoryIndex: 0,
    serviceList: [],
    itemId: '',
    cartList: [], cartTotalPrice: 0, cartTotalCount: 0,
    currentTab: 'service',
    searchKeyword: '', isSearching: false, searchHistory: [], showHistory: false,
    hkKeyword: '', hkSearchHistory: [], hkShowHistory: false,
    hkList: [], hkPage: 1, hkPageSize: 10, hkHasMore: true, hkLoading: false,
    hkScrollHeight: 500,
    showSpecPop: false, showDetailPop: false,
    detailService: {}, currentService: {}, currentRequirements: [],
    loading: false
  },

  onLoad: function() {
    this.loadCartFromBackend();
    // 计算家政员列表滚动区域高度
    var sysInfo = wx.getSystemInfoSync();
    var pxToRpx = 750 / sysInfo.windowWidth;
    this.setData({ hkScrollHeight: sysInfo.windowHeight * pxToRpx - 260 });
  },
  onTabSwitch: function(e) {
    var tab = e.currentTarget.dataset.tab;
    this.setData({ currentTab: tab });
    if (tab === 'housekeeper') {
      // 切到家政员，清服务搜索状态
      this.setData({ searchKeyword: '', isSearching: false });
      this.hkLoadSearchHistory();
      this.hkLoadList(true);
    } else {
      // 切到服务，清家政员搜索状态
      this.setData({ hkKeyword: '' });
      this.setData({ isSearching: false });
      this.loadSearchHistory();
      this.loadCategories();
    }
  },

  // 跳转智能搜索
  goSmartSearch: function() {
    wx.navigateTo({ url: '/pages/smart-search/smart-search' });
  },

  onShow: function() {
wx.hideTabBar();
    if (!this.checkLogin()) return;
    this.setData({ loading: false });
    if (app.globalData.bookingTab === 'housekeeper') {
      this.setData({ currentTab: 'housekeeper' });
      app.globalData.bookingTab = '';
    }
    // 根据当前tab重新加载对应数据
    if (this.data.currentTab === 'housekeeper') {
      this.setData({ isSearching: false });
      this.hkLoadList(true);
    } else {
      this.setData({ isSearching: false });
      this.loadSearchHistory();
      this.loadCategories();
    }
    this.loadCartFromStorage();
  },

  loadCategories: function() {
    var that = this;
    util.showLoading('加载中...');
    request.get('/admin/category/list').then(function(res) {
      util.hideLoading();
      var list = (res.data && res.data.length > 0) ? res.data : [];
      var all = list.filter(function(item) {
        return item && item.status == 1;
      });
      all.sort(function(a, b) { return (a.sort || 0) - (b.sort || 0); });
      that.setData({ categoryList: all, categoryIndex: 0, itemId: '' });
      if (all.length > 0) {
        that.switchCategory(0, all[0]);
      } else {
        that.setData({ serviceList: [] });
      }
    }).catch(function() { util.hideLoading(); });
  },

  loadServices: function(categoryId) {
    var that = this;
    util.showLoading('加载中...');
    request.get('/employee/service-item/page', {
      page: 1, pageSize: 100, categoryId: categoryId
    }).then(function(res) {
      util.hideLoading();
      var records = (res.data && res.data.records) ? res.data.records : [];
      records.forEach(function(item) {
        item.quantity = 0;
        item.type = 'item';
        item.requirements = (item.requirements || []).filter(function(r) {
          return r.name || r.value;
        });
      });
      that.setData({ serviceList: records });
    }).catch(function() {
      util.hideLoading();
      that.setData({ serviceList: [] });
    });
  },

  loadPackages: function(categoryId) {
    var that = this;
    util.showLoading('加载中...');
    request.get('/employee/service-package/page', {
      page: 1, pageSize: 100, categoryId: categoryId
    }).then(function(res) {
      util.hideLoading();
      var records = (res.data && res.data.records) ? res.data.records : [];
      records.forEach(function(item) {
        item.quantity = 0;
        item.type = 'package';
        var rawReqs = item.servicePackageItems || [];
        item.requirements = rawReqs.filter(function(r) {
          return r.name || r.value;
        });
      });
      that.setData({ serviceList: records });
    }).catch(function() {
      util.hideLoading();
      that.setData({ serviceList: [] });
    });
  },

  // 搜索输入
  onSearchInput: function(e) {
    var val = e.detail.value;
    this.setData({ searchKeyword: val, showHistory: val === '' && this.data.searchHistory.length > 0 });
  },

  // 搜索确认（回车）
  onSearchConfirm: function(e) {
    var keyword = e.detail.value.trim();
    if (!keyword) {
      this.clearSearch();
      return;
    }
    this.searchServices(keyword);
  },

  // 加载搜索历史
  loadSearchHistory: function() {
    var that = this;
    request.get('/user/search-history/list').then(function(res) {
      var history = res.data || [];
      that.setData({ searchHistory: history, showHistory: history.length > 0 && that.data.searchKeyword === '' });
    }).catch(function() {});
  },

  // 搜索栏聚焦
  onSearchFocus: function() {
    if (this.data.searchKeyword === '' && this.data.searchHistory.length > 0) {
      this.setData({ showHistory: true });
    }
  },

  // 点击历史项
  tapHistoryItem: function(e) {
    var keyword = e.currentTarget.dataset.keyword;
    this.setData({ searchKeyword: keyword, showHistory: false });
    this.searchServices(keyword, true);
  },

  // 清空搜索历史
  clearSearchHistory: function() {
    var that = this;
    request.del('/user/search-history/clean').then(function() {
      that.setData({ searchHistory: [] });
      wx.showToast({ title: '已清空', icon: 'success', duration: 1000 });
    }).catch(function() {});
  },

  // 执行搜索：同时查服务和服务套餐
  searchServices: function(keyword, skipSave) {
    var that = this;
    if (!keyword) return;
    util.showLoading('搜索中...');
    // 保存搜索记录（点击历史项时不重复保存）
    if (!skipSave) {
      request.post('/user/search-history?keyword=' + encodeURIComponent(keyword)).then(function() {
        that.loadSearchHistory();
      }).catch(function() {});
    }
    var itemPromise = request.get('/employee/service-item/page', {
      page: 1, pageSize: 100, name: keyword
    });
    var packagePromise = request.get('/employee/service-package/page', {
      page: 1, pageSize: 100, name: keyword
    });

    Promise.all([itemPromise, packagePromise]).then(function(results) {
      util.hideLoading();
      var items = (results[0].data && results[0].data.records) ? results[0].data.records : [];
      var packages = (results[1].data && results[1].data.records) ? results[1].data.records : [];

      items.forEach(function(item) {
        item.quantity = 0;
        item.type = 'item';
        item.requirements = (item.requirements || []).filter(function(r) { return r.name || r.value; });
      });
      packages.forEach(function(item) {
        item.quantity = 0;
        item.type = 'package';
        var rawReqs = item.servicePackageItems || [];
        item.requirements = rawReqs.filter(function(r) { return r.name || r.value; });
      });

      var allResults = items.concat(packages);
      that.setData({
        serviceList: allResults,
        isSearching: true,
        categoryIndex: -1
      });
    }).catch(function() {
      util.hideLoading();
      wx.showToast({ title: '搜索失败', icon: 'none' });
    });
  },

  // 清除搜索
  clearSearch: function() {
    this.setData({ searchKeyword: '', isSearching: false, serviceList: [], showHistory: this.data.searchHistory.length > 0 });
    if (this.data.categoryList.length > 0) {
      this.switchCategory(0, this.data.categoryList[0]);
    } else {
      this.loadCategories();
    }
  },

  // ========== 家政员预约搜索 ==========
  // 家政员搜索历史
  hkLoadSearchHistory: function() {
    var that = this;
    request.get('/user/housekeeper-search-history/list').then(function(res) {
      var history = res.data || [];
      that.setData({ hkSearchHistory: history, hkShowHistory: history.length > 0 && that.data.hkKeyword === '' });
    }).catch(function() {});
  },

  hkTapHistoryItem: function(e) {
    var keyword = e.currentTarget.dataset.keyword;
    this.setData({ hkKeyword: keyword, hkShowHistory: false });
    this.hkLoadList(true);
  },

  hkClearSearchHistory: function() {
    var that = this;
    request.del('/user/housekeeper-search-history/clean').then(function() {
      that.setData({ hkSearchHistory: [], hkShowHistory: false });
      wx.showToast({ title: '已清空', icon: 'success', duration: 1000 });
    }).catch(function() {});
  },

  onHkInput: function(e) {
    var val = e.detail.value;
    this.setData({ hkKeyword: val, hkShowHistory: val === '' && this.data.hkSearchHistory.length > 0 });
  },

  onHkSearch: function(e) {
    var keyword = e.detail.value.trim();
    if (!keyword) {
      this.setData({ hkKeyword: '', hkShowHistory: this.data.hkSearchHistory.length > 0 });
      this.hkLoadList(true);
      return;
    }
    var that = this;
    this.setData({ hkKeyword: keyword, hkShowHistory: false });
    request.post('/user/housekeeper-search-history?keyword=' + encodeURIComponent(keyword)).then(function() {
      that.hkLoadSearchHistory();
    }).catch(function() {});
    this.hkLoadList(true);
  },

  onHkClear: function() {
    this.setData({ hkKeyword: '', hkShowHistory: this.data.hkSearchHistory.length > 0 });
    this.hkLoadSearchHistory();
    this.hkLoadList(true);
  },

  hkLoadList: function(reset) {
    var that = this;
    if (reset) {
      that.setData({ hkList: [], hkPage: 1, hkHasMore: true }, function() {
        that._hkDoLoad();
      });
    } else {
      if (!that.data.hkHasMore || that.data.hkLoading) return;
      that._hkDoLoad();
    }
  },

  _hkDoLoad: function() {
    var that = this;
    that.setData({ hkLoading: true });
    var params = { page: that.data.hkPage, pageSize: that.data.hkPageSize };
    if (that.data.hkKeyword) params.name = that.data.hkKeyword;
    request.get('/user/housekeeper/page', params).then(function(res) {
      var records = (res.data && res.data.records) ? res.data.records : [];
      records.forEach(function(item) {
        item.ratingText = util.formatRating(item.rating);
      });
      var newList = that.data.hkList.concat(records);
      that.setData({
        hkList: newList,
        hkHasMore: records.length >= that.data.hkPageSize,
        hkLoading: false,
        hkPage: that.data.hkPage + 1
      });
    }).catch(function() { that.setData({ hkLoading: false }); });
  },

  hkScrollLower: function() { this.hkLoadList(); },
  onHkDetailTap: function(e) {
    var id = e.currentTarget.dataset.id;
    var that = this;
    request.get('/admin/shop/getStatus').then(function(res) {
      if (res.data === 0) {
        wx.showToast({ title: '已经休息了哟，暂时无法预约', icon: 'none' });
        return;
      }
      wx.navigateTo({ url: '/pages/housekeeper-detail/housekeeper-detail?id=' + id });
    }).catch(function() {
      wx.navigateTo({ url: '/pages/housekeeper-detail/housekeeper-detail?id=' + id });
    });
  },

  hkLoadMore: function() { this.hkLoadList(); },

  switchCategory: function(index, item) {
    this.setData({ categoryIndex: index, itemId: 'target' + index });
    if (item.type == 1) {
      this.loadServices(item.id);
    } else if (item.type == 2) {
      this.loadPackages(item.id);
    }
  },

  switchMenu: function(e) {
    var index = e.currentTarget.dataset.index;
    var item = this.data.categoryList[index];
    if (item) this.switchCategory(index, item);
  },

  addToCart: function(e) {
    if (this.data.loading) return;
    var service = e.currentTarget.dataset.item;
    var that = this;
    that.setData({ loading: true });
    // 先检查营业状态
    request.get('/admin/shop/getStatus').then(function(res) {
      if (res.data === 0) {
        that.setData({ loading: false });
        wx.showToast({ title: '已经休息了哟，暂时无法预约', icon: 'none' });
        return;
      }
      if (res.data === 0) {
        that.setData({ loading: false });
        wx.showToast({ title: '已经休息了哟，暂时无法预约', icon: 'none' });
        return;
      }
      // 检查是否已加入
      var cartList = that.data.cartList;
      for (var i = 0; i < cartList.length; i++) {
        if (cartList[i].name === service.name) {
          that.setData({ loading: false });
          wx.showToast({ title: '已加入相同的服务，请勿重复添加', icon: 'none' });
          return;
        }
      }
      // 有需求 → 弹窗选择；无需求 → 直接加入
      if (service.requirements && service.requirements.length > 0) {
        that.setData({ loading: false });
        var reqs = JSON.parse(JSON.stringify(service.requirements));
        reqs.forEach(function(r) {
          try { r.options = JSON.parse(r.value); } catch (e) { r.options = []; }
          r.selected = '';
        });
        that.setData({ currentService: service, currentRequirements: reqs, showSpecPop: true });
      } else {
        that.doAddToCart(service, '');
      }
    }).catch(function() {
      that.setData({ loading: false });
      wx.showToast({ title: '网络异常', icon: 'none' });
    });
  },

  doAddToCart: function(service, requirementStr) {
    var that = this;
    var cartList = JSON.parse(JSON.stringify(this.data.cartList));
    var found = false;
    for (var i = 0; i < cartList.length; i++) {
      if (cartList[i].name === service.name) {
        found = true;
        break;
      }
    }
    if (found) {
      that.setData({ loading: false });
      wx.showToast({ title: '已加入相同的服务，请勿重复添加', icon: 'none' });
      return;
    }
    cartList.push({
      id: service.id,
      cartKey: service.type + '_' + service.id + '_' + (requirementStr || ''),
      type: service.type,
      name: service.name,
      image: service.image,
      price: service.price,
      quantity: 1,
      requirement: requirementStr || '',
      selectedRequirements: service.selectedRequirements || [],
      requirements: service.requirements || []
    });
    this.updateCart(cartList);
    this.updateServiceQuantity(cartList);
    this.setData({ showSpecPop: false, loading: false });

    var dto = {};
    if (service.type === 'item') {
      dto.serviceItemId = service.id;
    } else if (service.type === 'package') {
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
    if (this.data.loading) return;
    var currentRequirements = this.data.currentRequirements;
    var allSelected = currentRequirements.every(function(r) { return r.selected; });
    if (!allSelected) {
      wx.showToast({ title: '请选择全部服务需求', icon: 'none' });
      return;
    }
    this.setData({ loading: true, showSpecPop: false });
    var service = JSON.parse(JSON.stringify(this.data.currentService));
    var requirementStr = currentRequirements.map(function(r) {
      return r.name + ':' + r.selected;
    }).join(',');
    this.doAddToCart(service, requirementStr);
  },

  updateCart: function(cartList) {
    var totalPrice = 0, totalCount = 0;
    cartList.forEach(function(item) {
      totalCount += item.quantity || 0;
      totalPrice += (item.price || 0) * (item.quantity || 0);
    });
    this.setData({ cartList: cartList, cartTotalPrice: totalPrice.toFixed(2), cartTotalCount: totalCount });
    wx.setStorageSync('cartList', cartList);
  },

  updateServiceQuantity: function(cartList) {
    var serviceList = this.data.serviceList;
    serviceList.forEach(function(s) {
      s.quantity = 0;
      cartList.forEach(function(c) {
        if (c.type === s.type && c.id === s.id) {
          s.quantity = (s.quantity || 0) + (c.quantity || 0);
        }
      });
    });
    this.setData({ serviceList: serviceList });
  },

  loadCartFromBackend: function() {
    var that = this;
    request.get('/user/shopping-cart/list').then(function(res) {
      var backendCartList = res.data || [];
      var cartList = that.convertBackendCartToFrontend(backendCartList);
      that.updateCart(cartList);
      that.updateServiceQuantity(cartList);
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
    var totalPrice = 0, totalCount = 0;
    cartList.forEach(function(item) {
      totalCount += item.quantity || 0;
      totalPrice += (item.price || 0) * (item.quantity || 0);
    });
    this.setData({ cartList: cartList, cartTotalPrice: totalPrice.toFixed(2), cartTotalCount: totalCount });
    this.updateServiceQuantity(cartList);
  },

  showDetail: function(e) {
    this.setData({ showDetailPop: true, detailService: e.currentTarget.dataset.item });
  },
  closeDetail: function() { this.setData({ showDetailPop: false }); },
  closeSpecPop: function() { this.setData({ showSpecPop: false }); },
  stopPropagation: function() {},

  // 检查登录状态，未登录跳转首页
  checkLogin: function() {
    if (!app.globalData.isLoggedIn) {
      app.globalData.needReAuth = true;
      wx.switchTab({ url: '/pages/index/index' });
      return false;
    }
    return true;
  }
});
