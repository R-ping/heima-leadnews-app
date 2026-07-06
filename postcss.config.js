// https://github.com/michael-ciniawsky/postcss-load-config

export default {
  plugins: {
    'postcss-plugin-px2rem': {
      rootValue: 75,
      minPixelValue: 1.01
    }
  }
}
