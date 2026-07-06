// Browser environment utilities — replaces weex-ui Utils/BindEnv
var env = {
  getPageHeight: function () {
    return window.innerHeight || document.documentElement.clientHeight;
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
