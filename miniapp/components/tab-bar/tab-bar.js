Component({
  properties: {
    selected: { type: Number, value: 0 }
  },
  data: {
    list: [
      { pagePath: '/pages/index/index', text: '首页' },
      { pagePath: '/pages/booking/booking', text: '家政预约' },
      { pagePath: '/pages/cart/cart', text: '预约清单' },
      { pagePath: '/pages/my/my', text: '我的' }
    ]
  },
  methods: {
    onTap(e) {
      var path = e.currentTarget.dataset.path;
      wx.switchTab({ url: path });
    }
  }
});
