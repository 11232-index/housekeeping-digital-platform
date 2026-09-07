var app = getApp();
var BASE_URL = 'http://192.168.137.1:8080'; // 真机调试用：当前为【手机连电脑热点】时使用；若手机改连路由器 WiFi 则改回 10.163.145.106；换网络需同步更新

function request(options) {
  return new Promise(function(resolve, reject) {
    var token = wx.getStorageSync('token');
    var url = options.url || '';
    var header = { 'Content-Type': 'application/json' };
    if (options.header) { Object.assign(header, options.header); }
    var isLoginRequest = url.indexOf('/auth/login') !== -1;
    if (!isLoginRequest && token) { header['token'] = token; }
    wx.request({
      url: BASE_URL + url,
      method: options.method || 'GET',
      data: options.data || {},
      header: header,
      timeout: options.timeout || 15000,
      success: function(res) {
        if (res.statusCode === 200) {
          var data = res.data;
          if (data.code === 1) { resolve(data); }
          else {
            console.log('Request error:', data.msg);
            reject(new Error(data.msg || 'Request failed'));
          }
        } else if (res.statusCode === 401) {
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          reject(new Error('Unauthorized'));
        } else {
          console.log('Network error:', url);
          reject(new Error('Network error'));
        }
      },
      fail: function(err) {
        console.log('Request failed:', url, err);
        reject(err);
      }
    });
  });
}
function get(url, data) { return request({ url: url, method: 'GET', data: data || {} }); }
function post(url, data) { return request({ url: url, method: 'POST', data: data || {} }); }
function put(url, data) { return request({ url: url, method: 'PUT', data: data || {} }); }
function del(url, data) { return request({ url: url, method: 'DELETE', data: data || {} }); }
module.exports = { request: request, get: get, post: post, put: put, del: del, BASE_URL: BASE_URL };
