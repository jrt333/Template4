import Vue from 'vue';
import App from './App.vue';
import router from './router';
import ElementUI from 'element-ui';
import enLocale from 'element-ui/lib/locale/lang/en'; // 引入英文语言包
import locale from 'element-ui/lib/locale';           // 引入 locale
import $ from 'jquery';
import 'element-ui/lib/theme-chalk/index.css';

import 'babel-polyfill';
import { ensureGlobalChat } from '@/utils/chatBus'
import { startGoogleOAuth } from './utils/googleAuth';

import { Message } from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import { initNotify } from '@/utils/notify';
import api from './api/index.js';
Vue.prototype.$api = api;


let globalData={
    userInfo:{
        nickname:''
    }
};
let sta={
    isLogin:false,
    adminName:''
};
Vue.prototype.$sta = sta;

Vue.prototype.$globalData=globalData;

Vue.config.productionTip = false;



Vue.use(ElementUI, {
    size: 'medium'
});

initNotify();

const appMessage = (options) => {
        if (typeof options === 'string') options = { message: options }
        return Message({
            duration: 3000,
            offset: 80,
            showClose: true,
            customClass: 'my-global-message',
            ...options,
        })
    }

// 重要：把四種快捷方法掛回去
;['success', 'warning', 'info', 'error'].forEach(type => {
    appMessage[type] = (msg, opts = {}) => appMessage({ type, message: msg, ...opts })
})

// 掛到 Vue 原型上
Vue.prototype.$message = appMessage




Vue.prototype.$globalData = Vue.observable({
    userInfo: { nickname: '' }
});

function hydrateAuthFromStorage () {
    try {
        const raw = sessionStorage.getItem('authUser') || localStorage.getItem('authUser');
        if (!raw) return null;
        const user = JSON.parse(raw);
        if (user) {
            Vue.prototype.$globalData.userInfo = user;
            const uid = String(user.id || user.userId || user.uid || '');
            if (uid) ensureGlobalChat(uid);   // ★ 冪等，不會重複連
        }
        return user;
    } catch (e) {
        return null;
    }
}
hydrateAuthFromStorage();





router.beforeEach((to, from, next) => {
    document.title = `${to.meta.title}`;
    // console.log(to.path,'userInfo:',Vue.prototype.$globalData.userInfo);
    const nickname = Vue.prototype.$globalData.userInfo.nickname;
    const needLogin = !nickname && (to.path === '/me'
        || to.path === '/message'
        || to.path === '/release'
        || to.path === '/order');

    if (!needLogin) {
        next();
        return;
    }
    api.getUserInfo().then(res=>{
        console.log('getUserInfo:',res);
        if(res.status_code!==1){
            const back = to.fullPath || '/index';
            startGoogleOAuth(back);
        }else {
            if (res.data && res.data.signInTime) {
                res.data.signInTime=res.data.signInTime.substring(0,10);
            }
            Vue.prototype.$globalData.userInfo=res.data;
            next();
        }
    }).catch(e=>{
        const back = to.fullPath || '/index';
        startGoogleOAuth(back);
    });
});

locale.use(enLocale);
Vue.use(ElementUI);

Vue.config.productionTip = false;

new Vue({
    router,
    render: h => h(App)
}).$mount('#app');
