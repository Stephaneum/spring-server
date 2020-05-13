const MomentLocalesPlugin = require('moment-locales-webpack-plugin');

module.exports = {
    assetsDir: 'static',
    devServer: {
        proxy: {
          "/api": {
            target: 'http://localhost:9050',
            changeOrigin: true
          }
        }
    },
    configureWebpack: {
        plugins: [
            new MomentLocalesPlugin({ localesToKeep: ['de'] })
        ]
    }
}