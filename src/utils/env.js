// Browser environment utilities — replaces weex-ui Utils/BindEnv
var env = {
  getPageHeight: function () {
    return window.innerHeight || document.documentElement.clientHeight;
  },
  getPageWidth: function () {
    return window.innerWidth || document.documentElement.clientWidth;
  },
  isMobile: function () {
    if (typeof window === 'undefined') return true;
    var w = this.getPageWidth();
    if (w < 768) return true;
    return /Android|webOS|iPhone|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
  },
  isDesktop: function () {
    return !this.isMobile();
  },
  isIPhoneX: function () {
    if (typeof window === 'undefined') return false;
    return /iPhone/.test(navigator.userAgent) && window.screen.height >= 812;
  },
  goToH5Page: function (url) {
    window.open(url, '_blank');
  }
};

// BindEnv replacements — expression binding is Weex-native, not available in browser
var BindEnv = {
  supportsEBForAndroid: function () { return false; },
  supportsEBForIos: function () { return false; }
};

export { env, BindEnv };
export default env;
