// src/utils/notify.js
let _titleTimer = null;
let _origTitle = document.title;
let _audio;

export function initNotify() {
    if ('Notification' in window && Notification.permission === 'default') {
        try { Notification.requestPermission(); } catch (_) {}
    }
    try {
        _audio = new Audio('/notify.mp3'); // 放到 public/notify.mp3
        _audio.preload = 'auto';
    } catch (_) {}
}

export function playSound() { try { _audio && _audio.play(); } catch (_) {} }

export function flashTitle(txt = '【新消息】') {
    clearInterval(_titleTimer);
    let on = false;
    _titleTimer = setInterval(() => {
        document.title = on ? `${txt} ${_origTitle}` : _origTitle;
        on = !on;
    }, 800);
}

export function stopFlashTitle() {
    clearInterval(_titleTimer);
    _titleTimer = null;
    document.title = _origTitle;
}

export function desktopNotify({ title = '新消息', body = '', icon = '/favicon.ico' } = {}) {
    if (!('Notification' in window)) return;
    if (Notification.permission === 'granted') {
        try { new Notification(title, { body, icon }); } catch (_) {}
    }
}
