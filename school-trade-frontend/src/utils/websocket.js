// src/utils/websocket.js
// --- WebSocket 工具模块（适用于 Vue CLI）---

let socket = null;

// WebSocket 服务器基础地址
const BASE_WS = 'ws://localhost:3001/webSocketServer';

/**
 * 拼接 URL
 * @param {string} a - 基础路径
 * @param {string} b - 附加路径
 */
function join(a, b) {
    return String(a).replace(/\/+$/, '') + '/' + String(b).replace(/^\/+/, '');
}

/**
 * 建立 WebSocket 连接
 * @param {string|number} userId - 用户 ID
 * @param {Function} onMessage - 消息回调函数
 * @returns {WebSocket} socket 实例
 */
export function connectWebSocket(userId, onMessage) {
    const url = join(BASE_WS, encodeURIComponent(String(userId)));
    console.log('[WS connect]', url); // ★ 记录连接的用户 ID

    // 若已有连接则关闭
    try {
        socket && socket.close();
    } catch (_) {}

    // 创建新 WebSocket 实例
    socket = new WebSocket(url);

    socket.onopen = () => {
        console.log('[WS open]');
    };

    socket.onmessage = (evt) => {
        let data = evt && evt.data ? evt.data : evt;
        try {
            data = JSON.parse(data);
        } catch (_) {}

        const type = data && data.type ? String(data.type) : 'chat';
        const event = { ...data, type };

        // ★ 不论是 chat 或 TRADE_CARD，都回调事件
        if (typeof onMessage === 'function') {
            onMessage(event);
        }
    };

    socket.onclose = () => {
        console.log('[WS closed]');
    };

    socket.onerror = (err) => {
        console.error('[WS error]', err);
    };

    // ★ 返回 socket 实例供外部使用
    return socket;
}

/**
 * 发送聊天消息
 * @param {string|number} to - 目标用户 ID
 * @param {string} text - 聊天内容
 */
export function sendChat(to, text) {
    if (!socket || socket.readyState !== WebSocket.OPEN) return;

    socket.send(
        JSON.stringify({
            type: 'chat',
            to: String(to),
            text: String(text || ''),
        })
    );
}

/**
 * 发送自定义 Payload（可用于交易、系统消息等）
 * @param {Object} obj - 要发送的对象
 * @returns {boolean} 是否发送成功
 */
export function sendPayload(obj) {
    if (!obj) {
        console.warn('[WS] sendPayload: empty payload');
        return false;
    }

    if (!socket) {
        console.warn('[WS] not connected');
        return false;
    }

    if (socket.readyState !== WebSocket.OPEN) {
        console.warn('[WS] not open:', socket.readyState);
        return false;
    }

    socket.send(JSON.stringify(obj));
    return true;
}
