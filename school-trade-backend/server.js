// server.js (CommonJS 版本)
const express = require('express');
const { WebSocketServer } = require('ws');
const { createProxyMiddleware } = require('http-proxy-middleware');

const app = express();
const PORT = Number(process.env.WS_PORT || 3001);
const HOST = process.env.WS_HOST || '0.0.0.0';
const backendTarget = process.env.BACKEND_BASE_URL || 'http://127.0.0.1:8080';
const rawUploadsRoute = process.env.UPLOADS_ROUTE || '/uploads';
const uploadsRoute = rawUploadsRoute.startsWith('/') ? rawUploadsRoute : `/${rawUploadsRoute}`;
const wsPrefix = (process.env.WS_PREFIX || 'webSocketServer').replace(/^\/+|\/+$/g, '');

// 反向代理到 Spring Boot 8080，讓 http://localhost:3001/uploads/** 能取到圖片
app.use(
    uploadsRoute,
    createProxyMiddleware({
        target: backendTarget,
        changeOrigin: true,
    })
);

const server = app.listen(PORT, HOST, () => {
    console.log(`Express + WS server running at http://${HOST === '0.0.0.0' ? '0.0.0.0' : HOST}:${PORT}`);
    console.log(`Proxying ${uploadsRoute} -> ${backendTarget}`);
});

const wss = new WebSocketServer({ server });
console.log(`WS server ready on ws(s)://<host>:${PORT}/${wsPrefix}/<userId>`);

const clients = new Map();
const inbox = new Map();

function parseUserIdFromUrl(url) {
    try {
        const path = new URL(url, 'ws://placeholder').pathname;
        const seg = path.split('/').filter(Boolean);
        if (!seg.length) return '';
        if (seg[0] === 'wsPrefix') return seg[1] || '';
        return seg[0];
    } catch { return ''; }
}

function queueMessage(to, payload) {
    const k = String(to);
    const list = inbox.get(k) || [];
    list.push(payload);
    inbox.set(k, list);
    console.log(`[QUEUE] -> ${k} (#${list.length})`, payload.type);
}

wss.on('connection', (ws, req) => {
    const userId = parseUserIdFromUrl(req.url || '/') || `guest-${Math.random().toString(36).slice(2,8)}`;
    clients.set(userId, ws);
    console.log(`[OPEN] ${userId} connected`);
    try { ws.send(JSON.stringify({ type: 'welcome', msg: `connected as ${userId}` })); } catch {}

    const pending = inbox.get(String(userId));
    if (pending && pending.length) {
        console.log(`[FLUSH] deliver ${pending.length} message(s) to ${userId}`);
        for (const msg of pending) {
            try { ws.send(JSON.stringify(msg)); } catch (e) { console.warn('send fail', e); }
        }
        inbox.delete(String(userId));
    }

    ws.on('message', (raw) => {
        let data;
        try { data = JSON.parse(raw.toString()); } catch { return; }

        const type = data && data.type ? String(data.type) : 'chat';
        const ts = Date.now();
        let payload;

        if (type === 'chat') {
            const to = data && data.to != null ? String(data.to) : '';
            if (!to || typeof data.text !== 'string') {
                try { ws.send(JSON.stringify({ type: 'error', msg: 'invalid chat' })); } catch {}
                return;
            }
            payload = { type: 'chat', from: String(userId), to, text: String(data.text), ts };
            return deliver(to, payload, ws, userId, type);
        }

        if (type === 'TRADE_CARD') {
            const targetTo = data && data.to != null ? String(data.to) : '';
            if (!targetTo) {
                try { ws.send(JSON.stringify({ type: 'error', msg: 'TRADE_CARD missing "to"' })); } catch {}
                return;
            }
            payload = { ...data, type: 'TRADE_CARD', from: String(userId), to: targetTo, ts };
            return deliver(targetTo, payload, ws, userId, 'TRADE_CARD');
        }

        if (type === 'IMAGE') {
            const targetTo = data && data.to != null ? String(data.to) : '';
            if (!targetTo) {
                try { ws.send(JSON.stringify({ type: 'error', msg: 'IMAGE missing "to"' })); } catch {}
                return;
            }
            payload = { ...data, type: 'IMAGE', from: String(userId), to: targetTo, ts };
            return deliver(targetTo, payload, ws, userId, 'IMAGE');
        }

        if (type === 'LOCATION') {
            const targetTo = data && data.to != null ? String(data.to) : '';
            if (!targetTo) {
                try { ws.send(JSON.stringify({ type: 'error', msg: 'LOCATION missing "to"' })); } catch {}
                return;
            }
            payload = {
                ...data,
                type: 'LOCATION',
                from: String(userId),
                to: targetTo,
                location: data.location,
                ts
            };
            deliver(targetTo, payload, ws, userId, 'LOCATION');
            return;
        }
        const targetTo = data && data.to != null ? String(data.to) : '';
        if (!targetTo) {
            try { ws.send(JSON.stringify({ type: 'error', msg: `missing "to" for ${type}` })); } catch {}
            return;
        }
        payload = { ...data, type, from: String(userId), to: targetTo, ts };
        deliver(targetTo, payload, ws, userId, type);
    });

    ws.on('close', () => {
        clients.delete(userId);
        console.log(`[CLOSE] ${userId}`);
    });
});

function deliver(to, payload, ws, userId, kind) {
    const msg = { ...payload, from: String(userId), to: String(to) };
    const toWs = clients.get(String(to));
    const fromWs = ws;

    if (toWs && toWs.readyState === toWs.OPEN) {
        try { toWs.send(JSON.stringify(msg)); } catch (e) { console.warn('send to peer fail', e); }
    } else {
        queueMessage(String(to), msg);
        try { fromWs && fromWs.send(JSON.stringify({ type: 'queued', to, ts: msg.ts, kind })); } catch {}
    }

}
