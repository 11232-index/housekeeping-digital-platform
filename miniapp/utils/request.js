var app = getApp();
// 真机调试用的后端地址 —— 走【电脑的移动热点】：
//   热点网卡的地址固定是 192.168.137.1，不会像校园网/WiFi 那样被 DHCP 换来换去，
//   所以这里写死即可，换网络也不用改。唯一前提：手机要连上电脑开的那个热点。
//   （如果哪天非要走 WiFi，把下面换成电脑当前的局域网 IP —— 用 ipconfig 看 WLAN 那行的 IPv4；
//     注意校园网/公共 WiFi 常开客户端隔离，手机可能根本访问不到电脑。）
var BASE_URL = 'http://192.168.137.1:8080';

function request(options) {
  return new Promise(function(resolve, reject) {
    var url = options.url || '';
    // 家政员端接口（/worker/**）用家政员自己的 workerToken，其余走用户端 token
    var isWorkerRequest = url.indexOf('/worker/') === 0;
    var token = wx.getStorageSync(isWorkerRequest ? 'workerToken' : 'token');
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
          if (isWorkerRequest) {
            wx.removeStorageSync('workerToken');
            wx.removeStorageSync('workerInfo');
          } else {
            wx.removeStorageSync('token');
            wx.removeStorageSync('userInfo');
          }
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
