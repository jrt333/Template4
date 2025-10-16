// src/stores/chatStoreLite.js
// 以雙方固定順序存一個會話訊息清單：chat:<minId>:<maxId>
// 並以 chatIndex:<selfId> 存會話索引：[{peerId, lastText, lastTs, unread}]

function threadKey(a, b) {
    var x = String(a)
    var y = String(b)
    return 'chat:' + (x < y ? x : y) + ':' + (x < y ? y : x)
}
function indexKey(selfId) {
    return 'chatIndex:' + String(selfId)
}

function loadJSON(key, fallback) {
    try {
        var raw = localStorage.getItem(key)
        return raw ? JSON.parse(raw) : (typeof fallback !== 'undefined' ? fallback : null)
    } catch (e) {
        return (typeof fallback !== 'undefined' ? fallback : null)
    }
}
function saveJSON(key, v) {
    try { localStorage.setItem(key, JSON.stringify(v)) } catch (e) {}
}

function loadThread(a, b) {
    var data = loadJSON(threadKey(a, b), [])
    return data || []
}
function saveThread(a, b, messages) {
    saveJSON(threadKey(a, b), messages || [])
}

function loadIndex(selfId) {
    var data = loadJSON(indexKey(selfId), [])
    return data || []
}
function saveIndex(selfId, list) {
    saveJSON(indexKey(selfId), list || [])
}

// 會話摘要：若是小卡顯示簡短提示
function previewText(msg) {
    if (msg.kind === 'image') {
        return '[Image]'
    }
    if (msg.kind === 'card' && msg.card) {
        const c = msg.card.content || {}
        const price = c.priceCents ? ' $' + (c.priceCents / 100).toFixed(2) : ''
        return `交易小卡#${msg.card.itemId || ''} ${c.meetPlace || ''}${price}`
    }if (msg.kind === 'location' && msg.location) {
        return `📍 ${msg.location.name || 'Location'}`
    }
    return msg.text || ''
}

// 標準化訊息
function normalizeMessage(payload, fromHint) {
    payload = payload || {}
    let kind = payload.kind

    if (!kind) {
        if (payload.text) kind = 'text'
        else if (payload.card) kind = 'card'
        else if (payload.url) kind = 'image'   // ✅ 新增這行
        else kind = 'sys'
    }

    return {
        from: payload.from || fromHint || 'sys',
        ts: Number(payload.ts || Date.now()),
        kind,
        text: payload.text ? String(payload.text) : '',
        card: payload.card || null,
        url: payload.url || null,                // ✅ 新增這行
        location: payload.location || null
    }
}

// —— 對話訊息層（單一會話使用） ——
export function createChatStore(selfId, peerId) {
    var state = {
        selfId: String(selfId || ''),
        peerId: String(peerId || ''),
        messages: loadThread(selfId, peerId)
    }

    function addMessage(payload) {
        const msg = normalizeMessage(payload)
        state.messages.push(msg)
        saveThread(state.selfId, state.peerId, state.messages)

        if (msg.from !== 'sys') {
            const isPeer = (msg.from === 'peer')
            upsertIndex(state.selfId, state.peerId, {
                lastText: previewText(msg),
                lastTs: msg.ts,
                incUnread: isPeer
            })
        }
    }

    return { state: state, addMessage: addMessage }
}

// —— 上/下行更新索引 ——
function upsertIndex(selfId, peerId, opts) {
    opts = opts || {}
    var sid = String(selfId)
    var pid = String(peerId)
    var idx = loadIndex(sid)
    var i, row = null
    for (i = 0; i < idx.length; i++) {
        if (String(idx[i].peerId) === pid) { row = idx[i]; break }
    }
    if (!row) {
        row = { peerId: pid, lastText: '', lastTs: 0, unread: 0 }
        idx.push(row)
    }
    if (typeof opts.lastText !== 'undefined' && opts.lastText !== null) {
        row.lastText = String(opts.lastText)
    }
    row.lastTs = typeof opts.lastTs === 'number' ? opts.lastTs : Date.now()
    if (opts.incUnread) {
        row.unread = Number(row.unread || 0) + 1
    }
    idx.sort(function (a, b) { return (b.lastTs || 0) - (a.lastTs || 0) })
    saveIndex(sid, idx)
}

export function listConversations(selfId) {
    return loadIndex(selfId)
}
export function resetUnread(selfId, peerId) {
    var sid = String(selfId)
    var pid = String(peerId)
    var idx = loadIndex(sid)
    for (var i = 0; i < idx.length; i++) {
        if (String(idx[i].peerId) === pid) {
            idx[i].unread = 0
            saveIndex(sid, idx)
            break
        }
    }
}

// —— 全局快捷 API ——
// 收到別人 -> 加到對應 thread
export function recordIncoming(selfId, fromId, dataOrText, ts) {
    var messages = loadThread(selfId, fromId)
    const payload = (dataOrText && typeof dataOrText === 'object')
        ? dataOrText
        : { text: String(dataOrText || ''), ts: ts }
    const msg = normalizeMessage(payload, 'peer')
    messages.push(msg)
    saveThread(selfId, fromId, messages)
    upsertIndex(selfId, fromId, { lastText: previewText(msg), lastTs: msg.ts, incUnread: true })
}

// 自己發送 -> 加到對應 thread
export function recordOutgoing(selfId, toId, dataOrText, ts) {
    var messages = loadThread(selfId, toId)
    const payload = (dataOrText && typeof dataOrText === 'object')
        ? dataOrText
        : { text: String(dataOrText || ''), ts: ts }
    const msg = normalizeMessage(payload, 'me')
    messages.push(msg)
    saveThread(selfId, toId, messages)
    upsertIndex(selfId, toId, { lastText: previewText(msg), lastTs: msg.ts, incUnread: false })
}
