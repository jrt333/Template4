import axios from 'axios';

const service = axios.create({
    timeout: 20000,
    baseURL: 'http://localhost:8080',
    withCredentials: true
});

/** ---------- helpers ---------- */
function computeUpiLike(obj) {
    const pick = k => (obj[k] || '').toString().trim();
    const direct = pick('UPI') || pick('upi');
    if (direct) return direct;
    const email = pick('Email_number') || pick('email');
    if (email) return email.split('@')[0]; // user@xxx -> user
    const acct = pick('accountNumber') || pick('account_number');
    return acct; // 退而求其次
}

function ensureUpiOnData(data) {
    // data 可能是 FormData / URLSearchParams / 物件 / JSON字串
    if (typeof FormData !== 'undefined' && data instanceof FormData) {
        const probe = (k) => (data.has(k) ? (data.get(k) || '').toString() : '');
        const tmp = {
            UPI: probe('UPI'),
            upi: probe('upi'),
            Email_number: probe('Email_number'),
            email: probe('email'),
            accountNumber: probe('accountNumber'),
            account_number: probe('account_number')
        };
        const v = computeUpiLike(tmp);
        if (v) { data.set('UPI', v); if (!probe('upi')) data.set('upi', v); }
        return data;
    }

    if (typeof URLSearchParams !== 'undefined' && data instanceof URLSearchParams) {
        const probe = (k) => (data.get(k) || '');
        const tmp = {
            UPI: probe('UPI'),
            upi: probe('upi'),
            Email_number: probe('Email_number'),
            email: probe('email'),
            accountNumber: probe('accountNumber'),
            account_number: probe('account_number')
        };
        const v = computeUpiLike(tmp);
        if (v) { data.set('UPI', v); if (!probe('upi')) data.set('upi', v); }
        return data;
    }

    if (typeof data === 'string') {
        try {
            const obj = JSON.parse(data);
            const v = computeUpiLike(obj);
            if (v) { obj.UPI = v; if (!obj.upi) obj.upi = v; }
            return JSON.stringify(obj);
        } catch (_) {
            return data; // 不是 JSON 字串就放過
        }
    }

    if (data && typeof data === 'object') {
        const v = computeUpiLike(data);
        if (v) { data.UPI = v; if (!data.upi) data.upi = v; }
        return data;
    }

    return data;
}

/** ---------- interceptors ---------- */
service.interceptors.request.use(
    (config) => {
        try {
            const method = (config.method || 'get').toLowerCase();
            const url = (config.url || '');
            // 簡單判斷：註冊/登入頁面可能的路徑（可按你後端實際路徑再加）
            const isSignUp =
                /\/user\/sign-in\b/i.test(url) ||
                /\/user\/register\b/i.test(url) ||
                /\/sign-?up\b/i.test(url);

            if (isSignUp && ['post', 'put', 'patch'].includes(method)) {
                config.data = ensureUpiOnData(config.data || {});
            }
        } catch (e) {
            console.warn('UPI injector warning:', e);
        }
        return config;
    },
    (error) => {
        console.log(error);
        return Promise.reject(error);
    }
);

service.interceptors.response.use(
    (response) => {
        if (response.status === 200) {
            return response.data;
        }
        return Promise.reject(response);
    },
    (error) => {
        console.log(error);
        return Promise.reject(error);
    }
);

export default service;
