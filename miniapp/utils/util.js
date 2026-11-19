function formatTime(date) {
  var y=date.getFullYear(),m=date.getMonth()+1,d=date.getDate();
  var h=date.getHours(),mi=date.getMinutes(),s=date.getSeconds();
  return [y,m,d].map(formatNumber).join('-')+' '+[h,mi,s].map(formatNumber).join(':');
}
function formatNumber(n){ n=n.toString(); return n[1]?n:'0'+n; }
function formatPrice(price){ return parseFloat(price).toFixed(2); }
function formatRating(rating){ return (parseFloat(rating) || 0).toFixed(1); }
function getServiceStatusText(status){
  var map={0:'休息中',1:'可预约'};
  return map[status]||'未知';
}
function getOrderStatusText(status){
  var map={1:'待付款',2:'待接单',3:'已接单',4:'服务中',5:'已完成',6:'待退款',7:'已取消'};
  return map[status]||'未知';
}
function validatePhone(phone){ return /^1[3-9]\d{9}$/.test(phone); }
function showLoading(title){ wx.showLoading({title:title||'加载中...',mask:true}); }
function hideLoading(){ wx.hideLoading(); }
module.exports={
  formatTime:formatTime,formatPrice:formatPrice,formatRating:formatRating,
  getServiceStatusText:getServiceStatusText,getOrderStatusText:getOrderStatusText,
  validatePhone:validatePhone,showLoading:showLoading,hideLoading:hideLoading
};
