// Simple toast/confirm utilities — replaces weex modal module

export function toast(message, duration) {
  // Simple non-blocking toast
  var div = document.createElement('div');
  div.textContent = message;
  div.style.cssText = 'position:fixed;top:50%;left:50%;transform:translate(-50%,-50%);background:rgba(0,0,0,0.75);color:#fff;padding:12px 24px;border-radius:8px;font-size:28px;z-index:9999;pointer-events:none;';
  document.body.appendChild(div);
  setTimeout(function () {
    if (div.parentNode) div.parentNode.removeChild(div);
  }, (duration || 2) * 1000);
}

export function confirmDialog(message, callback) {
  if (window.confirm(message)) {
    callback && callback('OK');
  } else {
    callback && callback('CANCEL');
  }
}
