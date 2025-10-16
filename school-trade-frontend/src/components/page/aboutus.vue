<template>
  <div class="about-page">
    <!-- 顶部大横幅 -->
    <section class="hero-section">
      <h1>ABOUT TEAM</h1>
      <p>
        UoA CS students building a secondhand marketplace — by students, for students.
      </p>
    </section>

    <!-- 团队介绍 -->
    <section class="team-section">
      <div class="header-section">
        <h2>OUR TEAM</h2>
        <p>Scroll down to learn more about us</p>
      </div>

      <div class="content-container">
        <!-- 左侧卡片 -->
        <div class="cards-container">
          <div
              v-for="(member, index) in team"
              :key="member.username"
              class="card-wrapper"
              :style="{ zIndex: index + 1 }"
          >
            <ProfileCard
                :name="member.name"
                :username="member.username"
                :avatar="member.avatar"
                class="stack-card"
            />
          </div>
        </div>

        <!-- 右侧介绍 -->
        <div class="bio-container">
          <transition name="fade" mode="out-in">
            <div :key="currentIndex" class="bio-text">
              <h3>{{ team[currentIndex].name }}</h3>
              <p>{{ team[currentIndex].bio }}</p>
            </div>
          </transition>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import ProfileCard from "@/components/common/teamprofilecard.vue";

export default {
  name: "AboutUs",
  components: { ProfileCard },
  data() {
    return {
      currentIndex: 0,
      team: [
        { name: "Jack Yang", username: "iceeee", avatar: "", bio: "Team Leader & Full stack" },
        { name: "Shengwei Ni", username: "Pony", avatar: "", bio: "Database & Back stack" },
        { name: "Haixiang Xin", username: "lmzxmg", avatar: "", bio: "Front-end designer" },
        { name: "Xinyi Qin", username: "X", avatar: "", bio: "Front-end designer" },
        { name: "Shenglong Li", username: "Lee", avatar: "", bio: "Database & Back-stack" }
      ]
    };
  },
  mounted() {
    // 监听滚动切换团队卡片
    window.addEventListener("scroll", this.handleScroll, { passive: true });
    this.handleScroll();
  },
  beforeDestroy() {
    window.removeEventListener("scroll", this.handleScroll);
  },
  methods: {
    handleScroll() {
      const container = document.querySelector(".team-section");
      const cards = document.querySelector(".cards-container");
      if (!container || !cards) return;

      const totalHeight = cards.offsetHeight;
      const sectionHeight = totalHeight / this.team.length;
      const scrollY = window.scrollY - container.offsetTop;

      let index = Math.floor(scrollY / sectionHeight);
      if (index < 0) index = 0;
      if (index >= this.team.length) index = this.team.length - 1;
      this.currentIndex = index;
    }
  },
};
</script>

<style scoped>
.about-page {
  min-height: 100vh;
  background: #2b2b2b;
  background-repeat: no-repeat;
  background-attachment: fixed;
  color: white;
  font-family: sans-serif;
}

/* Hero 顶部 */
.hero-section {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding: 0 20px;
}

.hero-section h1 {
  font-size: 96px;
  margin-bottom: 30px;
  font-weight: bold;
  text-shadow:
      0 0 15px rgba(255, 255, 255, 0.7),
      0 0 30px rgba(255, 255, 255, 0.5),
      0 0 60px rgba(0, 153, 255, 0.6);
}

.hero-section p {
  font-size: 28px;
  max-width: 900px;
  line-height: 1.8;
  color: #f5f5f5;
}

/* 团队介绍 */
.team-section {
  padding: 80px 20px;
  background: #2b2b2b;
}

.header-section {
  text-align: center;
  margin-bottom: 40px;
}

.header-section h2 {
  font-size: 48px;
  margin-bottom: 16px;
}

.header-section p {
  font-size: 18px;
  color: #cccccc;
}

.content-container {
  display: flex;
  position: relative;
  gap: 20px;
}

.cards-container {
  position: relative;
  flex: 0 0 600px;
  height: 500vh;
}

.card-wrapper {
  position: sticky;
  top: 100px;
  display: flex;
  justify-content: flex-start;
  height: 100vh;
  padding-left: 250px;
}

.bio-container {
  flex: 0 0 350px;
  position: sticky;
  top: 50px;
  height: 80vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding-left: 300px;
}

.bio-text h3 {
  font-size: 28px;
  margin-bottom: 12px;
}

.bio-text p {
  font-size: 18px;
  line-height: 1.8;
  color: #cccccc;
}

/* 淡入淡出动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.6s ease, transform 0.6s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .content-container {
    flex-direction: column;
    align-items: center;
  }

  .cards-container {
    flex: none;
    width: 100%;
    height: auto;
    min-height: 400px;
  }

  .card-wrapper {
    position: relative;
    top: 0;
    height: auto;
    padding: 20px;
    justify-content: center;
  }

  .bio-container {
    flex: none;
    position: relative;
    top: 0;
    height: auto;
    width: 100%;
    padding: 20px;
    text-align: center;
  }
}

@media (max-width: 768px) {
  .hero-section h1 {
    font-size: 48px;
  }

  .hero-section p {
    font-size: 20px;
  }

  .header-section h2 {
    font-size: 32px;
  }

  .bio-text h3 {
    font-size: 24px;
  }

  .bio-text p {
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  .hero-section h1 {
    font-size: 36px;
  }

  .hero-section p {
    font-size: 16px;
  }

  .bio-text h3 {
    font-size: 20px;
  }

  .bio-text p {
    font-size: 14px;
  }
}
</style>






