<template>
  <div class="oauth-callback">
    <div class="card">
      <div class="spinner" aria-hidden="true"></div>
      <h1 class="title">Signing you in…</h1>
      <p class="message">{{ message }}</p>
      <el-button v-if="showRetry" type="primary" @click="retry">Try again</el-button>
    </div>
  </div>
</template>

<script>
import { getGoogleRedirectUri, startGoogleOAuth } from '@/utils/googleAuth';

export default {
  name: 'GoogleOAuthCallback',
  data () {
    return {
      message: 'Please wait while we verify your Google account.',
      showRetry: false,
      redirectTimer: null
    };
  },
  async created () {
    const { code, state, error, error_description: errorDescription } = this.$route.query || {};

    if (error) {
      this.message = errorDescription || 'Google authentication was cancelled.';
      this.showRetry = true;
      return;
    }

    if (!code) {
      this.message = 'Missing authorization code. Please start the sign-in flow again.';
      this.showRetry = true;
      return;
    }

    try {
      const payload = { code, redirectUri: getGoogleRedirectUri() };
      const res = await this.$api.googleLogin(payload);

      if (!res || res.status_code !== 1) {
        const msg = (res && (res.msg || res.message)) || 'Unable to complete Google authentication.';
        this.message = msg;
        if (this.shouldRedirectToEscortify(msg)) {
          this.redirectToEscortify();
          return;
        }
        this.showRetry = true;
        return;
      }

      try {
        localStorage.setItem('user', JSON.stringify(res.data || {}));
        localStorage.setItem('loginStatus', '1');
        window.dispatchEvent(new Event('bag2bag:user-updated'));
      } catch (e) {
        console.warn('Persist user error:', e);
      }

      try {
        const info = await this.$api.getUserInfo();
        if (info && info.status_code === 1 && info.data) {
          this.$globalData.userInfo = info.data;
        }
      } catch (err) {
        console.warn('Fetch user info error:', err);
      }

      const target = this.normalizeState(state);
      this.message = 'Authentication successful. Redirecting…';
      this.redirectTimer = setTimeout(() => {
        this.redirectToTarget(target);
      }, 400);
    } catch (e) {
      console.error(e);
      const possibleMsg =
          (e && e.response && e.response.data && (e.response.data.msg || e.response.data.message)) ||
          (e && e.message);
      if (this.shouldRedirectToEscortify(possibleMsg)) {
        this.redirectToEscortify();
        return;
      }
      this.message = 'An unexpected error occurred while signing you in.';
      this.showRetry = true;
    }
  },
  beforeDestroy () {
    if (this.redirectTimer) {
      clearTimeout(this.redirectTimer);
    }
  },
  methods: {
    retry () {
      startGoogleOAuth(this.$route.query && this.$route.query.state ? this.$route.query.state : '/index');
    },
    shouldRedirectToEscortify (message) {
      if (!message || typeof window === 'undefined') return false;
      const normalized = message.toString().toLowerCase();
      return normalized.includes('@aucklanduni.ac.nz') || normalized.includes('aucklanduni');
    },

    redirectToEscortify () {
      if (typeof window === 'undefined' || !window.location) return;
      window.location.replace('https://www.youtube.com/watch?v=dQw4w9WgXcQ&list=RDdQw4w9WgXcQ&start_radio=1');
    },

    normalizeState (state) {
      if (!state || typeof state !== 'string') {
        return '/index';
      }
      let decoded = state;
      try {
        decoded = decodeURIComponent(state);
      } catch (e) {
        decoded = state;
      }
      if (!decoded || decoded === '/') {
        return '/index';
      }
      return decoded.startsWith('/') ? decoded : `/${decoded}`;
    },
    redirectToTarget (target) {
      const path = target && typeof target === 'string' ? target : '/index';
      if (this.$router && typeof this.$router.replace === 'function') {
        this.$router.replace(path).catch(() => {
          this.forceWindowRedirect(path);
        });
      } else {
        this.forceWindowRedirect(path);
      }
    },
    forceWindowRedirect (path) {
      if (typeof window === 'undefined') {
        return;
      }
      const finalPath = path === '/index' ? '/' : path;
      const origin = window.location && window.location.origin ? window.location.origin : '';
      window.location.replace(`${origin}${finalPath === '/' ? '' : finalPath}`);
    }
  }
};
</script>

<style scoped>
.oauth-callback {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0c1240 0%, #1b2c80 100%);
  color: #fff;
  padding: 32px 16px;
}

.card {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 32px 24px;
  max-width: 420px;
  width: 100%;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(12px);
}

.spinner {
  width: 48px;
  height: 48px;
  margin: 0 auto 16px auto;
  border-radius: 50%;
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-top-color: #3ba7ff;
  animation: spin 1s linear infinite;
}

.title {
  margin: 0 0 12px;
  font-size: 24px;
  font-weight: 700;
}

.message {
  margin: 0 0 20px;
  font-size: 16px;
  line-height: 1.6;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>