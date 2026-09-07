var request = require('../../utils/request.js');
var util = require('../../utils/util.js');

Page({
  data: {
    addressList: [],
    isEmpty: false,
    fromOrder: false,
    // 分页
    page: 1,
    pageSize: 10,
    hasMore: true,
    loading: false,
    loadingMore: false,
    // 标签映射
    labelMap: { '1': '公司', '2': '家', '3': '学校' }
  },

  onLoad: function(options) {
    if (options && options.from === 'order') {
      this.setData({ fromOrder: true });
    }
  },

  onShow: function() {
    this.setData({ page: 1, addressList: [], hasMore: true });
    this.loadAddressList();
  },

  // ========== 加载地址列表（分页） ==========
  loadAddressList: function() {
    var that = this;
    if (this.data.loading) return;
    this.setData({ loading: true });

    request.get('/user/address-book/page', {
      page: this.data.page,
      pageSize: this.data.pageSize
    }).then(function(res) {
      var data = res.data;
      var records = (data && data.records) ? data.records : [];
      var total = (data && data.total) ? data.total : 0;

      // 处理 label 显示
      records.forEach(function(item) {
        if (item.label && that.data.labelMap[item.label]) {
          item.labelName = that.data.labelMap[item.label];
        } else if (!item.label) {
          item.labelName = '家';
        } else {
          item.labelName = item.label;
        }
      });

      var addressList;
      if (that.data.page === 1) {
        addressList = records;
      } else {
        addressList = that.data.addressList.concat(records);
      }

      var hasMore = addressList.length < total;

      that.setData({
        addressList: addressList,
        isEmpty: addressList.length === 0,
        hasMore: hasMore,
        loading: false,
        loadingMore: false
      });
    }).catch(function(err) {
      console.log('加载地址列表失败', err);
      that.setData({ loading: false, loadingMore: false });
      wx.showToast({ title: err.message || '加载失败', icon: 'none' });
    });
  },

  // ========== 上拉加载更多 ==========
  onReachBottom: function() {
    if (!this.data.hasMore || this.data.loadingMore) return;
    this.setData({
      page: this.data.page + 1,
      loadingMore: true
    });
    this.loadAddressList();
  },

  // ========== 下拉刷新 ==========
  onPullDownRefresh: function() {
    this.setData({ page: 1, addressList: [], hasMore: true });
    this.loadAddressList();
    wx.stopPullDownRefresh();
  },

  // ========== 新增 / 编辑地址 ==========
  addOrEdit: function(e) {
    var type = e.currentTarget.dataset.type;
    if (type === 'add') {
      wx.navigateTo({ url: '/pages/addOrEditAddress/addOrEditAddress' });
    } else {
      var item = e.currentTarget.dataset.item;
      wx.navigateTo({
        url: '/pages/addOrEditAddress/addOrEditAddress?type=edit&id=' + item.id
      });
    }
  },

  // ========== 选择地址（从下单页进入时） ==========
  selectAddress: function(e) {
    if (this.data.fromOrder) {
      var item = e.currentTarget.dataset.item;
      wx.setStorageSync('selectedAddress', item);
      wx.navigateBack();
    }
  },

  // ========== 设置默认地址 ==========
  setDefault: function(e) {
    var that = this;
    var index = e.currentTarget.dataset.index;
    var item = that.data.addressList[index];

    util.showLoading('设置中...');
    request.put('/user/address-book/default/' + item.id).then(function() {
      util.hideLoading();
      // 更新本地列表的默认状态
      var list = that.data.addressList;
      list.forEach(function(a) { a.isDefault = 0; });
      list[index].isDefault = 1;
      that.setData({ addressList: list });
      wx.showToast({ title: '已设为默认地址', icon: 'success' });
    }).catch(function(err) {
      util.hideLoading();
      wx.showToast({ title: err.message || '设置失败', icon: 'none' });
    });
  },

  // ========== 删除地址 ==========
  deleteAddress: function(e) {
    var that = this;
    var index = e.currentTarget.dataset.index;
    var item = that.data.addressList[index];

    wx.showModal({
      title: '确认删除',
      content: '确定要删除该地址吗？',
      success: function(res) {
        if (res.confirm) {
          util.showLoading('删除中...');
          request.del('/user/address-book/' + item.id).then(function() {
            util.hideLoading();
            var list = that.data.addressList;
            list.splice(index, 1);

            // 如果删除的是默认地址，后端会自动将第一个设为默认
            // 重新加载列表以获取正确状态
            that.setData({ page: 1, addressList: [], hasMore: true });
            that.loadAddressList();
            wx.showToast({ title: '已删除', icon: 'success' });
          }).catch(function(err) {
            util.hideLoading();
            wx.showToast({ title: err.message || '删除失败', icon: 'none' });
          });
        }
      }
    });
  }
});
