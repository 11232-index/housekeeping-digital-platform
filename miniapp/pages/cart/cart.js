var app = getApp();
var request = require('../../utils/request.js');
var util = require('../../utils/util.js');

Page({
  data: {
    cartList: [],
    totalPrice: 0,
    totalCount: 0,
    shopStatus: 1,
    clearing: false, ordering: false
  },

  onShow: function() {
wx.hideTabBar();
    if (!this.checkLogin()) return;
    this.setData({ clearing: false, ordering: false });
    this.loadCartFromBackend();
    this.loadShopStatus();
  },

  loadShopStatus: function() {
    var that = this;
    request.get('/admin/shop/getStatus').then(function(res) {
      if (res.data !== undefined) that.setData({ shopStatus: res.data });
    }).catch(function() {});
  },

  // 从后端加载预约清单（合并本地缓存保留需求选项）
  loadCartFromBackend: function() {
    var that = this;
    request.get('/user/shopping-cart/list').then(function(res) {
      var backendCartList = res.data || [];
      var cartList = that.convertBackendCartToFrontend(backendCartList);
      that.updateCart(cartList);
    }).catch(function(err) {
      console.log('加载预约清单失败', err);
      var cartList = wx.getStorageSync('cartList') || [];
      that.updateCart(cartList);
    });
  },

  // 后端预约清单数据 → 前端格式（合并本地缓存的 requirements）
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

      // 从本地缓存中查找匹配项，合并 requirements
      var localRequirements = [];
      for (var i = 0; i < localCart.length; i++) {
        if (localCart[i].id === serviceId && localCart[i].type === type) {
          localRequirements = localCart[i].requirements || [];
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

  updateCart: function(cartList) {
    var totalPrice = 0, totalCount = 0;
    cartList.forEach(function(item) {
      totalCount += item.quantity || 0;
      totalPrice += (item.price || 0) * (item.quantity || 0);
    });
    this.setData({ cartList: cartList, totalPrice: totalPrice.toFixed(2), totalCount: totalCount });
    wx.setStorageSync('cartList', cartList);
  },

  // 删除单项
  removeFromCart: function(e) {
    var that = this;
    var service = e.currentTarget.dataset.item;
    var cartList = this.data.cartList;

    var cartListCopy = JSON.parse(JSON.stringify(cartList));
    var matched = null;
    for (var i = 0; i < cartListCopy.length; i++) {
      var matchVal = false;
      if (service.cartKey && cartListCopy[i].cartKey) {
        matchVal = cartListCopy[i].cartKey === service.cartKey;
      } else {
        matchVal = cartListCopy[i].id === service.id && cartListCopy[i].type === service.type;
      }
      if (matchVal) {
        matched = cartListCopy[i];
        cartListCopy.splice(i, 1);
        break;
      }
    }
    if (matched) {
      this.updateCart(cartListCopy);
    }

    var dto = {};
    if (service.type === 'item') {
      dto.serviceItemId = service.id;
    } else if (service.type === 'package') {
      dto.servicePackageId = service.id;
    }
    if (service.requirement) {
      dto.requirement = service.requirement;
    }

    request.post('/user/shopping-cart/sub', dto).then(function() {
      that.loadCartFromBackend();
    }).catch(function(err) {
      that.loadCartFromBackend();
      wx.showToast({ title: err.message || '操作失败', icon: 'none' });
    });
  },

  // 清空预约清单
  clearCart: function() {
    if (this.data.clearing) return;
    var that = this;
    wx.showModal({
      title: '提示',
      content: '确定要清空预约清单吗？',
      success: function(res) {
        if (res.confirm) {
          that.setData({ clearing: true });
          request.del('/user/shopping-cart/clean').then(function() {
            that.setData({ clearing: false });
            that.updateCart([]);
            wx.showToast({ title: '预约清单已清空', icon: 'success', duration: 1000 });
          }).catch(function(err) {
            that.setData({ clearing: false });
            that.updateCart([]);
            wx.showToast({ title: err.message || '清空失败', icon: 'none' });
          });
        }
      }
    });
  },

  // 点击某项 → 查询需求 → 单项跳转下单页
  goOrderWithItem: function(e) {
    if (this.data.ordering) return;
    var index = e.currentTarget.dataset.index;
    var item = this.data.cartList[index];
    var that = this;
    that.setData({ ordering: true });
    // 实时查询营业状态
    request.get('/admin/shop/getStatus').then(function(res) {
      if (res.data === 0) {
        that.setData({ ordering: false });
        wx.showToast({ title: '已经休息了哟，暂时无法预约', icon: 'none' });
        return;
      }
      // 已选过需求 → 直接进入下单页；未选且有需求 → 查后端需求表
      if (item.requirement) {
        that.setData({ ordering: false });
        wx.setStorageSync('singleOrderItem', JSON.stringify(item));
        wx.setStorageSync('currentRequirements', JSON.stringify({ requirements: [] }));
        wx.navigateTo({ url: '/pages/order/index?single=1' });
        return;
      }
      var params = {};
      if (item.type === 'item') {
        params.serviceItemId = item.id;
      } else if (item.type === 'package') {
        params.servicePackageId = item.id;
      }
      // 查询后端需求表
      request.get('/user/shopping-cart/requirements', params).then(function(res2) {
        that.setData({ ordering: false });
        var requirements = res2.data || [];
        wx.setStorageSync('singleOrderItem', JSON.stringify(item));
        wx.setStorageSync('currentRequirements', JSON.stringify({ requirements: requirements }));
        wx.navigateTo({ url: '/pages/order/index?single=1' });
      }).catch(function() {
        that.setData({ ordering: false });
        wx.setStorageSync('singleOrderItem', JSON.stringify(item));
        wx.setStorageSync('currentRequirements', JSON.stringify({ requirements: [] }));
        wx.navigateTo({ url: '/pages/order/index?single=1' });
      });
    }).catch(function() {
      that.setData({ ordering: false });
      wx.showToast({ title: '网络异常', icon: 'none' });
    });
  },

  // 去预约
  goOrder: function() {
    if (this.data.totalCount === 0) {
      wx.showToast({ title: '请先选择服务', icon: 'none' });
      return;
    }
    var that = this;
    // 实时查询营业状态
    request.get('/admin/shop/getStatus').then(function(res) {
      if (res.data === 0) {
        wx.showToast({ title: '已经休息了哟，暂时无法预约', icon: 'none' });
        return;
      }
      wx.navigateTo({ url: '/pages/order/index' });
    }).catch(function() {
      wx.showToast({ title: '网络异常', icon: 'none' });
    });
  },

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
