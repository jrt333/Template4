<template>
  <div class="chat-page">
    <app-head />
    <div class="chat-wrapper">
      <!-- 左側：會話列表 -->
      <aside class="sidebar" :style="{ width: sidebarWidth + 'px' }" v-show="sidebarWidth > 0">
        <div class="sidebar-header">Messages</div>
        <div v-if="!conversations.length" class="sidebar-empty">No conversations yet</div>
        <ul v-else class="conv-list">
          <li
              v-for="row in conversations"
              :key="row.peerId"
              :class="{ active: String(row.peerId) === String(peerId) }"
              @click="switchPeer(row.peerId)"
          >
            <div class="conv-item">
              <!-- 用户头像 -->
              <div class="avatar-wrapper">
                <img :src="getUserAvatar(row.peerId)" :alt="getUserName(row.peerId)" class="avatar" />
                <span v-if="row.unread" class="unread-badge">{{ row.unread }}</span>
              </div>

              <!-- 对话信息 -->
              <div class="conv-info">
                <div class="conv-header">
                  <span class="conv-name">{{ getUserName(row.peerId) }}</span>
                  <span class="conv-time">{{ formatTs(row.lastTs) }}</span>
                </div>
                <div class="conv-preview">{{ row.lastText }}</div>
              </div>
            </div>
          </li>
        </ul>
      </aside>

      <!-- 拖动分隔条 -->
      <div
          class="resize-handle"
          @mousedown="startResize"
          :class="{ 'resizing': isResizing }"
          v-show="sidebarWidth > 0"
      ></div>

      <!-- 右側：聊天區 -->
      <section class="main">
        <!-- 顶部栏 -->
        <div class="topbar">
          <!-- 返回按钮 -->
          <button class="back-btn" @click="goBack" title="Go back">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="19" y1="12" x2="5" y2="12"></line>
              <polyline points="12 19 5 12 12 5"></polyline>
            </svg>
          </button>

          <!-- 切换侧边栏按钮 -->
          <button class="toggle-sidebar-btn" @click="toggleSidebar" :title="sidebarWidth === 0 ? 'Show conversations' : 'Hide conversations'">
            <svg v-if="sidebarWidth === 0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="3" y1="12" x2="21" y2="12"></line>
              <line x1="3" y1="6" x2="21" y2="6"></line>
              <line x1="3" y1="18" x2="21" y2="18"></line>
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>

          <div v-if="peerId" class="peer-info" @click="navigateToProfile(peerId)">
            <img :src="getUserAvatar(peerId)" :alt="getUserName(peerId)" class="peer-avatar" />
            <div>
              <div class="peer-name">{{ getUserName(peerId) }}</div>
              <div class="peer-id">ID: {{ peerId }}</div>
            </div>
          </div>
          <div v-else class="no-chat-selected">
            <span>Select a conversation to start chatting</span>
          </div>
        </div>

        <!-- 消息列表 -->
        <div ref="log" class="log">
          <div v-if="!peerId" class="empty-chat-state">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="empty-icon">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
            </svg>
            <div class="empty-text">Select a conversation to start chatting</div>
          </div>

          <div v-else v-for="(m,i) in messages" :key="i" class="msg" :class="m.from">

            <!-- 只顯示「純文字」系統訊息；final 讓它往下走渲染卡片 -->
            <div v-if="m.from === 'sys' && m.kind !== 'final'" class="sys-msg">
              <span class="sys-text">{{ m.text }}</span>
            </div>

            <!-- 普通/卡片類訊息（包含 sys+final ） -->
            <template v-else>
              <!-- 头像：系統訊息不顯示頭像 -->
              <img
                  v-if="m.from !== 'sys'"
                  :src="getUserAvatar(m.from === 'me' ? selfId : peerId)"
                  :alt="getUserName(m.from === 'me' ? selfId : peerId)"
                  class="msg-avatar"
                  @click="navigateToProfile(m.from === 'me' ? selfId : peerId)"
              />

              <div class="msg-content">
                <!-- 用戶名：系統訊息不顯示名字 -->
                <div
                    v-if="m.from !== 'sys'"
                    class="msg-name"
                    :class="{ 'msg-name-right': m.from === 'me' }"
                    @click="navigateToProfile(m.from === 'me' ? selfId : peerId)"
                >
                  {{ getUserName(m.from === 'me' ? selfId : peerId) }}
                </div>

                <!-- 氣泡 -->
                <div class="bubble">
                  <!-- 文字 -->
                  <template v-if="m.kind === 'text' || (!m.kind && m.text)">
                    <div class="text">{{ m.text }}</div>
                  </template>

                  <!-- 位置 -->
                  <template v-else-if="m.kind === 'location' && m.location">
                    <div class="location-msg">
                      <div class="location-header">
                        <svg class="location-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                          <circle cx="12" cy="10" r="3"></circle>
                        </svg>
                        <span class="location-title">Location</span>
                      </div>
                      <div class="location-name">{{ m.location.name }}</div>
                      <a :href="m.location.url" target="_blank" class="location-link">View on map →</a>
                    </div>
                  </template>

                  <!-- 交易小卡 -->
                  <template v-else-if="m.kind === 'card' && m.card">
                    <div class="trade-card">
                      <div class="tc-title">TRADE</div>
                      <div class="tc-row"><b>ITEM ID</b>: #{{ m.card.itemId }}</div>
                      <div class="tc-row"><b>Time to meet</b>: {{ formatTs(m.card.content && m.card.content.meetTime) }}</div>
                      <div class="tc-row"><b>Place to meet</b>: {{ m.card.content && m.card.content.meetPlace }}</div>
                      <div class="tc-row" v-if="hasPrice(m.card)">
                        <b>PRICE</b>：${{ (Number(m.card.content.priceCents || 0) / 100).toFixed(2) }}
                      </div>
                      <div class="tc-row" v-else>
                        <b>EXCHANGE ITEM</b>：{{ m.card.content && m.card.content.tradeItem }}
                      </div>

                      <div
                          class="tc-actions"
                          v-if="String(selfId)===String(m.card.to) && (!m.card.status || m.card.status==='PENDING')"
                      >
                        <el-button size="mini" type="primary" @click="openAgreeConfirm(m.card)">Agree</el-button>
                        <el-button size="mini" @click="openTradeEdit(m.card)">Counter</el-button>
                      </div>
                    </div>
                  </template>

                  <!-- 完成卡（接在同一條鏈上，避免觸發 Unknown） -->
                  <template v-else-if="m.kind === 'final' && m.card">
                    <div class="trade-card">
                      <div class="tc-title">TRADE (for you)</div>
                      <div class="tc-row"><b>ITEM ID</b>: #{{ m.card.itemId }}</div>
                      <div class="tc-row"><b>Time to meet</b>: {{ formatTs(m.card.content && m.card.content.meetTime) }}</div>
                      <div class="tc-row"><b>Place to meet</b>: {{ m.card.content && m.card.content.meetPlace }}</div>
                      <div class="tc-row" v-if="hasPrice(m.card)">
                        <b>PRICE</b>：${{ (Number(m.card.content.priceCents || 0) / 100).toFixed(2) }}
                      </div>
                      <div class="tc-row" v-else>
                        <b>EXCHANGE ITEM</b>：{{ m.card.content && m.card.content.tradeItem }}
                      </div>

                      <div class="tc-actions" v-if="m.card.status !== 'DONE'">
                        <el-button size="mini" type="success" @click="openFinishDialog(m.card)">完成交易</el-button>
                      </div>

                      <div v-else class="tc-actions">
                        <el-tag type="success">已完成</el-tag>
                      </div>
                    </div>
                  </template>

                  <!-- 圖片 -->
                  <template v-else-if="m.kind === 'image' && m.url">
                    <el-image
                        :src="m.url"
                        :preview-src-list="[m.url]"
                        fit="contain"
                        style="max-width: 280px; max-height: 280px; border-radius:8px;"
                    >
                      <template #error>
                        <div class="text">[image unavailable]</div>
                      </template>
                    </el-image>
                  </template>

                  <!-- 其他未知 -->
                  <template v-else>
                    <div class="unknown-msg">
                      <div class="unknown-title">[Unknown Message Type]</div>
                      <div class="unknown-debug">
                        <div><strong>kind:</strong> {{ m.kind || '(none)' }}</div>
                        <div><strong>from:</strong> {{ m.from }}</div>
                        <div><strong>Complete data:</strong></div>
                        <pre class="unknown-json">{{ JSON.stringify(m, null, 2) }}</pre>
                      </div>
                    </div>
                  </template>

                  <!-- 時間 -->
                  <div class="ts">{{ formatTs(m.ts) }}</div>
                </div>
              </div>
            </template>

          </div>
        </div>


        <!-- 输入框 -->
        <div v-if="peerId" class="composer">
          <!-- 加号按钮 -->
          <div class="plus-wrapper">
            <button class="plus-btn" @click="showPlusMenu = !showPlusMenu">
              <svg v-if="!showPlusMenu" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="5" x2="12" y2="19"></line>
                <line x1="5" y1="12" x2="19" y2="12"></line>
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>

            <!-- 加号菜单 -->
            <div v-if="showPlusMenu" class="plus-menu">
              <div class="plus-menu-item" @click="openMapDialog">
                <svg class="plus-menu-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                  <circle cx="12" cy="10" r="3"></circle>
                </svg>
                <span>Location</span>
              </div>

              <input ref="imgInput" type="file" accept="image/*" style="display:none" @change="pickImage">
              <button @click="$refs.imgInput && $refs.imgInput.click()">📷</button>

            </div>
          </div>

          <!-- 输入框 -->
          <div class="input-wrapper">
            <input
                v-model="input"
                @keyup.enter="send"
                placeholder="Type a message..."
                class="input-field"
            />
          </div>

          <!-- 发送按钮 -->
          <button class="send-btn" @click="send" :disabled="!input.trim()">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="22" y1="2" x2="11" y2="13"></line>
              <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
            </svg>
          </button>
        </div>
      </section>
    </div>

    <!-- 地图位置对话框 -->
    <el-dialog
        :visible.sync="showMapDialog"
        append-to-body
        :modal-append-to-body="true"
        :lock-scroll="false"
        :close-on-click-modal="false"
        title="Share Location"
        width="460px"
    >
      <div>
        <div style="margin-bottom: 16px;">
          <label style="display: block; font-weight: 500; margin-bottom: 8px; font-size: 14px;">
            Enter Classroom/Building
          </label>
          <el-input
              v-model="classroomInput"
              placeholder="e.g., Building 201, Room 301"
              @keyup.enter.native="sendLocation"
          />
        </div>

        <div style="color: #999; font-size: 12px; display: flex; align-items: center; gap: 4px;">
          <svg style="width: 16px; height: 16px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
            <circle cx="12" cy="10" r="3"></circle>
          </svg>
          <span>This will search Auckland University map</span>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="showMapDialog = false">Cancel</el-button>
        <el-button type="primary" @click="sendLocation" :disabled="!classroomInput.trim()">Send</el-button>
      </span>
    </el-dialog>

    <trade-dialog
        :visible="showTradeDialog"
        :item-id="editingCard && editingCard.itemId"
        :seller-id="selfId"
        :default-price-dollar="(draft.priceCents != null ? (draft.priceCents/100).toFixed(2) : '')"
        :idle-trade="draftIdleTrade"
        @close="showTradeDialog=false"
        @created="onTradeEdited"
        @error="onTradeEditError"
    />


    <!-- 完成交易对话框 -->
    <el-dialog
        :visible.sync="showFinishDialog"
        append-to-body
        :modal-append-to-body="true"
        :lock-scroll="false"
        :close-on-click-modal="false"
        title="Confirm this trade?"
        width="460px"
    >

      <div v-if="finishingCard">
        <p><b>ITEM ID</b>: #{{ finishingCard.itemId }}</p>
        <p><b>Time to meet</b>: {{ formatTs(finishingCard.content && finishingCard.content.meetTime) }}</p>
        <p><b>Place to meet</b>: {{ finishingCard.content && finishingCard.content.meetPlace }}</p>

        <p v-if="hasPrice(finishingCard)">
          <b>PRICE</b>：${{ (Number(finishingCard.content.priceCents || 0) / 100).toFixed(2) }}
        </p>
        <p v-else>
          <b>EXCHANGE ITEM</b>：{{ finishingCard.content && finishingCard.content.tradeItem }}
        </p>

        <div style="margin-top: 10px;">
          <div style="margin-bottom:6px;"><b>給對方評分</b></div>
          <el-rate v-model="rateForm.stars" :max="5" show-text />
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="showFinishDialog=false">取消</el-button>
        <el-button type="primary" :loading="finishSubmitting" @click="submitFinish">提交</el-button>
      </span>
    </el-dialog>

    <!-- 交易编辑对话框 -->
    <trade-dialog
        :visible="showTradeDialog"
        :item-id="editingCard && editingCard.itemId"
        :seller-id="selfId"
        :default-price-dollar="(draft.priceCents != null ? (draft.priceCents/100).toFixed(2) : '')"
        @close="showTradeDialog=false"
        @created="onTradeEdited"
        @error="onTradeEditError"
    />

    <!-- 确认交易对话框 -->
    <el-dialog
        :visible.sync="showAgreeConfirm"
        append-to-body
        :modal-append-to-body="true"
        :lock-scroll="false"
        :close-on-click-modal="false"
        :show-close="false"
        width="420px"
        title="Confirm this trade?"
    >
      <div v-if="confirmingCard" class="space-y-2">
        <p><b>ITEM ID</b>: #{{ confirmingCard.itemId }}</p>
        <p><b>Time to meet</b>: {{ formatTs(confirmingCard.content && confirmingCard.content.meetTime) }}</p>
        <p><b>Place to meet</b>: {{ confirmingCard.content && confirmingCard.content.meetPlace }}</p>
        <p v-if="hasPrice(confirmingCard)">
          <b>PRICE</b>：${{ (Number(confirmingCard.content.priceCents || 0) / 100).toFixed(2) }}
        </p>
        <p v-else>
          <b>EXCHANGE ITEM</b>：{{ confirmingCard.content && confirmingCard.content.tradeItem }}
        </p>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="showAgreeConfirm=false">Cancel</el-button>
        <el-button type="primary" @click="confirmAgree">Confirm</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import AppHead from '../common/AppHeader.vue'
import TradeDialog from '../common/TradeDialog.vue'
import { createChatStore, listConversations, resetUnread, recordOutgoing } from '@/stores/chatStoreLite'
import { sendChat, sendPayload } from '@/utils/websocket'
import { ensureGlobalChat, subscribeChat, setActivePeer } from '@/utils/chatBus'
import request from "@/utils/request"

export default {
  name: 'PrivateChat',
  components: {
    AppHead,
    TradeDialog
  },
  data () {
    return {
      showAgreeConfirm: false,
      confirmingCard: null,
      selfId: '',
      peerId: '',
      input: '',
      chat: null,
      conversations: [],
      unsub: null,
      showTradeDialog: false,
      editingCard: null,
      draft: {
        meetTime: '',
        meetPlace: '',
        priceCents: null,
        tradeItem: ''
      },
      draftIdleTrade: 1,
      showFinishDialog: false,
      finishingCard: null,
      rateForm: { stars: 0, note: '' },
      finishSubmitting: false,
      showMapDialog: false,
      classroomInput: '',
      showPlusMenu: false,
      userCache: {},
      defaultAvatarUrl: userId => `https://ui-avatars.com/api/?name=User+${userId}&background=random&size=128`,
      sidebarWidth: 320,
      isResizing: false,
      minSidebarWidth: 0,
      maxSidebarWidth: 500
    }
  },
  computed: {
    messages () { return this.chat ? this.chat.state.messages : [] }
  },
  async created () {
    const q = (this.$route && this.$route.query) ? this.$route.query : {}
    this.selfId = q.selfId ? String(q.selfId) : ''
    this.peerId = q.peerId ? String(q.peerId) : ''
    ensureGlobalChat(this.selfId)
    this.chat = createChatStore(this.selfId, this.peerId)
    await Promise.all([
      this.loadUserInfo(this.selfId),
      this.loadUserInfo(this.peerId)
    ])
    this.loadConversations()
  },
  async mounted () {
    this.unsub = subscribeChat(this.onBusEvent)
    resetUnread(this.selfId, this.peerId)
    await this.preloadAllUsers()
    this.$nextTick(this.scrollToBottom)
    document.addEventListener('mousemove', this.handleMouseMove)
    document.addEventListener('mouseup', this.handleMouseUp)
  },
  beforeDestroy () {
    this.unsub && this.unsub()
    document.removeEventListener('mousemove', this.handleMouseMove)
    document.removeEventListener('mouseup', this.handleMouseUp)
  },
  watch: {
    async '$route.query.peerId'(newPeerId) {
      this.peerId = newPeerId ? String(newPeerId) : ''
      this.chat = createChatStore(this.selfId, this.peerId)
      if (this.peerId) {
        await this.loadUserInfo(this.peerId)
      }
      this.loadConversations()
      resetUnread(this.selfId, this.peerId)
      this.$nextTick(this.scrollToBottom)
    },

    peerId(n) {
      setActivePeer(n)
      resetUnread(this.selfId, n)
    },

    messages () { this.$nextTick(this.scrollToBottom) },

    conversations: {
      deep: true,
      async handler() {
        await this.preloadAllUsers()
      }
    }
  },
  methods: {
    hasPrice (card) {
      return !!(card && card.content && card.content.priceCents != null)
    },

    openFinishDialog(card) {
      this.finishingCard = card
      this.rateForm = { stars: 0, note: '' }
      this.showFinishDialog = true
    },

    async loadUserInfo(userId) {
      if (!userId) return null
      if (this.userCache[userId] && this.userCache[userId].loading) {
        return new Promise(resolve => {
          const checkInterval = setInterval(() => {
            if (!this.userCache[userId] || !this.userCache[userId].loading) {
              clearInterval(checkInterval)
              resolve(this.userCache[userId])
            }
          }, 100)
        })
      }
      if (this.userCache[userId] && !this.userCache[userId].loading) {
        return this.userCache[userId]
      }
      this.$set(this.userCache, userId, {
        nickname: `User ${userId}`,
        avatar: this.defaultAvatarUrl(userId),
        loading: true
      })
      try {
        const response = await request.get('/user/' + userId)
        var userInfo = null
        if (response.data && response.data.status_code === 1 && response.data.data) {
          userInfo = response.data.data
        } else if (response.data && response.data.data) {
          userInfo = response.data.data
        } else if (response.data && response.data.userInfo) {
          userInfo = response.data.userInfo
        } else if (response.data && (response.data.nickname || response.data.id)) {
          userInfo = response.data
        }
        if (!userInfo) {
          throw new Error('无法解析用户信息')
        }
        var userData = {
          nickname: userInfo.nickname || userInfo.name || userInfo.username || 'User ' + userId,
          avatar: userInfo.avatar || userInfo.avatarUrl || this.defaultAvatarUrl(userId),
          id: userInfo.id || userId,
          loading: false
        }
        this.$set(this.userCache, userId, userData)
        return userData
      } catch (error) {
        console.error(`获取用户 ${userId} 信息失败:`, error)
        this.$set(this.userCache, userId, {
          nickname: `User ${userId}`,
          avatar: this.defaultAvatarUrl(userId),
          loading: false,
          error: true
        })
        return this.userCache[userId]
      }
    },
    getUserName(userId) {
      if (!userId) return 'Unknown'
      if (!this.userCache[userId]) {
        this.loadUserInfo(userId)
      }
      return this.userCache[userId] && this.userCache[userId].nickname ? this.userCache[userId].nickname : 'User ' + userId
    },
    getUserAvatar(userId) {
      if (!userId) return ''
      if (!this.userCache[userId]) {
        this.loadUserInfo(userId)
      }
      return this.userCache[userId] && this.userCache[userId].avatar ? this.userCache[userId].avatar : this.defaultAvatarUrl(userId)
    },
    async preloadAllUsers() {
      const userIds = new Set()
      if (this.selfId) userIds.add(this.selfId)
      if (this.peerId) userIds.add(this.peerId)
      this.conversations.forEach(conv => {
        if (conv.peerId) userIds.add(conv.peerId)
      })
      this.messages.forEach(msg => {
        if (msg.from && msg.from !== 'sys' && msg.from !== 'me' && msg.from !== 'peer') {
          userIds.add(msg.from)
        }
      })
      await Promise.all(
          Array.from(userIds).map(id => this.loadUserInfo(id))
      )
    },
    navigateToProfile(userId) {
      this.$router.push({
        name: 'UserProfile',
        query: { userId: userId }
      })
    },
    openMapDialog() {
      this.showMapDialog = true
      this.showPlusMenu = false
    },
    // 替换你现有的 sendLocation 方法
    async sendLocation() {
      const input = this.classroomInput.trim()
      if (!input) {
        this.$message.warning('Please enter a classroom or building name')
        return
      }

      console.log('==================== Sending Location ====================')

      // 检查 chat 对象
      if (!this.chat) {
        console.error('❌ this.chat does not exist!')
        this.chat = createChatStore(this.selfId, this.peerId)
        if (!this.chat) {
          this.$message.error('Chat initialization failed')
          return
        }
      }

      try {
        this.$message.info('Searching on map...')

        // 智能处理建筑号和房间号搜索
        let searchQueries = [input]

        // 策略1: 如果是 "数字+字母-xxx" 格式（如 303s-g91），尝试多种变体
        const roomPattern = /^(\d+)([A-Za-z])?-(.+)$/
        const roomMatch = input.match(roomPattern)
        if (roomMatch) {
          const buildingNum = roomMatch[1]
          const buildingLetter = roomMatch[2] || ''
          const roomNum = roomMatch[3]

          // 优先搜索建筑号，因为房间信息在建筑结果中
          searchQueries = [
            buildingNum + buildingLetter,
            buildingNum + buildingLetter.toUpperCase(),
            'B' + buildingNum + buildingLetter,
            'B' + buildingNum + buildingLetter.toUpperCase(),
            input,
            roomNum,
            roomNum.toUpperCase(),
            buildingNum + '-' + roomNum,
            buildingNum + buildingLetter.toUpperCase() + '-' + roomNum.toUpperCase()
          ]
        }
        // 策略2: 如果是纯数字或"数字+字母"格式（如 405, 303, 303S），可能是建筑物
        else if (/^\d+[A-Za-z]?$/.test(input)) {
          searchQueries = [
            input,
            'B' + input,
            'B' + input.toUpperCase(),
            input + '-',
            input.toUpperCase(),
            input.toUpperCase() + '-'
          ]
        }
            // 策略3: 如果是地点名称（如 library, gym），直接搜索
        // 这些输入通常是纯字母，不包含数字
        else {
          searchQueries = [
            input,
            input.toLowerCase(),
            input.toUpperCase(),
            // 首字母大写
            input.charAt(0).toUpperCase() + input.slice(1).toLowerCase()
          ]
        }

        let results = []
        let successQuery = null
        let hasRoomNumber = input.indexOf('-') > -1

        // 尝试每个搜索词，直到找到结果
        for (let i = 0; i < searchQueries.length; i++) {
          const query = searchQueries[i]
          const takeCount = 50

          const apiUrl = 'https://api.mapsindoors.com/auckland/api/locations?q=' + encodeURIComponent(query) + '&take=' + takeCount

          const response = await fetch(apiUrl)

          if (!response.ok) {
            continue
          }

          const data = await response.json()

          if (data && data.length > 0) {
            results = data
            successQuery = query

            // 智能匹配：如果原始输入包含 "-"，尝试在结果中找到最匹配的
            if (input.indexOf('-') > -1) {
              const originalParts = input.toLowerCase().split('-')
              const buildingPart = originalParts[0]
              const roomNumber = originalParts.length > 1 ? originalParts[1] : ''
              const fullInputLower = input.toLowerCase()

              if (roomNumber) {
                let bestMatch = null
                let bestMatchScore = 0

                // 在所有结果中查找包含该房间号的项目
                for (let j = 0; j < data.length; j++) {
                  const item = data[j]
                  const props = item.properties || {}
                  const name = (props.name || item.name || '').toLowerCase()
                  const itemId = (item.id || '').toLowerCase()
                  const externalId = (props.externalId || '').toLowerCase()
                  const aliases = props.aliases || []

                  let aliasesStr = ''
                  if (Array.isArray(aliases) && aliases.length > 0) {
                    aliasesStr = aliases.map(function(a) {
                      return String(a).toLowerCase()
                    }).join(' ')
                  } else if (aliases) {
                    aliasesStr = String(aliases).toLowerCase()
                  }

                  const roomId = (props.roomId || '').toLowerCase()
                  const description = (props.description || '').toLowerCase()
                  const displayName = (props.displayName || '').toLowerCase()
                  const fullItemStr = JSON.stringify(item).toLowerCase()

                  const allIdentifiers = [name, itemId, externalId, aliasesStr, roomId, description, displayName]
                      .filter(function(s) { return s && s.length > 0 })
                      .join(' ')

                  let matchScore = 0

                  const hasFullRoomId = roomId === fullInputLower
                  const hasRoomNumber = roomId.indexOf(roomNumber) > -1

                  if (hasFullRoomId) {
                    matchScore = 100
                  } else if (hasRoomNumber) {
                    matchScore = 90
                  } else if (name === roomNumber || itemId === roomNumber || externalId === roomNumber) {
                    matchScore = 80
                  } else if (name.indexOf(roomNumber) > -1) {
                    matchScore = 70
                  } else if (itemId.indexOf(roomNumber) > -1 || externalId.indexOf(roomNumber) > -1) {
                    matchScore = 60
                  } else if (aliasesStr.indexOf(roomNumber) > -1) {
                    matchScore = 40
                  } else if (description.indexOf(roomNumber) > -1 || displayName.indexOf(roomNumber) > -1) {
                    matchScore = 30
                  } else if (allIdentifiers.indexOf(roomNumber) > -1) {
                    matchScore = 20
                  } else if (fullItemStr.indexOf(roomNumber) > -1) {
                    matchScore = 10
                  }

                  if (matchScore > 0 && buildingPart) {
                    if (roomId.indexOf(buildingPart) > -1 || allIdentifiers.indexOf(buildingPart) > -1) {
                      matchScore += 5
                    }
                  }

                  if (matchScore > bestMatchScore) {
                    bestMatchScore = matchScore
                    bestMatch = item
                  }
                }

                if (bestMatch) {
                  results = [bestMatch]
                }
              }
            }

            break
          }
        }

        let locationData = null

        if (results && results.length > 0) {
          const firstResult = results[0]
          const props = firstResult.properties || {}

          let mapUrl = ''

          if (firstResult.id) {
            mapUrl = 'https://maps.auckland.ac.nz/auckland/fa64ffa351cb4fe680fa2929/details/' + firstResult.id
          } else if (props.name) {
            mapUrl = 'https://maps.auckland.ac.nz/auckland/fa64ffa351cb4fe680fa2929/search?q=' + encodeURIComponent(props.name)
          } else {
            if (input.indexOf('-') === -1 && /^\d+[A-Za-z]?$/.test(input)) {
              mapUrl = 'https://maps.auckland.ac.nz/auckland/fa64ffa351cb4fe680fa2929/search?cat=buildings&q=' + encodeURIComponent(input)
            } else {
              mapUrl = 'https://maps.auckland.ac.nz/auckland/fa64ffa351cb4fe680fa2929/search?q=' + encodeURIComponent(input)
            }
          }

          const buildingInfo = props.building ? ' (' + props.building + ')' : ''
          const displayName = props.name || firstResult.name || input
          const fullName = displayName + buildingInfo

          let coordinates = null
          if (firstResult.geometry && firstResult.geometry.coordinates) {
            coordinates = {
              lng: firstResult.geometry.coordinates[0],
              lat: firstResult.geometry.coordinates[1]
            }
          }

          locationData = {
            id: firstResult.id || null,
            name: fullName,
            displayName: displayName,
            url: mapUrl,
            building: props.building || null,
            floor: props.floor || null,
            type: props.type || null,
            coordinates: coordinates
          }
        } else {
          let fallbackUrl = ''
          if (input.indexOf('-') === -1 && /^\d+[A-Za-z]?$/.test(input)) {
            fallbackUrl = 'https://maps.auckland.ac.nz/auckland/fa64ffa351cb4fe680fa2929/search?cat=buildings&q=' + encodeURIComponent(input)
          } else {
            fallbackUrl = 'https://maps.auckland.ac.nz/auckland/fa64ffa351cb4fe680fa2929/search?q=' + encodeURIComponent(input)
          }

          locationData = {
            id: null,
            name: input,
            displayName: input,
            url: fallbackUrl,
            building: null,
            floor: null,
            type: null,
            coordinates: null
          }
        }

        const timestamp = Date.now()

        // 发送到服务器
        const serverPayload = {
          clientMsgId: 'loc-' + timestamp + '-' + Math.random().toString(36).slice(2, 8),
          type: 'LOCATION',
          from: String(this.selfId),
          to: String(this.peerId),
          location: locationData,
          ts: timestamp
        }

        const sent = sendPayload(serverPayload)

        if (!sent) {
          throw new Error('WebSocket not connected, please refresh the page')
        }

        // 更新对话列表
        recordOutgoing(this.selfId, this.peerId, {
          kind: 'location',
          location: locationData,
          ts: timestamp
        })

        if (this.loadConversations) {
          this.loadConversations()
        }

        // 关闭对话框
        this.showMapDialog = false
        this.classroomInput = ''
        this.$message.success('Location sent: ' + locationData.displayName)

        // 滚动到底部
        this.$nextTick(function() {
          this.scrollToBottom()
        }.bind(this))

      } catch (error) {
        console.error('❌ Send failed:', error)
        this.$message.error(error.message || 'Failed to send location')
      }
    },


    async rateUser(userId, score) {
      const base = 'http://localhost:8080'
      const url = `${base}/user/${encodeURIComponent(userId)}/rating?score=${encodeURIComponent(score)}`
      const res = await fetch(url, { method: 'POST', credentials: 'include' })
      let text = ''
      try { text = await res.text() } catch (_) {}
      if (!res.ok) throw new Error(`HTTP ${res.status} ${text || ''}`.trim())
      return true
    },

    async submitFinish() {
      if (!this.finishingCard) return

      const stars = Number((this.rateForm && this.rateForm.stars) ? this.rateForm.stars : 0)
      if (!stars) {
        if (this.$message && typeof this.$message.warning === 'function') {
          this.$message.warning('請先選擇星等')
        } else {
          alert('請先選擇星等')
        }
        return
      }
      this.finishSubmitting = true

      const me = String(this.selfId || '');
      const from = this.finishingCard && this.finishingCard.from != null ? String(this.finishingCard.from) : '';
      const to = this.finishingCard && this.finishingCard.to != null ? String(this.finishingCard.to) : '';
      let toUserId = '';
      if (from && from !== me) toUserId = from;
      else if (to && to !== me) toUserId = to;
      else toUserId = String(this.peerId || (this.finishingCard && (this.finishingCard.sellerId || this.finishingCard.buyerId)) || '');

      try {
        await this.rateUser(Number(toUserId), stars)
        if (this.$message && typeof this.$message.success === 'function') {
          this.$message.success('評分成功')
        } else {
          alert('評分成功')
        }
        if (this.finishingCard) {
          this.$set(this.finishingCard, 'status', 'DONE')
        }
        this.showFinishDialog = false
        if (this.chat && typeof this.chat.addMessage === 'function') {
          this.chat.addMessage({
            from: 'sys',
            text: '已完成交易並評分 ' + stars + '★',
            ts: Date.now()
          })
        }
      } catch (e) {
        console.error(e)
        const msg = (e && e.message) ? e.message : '提交失敗'
        if (this.$message && typeof this.$message.error === 'function') {
          this.$message.error(msg)
        } else {
          alert(msg)
        }
      } finally {
        this.finishSubmitting = false
      }
    },


    openAgreeConfirm(card) {
      if (String(this.selfId) !== String(card.to)) return
      this.confirmingCard = card
      this.showAgreeConfirm = true
    },

    confirmAgree() {

      if (!this.confirmingCard) return
      const base = this.confirmingCard

      this.$set(base, 'status', 'FINAL')
      const info = {
        meetTime:  base.content && base.content.meetTime,
        meetPlace: base.content && base.content.meetPlace,
      }

      if (this.hasPrice(base)) {
        info.priceCents = base.content.priceCents
      } else {
        info.tradeItem = base.content && base.content.tradeItem
      }

      const now = Date.now()

      const selfPayload = {
        clientMsgId: 'final-self-' + now + '-' + Math.random().toString(36).slice(2,8),
        type: 'TRADE_FINAL',
        noEchoToSender: true,
        private: true,
        peerContext: String(base.from),
        tradeId: base.tradeId || base.id,
        itemId: base.itemId,
        from: String(this.selfId),
        to: String(this.selfId),
        content: info,
        ts: now,
        status: 'FINAL'
      }

      const peerPayload = {
        clientMsgId: 'final-peer-' + now + '-' + Math.random().toString(36).slice(2,8),
        type: 'TRADE_FINAL',
        private: true,
        peerContext: String(this.selfId),
        noEchoToSender: true,
        tradeId: base.tradeId || base.id,
        itemId: base.itemId,
        from: String(this.selfId),
        to: String(base.from),
        content: info,
        ts: now,
        status: 'FINAL'
      }
      this.chat.addMessage({ from: 'sys', kind: 'final', card: selfPayload, ts: selfPayload.ts })

      sendPayload && sendPayload(peerPayload)

      this.showAgreeConfirm = false
    },

    openTradeEdit (card) {
      console.log('[openTradeEdit] selfId=', this.selfId, ' card.to=', card && card.to)
      // 安全保險：只有接收者（seller）才可開窗
      if (String(this.selfId) !== String(card.to))
        console.warn('[openTradeEdit] blocked: selfId != card.to')

      this.editingCard = card || {}
      this.draft = {
        meetTime:  (card.content && card.content.meetTime)  || '',
        meetPlace: (card.content && card.content.meetPlace) || '',
        priceCents: (card.content && card.content.priceCents) != null
            ? Number(card.content.priceCents)
            : null,
        tradeItem: (card.content && card.content.tradeItem) || ''
      }
      this.draftIdleTrade = (card.content && card.content.priceCents != null) ? 1 : 2
      this.showTradeDialog = true
      console.log('[openTradeEdit] showTradeDialog ->', this.showTradeDialog)
    },

    onTradeEdited (resp) {
      console.log('[onTradeEdited] payload =', resp)
      try {
        var r = resp || {}
        var card = this.editingCard || {}

        // 標記原卡：已提出修改（隱藏按鈕）
        this.$set(card, 'status', 'COUNTERED')

        const clientMsgId = 'cli-' + Date.now() + '-' + Math.random().toString(36).slice(2,8)

        const newMeetTime  = (r.meetTime  !== undefined && r.meetTime  !== null && r.meetTime  !== '') ? r.meetTime  : this.draft.meetTime
        const newMeetPlace = (r.meetPlace !== undefined && r.meetPlace !== null && r.meetPlace !== '') ? r.meetPlace : this.draft.meetPlace

        // ★ 二選一：若回傳有 priceCents 就走價錢；否則走 exchange 文字
        const content = { meetTime: newMeetTime, meetPlace: newMeetPlace }
        if (r.priceCents !== undefined && r.priceCents !== null) {
          content.priceCents = Number(r.priceCents)
        } else if (r.tradeItem !== undefined) {
          content.tradeItem = r.tradeItem || this.draft.tradeItem || ''
        } else if (this.draft.priceCents != null) {
          content.priceCents = Number(this.draft.priceCents)
        } else {
          content.tradeItem = this.draft.tradeItem || ''
        }

        // ★★★ 這裡把方向明確寫死：from=賣家(selfId)，to=原買家(card.from)
        var updated = {
          clientMsgId,
          type: 'TRADE_CARD',
          tradeId: card.tradeId || card.id,
          itemId: card.itemId,
          from: String(this.selfId),          // ← seller
          to:   String(card.from),            // ← buyer
          content,
          ts: Date.now(),
          status: 'PENDING',
          noEchoToSender: true
        }

        console.log('[counter-offer] from=', updated.from, 'to=', updated.to)

        this.chat.addMessage({ from: 'me', kind: 'card', card: updated, ts: updated.ts })
        sendPayload(updated)  // 送給 server（成功與否都無所謂，本地已顯示）


        this.loadConversations()
        this.showTradeDialog = false
        this.pushSys('已提出新的交易條件（#' + (updated.tradeId || '') + '）', updated.ts)
      } catch (e) {
        this.onTradeEditError(e)
      }
    },

    onTradeEditError(e) {
      if (this.$message && typeof this.$message.error === 'function') {
        this.$message.error('提交修改失敗，請稍後重試')
      } else {
        alert('提交修改失敗，請稍後重試')
      }
      console.error(e)
    },

    loadConversations() {
      var list = listConversations(this.selfId) || []
      this.conversations = list
          .filter(function (r) { return r && typeof r === 'object' && r.peerId != null })
          .map(function (r) { return {
            peerId: String(r.peerId),
            lastText: String(r.lastText || ''),
            lastTs: Number(r.lastTs || 0),
            unread: Number(r.unread || 0)
          }})
    },
    switchPeer(pid) {
      if (String(pid) === String(this.peerId)) return
      this.$router.push({
        name: 'PrivateChat',
        query: { selfId: this.selfId, peerId: String(pid) }
      })
    },
    onBusEvent(e) {
      if (!this._seenMsgIds) this._seenMsgIds = new Set()
      const msgId = e && (e.clientMsgId || (e.ts + ':' + e.from + ':' + e.to))
      if (msgId && this._seenMsgIds.has(msgId)) return
      if (msgId) this._seenMsgIds.add(msgId)
      if (!e || (e.type !== 'chat' && e.type !== 'TRADE_CARD' && e.type !== 'TRADE_FINAL' && e.type !== 'LOCATION'&& e.type !== 'IMAGE')) return
      const involve =
          String(e.from) === String(this.peerId) ||
          String(e.to) === String(this.peerId) ||
          String(e.peerContext) === String(this.peerId)
      if (!involve) {
        this.loadConversations()
        return
      }
      const mine = (e.type === 'TRADE_FINAL' && e.private && String(e.to) === String(this.selfId)) ? true : String(e.from) === String(this.selfId)
      if (e.type === 'chat') {
        this.chat.addMessage({
          from: mine ? 'me' : 'peer',
          kind: 'text',
          text: e.text || '',
          ts: e.ts || Date.now()
        })
      } else if (e.type === 'IMAGE') {
        this.chat.addMessage({
          from: mine ? 'me' : 'peer',
          kind: 'image',
          url: e.url,
          ts: e.ts || Date.now()
        })
      }else if (e.type === 'TRADE_CARD') {
        this.chat.addMessage({
          from: mine ? 'me' : 'peer',
          kind: 'card',
          card: e,
          ts: e.ts || Date.now()
        })
      } else if (e.type === 'TRADE_FINAL') {
        const isPrivateToMe = e.private && String(e.to) === String(this.selfId)
        try {
          const tid = e.tradeId || e.itemId
          this.chat.state.messages.forEach(msg => {
            if (msg.kind === 'card' && msg.card) {
              const sameTrade = (msg.card.tradeId || msg.card.id) === tid
              const iAmReceiver = String(msg.card.to) === String(this.selfId)
              if (sameTrade && iAmReceiver) this.$set(msg.card, 'status', 'FINAL')
            }
          })
        } catch {}
        this.chat.addMessage({
          from: isPrivateToMe ? 'sys' : (mine ? 'me' : 'peer'),
          kind: 'final',
          card: e,
          ts: e.ts || Date.now()
        })
      } else if (e.type === 'LOCATION') {
        this.chat.addMessage({
          from: mine ? 'me' : 'peer',
          kind: 'location',
          location: e.location,
          ts: e.ts || Date.now()
        })
      }
      this.loadConversations()
      if (!mine) resetUnread(this.selfId, this.peerId)
      this.$nextTick(this.scrollToBottom)
    },

    async pickImage (e) {
      const f = e && e.target && e.target.files && e.target.files[0]
      if (!f) return
      try {
        const url = await this.uploadChatImage(f)
        await this.sendImage(url)
      } catch (err) {
        console.error(err)
        this.$message && this.$message.error && this.$message.error(err.message || 'Image upload failed')
      } finally {
        e.target.value = '' // 清空，避免同圖不觸發 change
      }
    },

    async uploadChatImage (file) {
      const form = new FormData()
      form.append('file', file)

      const res = await fetch('/api/upload/chat-image', {
        method: 'POST',
        body: form,
        credentials: 'include'
      })
      if (!res.ok) throw new Error('HTTP ' + res.status)

      const raw  = await res.json()
      const data = raw && (raw.data || raw.result || raw)
      if (!data || !data.url) throw new Error(raw.msg || raw.message || 'No URL')

      // 關鍵：用這次回應的最終 URL 取得「後端的 origin」
      const backendOrigin = new URL(res.url, window.location.href).origin
      // 如果後端回的是相對路徑，就用後端 origin 來拼；如果是完整 http(s) 就直接用
      return data.url.startsWith('http') ? data.url : (backendOrigin + data.url)
    },

    async sendImage (url) {
      const payload = {
        type: 'IMAGE',
        from: String(this.selfId),
        to:   String(this.peerId),
        url,
        ts: Date.now(),
        noEchoToSender: true      // ★ 不要 echo 回自己，避免重覆
      }

      // 本地即時顯示
      this.chat.addMessage({ from: 'me', kind: 'image', url, ts: payload.ts })
      // 通知對方
      sendPayload && sendPayload(payload)
      // 左側會話列表摘要
      recordOutgoing(this.selfId, this.peerId, '[Image]')
      this.loadConversations()
      this.$nextTick(this.scrollToBottom)
    },

    send() {
      const text = (this.input || '').trim()
      if (!text) return
      sendChat(this.peerId, text)
      recordOutgoing(this.selfId, this.peerId, text)
      this.loadConversations()
      this.input = ''
      this.$nextTick(this.scrollToBottom)
    },

    pushSys(text, ts) {
      this.chat && this.chat.addMessage({ from: 'sys', text: text, ts: ts })
    },
    scrollToBottom() {
      const el = this.$refs.log
      if (!el) return
      el.scrollTop = el.scrollHeight
    },
    formatTs(ts) {
      if (!ts) return ''
      try {
        const d = new Date(ts)
        const now = new Date()
        const diff = now - d
        if (diff < 86400000) {
          const pad = n => (n < 10 ? '0'+n : ''+n)
          return `${pad(d.getHours())}:${pad(d.getMinutes())}`
        } else if (diff < 604800000) {
          const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']
          return days[d.getDay()]
        } else {
          const pad = n => (n < 10 ? '0'+n : ''+n)
          return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`
        }
      } catch (e) { return '' }
    },
    startResize() {
      this.isResizing = true
    },
    handleMouseMove(e) {
      if (!this.isResizing) return
      let newWidth = e.clientX
      if (newWidth < 100) {
        newWidth = 0
      } else if (newWidth < this.minSidebarWidth) {
        newWidth = this.minSidebarWidth
      } else if (newWidth > this.maxSidebarWidth) {
        newWidth = this.maxSidebarWidth
      }
      this.sidebarWidth = newWidth
    },
    handleMouseUp() {
      this.isResizing = false
    },
    toggleSidebar() {
      if (this.sidebarWidth === 0) {
        this.sidebarWidth = 320
      } else {
        this.sidebarWidth = 0
      }
    },
    goBack() {
      this.$router.back()
    }
  }
}
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: #f5f5f5;
}

.chat-wrapper {
  flex: 1;
  min-height: 0;
  display: flex;
  overflow: hidden;
}

.sidebar {
  background: #fff;
  border-right: 1px solid #e5e5e5;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: width 0.3s ease;
}

.sidebar-header {
  padding: 16px 20px;
  font-size: 20px;
  font-weight: 600;
  border-bottom: 1px solid #e5e5e5;
}

.sidebar-empty {
  padding: 40px 20px;
  text-align: center;
  color: #999;
}

.conv-list {
  flex: 1;
  overflow-y: auto;
  list-style: none;
  padding: 0;
  margin: 0;
}

.conv-list li {
  cursor: pointer;
  transition: background 0.2s;
}

.conv-list li:hover {
  background: #f5f5f5;
}

.conv-list li.active {
  background: #e6f2ff;
}

.conv-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #ff4d4f;
  color: white;
  font-size: 11px;
  font-weight: 600;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
  border: 2px solid white;
}

.conv-info {
  flex: 1;
  min-width: 0;
}

.conv-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.conv-name {
  font-weight: 600;
  color: #222;
  font-size: 15px;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conv-time {
  font-size: 12px;
  color: #999;
  margin-left: 8px;
  flex-shrink: 0;
}

.conv-preview {
  font-size: 13px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.resize-handle {
  width: 4px;
  background: transparent;
  cursor: col-resize;
  flex-shrink: 0;
  position: relative;
  transition: background 0.2s;
}

.resize-handle:hover,
.resize-handle.resizing {
  background: #1890ff;
}

.resize-handle::before {
  content: '';
  position: absolute;
  left: -4px;
  right: -4px;
  top: 0;
  bottom: 0;
}

.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f8f8f8;
  overflow: hidden;
}

.topbar {
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
  padding: 12px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: transparent;
  border: 1px solid #e5e5e5;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
}

.back-btn:hover {
  background: #f5f5f5;
  border-color: #1890ff;
}

.back-btn svg {
  width: 18px;
  height: 18px;
  stroke: #666;
  transition: stroke 0.2s;
}

.back-btn:hover svg {
  stroke: #1890ff;
}

.toggle-sidebar-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: transparent;
  border: 1px solid #e5e5e5;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
}

.toggle-sidebar-btn:hover {
  background: #f5f5f5;
  border-color: #1890ff;
}

.toggle-sidebar-btn svg {
  width: 18px;
  height: 18px;
  stroke: #666;
  transition: stroke 0.2s;
}

.toggle-sidebar-btn:hover svg {
  stroke: #1890ff;
}

.peer-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  width: fit-content;
}

.peer-info:hover .peer-name {
  color: #1890ff;
}

.peer-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.peer-name {
  font-weight: 600;
  font-size: 16px;
  color: #222;
  transition: color 0.2s;
}

.peer-id {
  font-size: 12px;
  color: #999;
}

.no-chat-selected {
  color: #999;
  font-size: 14px;
  font-style: italic;
}

.empty-chat-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 16px;
  padding: 40px;
}

.empty-icon {
  width: 64px;
  height: 64px;
  stroke: #d9d9d9;
  opacity: 0.5;
}

.empty-text {
  color: #999;
  font-size: 14px;
  text-align: center;
  max-width: 300px;
}

.log {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f8f8f8;
}

.msg {
  display: flex;
  margin-bottom: 16px;
  gap: 8px;
}

.msg.me {
  flex-direction: row-reverse;
}

.msg.sys {
  justify-content: center;
}

.sys-msg {
  display: flex;
  justify-content: center;
  margin: 8px 0;
}

.sys-text {
  background: rgba(0, 0, 0, 0.05);
  color: #666;
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 12px;
}

.msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  cursor: pointer;
  flex-shrink: 0;
  transition: opacity 0.2s;
}

.msg-avatar:hover {
  opacity: 0.8;
}

.msg-content {
  display: flex;
  flex-direction: column;
  max-width: 60%;
  min-width: 0;
}

.msg.me .msg-content {
  align-items: flex-end;
}

.msg-name {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
  padding: 0 8px;
  cursor: pointer;
  transition: color 0.2s;
}

.msg-name:hover {
  color: #1890ff;
}

.msg-name-right {
  text-align: right;
}

.bubble {
  padding: 10px 14px;
  border-radius: 12px;
  word-break: break-word;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.msg.peer .bubble {
  background: #fff;
  color: #222;
  border-radius: 12px 12px 12px 2px;
}

.msg.me .bubble {
  background: #1890ff;
  color: #fff;
  border-radius: 12px 12px 2px 12px;
}

.text {
  font-size: 14px;
  line-height: 1.5;
}

.ts {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 4px;
}

.msg.peer .ts {
  color: #999;
}

.location-msg {
  min-width: 200px;
}

.location-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  font-weight: 600;
  font-size: 13px;
}

.location-icon {
  width: 16px;
  height: 16px;
}

.location-title {
  font-weight: 600;
}

.location-name {
  font-size: 14px;
  margin-bottom: 8px;
}

.location-link {
  font-size: 12px;
  text-decoration: underline;
  color: inherit;
  opacity: 0.9;
}

.location-link:hover {
  opacity: 1;
}

.trade-card {
  min-width: 260px;
}

.tc-title {
  font-weight: 600;
  padding-bottom: 8px;
  margin-bottom: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.msg.peer .tc-title {
  border-bottom-color: #e5e5e5;
}

.tc-row {
  font-size: 13px;
  margin: 4px 0;
}

.tc-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}

.composer {
  background: #fff;
  border-top: 1px solid #e5e5e5;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.plus-wrapper {
  position: relative;
}

.plus-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f5f5f5;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.plus-btn:hover {
  background: #e5e5e5;
}

.plus-btn svg {
  width: 20px;
  height: 20px;
  stroke: #666;
}

.plus-menu {
  position: absolute;
  bottom: 48px;
  left: 0;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  border: 1px solid #e5e5e5;
  padding: 8px;
  min-width: 160px;
  z-index: 100;
}

.plus-menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
  font-size: 14px;
}

.plus-menu-item:hover {
  background: #f5f5f5;
}

.plus-menu-icon {
  width: 20px;
  height: 20px;
  stroke: #1890ff;
  flex-shrink: 0;
}

.input-wrapper {
  flex: 1;
  background: #f5f5f5;
  border-radius: 20px;
  padding: 0 16px;
}

.input-field {
  width: 100%;
  border: none;
  background: transparent;
  outline: none;
  padding: 10px 0;
  font-size: 14px;
  color: #222;
}

.input-field::placeholder {
  color: #999;
}

.send-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #1890ff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.send-btn:hover:not(:disabled) {
  background: #40a9ff;
}

.send-btn:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}

.send-btn svg {
  width: 18px;
  height: 18px;
  stroke: white;
}

.log::-webkit-scrollbar,
.conv-list::-webkit-scrollbar {
  width: 6px;
}

.log::-webkit-scrollbar-thumb,
.conv-list::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.log::-webkit-scrollbar-thumb:hover,
.conv-list::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}
.unknown-msg {
  min-width: 300px;
  max-width: 100%;
}

.unknown-title {
  font-weight: 600;
  color: #ff4d4f;
  margin-bottom: 8px;
  font-size: 13px;
}

.unknown-debug {
  font-size: 12px;
  line-height: 1.6;
}

.unknown-debug > div {
  margin: 4px 0;
}

.unknown-debug strong {
  font-weight: 600;
}

.unknown-json {
  margin-top: 8px;
  padding: 8px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
  overflow-x: auto;
  font-family: 'Courier New', monospace;
  font-size: 11px;
  line-height: 1.4;
  white-space: pre-wrap;
  word-break: break-all;
}

.msg.me .unknown-json {
  background: rgba(255, 255, 255, 0.2);
}
.unknown-msg {
  min-width: 300px;
  max-width: 100%;
}

.unknown-title {
  font-weight: 600;
  color: #ff4d4f;
  margin-bottom: 8px;
  font-size: 13px;
}

.unknown-debug {
  font-size: 12px;
  line-height: 1.6;
}

.unknown-debug > div {
  margin: 4px 0;
}

.unknown-debug strong {
  font-weight: 600;
}

.unknown-json {
  margin-top: 8px;
  padding: 8px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
  overflow-x: auto;
  font-family: 'Courier New', monospace;
  font-size: 11px;
  line-height: 1.4;
  white-space: pre-wrap;
  word-break: break-all;
}

.msg.me .unknown-json {
  background: rgba(255, 255, 255, 0.2);
}
.msg.sys .bubble { background:#fff; color:#222; }
</style>
