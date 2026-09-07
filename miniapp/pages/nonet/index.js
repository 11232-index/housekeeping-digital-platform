Page({
  retry: function() {
    wx.getNetworkType({
      success: function(res) {
        if (res.networkType === 'none') {
          wx.showToast({ title: '当前无网络连接', icon: 'none' });
        } else {
          wx.navigateBack();
        }
      }
    });
  }
});
