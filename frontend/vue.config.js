const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8080,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8081', // 改为 explicitly IPv4
        changeOrigin: true,
        pathRewrite: { '^/api': '' }
      }
    }
  }
})
