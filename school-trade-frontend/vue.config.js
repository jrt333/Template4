const webpack = require('webpack')

module.exports = {
    publicPath: './',
    assetsDir: 'static',
    productionSourceMap: false,
    configureWebpack: {
        plugins: [
            new webpack.ProvidePlugin({
                $: 'jquery',
                jQuery: 'jquery',
                'windows.jQuery': 'jquery'
            })
        ]
    },
    devServer: {
        port: 8081, // 建議改成 8081（避免和後端衝突）
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                pathRewrite: { '^/api': '' }
            },
            '/uploads': {
                target: 'http://localhost:8080', // 代理圖片請求
                changeOrigin: true,
                ws: false
            }
        }
    }
};
