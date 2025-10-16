<template>
  <div class="home-page">
    <!-- 顶部导航 -->
    <app-head />

    <!-- Hero -->
    <section class="hero">
      <div class="hero-text">
        <span class="hero-badge">Welcome to Bag2Bag</span>
        <h1>Buy, Sell & <span>Exchange</span> with Ease</h1>
        <p>
          Share what you have, find what you need<br />
          Connect with people near you
        </p>
        <div class="hero-buttons">
          <button class="btn-primary" @click="handleStartSelling">Start Here</button>
          <button class="btn-secondary" @click="goLearnMore">About Team</button>
        </div>
      </div>

      <div class="hero-image">
        <img src="@/assets/AmigosShapes.png" alt="hero-img" />
      </div>
    </section>

    <!-- 1️⃣ 顶部大横幅 -->
    <section class="hero-section">
      <h1>ABOUT US</h1>
      <p>
        Campus Second-Hand Trading Platform — Keeping unused items in <span class="highlight-eco">circulation</span> and enabling resource <span class="highlight-eco">sharing</span>.
      </p>
    </section>

    <!-- 2️⃣ 项目介绍（带弧形动画 + 圆形文字） -->
    <section ref="project" class="project-section">
      <!-- 弧形 SVG -->
      <div class="curve">
        <svg viewBox="0 0 1440 150" xmlns="http://www.w3.org/2000/svg">
          <path
              d="M0,150 C480,0 960,0 1440,150 L1440,0 L0,0 Z"
              fill="#f0f8ff"
          ></path>
        </svg>
      </div>

      <!-- 圆形旋转文字 -->
      <div class="circular-text-wrapper" v-if="projectVisible">
        <CircularText
            text="BAG2BAG2BAG2"
            :spinDuration="20"
            onHover="speedUp"
        />
      </div>

      <!-- 文字内容 -->
      <div class="project-content" :class="{ active: projectVisible }">
        <h2>WHY WE HERE</h2>
        <p>
          Our campus second-hand marketplace is dedicated to providing students with a secure, convenient and eco-friendly community for trading pre-owned items.
          Here, you can effortlessly list unused possessions, discover great bargains on second-hand goods, and complete transactions face-to-face with fellow alumni.
        </p>
        <ul>
          <li>Convenient and swift: on-campus transactions with face-to-face delivery</li>
          <li>Secure and reliable: Real-name authentication safeguards transaction security.</li>
          <li>Green and eco-friendly: giving unused items a new lease of life</li>
          <li>Campus Mutual Support: Bridging the Gap Between Students</li>
        </ul>
      </div>
    </section>

    <!-- Flow Section -->
    <section class="flow">
      <h2 class="flow-title">How It Works</h2>
      <div class="flow-steps">
        <div class="step">
          <div class="icon">👜</div>
          <h3>1. List Your Bag</h3>
          <p>Upload photos and set your price in minutes.</p>
        </div>
        <div class="step">
          <div class="icon">🔍</div>
          <h3>2. Get Discovered</h3>
          <p>Shoppers find your listing easily.</p>
        </div>
        <div class="step">
          <div class="icon">🤝</div>
          <h3>3. Make a Deal</h3>
          <p>Chat and confirm your buyer securely.</p>
        </div>
        <div class="step">
          <div class="icon">🚚</div>
          <h3>4. Face-to-Face Trade</h3>
          <p>Meet in person and complete the deal quickly.</p>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <app-foot />
  </div>
</template>

<script>
import AppHead from "../common/AppHeader.vue";
import AppFoot from "../common/AppFoot.vue";
import CircularText from "@/components/common/CircularText.vue";
import { startGoogleOAuth } from '@/utils/googleAuth'

export default {
  name: "HomePage",
  components: { AppHead, AppFoot, CircularText },
  data() {
    return {
      currentUser: null,
      projectVisible: false,
      startingOAuth: false
    };
  },
  computed: {
    isAuthed() {
      return !!this.currentUser;
    },
  },
  methods: {
    handleStartSelling() {
      // 检查用户是否已登录
      if (!this.isAuthed) {
        // 未登录，显示提示并触发 Google 登录
        if (this.$message && this.$message.warning) {
          this.$message.warning('Please sign in with Google to continue');
        } else {
          alert('Please sign in with Google to continue');
        }

        // 触发 Google OAuth 登录，登录成功后跳转到 /home
        this.startGoogleLogin('/home');
      } else {
        // 已登录，直接跳转到 home 页面
        this.$router.push({ path: "/home" });
      }
    },

    startGoogleLogin(redirectPath) {
      if (this.startingOAuth) return;
      this.startingOAuth = true;

      // 使用 googleAuth 工具触发登录，登录成功后跳转到指定路径
      const targetPath = redirectPath || this.$route.fullPath;
      startGoogleOAuth(targetPath);
    },

    goLearnMore() {
      this.$router.push({ path: "/about" });
    },

    syncUser() {
      try {
        const raw = localStorage.getItem("user");
        this.currentUser = raw ? JSON.parse(raw) : null;
      } catch (e) {
        this.currentUser = null;
      }
    },
  },
  mounted() {
    this.syncUser();

    // 监听 storage 变化（同一浏览器其他标签页的变化）
    window.addEventListener("storage", (e) => {
      if (e.key === "user") {
        this.syncUser();
      }
    });

    // 监听自定义事件（同一页面的登录状态变化）
    window.addEventListener('bag2bag:user-updated', this.syncUser);

    // 监听项目介绍进入视口
    const observer = new IntersectionObserver(
        ([entry]) => {
          if (entry.isIntersecting) {
            this.projectVisible = true;
          }
        },
        { threshold: 0.3 }
    );
    if (this.$refs.project) {
      observer.observe(this.$refs.project);
    }
  },

  beforeDestroy() {
    // 清理事件监听
    window.removeEventListener('bag2bag:user-updated', this.syncUser);
  }
};
</script>

<style scoped>
.home-page {
  background: #f0f8ff;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.home-page > .flow {
  flex-grow: 1;
}

/* Hero Section */
.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 600px;
  padding: 100px;
  background: #f0f8ff;
}
.hero-text {
  flex: 1;
  max-width: 50%;
  z-index: 2;
}
.hero-badge {
  display: inline-block;
  background: #dbeafe;
  color: #2563eb;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 18px;
  margin-bottom: 15px;
}
.hero-text h1 {
  font-size: 56px;
  margin-bottom: 20px;
  font-weight: bold;
}
.hero-text h1 span {
  color: #14b8a6;
}
.hero-text p {
  font-size: 20px;
  margin-bottom: 30px;
  line-height: 1.6;
}
.hero-buttons {
  display: flex;
  gap: 16px;
}
.btn-primary {
  background: #0c1240;
  color: white;
  padding: 14px 28px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 18px;
  transition: all 0.3s ease;
}
.btn-primary:hover {
  background: #14b8a6;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(20, 184, 166, 0.3);
}
.btn-secondary {
  background: transparent;
  border: 2px solid #0c1240;
  color: #0c1240;
  padding: 14px 28px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 18px;
  transition: all 0.3s ease;
}
.btn-secondary:hover {
  background: #e0f2fe;
  transform: translateY(-2px);
}
.hero-image {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}
.hero-image::before {
  content: "";
  position: absolute;
  width: 420px;
  height: 420px;
  border-radius: 50%;
  background: radial-gradient(circle, #e6f5ff 0%, #ffffff 80%);
  z-index: 1;
}
.hero-image img {
  max-width: 100%;
  height: auto;
  position: relative;
  z-index: 2;
}

/* 1️⃣ About Us 大标题 */
.hero-section {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding: 0 20px;
  background: #f0f8ff;
}

.hero-section h1 {
  font-size: 96px;
  margin-bottom: 30px;
  font-weight: bold;
  color: #0c1240;
  letter-spacing: 8px;
}

.hero-section p {
  font-size: 28px;
  max-width: 900px;
  line-height: 1.8;
  color: #0c1240;
}

.hero-section .highlight-eco {
  color: #14b8a6;
  font-weight: 600;
}

/* 2️⃣ 项目介绍 */
.project-section {
  min-height: 100vh;
  background: #dbeafe;
  color: #0c1240;
  font-family: sans-serif;
  position: relative;
  overflow: hidden;
  padding-top: 80px;
}

/* 圆弧 SVG */
.curve {
  position: absolute;
  top: -1px;
  left: 0;
  width: 100%;
  height: 150px;
  line-height: 0;
  z-index: 1;
}

/* 圆形旋转文字 */
.circular-text-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 60px;
  position: relative;
  z-index: 2;
}

/* 项目介绍文字内容 */
.project-content {
  padding: 120px 20px 80px;
  max-width: 900px;
  margin: 0 auto;
  text-align: center;
  opacity: 0;
  transform: translateY(40px);
  transition: opacity 0.8s ease, transform 0.8s ease;
  position: relative;
  z-index: 2;
}

.project-content.active {
  opacity: 1;
  transform: translateY(0);
}

.project-content h2 {
  font-size: 32px;
  margin-bottom: 20px;
  color: #0c1240;
}

.project-content p {
  font-size: 18px;
  line-height: 1.6;
  margin-bottom: 30px;
  color: #1e3c72;
}

.project-content ul {
  list-style: none;
  padding: 0;
}

.project-content li {
  font-size: 18px;
  margin: 12px 0;
  color: #0c1240;
}

/* Flow Section */
.flow {
  padding: 80px 40px 0;
  background: linear-gradient(to bottom, #dbeafe 0%, #f0f8ff 100%);
  text-align: center;
}

.flow-title {
  font-size: 32px;
  margin-bottom: 50px;
  color: #0c1240;
}

.flow-steps {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  max-width: 1200px;
  margin: 0 auto;
  padding-bottom: 80px;
}

/* Footer 包装器 */
.footer-wrapper {
  background: #f0f8ff;
  margin-top: 0;
  padding-top: 0;
}

.step {
  flex: 1 1 220px;
  margin: 20px;
  padding: 20px;
  border-radius: 12px;
  background: #bfdbfe;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease;
}

.step:hover {
  transform: translateY(-6px);
  background: #93c5fd;
}

.step .icon {
  font-size: 40px;
  margin-bottom: 15px;
}

.step h3 {
  font-size: 20px;
  margin-bottom: 10px;
  color: #0c1240;
}

.step p {
  font-size: 16px;
  color: #1e3c72;
}

@media (max-width: 768px) {
  .hero {
    flex-direction: column;
    text-align: center;
    padding: 20px;
    min-height: 500px;
  }
  .hero-text {
    max-width: 100%;
    margin-bottom: 20px;
  }
  .hero-text h1 {
    font-size: 40px;
  }
  .hero-section h1 {
    font-size: 48px;
  }
  .hero-section p {
    font-size: 20px;
  }
  .project-content h2 {
    font-size: 28px;
  }
  .flow-title {
    font-size: 28px;
  }
}
</style>


