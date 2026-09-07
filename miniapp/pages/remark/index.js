Page({
  data: { remark: '', numVal: 0 },
  onLoad: function(options) {
    if (options && options.remark) {
      var remark = decodeURIComponent(options.remark);
      this.setData({ remark: remark, numVal: remark.length });
    }
  },
  onInput: function(e) {
    var val = e.detail.value;
    if (val.length > 50) val = val.substring(0, 50);
    this.setData({ remark: val, numVal: val.length });
  },
  save: function() {
    wx.setStorageSync('orderRemark', this.data.remark);
    wx.navigateBack();
  },
  goBack: function() { wx.navigateBack(); }
});
