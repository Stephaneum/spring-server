module.exports = {
    assetsDir: 'static',
    devServer: {
        proxy: {
          "/api": {
            target: 'http://localhost:9050',
            changeOrigin: true
          }
        }
    }
}