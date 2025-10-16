// src/utils/chatBus.js
// 全站僅建立一次 WS 連線，把收到的訊息：
// 1) 寫入本地資料層（索引/未讀/訊息）
// 2) 廣播給訂閱者（例如 PrivateChat.vue 的 onBusEvent）

import {connectWebSocket} from '@/utils/websocket'
import {recordIncoming} from '@/stores/chatStoreLite'
import {desktopNotify, flashTitle, playSound, stopFlashTitle} from '@/utils/notify'

let _ws = null
let _selfId = null
const _subs = new Set()

let _activePeerId = null
let _visible = document.visibilityState === 'visible'
document.addEventListener('visibilitychange', () => {
    _visible = document.visibilityState === 'visible'
    if (_visible) stopFlashTitle()
})
export function setActivePeer(peerId) { _activePeerId = String(peerId || '') }

export function ensureGlobalChat (selfId) {
    const id = String(selfId || '')
    if (!id) return
    if (_ws && _selfId === id) return   // 已連上同一個使用者
    _selfId = id
    _ws = connectWebSocket(id, onWSMessage)  // onWSMessage 會拿到已解析且帶 type 的物件
}

// 統一廣播工具
function _emitToSubs (evt) {
    _subs.forEach(fn => { try { fn(evt) } catch (_) {} })
}

function doNotifyPreview(e) {
    const body = e.text ? String(e.text) : (e.card ? '[交易小卡]' : '[新消息]')
    desktopNotify({ title: `來自用戶 ${e.from}`, body })
    playSound()
    flashTitle('【新消息】')
}


function shouldNotify(fromId) {
    if (!_visible) return true
    return String(fromId) !== String(_activePeerId)
}


function onWSMessage (evt) {
    // connectWebSocket 已經 JSON.parse 過，且補上了 type（見你目前的 websocket.js）
    // 這裡保險再正規化一次
    const e = evt && typeof evt === 'object' ? evt : {}
    const type = e.type ? String(e.type) : 'chat'
    const from = e.from != null ? String(e.from) : ''
    const ts   = e.ts || Date.now()

    if (!type || !from) {
        // 無效事件不處理
        return
    }

    if (type === 'chat') {
        // 寫入本地 thread（新版 chatStoreLite 可收物件）
        recordIncoming(_selfId, from, { kind: 'text', text: String(e.text || ''), ts })
        // 廣播給頁面（例如 privateChat.vue 的 onBusEvent）
        _emitToSubs({ type: 'chat', from, to: e.to, text: String(e.text || ''), ts })

    } else if (type === 'TRADE_CARD') {
        // 交易小卡：把整包 payload 當作 card 存
        recordIncoming(_selfId, from, { kind: 'card', card: e, ts })
        _emitToSubs({ ...e, type: 'TRADE_CARD', from, ts })

    }else if (type === 'chat-image') {
        // ★ 這是重點：把圖片當成 image 類型存起來＆廣播
        recordIncoming(_selfId, from, {kind: 'image', url: e.url, ts})
        _emitToSubs({...e, type: 'IMAGE', from, ts})
    }
    else if (type === 'LOCATION') {
        // 👇 新增：处理位置消息
        const locationData = {
            kind: 'location',
            location: e.location || { name: e.locationName, url: e.locationUrl },
            ts
        }
        recordIncoming(_selfId, from, locationData)
        _emitToSubs({ ...e, type: 'LOCATION', from, ts })
    }else{
        // 其他型別先也寫入/廣播（視需求可加更多分支）
        recordIncoming(_selfId, from, { kind: e.card ? 'card' : (e.text ? 'text' : 'sys'), card: e.card || null, text: e.text || '', ts })
        _emitToSubs({ ...e, type, from, ts })
    }
}

export function subscribeChat (fn) {
    _subs.add(fn)
    return () => _subs.delete(fn)
}
