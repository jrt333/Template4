<template>
  <div>
    <app-head></app-head>
    <app-body>
      <div class="idle-details-container">
        <!-- 1. 顶部：卖家信息 -->
        <div class="seller-info">
          <el-image
              style="width: 50px; height: 50px; border-radius: 50%; cursor: pointer;"
              :src="idleItemInfo.user.avatar"
              @click="goSeller(idleItemInfo.userId)"
              fit="cover"
          />
          <div class="seller-text">
            <p class="seller-nickname" @click="goSeller(idleItemInfo.userId)">{{ idleItemInfo.user.nickname }}</p>
          </div>
        </div>

        <!-- 2. 中间：图片展示 - TradeMe风格：上大图，下小图 -->
        <div class="image-gallery">
          <!-- 大图 -->
          <div class="main-image-wrapper">
            <el-image
                v-if="idleItemInfo.pictureList.length"
                :src="idleItemInfo.pictureList[activeImage]"
                :preview-src-list="idleItemInfo.pictureList"
                class="main-image"
                fit="contain"
            >
              <template #error>
                <div class="image-slot">
                  <i class="el-icon-picture-outline"></i>
                </div>
              </template>
            </el-image>

            <!-- 左右切换按钮 -->
            <button
                v-if="idleItemInfo.pictureList.length > 1 && activeImage > 0"
                class="nav-button nav-button-prev"
                @click.stop="prevImage"
            >
              <i class="el-icon-arrow-left"></i>
            </button>
            <button
                v-if="idleItemInfo.pictureList.length > 1 && activeImage < idleItemInfo.pictureList.length - 1"
                class="nav-button nav-button-next"
                @click.stop="nextImage"
            >
              <i class="el-icon-arrow-right"></i>
            </button>
          </div>

          <!-- 缩略图横排 -->
          <div class="thumbnails-wrapper">
            <div
                v-for="(imgUrl, i) in idleItemInfo.pictureList"
                :key="i"
                class="thumbnail"
                :class="{ active: activeImage === i }"
                @click="handleThumbnailClick(i)"
            >
              <el-image :src="imgUrl" fit="cover" />
            </div>
          </div>
        </div>

        <!-- 3. 下面：物品详细信息 -->
        <div class="details-section">
          <div class="info-card main-info-card">
            <div class="title-price-row">
              <h1 class="item-title">{{ idleItemInfo.idleName }}</h1>
              <div class="price-amount" v-if="idleItemInfo.idleTrade === 1">NZD ${{ idleItemInfo.idlePrice }}</div>
              <div class="exchange-badge" v-else>EXCHANGE</div>
            </div>

            <div class="details-grid">
              <div class="detail-item">
                <span class="detail-label">Transaction Type</span>
                <span class="detail-value">{{ tradeText }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Condition</span>
                <span class="detail-value">{{ newText }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Posted</span>
                <span class="detail-value">{{ formatDate(idleItemInfo.releaseTime) }}</span>
              </div>
            </div>

            <div class="description-section">
              <h3 class="section-subtitle">Description</h3>
              <div class="description-content" v-html="idleItemInfo.idleDetails"></div>
            </div>

            <div class="action-buttons">
              <el-button
                  v-if="!isMaster && idleItemInfo.idleStatus === 1"
                  type="danger"
                  size="large"
                  class="buy-button"
                  @click="onBuyClick(idleItemInfo)"
              >
                <i class="el-icon-shopping-cart-2"></i> BUY NOW
              </el-button>
              <el-button
                  v-if="!isMaster && idleItemInfo.idleStatus === 1"
                  size="large"
                  class="favorite-button"
                  :class="{ 'is-favorite': isFavorite }"
                  @click="favoriteButton(idleItemInfo)"
              >
                <i :class="isFavorite ? 'el-icon-star-on' : 'el-icon-star-off'"></i>
                {{ isFavorite ? 'Saved' : 'Save' }}
              </el-button>
              <el-button
                  v-if="isMaster && idleItemInfo.idleStatus === 1"
                  type="warning"
                  size="large"
                  @click="changeStatus(idleItemInfo, 2)"
              >
                <i class="el-icon-remove-outline"></i> Unlist Item
              </el-button>
              <el-button
                  v-if="isMaster && idleItemInfo.idleStatus === 2"
                  type="success"
                  size="large"
                  @click="changeStatus(idleItemInfo, 1)"
              >
                <i class="el-icon-circle-plus-outline"></i> Relist Item
              </el-button>
            </div>

            <p v-if="tip" class="tip-message">{{ tip }}</p>
          </div>
        </div>

        <!-- 4. 最后：留言/评论区 -->
        <div class="message-section">
          <h2 class="section-title">
            <i class="el-icon-chat-dot-round"></i>
            Messages
            <span class="message-count" v-if="messageList.length">({{ messageList.length }})</span>
          </h2>

          <!-- 留言输入区 -->
          <div class="message-input-card">
            <div class="input-header">
              <i class="el-icon-edit"></i>
              <span>Leave a message</span>
            </div>
            <el-input
                type="textarea"
                :rows="4"
                placeholder="Write your message..."
                v-model="messageContent"
                maxlength="200"
                show-word-limit
                ref="messageInput"
                class="message-textarea"
            />
            <div class="message-send-button">
              <el-button type="primary" @click="sendMessage" :disabled="!messageContent.trim()">
                <i class="el-icon-s-promotion"></i> Send Message
              </el-button>
            </div>
          </div>

          <!-- 留言区展示 -->
          <div v-if="messageList && messageList.length" class="messages-list">
            <div v-for="mes in messageList" :key="mes.id" class="message-item">
              <!-- 主留言头像 -->
              <el-image class="message-avatar" :src="mes.fromU.avatar" fit="cover">
                <template #error>
                  <div class="avatar-error"><i class="el-icon-user-solid"></i></div>
                </template>
              </el-image>

              <div class="message-body">
                <div class="message-header">
                  <span class="message-author">{{ mes.fromU.nickname }}</span>
                  <span class="message-time">
                    <i class="el-icon-time"></i>
                    {{ mes.createTime }}
                  </span>
                </div>
                <div class="message-content" v-html="mes.content"></div>

                <!-- 回复按钮 -->
                <div class="message-actions">
                  <button class="reply-button" @click="openReplyInput(mes.id)">
                    <i class="el-icon-chat-line-round"></i>
                    Reply
                  </button>
                </div>

                <!-- 回复输入框 -->
                <div v-if="replyingTo === mes.id" class="reply-input-card">
                  <div class="reply-input-header">
                    <i class="el-icon-edit-outline"></i>
                    <span>Replying to {{ mes.fromU.nickname }}</span>
                  </div>
                  <el-input
                      type="textarea"
                      :rows="3"
                      placeholder="Write your reply..."
                      v-model="replyContent"
                      maxlength="200"
                      show-word-limit
                      class="reply-textarea"
                  />
                  <div class="reply-actions-buttons">
                    <el-button size="small" @click="cancelReply">
                      Cancel
                    </el-button>
                    <el-button
                        type="primary"
                        size="small"
                        @click="sendReply(mes.id)"
                        :disabled="!replyContent.trim()"
                    >
                      <i class="el-icon-s-promotion"></i>
                      Send Reply
                    </el-button>
                  </div>
                </div>

                <!-- 回复列表 -->
                <div v-if="mes.replies && mes.replies.length" class="replies-list">
                  <div v-for="rep in mes.replies" :key="rep.id" class="reply-item">
                    <el-image class="reply-avatar" :src="rep.fromU.avatar" fit="cover">
                      <template #error>
                        <div class="avatar-error"><i class="el-icon-user-solid"></i></div>
                      </template>
                    </el-image>
                    <div class="reply-body">
                      <div class="reply-header">
                        <span class="reply-author">{{ rep.fromU.nickname }}</span>
                        <span class="reply-time">{{ rep.createTime }}</span>
                      </div>
                      <div class="reply-content" v-html="rep.content"></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else class="empty-messages">
            <i class="el-icon-chat-dot-square"></i>
            <p>No messages yet</p>
            <span>Be the first to leave a message!</span>
          </div>
        </div>

        <trade-dialog
            :visible="showTrade"
            :item-id="itemId"
            :seller-id="sellerId"
            :default-price-dollar="defaultPriceDollar"
            :idle-trade="idleItemInfo.idleTrade"
            @close="showTrade=false"
            @created="onTradeCreated"
            @error="onTradeError"
        />
      </div>

    </app-body>
    <app-foot></app-foot>
  </div>
</template>

<script>
import AppHead from "../common/AppHeader.vue";
import AppBody from "../common/AppPageBody.vue";
import AppFoot from "../common/AppFoot.vue";
import TradeDialog from '../common/TradeDialog.vue';
import {sendPayload} from "@/utils/websocket";
import { ensureGlobalChat } from '@/utils/chatBus'
import { createChatStore } from '@/stores/chatStoreLite'

export default {
  name: "idle-details-v2",
  components: { AppHead, AppBody, AppFoot, TradeDialog },
  data() {
    return {
      messageContent: '',
      replyContent: '',
      toUser: null,
      toMessage: null,
      isReply: false,
      replyingTo: null,
      replyData: {
        toUserNickname: '',
        toMessage: ''
      },
      messageList: [],
      activeImage: 0,
      showTrade: false,
      tip: '',
      idleItemInfo: {
        id: '',
        idleName: '',
        idleDetails: '',
        pictureList: [],
        idlePrice: 0,
        idlePlace: '',
        idleLabel: '',
        idleStatus: -1,
        userId: '',
        user: {
          avatar: '',
          nickname: '',
          signInTime: ''
        }
      },
      isMaster: false,
      isFavorite: true,
      favoriteId: 0,
      tradeMap: {
        1: "SELL",
        2: "EXCHANGE",
      },
      newMap: {
        1: "BRAND NEW",
        2: "LIKE NEW",
        3: "USED",
      }
    };
  },
  computed: {
    tradeText() { return this.tradeMap[this.idleItemInfo.idleTrade] || "Unknown"; },
    newText()   { return this.newMap[this.idleItemInfo.idleNew]   || "Unknown"; },

    itemId() {
      const fromInfo = this.idleItemInfo && this.idleItemInfo.id ? Number(this.idleItemInfo.id) : 0
      const fromRoute = this.$route && this.$route.params && this.$route.params.id ? Number(this.$route.params.id) : 0
      return fromInfo || fromRoute || 0
    },

    sellerId() {
      if (!this.idleItemInfo) return 0
      if (this.idleItemInfo.userId != null) return Number(this.idleItemInfo.userId)
      if (this.idleItemInfo.user && this.idleItemInfo.user.id != null) return Number(this.idleItemInfo.user.id)
      return 0
    },

    defaultPriceDollar() {
      return this.idleItemInfo && this.idleItemInfo.idlePrice != null
          ? Number(this.idleItemInfo.idlePrice)
          : null
    },

    currentUserId() {
      return Number(this.getCookie('shUserId'))
    },

    canBuy() {
      const onShelf = String(this.idleItemInfo.idleStatus) === '1'
      const notSelf = this.sellerId && (this.sellerId !== this.currentUserId)
      return onShelf && notSelf
    }
  },
  created() {
    let id = this.$route.query.id;
    this.$api.getIdleItem({ id }).then(res => {
      if (res.data) {
        res.data.pictureList = JSON.parse(res.data.pictureList);
        this.idleItemInfo = res.data;

        let userId = this.getCookie("shUserId");
        if (userId == this.idleItemInfo.userId) {
          this.isMaster = true;
        }
        this.checkFavorite();
        this.getAllIdleMessage();
      }
    });
    const selfId = String((this.$globalData && this.$globalData.userInfo && this.$globalData.userInfo.id) || this.buyerId)
    ensureGlobalChat(selfId)
  },
  methods: {
    onBuyClick() {
      console.log('[idle-details] Trade parameters:', {
        itemId: this.itemId,
        sellerId: this.sellerId,
        buyerId: this.$globalData.userInfo.id,
        defaultPriceDollar: this.defaultPriceDollar,
      });
      if (!this.itemId) { this.$message.error('Item ID not found'); return }
      if (!this.sellerId) { this.$message.error('Seller ID not found'); return }
      if (!this.canBuy) { this.$message.warning('Cannot purchase: This might be your own item or not listed'); return }
      this.showTrade = true
    },

    toIsoLocalDateTime (val) {
      if (!val) return null
      if (Object.prototype.toString.call(val) === '[object Date]') {
        const p = n => (n < 10 ? '0' + n : '' + n)
        return (
            val.getFullYear() + '-' +
            p(val.getMonth() + 1) + '-' +
            p(val.getDate()) + 'T' +
            p(val.getHours()) + ':' +
            p(val.getMinutes()) + ':' +
            p(val.getSeconds())
        )
      }
      return String(val).replace(' ', 'T')
    },

    dollarsToCents (v) {
      const n = parseFloat(v)
      return isNaN(n) ? 0 : Math.round(n * 100)
    },

    async onTradeCreated (form) {
      const itemId     = this.itemId
      const sellerId   = this.sellerId
      const meetTime   = this.toIsoLocalDateTime(form && form.meetTime)
      const meetPlace  = (form && form.meetPlace) || ''

      let payload = { itemId, sellerId, meetTime, meetPlace }
      if (this.idleItemInfo.idleTrade === 1) {
        // SELL 模式：帶 priceCents
        const priceCents = (form && typeof form.priceCents === 'number')
            ? form.priceCents
            : this.dollarsToCents(form && form.priceDollar)
        payload.priceCents = priceCents
      } else if (this.idleItemInfo.idleTrade === 2) {
        // EXCHANGE 模式：帶 tradeItem
        payload.tradeItem = (form && form.tradeItem) || ''
      }
      console.log('[createTrade payload]', payload)

      let tradeId = null
      try {
        const res = await fetch('/api/trades', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json;charset=UTF-8' },
          body: JSON.stringify(payload),
          credentials: 'include'
        })
        if (!res.ok) throw new Error('HTTP ' + res.status)

        const raw = await res.json()
        const data    = (raw && raw.data != null) ? raw.data : raw
        const trade   = (data && data.data != null) ? data.data : data
        tradeId       = trade && (trade.id || trade.tradeId)

        if (!tradeId) throw new Error((raw && (raw.msg || raw.message)) || 'No tradeId returned')
        console.log('[createTrade ok] tradeId=', tradeId)
      } catch (err) {
        console.error('[createTrade failed]', err)
        this.$message && this.$message.error && this.$message.error(err.message || 'Failed to create trade')
        return
      }

      const buyerId = (this.$globalData && this.$globalData.userInfo && this.$globalData.userInfo.id) || this.buyerId

      const content = { meetTime, meetPlace }
      if (this.idleItemInfo.idleTrade === 1) {
        content.priceCents = payload.priceCents
      } else if (this.idleItemInfo.idleTrade === 2) {
        content.tradeItem = payload.tradeItem
      }

      const card = {
        type: 'TRADE_CARD',
        from: String(buyerId),
        to:   String(sellerId),
        itemId,
        tradeId,
        content,
        ts: Date.now(),
        noEchoToSender: true
      }
      console.log('[send TRADE_CARD]', card)

      try {
        const ok = sendPayload(card)
        console.log('[send TRADE_CARD result]', ok)
        this.tip = `Trade card sent to seller (Trade #${tradeId})`
        try {
          const chat = createChatStore(buyerId, sellerId)
          chat.addMessage({
            from: 'me',
            kind: 'card',
            card: card,
            ts: card.ts
          })
          this.loadConversations && this.loadConversations()
          this.$nextTick(this.scrollToBottom)
          this.$router.push({
            name: 'PrivateChat',
            query: { selfId: buyerId, peerId: sellerId }
          })
        } catch (e) { console.warn('local echo failed', e) }
      } catch (e) {
        console.error('[WS send error]', e)
        this.$message && this.$message.error && this.$message.error('WebSocket not connected')
      }
    },

    onTradeError(err) {
      console.error('create trade fail:', err)
      this.tip = 'Failed to create trade'
    },

    formatDate(dateStr) {
      if (!dateStr) return "";
      const date = new Date(dateStr);
      const day = String(date.getDate()).padStart(2, "0");
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const year = date.getFullYear();
      return `${day}/${month}/${year}`;
    },

    handleThumbnailClick(i) {
      this.activeImage = i;
    },

    prevImage() {
      if (this.activeImage > 0) {
        this.activeImage--;
      }
    },

    nextImage() {
      if (this.activeImage < this.idleItemInfo.pictureList.length - 1) {
        this.activeImage++;
      }
    },

    checkFavorite() {
      this.$api.checkFavorite({ idleId: this.idleItemInfo.id }).then(res => {
        if (!res.data) {
          this.isFavorite = false;
        } else {
          this.favoriteId = res.data;
          this.isFavorite = true;
        }
      });
    },

    getCookie(cname) {
      let name = cname + "=";
      let ca = document.cookie.split(";");
      for (let c of ca) {
        c = c.trim();
        if (c.indexOf(name) === 0) return c.substring(name.length);
      }
      return "";
    },

    openReplyInput(messageId) {
      this.replyingTo = this.replyingTo === messageId ? null : messageId;
      this.replyContent = '';
    },

    cancelReply() {
      this.isReply = false;
      this.replyingTo = null;
      this.toUser = null;
      this.toMessage = null;
      this.replyContent = '';
      this.replyData.toUserNickname = '';
      this.replyData.toMessage = '';
    },

    sendMessage() {
      const content = this.messageContent.trim();
      if (!content) {
        this.$message.error("Message cannot be empty!");
        return;
      }

      if (!this.toUser) {
        this.toUser = this.idleItemInfo.userId;
      }

      const contentHtml = content.split(/\r?\n/).join('<br>');

      this.$api.sendMessage({
        idleId: this.idleItemInfo.id,
        content: contentHtml,
        toUser: this.toUser,
        toMessage: this.toMessage || null
      })
          .then(res => {
            if (res.status_code === 1) {
              this.$message.success('Message posted successfully!');
              this.messageContent = '';
              this.cancelReply();
              this.getAllIdleMessage();
            } else {
              this.$message.error("Failed to post message: " + res.msg);
            }
          })
          .catch(() => {
            this.$message.error("Failed to post message!");
          });
    },

    sendReply(messageId) {
      const content = this.replyContent.trim();
      if (!content) {
        this.$message.error("Reply cannot be empty!");
        return;
      }

      const message = this.messageList.find(m => m.id === messageId);
      if (!message) {
        this.$message.error("Message not found!");
        return;
      }

      const contentHtml = content.split(/\r?\n/).join('<br>');

      this.$api.sendMessage({
        idleId: this.idleItemInfo.id,
        content: contentHtml,
        toUser: message.userId,
        toMessage: messageId
      })
          .then(res => {
            if (res.status_code === 1) {
              this.$message.success('Reply posted successfully!');
              this.replyContent = '';
              this.replyingTo = null;
              this.getAllIdleMessage();
            } else {
              this.$message.error("Failed to post reply: " + res.msg);
            }
          })
          .catch(() => {
            this.$message.error("Failed to post reply!");
          });
    },

    getAllIdleMessage() {
      this.$api.getAllIdleMessage({ idleId: this.idleItemInfo.id })
          .then(res => {
            if (res.status_code === 1) {
              this.messageList = this.processMessageList(res.data);
              console.log('Processed message list:', this.messageList);
            }
          });
    },

    processMessageList(messages) {
      if (!messages || !Array.isArray(messages)) return [];

      const messageMap = new Map();
      const rootMessages = [];

      messages.forEach(msg => {
        messageMap.set(msg.id, {
          ...msg,
          replies: []
        });
      });

      messages.forEach(msg => {
        const message = messageMap.get(msg.id);

        if (msg.toMessage && messageMap.has(msg.toMessage)) {
          const parentMessage = messageMap.get(msg.toMessage);
          parentMessage.replies.push(message);
        } else {
          rootMessages.push(message);
        }
      });

      return rootMessages;
    },

    changeStatus(idle, status) {
      this.$api.updateIdleItem({ id: idle.id, idleStatus: status }).then(res => {
        if (res.status_code === 1) this.idleItemInfo.idleStatus = status;
        else this.$message.error(res.msg);
      });
    },

    favoriteButton(idle) {
      if (this.isFavorite) {
        this.$api.deleteFavorite({ id: this.favoriteId }).then(res => {
          if (res.status_code === 1) {
            this.isFavorite = false;
            this.$message.success("Removed from favorites!");
          }
        });
      } else {
        this.$api.addFavorite({ idleId: idle.id }).then(res => {
          if (res.status_code === 1) {
            this.isFavorite = true;
            this.favoriteId = res.data;
            this.$message.success("Added to favorites!");
          }
        });
      }
    },

    goSeller: function (sellerId) {
      var myId = null;
      if (this.$store && this.$store.state && this.$store.state.user) {
        myId = this.$store.state.user.userId || this.$store.state.user.id;
      }
      if (!myId && this.$globalData && this.$globalData.userInfo) {
        myId = this.$globalData.userInfo.userId || this.$globalData.userInfo.id;
      }

      if (String(sellerId) === String(myId)) {
        if (this.$route && this.$route.name === 'me') {
          this.$router.replace({ name: 'me', query: { r: Date.now() } });
        } else {
          this.$router.push({ name: 'me' });
        }
      } else {
        this.$router.push({ name: 'user-profile', params: { id: sellerId } });
      }
    },
  }
};
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.idle-details-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px 24px;
  min-height: 85vh;
  background-color: #fafafa;
}

/* 1. 卖家信息 - 顶部 */
.seller-info {
  display: flex;
  align-items: center;
  margin-bottom: 32px;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.seller-text {
  margin-left: 12px;
}

.seller-nickname {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  cursor: pointer;
  margin: 0;
  transition: color 0.2s;
}

.seller-nickname:hover {
  color: #2563eb;
}

/* 2. 图片画廊 - TradeMe风格：上大图，下小图横排 */
.image-gallery {
  width: 100%;
  margin-bottom: 32px;
  background-color: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* 大图容器 */
.main-image-wrapper {
  position: relative;
  width: 100%;
  height: 600px;
  background-color: #f9fafb;
  border-radius: 8px;
  overflow: hidden;
  cursor: zoom-in;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.main-image {
  width: 100%;
  height: 100%;
  cursor: zoom-in !important;
}

.main-image >>> .el-image__inner {
  cursor: zoom-in !important;
}

.main-image >>> img {
  cursor: zoom-in !important;
}

/* 左右切换按钮 */
.nav-button {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 48px;
  height: 48px;
  background-color: rgba(255, 255, 255, 0.9);
  border: none;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #374151;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.2s;
  z-index: 10;
  opacity: 0;
}

.main-image-wrapper:hover .nav-button {
  opacity: 1;
}

.nav-button:hover {
  background-color: #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  transform: translateY(-50%) scale(1.1);
}

.nav-button:active {
  transform: translateY(-50%) scale(0.95);
}

.nav-button-prev {
  left: 20px;
}

.nav-button-next {
  right: 20px;
}

/* 缩略图横排容器 */
.thumbnails-wrapper {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 8px 0;
}

.thumbnails-wrapper::-webkit-scrollbar {
  height: 8px;
}

.thumbnails-wrapper::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.thumbnails-wrapper::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 4px;
}

.thumbnails-wrapper::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}

.thumbnail {
  width: 100px;
  height: 100px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 3px solid transparent;
  transition: all 0.2s;
  background-color: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.thumbnail:hover {
  border-color: #d1d5db;
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.15);
}

.thumbnail.active {
  border-color: #2563eb;
  box-shadow: 0 0 0 2px #2563eb;
  transform: translateY(-2px);
}

.thumbnail .el-image {
  width: 100%;
  height: 100%;
}

/* 3. 物品详细信息 */
.details-section {
  margin-bottom: 32px;
}

.main-info-card {
  background-color: #ffffff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.title-price-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 2px solid #f3f4f6;
  flex-wrap: wrap;
  gap: 16px;
}

.item-title {
  font-size: 28px;
  font-weight: 700;
  color: #111827;
  margin: 0;
  line-height: 1.3;
  flex: 1;
  min-width: 200px;
}

.price-amount {
  font-size: 32px;
  font-weight: 700;
  color: #dc2626;
  white-space: nowrap;
}

.exchange-badge {
  font-size: 32px;
  font-weight: 700;
  color: #10b981;
  white-space: nowrap;
}

.details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
  padding-bottom: 32px;
  border-bottom: 2px solid #f3f4f6;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-label {
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.detail-value {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.description-section {
  margin-bottom: 32px;
  padding-bottom: 32px;
  border-bottom: 2px solid #f3f4f6;
}

.section-subtitle {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 16px 0;
}

.description-content {
  font-size: 15px;
  line-height: 1.7;
  color: #374151;
}

.action-buttons {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.buy-button,
.favorite-button {
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  transition: all 0.2s;
}

.buy-button {
  background-color: #dc2626;
  border-color: #dc2626;
  color: white;
}

.buy-button:hover {
  background-color: #b91c1c;
  border-color: #b91c1c;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px rgba(220, 38, 38, 0.3);
}

.favorite-button {
  background-color: white;
  border: 2px solid #e5e7eb;
  color: #374151;
}

.favorite-button:hover {
  border-color: #2563eb;
  color: #2563eb;
}

.favorite-button.is-favorite {
  background-color: #fef3c7;
  border-color: #f59e0b;
  color: #92400e;
}

.tip-message {
  padding: 12px 16px;
  background-color: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 8px;
  color: #166534;
  font-size: 14px;
}

/* 4. 留言区域 - 优化版 */
.message-section {
  background-color: #ffffff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 28px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-title i {
  color: #2563eb;
}

.message-count {
  font-size: 16px;
  font-weight: 500;
  color: #6b7280;
  margin-left: 4px;
}

.message-input-card {
  margin-bottom: 32px;
  padding: 24px;
  background-color: #f9fafb;
  border-radius: 12px;
  border: 2px solid #e5e7eb;
  transition: border-color 0.3s;
}

.message-input-card:focus-within {
  border-color: #2563eb;
}

.input-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-weight: 600;
  color: #374151;
  font-size: 15px;
}

.input-header i {
  color: #2563eb;
  font-size: 18px;
}

.message-textarea {
  margin-bottom: 12px;
}

.message-send-button {
  text-align: right;
}

.message-send-button .el-button {
  padding: 12px 28px;
  font-weight: 600;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.message-item {
  display: flex;
  gap: 16px;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  transition: all 0.2s;
}

.message-item:hover {
  border-color: #d1d5db;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.message-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 2px solid #e5e7eb;
  flex-shrink: 0;
  transition: border-color 0.2s;
}

.message-item:hover .message-avatar {
  border-color: #2563eb;
}

.avatar-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f3f4f6;
  color: #9ca3af;
  font-size: 24px;
}

.message-body {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 8px;
}

.message-author {
  font-weight: 700;
  color: #111827;
  font-size: 16px;
}

.message-time {
  font-size: 13px;
  color: #9ca3af;
  display: flex;
  align-items: center;
  gap: 4px;
}

.message-time i {
  font-size: 14px;
}

.message-content {
  color: #374151;
  line-height: 1.7;
  margin-bottom: 12px;
  word-wrap: break-word;
  font-size: 15px;
  padding: 12px 16px;
  background-color: #f9fafb;
  border-radius: 8px;
  border-left: 3px solid #2563eb;
}

.message-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.reply-button {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #2563eb;
  background: none;
  border: none;
  padding: 6px 12px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;
}

.reply-button:hover {
  color: #1d4ed8;
  background-color: #eff6ff;
}

.reply-button i {
  font-size: 16px;
}

/* 回复输入框卡片 */
.reply-input-card {
  margin-top: 16px;
  padding: 16px;
  border-radius: 8px;
}

.reply-input-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-weight: 600;
  color: #1e40af;
  font-size: 14px;
}

.reply-input-header i {
  font-size: 16px;
}

.reply-textarea {
  margin-bottom: 12px;
}

.reply-actions-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 回复列表 */
.replies-list {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reply-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background-color: #f9fafb;
  border-radius: 8px;
  border-left: 3px solid #d1d5db;
}

.reply-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px solid #e5e7eb;
  flex-shrink: 0;
}

.reply-body {
  flex: 1;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  gap: 8px;
}

.reply-author {
  font-weight: 600;
  font-size: 14px;
  color: #374151;
}

.reply-time {
  font-size: 12px;
  color: #9ca3af;
}

.reply-content {
  font-size: 14px;
  color: #374151;
  line-height: 1.6;
  word-wrap: break-word;
}

/* 空状态 */
.empty-messages {
  text-align: center;
  padding: 60px 20px;
  color: #9ca3af;
}

.empty-messages i {
  font-size: 64px;
  color: #d1d5db;
  margin-bottom: 16px;
}

.empty-messages p {
  font-size: 18px;
  font-weight: 600;
  color: #6b7280;
  margin: 0 0 8px 0;
}

.empty-messages span {
  font-size: 14px;
  color: #9ca3af;
}
</style>