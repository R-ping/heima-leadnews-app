const path = require('path')
const utils = require('./utils')
// const config = require('./config')
// const isProduction = process.env.NODE_ENV === 'production'
// const sourceMapEnabled = isProduction
//   ? config.prod.productionSourceMap
//   : config.dev.cssSourceMap

// module.exports = (options) => {
//   return {
//     loaders: utils.cssLoaders({
//       // sourceMap: use sourcemao or not.
//       sourceMap: options && sourceMapEnabled,
//       // useVue: use vue-style-loader or not
//       useVue: options && options.useVue,
//       // usePostCSS: use postcss to compile styles.
//       usePostCSS: options && options.usePostCSS
//     }),
//     cssSourceMap: sourceMapEnabled,
//     cacheBusting: config.dev.cacheBusting,
//     lang: {
//       less: ["less-loader"]
//     },
//     // 👇 从 options 中取出 vue 实例并返回
//     vue: options && options.vue
//   }
// }

const buildConfig = require('./config') // 👈 使用 buildConfig 命名避免冲突
module.exports = (options) => {
  const config = {
    cacheDirectory: path.resolve(__dirname, '../node_modules/.cache/vue-loader'),
    cssSourceMap: options && options.sourceMap,
    cacheIdentifier: JSON.stringify({
      vue: require('vue/package.json').version,
      vueLoader: require('vue-loader/package.json').version
    }),
    loaders: utils.cssLoaders({
      sourceMap: options && options.sourceMap,
      useVue: options && options.useVue,
      usePostCSS: options && options.usePostCSS
    }),
    postcss: options && options.postcss,
    compilerOptions: options && options.compilerOptions,
    compilerModules: options && options.compilerModules,
    optimizeSSR: options && options.optimizeSSR,
    transformAssetUrls: {
      video: ['src', 'poster'],
      source: 'src',
      img: 'src',
      image: 'xlink:href'
    }
  };

  if (options && options.vue) {
    config.vue = options.vue;
  }

  return config;
};