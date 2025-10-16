<template>
  <header class="app-head">
    <div class="bar">
      <!-- Left: brand - 添加登录检查 -->
      <a href="#" class="brand" @click.prevent="handleNavigation('/home')">BAG2BAG</a>

      <!-- Center: simple bag mark -->
      <img
          v-if="logo"
          class="brand-mark"
          :src="logo"
          alt="BAG2BAG logo"
          @error="handleImageError"
      />

      <!-- Right: actions -->
      <div class="actions">
        <!-- Post Item 按钮 - 添加登录检查 -->
        <a href="#" class="post-btn" @click.prevent="handleNavigation('/release')">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
          </svg>
          Post Item
        </a>

        <el-button class="menu-btn" icon="el-icon-menu" circle @click="drawer = true" />
      </div>
    </div>

    <el-drawer
        :visible.sync="drawer"
        direction="rtl"
        size="300px"
        :with-header="false"
        :append-to-body="true"
        custom-class="nav-drawer"
    >

      <!-- 用户信息 -->
      <div class="user-section" @click="onUserSectionClick">
        <div class="user-avatar">
          <img :src="user && user.avatar ? user.avatar : defaultAvatar" alt="User avatar" />
        </div>
        <div class="user-info">
          <template v-if="user">
            <div class="user-name">{{ userNickname }}</div>
            <div class="user-subtitle">View Profile</div>
          </template>
          <div v-else class="user-signin">Sign in with Google</div>
        </div>
      </div>

      <!-- Navigation Menu -->
      <nav class="drawer-nav">
        <!-- Home - 添加登录检查 -->
        <a href="#" class="nav-item" :class="{'router-link-active': isCurrentRoute('/home')}" @click.prevent="handleNavigation('/home')">
          <div class="nav-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
            </svg>
          </div>
          <span class="nav-text">Home</span>
        </a>

        <!-- My Messages - 已有登录检查 -->
        <div class="nav-item" @click="handleMessagesClick">
          <div class="nav-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/>
            </svg>
          </div>
          <div class="nav-text-container">
            <span class="nav-text">My Messages</span>
            <span class="message-badge" v-if="unreadCount > 0">{{ unreadCount }}</span>
          </div>
        </div>

        <a
            href="https://maps.auckland.ac.nz/auckland/fa64ffa351cb4fe680fa2929/search"
            target="_blank"
            rel="noopener noreferrer"
            class="nav-item"
            @click="drawer = false"
        >
          <div class="nav-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C8.13 2 5 5.13 5 9c0 3.25 2.66 6.93 7.27 11.65.4.42 1.06.42 1.46 0C16.34 15.93 19 12.25 19 9c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5S10.62 6.5 12 6.5 14.5 7.62 14.5 9 13.38 11.5 12 11.5z"/>
            </svg>
          </div>
          <span class="nav-text">Campus Map</span>
        </a>

        <router-link to="/about" exact class="nav-item" @click.native="drawer = false">
          <div class="nav-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/>
            </svg>
          </div>
          <span class="nav-text">About Team</span>
        </router-link>

        <div class="nav-divider"></div>

        <!-- 登录 / 登出 -->
        <a
            v-if="!user"
            href="#"
            class="nav-item"
            @click.prevent="startGoogleLogin"
        >
          <div class="nav-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M11 7L9.6 8.4l2.6 2.6H2v2h10.2l-2.6 2.6L11 17l5-5-5-5zm9 12h-8v2h8c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2h-8v2h8v12z"/>
            </svg>
          </div>
          <span class="nav-text">Sign in with Google</span>
        </a>

        <a
            v-if="user"
            href="#"
            class="nav-item"
            :class="{ 'is-disabled': loggingOut }"
            :aria-busy="loggingOut"
            :aria-disabled="loggingOut"
            @click.prevent="onLogout"
        >
          <div class="nav-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M16 13v-2H7V8l-5 4 5 4v-3h9zM20 3h-8v2h8v14h-8v2h8c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2z"/>
            </svg>
          </div>
          <span class="nav-text">Logout</span>
        </a>
      </nav>
    </el-drawer>
  </header>
</template>

<script>
import Logo from '@/assets/logo.png'
import { startGoogleOAuth } from '@/utils/googleAuth'

export default {
  name: 'AppHeader',
  data () {
    return {
      drawer: false,
      loggingOut: false,
      logo: Logo,
      user: null,
      defaultAvatar: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjAiIGhlaWdodD0iNjAiIHZpZXdCb3g9IjAgMCA2MCA2MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPGNpcmNsZSBjeD0iMzAiIGN5PSIzMCIgcj0iMzAiIGZpbGw9IiNmM2Y0ZjYiLz4KPGNpcmNsZSBjeD0iMzAiIGN5PSIyMyIgcj0iOCIgZmlsbD0iIzZiNzI4MCIvPgo8cGF0aCBkPSJNMTUgNDVjMC02IDYtMTIgMTUtMTJzMTUgNiAxNSAxMiIgZmlsbD0iIzZiNzI4MCIvPgo8L3N2Zz4K',
      unreadCount: 0,
      startingOAuth: false,
      pollingTimer: null
    }
  },
  computed: {
    userNickname () {
      if (!this.user) return ''
      if (this.user.nickname) return this.user.nickname
      if (this.user.upi) return this.user.upi
      if (this.user.emailNumber) return this.user.emailNumber
      return 'User'
    }
  },
  created() {
    this.hydrate()
    if (!this.user) this.fetchMe()

    window.addEventListener('bag2bag:user-updated', this.hydrate)
    this.$watch('$route', () => this.hydrate())

    // 启动未读消息轮询
    this.startUnreadPolling()

    // 监听新消息事件
    window.addEventListener('bag2bag:new-message', this.handleNewMessage)
    window.addEventListener('bag2bag:message-read', this.handleMessageRead)
  },
  beforeDestroy() {
    window.removeEventListener('bag2bag:user-updated', this.hydrate)
    window.removeEventListener('bag2bag:new-message', this.handleNewMessage)
    window.removeEventListener('bag2bag:message-read', this.handleMessageRead)

    // 清理轮询定时器
    this.stopUnreadPolling()
  },
  methods: {
    // 检查当前路由
    isCurrentRoute(path) {
      return this.$route && this.$route.path === path
    },

    // 处理图片加载错误
    handleImageError(e) {
      console.error('Logo image failed to load:', this.logo)
      // 可以设置一个备用图片或隐藏
      e.target.style.display = 'none'
    },

    // 统一的导航处理方法
    handleNavigation(path) {
      this.drawer = false

      if (!this.user) {
        // 未登录时显示提示并触发登录
        if (this.$message && this.$message.warning) {
          this.$message.warning('Please sign in with Google to continue')
        } else {
          alert('Please sign in with Google to continue')
        }
        // 保存目标路径，登录后跳转
        this.startGoogleLogin()
      } else {
        // 已登录，正常跳转
        if (this.$router) {
          this.$router.push(path)
        }
      }
    },

    // My Messages 点击处理
    handleMessagesClick() {
      this.drawer = false

      if (!this.user) {
        if (this.$message && this.$message.warning) {
          this.$message.warning('Please sign in with Google to view messages')
        } else {
          alert('Please sign in with Google to view messages')
        }
        this.startGoogleLogin()
        return
      }

      // 已登录，跳转到消息页面
      // 尝试从多个来源获取用户 ID
      let uid = null

      // 方式1：从 this.user 获取
      if (this.user) {
        uid = this.user.id || this.user.userId || this.user.uid
      }

      // 方式2：从 $globalData 获取（备用）
      if (!uid && this.$globalData && this.$globalData.userInfo) {
        const u = this.$globalData.userInfo
        uid = u.id || u.userId || u.uid
      }

      // 方式3：从 localStorage 直接获取（备用）
      if (!uid) {
        try {
          const storedUser = localStorage.getItem('user')
          if (storedUser) {
            const parsedUser = JSON.parse(storedUser)
            uid = parsedUser.id || parsedUser.userId || parsedUser.uid
          }
        } catch (e) {
          console.error('Failed to parse user from localStorage:', e)
        }
      }

      if (!uid) {
        if (this.$message && this.$message.error) {
          this.$message.error('Unable to load user information, please try logging in again')
        } else {
          alert('Unable to load user information, please try logging in again')
        }
        // 重新获取用户信息
        this.fetchMe()
        return
      }

      this.$router.push({
        name: 'PrivateChat',
        query: { selfId: String(uid) }
      })
    },

    hydrate() {
      try {
        const raw = localStorage.getItem('user')
        this.user = raw ? JSON.parse(raw) : null
      } catch {
        this.user = null
      }
    },

    async fetchMe() {
      try {
        if (!this.$api || typeof this.$api.getUserInfo !== 'function') return
        const res = await this.$api.getUserInfo()
        if (res && res.status_code === 1 && res.data) {
          localStorage.setItem('user', JSON.stringify(res.data))
          this.user = res.data

          // 同步到 $globalData（如果存在）
          if (this.$globalData) {
            this.$globalData.userInfo = res.data
          }

          // 触发用户更新事件
          window.dispatchEvent(new Event('bag2bag:user-updated'))
        }
      } catch (error) {
        console.error('Failed to fetch user info:', error)
      }
    },

    async onLogout() {
      if (this.loggingOut) return;
      this.loggingOut = true;

      try {
        // 1) 调后端注销
        if (this.$api && this.$api.logout) {
          await this.$api.logout({});
        }
      } catch (e) {
        console.error('logout api error:', e);
      }

      // 2) 清本地登录态
      try {
        localStorage.removeItem('user');
        localStorage.removeItem('token');
        sessionStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        localStorage.setItem('loginStatus', '0');
      } catch (e) {
        console.error('clear storage error:', e);
      }

      // 3) 清全局/本地用户状态
      if (this.$globalData && this.$globalData.userInfo) {
        this.$globalData.userInfo = null;
      }
      this.user = null;

      // 4) 清 Authorization 头
      if (this.$axios && this.$axios.defaults && this.$axios.defaults.headers && this.$axios.defaults.headers.common) {
        delete this.$axios.defaults.headers.common['Authorization'];
      }

      // 5) 清除未读消息数并停止轮询
      this.unreadCount = 0;
      this.stopUnreadPolling();

      // 6) 通知其他页面清状态
      window.dispatchEvent(new Event('app-logged-out'));

      // 7) 关闭抽屉
      this.drawer = false;

      // 8) 显示成功消息
      if (this.$message && this.$message.success) {
        this.$message.success('Logged out successfully');
      }

      this.loggingOut = false;

      // 9) 直接使用 window.location 跳转到首页（强制刷新）
      window.location.href = '/index';
    },

    startGoogleLogin() {
      if (this.startingOAuth) return
      this.drawer = false
      this.startingOAuth = true
      const back = (this.$route && this.$route.fullPath) ? this.$route.fullPath : '/index'
      startGoogleOAuth(back)
    },

    goToProfile () {
      if (this.$route && this.$route.name === 'me') {
        this.$router.replace({ name: 'me', query: { r: Date.now() } })
      } else {
        this.$router.push({ name: 'me' })
      }
      this.drawer = false
    },

    onUserSectionClick () {
      if (this.user) {
        this.goToProfile()
      } else {
        this.drawer = false
        this.startGoogleLogin()
      }
    },

    // ========== 未读消息相关方法 ==========

    // 启动未读消息轮询
    startUnreadPolling() {
      // 先立即获取一次
      this.fetchUnreadCount()

      // 每30秒轮询一次
      this.pollingTimer = setInterval(() => {
        if (this.user) {
          this.fetchUnreadCount()
        }
      }, 30000) // 30秒
    },

    // 停止轮询
    stopUnreadPolling() {
      if (this.pollingTimer) {
        clearInterval(this.pollingTimer)
        this.pollingTimer = null
      }
    },

    // 获取未读消息数
    async fetchUnreadCount() {
      if (!this.user) {
        this.unreadCount = 0
        return
      }

      try {
        // 根据您的后端 API 调整这里的接口
        if (this.$api && this.$api.getUnreadMessageCount) {
          const res = await this.$api.getUnreadMessageCount()
          if (res && res.status_code === 1) {
            this.unreadCount = res.data || 0
          }
        }
      } catch (error) {
        console.error('Failed to fetch unread count:', error)
      }
    },

    // 处理新消息事件
    handleNewMessage(event) {
      if (event.detail && event.detail.increment) {
        this.unreadCount += 1
      } else {
        // 重新获取未读数
        this.fetchUnreadCount()
      }
    },

    // 处理消息已读事件
    handleMessageRead(event) {
      if (event.detail && typeof event.detail.count === 'number') {
        this.unreadCount = Math.max(0, this.unreadCount - event.detail.count)
      } else {
        // 重新获取未读数
        this.fetchUnreadCount()
      }
    }
  },
}
</script>

<style scoped>
/* 所有原有样式保持不变 */
:root {
  --nav: #0c1240;
  --line: #3ba7ff;
}

.app-head {
  width: 100%;
  background: var(--nav);
  color: #fff;
  border-bottom: 4px solid var(--line);
  position: sticky;
  top: 0;
  z-index: 90;
}

.bar {
  height: clamp(72px, 4vw + 64px, 88px);
  width: min(var(--page-max-width), 100%);
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  column-gap: clamp(12px, 3vw, 32px);
  padding: 0 clamp(16px, 4vw, 40px);
}

.brand {
  font-weight: 900;
  letter-spacing: .08em;
  font-size: clamp(20px, 2.4vw + 12px, 28px);
  color: #fff;
  text-decoration: none;
  cursor: pointer;
  justify-self: start;
}

.brand:hover { opacity: .9; }

.brand-mark {
  justify-self: center;
  height: clamp(40px, 5vw + 24px, 80px);
  width: auto;
  opacity: .95;
  display: block;
  transition: all 0.3s ease;
}

.actions {
  justify-self: end;
  display: inline-flex;
  align-items: center;
  gap: clamp(8px, 2.5vw, 16px);
}

.post-btn {
  color: #fff;
  text-decoration: none;
  padding: clamp(6px, 2vw, 10px) clamp(12px, 3vw, 18px);
  border-radius: 12px;
  border: rgba(59, 167, 255, 0.1);
  background: #0c1240;
  display: flex;
  align-items: center;
  gap: clamp(6px, 2vw, 10px);
  font-weight: 600;
  font-size: clamp(12px, 1.2vw + 10px, 15px);
  transition: all 0.3s ease;
  min-height: clamp(36px, 4vw, 44px);
  cursor: pointer;
}

.post-btn svg {
  width: clamp(16px, 1.2vw + 12px, 20px);
  height: clamp(16px, 1.2vw + 12px, 20px);
}

.post-btn:hover {
  background: var(--line);
  color: #fff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 167, 255, 0.3);
}

.login {
  color: #e5e7eb;
  text-decoration: none;
  padding: 6px 10px;
  border-radius: 10px;
  border: 1px solid rgba(255,255,255,.18);
}

.login:hover { background: rgba(255,255,255,.1); }

.menu-btn {
  border: 0;
  background: transparent;
  color: #fff;
  font-size: clamp(18px, 1.8vw + 12px, 24px);
  cursor: pointer;
  padding: clamp(6px, 2vw, 10px);
  border-radius: 12px;
  width: clamp(36px, 4vw + 26px, 48px);
  height: clamp(36px, 4vw + 26px, 48px);
  display: grid;
  place-items: center;
}

.menu-btn:hover { background: var(--line); }

.nav-drawer >>> .el-drawer__body {
  padding: 0;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.nav-drawer >>> .el-drawer__header {
  display: none;
}

.user-section {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  padding: 30px 25px;
  display: flex;
  align-items: center;
  gap: 18px;
  border-bottom: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.user-section:hover {
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  transform: translateY(-1px);
}

.user-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  border: 3px solid #e2e8f0;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.user-section:hover .user-avatar {
  border-color: var(--line);
  transform: scale(1.05);
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info {
  flex: 1;
  text-align: left;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.user-name {
  font-size: 17px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 4px;
  letter-spacing: 0.5px;
}

.user-subtitle {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.user-signin {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
}

.user-section:hover .user-signin {
  color: var(--line);
}

.drawer-nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 20px 0;
  background: transparent;
}

.nav-item {
  padding: 16px 25px;
  text-decoration: none;
  color: #334155;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  cursor: pointer;
}

.nav-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(59, 167, 255, 0.08), transparent);
  transition: left 0.5s ease;
}

.nav-item:hover::before {
  left: 100%;
}

.nav-item:hover {
  background: rgba(59, 167, 255, 0.08);
  color: #0f172a;
  transform: translateX(6px);
}

.nav-item.router-link-active {
  background: linear-gradient(90deg, rgba(59, 167, 255, 0.15), rgba(59, 167, 255, 0.05));
  color: var(--line);
  border-left: 4px solid var(--line);
  padding-left: 21px;
}

.nav-item.router-link-active:hover {
  transform: translateX(6px);
  background: linear-gradient(90deg, rgba(59, 167, 255, 0.2), rgba(59, 167, 255, 0.08));
}

.nav-icon {
  width: 22px;
  height: 22px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
}

.nav-item:hover .nav-icon {
  transform: scale(1.1);
}

.nav-icon svg {
  width: 100%;
  height: 100%;
  transition: all 0.3s ease;
}

.nav-text {
  font-size: 15px;
  letter-spacing: 0.3px;
}

.nav-text-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.nav-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, #cbd5e1, transparent);
  margin: 12px 0;
  position: relative;
}

.nav-divider::before {
  content: '';
  position: absolute;
  top: -1px;
  left: 50%;
  transform: translateX(-50%);
  width: 4px;
  height: 4px;
  background: #cbd5e1;
  border-radius: 50%;
}

.message-badge {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: white;
  font-size: 11px;
  padding: 4px 8px;
  border-radius: 12px;
  min-width: 20px;
  text-align: center;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.3);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.8; }
}

@media (max-width: 860px) {
  .bar {
    grid-template-columns: 1fr auto 1fr;
    column-gap: clamp(8px, 2vw, 16px);
  }

  .brand {
    justify-self: start;
  }

  .brand-mark {
    justify-self: center;
    height: clamp(36px, 6vw + 20px, 64px);
  }

  .actions {
    justify-self: end;
  }
}

@media (max-width: 640px) {
  .bar {
    padding: 0 clamp(16px, 6vw, 24px);
    grid-template-columns: 1fr auto 1fr;
    column-gap: clamp(8px, 2vw, 12px);
  }

  .brand-mark {
    height: clamp(32px, 5vw + 16px, 56px);
  }

  .post-btn {
    min-width: auto;
    padding: clamp(6px, 2vw, 8px) clamp(10px, 2.5vw, 14px);
    gap: clamp(4px, 1.5vw, 8px);
  }

  .user-section {
    padding: 22px 18px;
  }
  .user-avatar {
    width: 48px;
    height: 48px;
  }
  .nav-item {
    padding: 14px 16px;
    gap: 14px;
  }
  .nav-icon {
    width: 20px;
    height: 20px;
  }
  .nav-text {
    font-size: 14px;
  }
}

@media (max-width: 420px) {
  .post-btn {
    display: none;
  }

  .actions {
    gap: 10px;
  }

  .brand-mark {
    height: clamp(28px, 4vw + 14px, 48px);
  }

  .brand {
    font-size: clamp(16px, 2vw + 10px, 22px);
  }
}
</style>