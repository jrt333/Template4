<template>
  <div class="pc-card-wrapper" @mousemove="handleMouseMove" @mouseleave="resetCard">
    <div class="pc-card">
      <!-- 白色炫光 -->
      <div class="pc-shine"></div>
      <div class="pc-glare"></div>

      <!-- 头像 -->
      <div class="pc-avatar-content">
        <img class="avatar" :src="avatar" alt="avatar" />
      </div>

      <!-- 名字和用户名 -->
      <div class="pc-content">
        <div class="pc-details">
          <h3>{{ name }}</h3>
          <p>@{{ username }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "ProfileCard",
  props: {
    name: { type: String, required: true },
    username: { type: String, required: true },
    avatar: { type: String, required: true }
  },
  methods: {
    handleMouseMove(e) {
      const wrapper = e.currentTarget;
      const card = wrapper.querySelector(".pc-card");
      const shine = card.querySelector(".pc-shine");
      const rect = card.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;

      // 卡片旋转
      const rotateX = ((y / rect.height) - 0.5) * 50;
      const rotateY = ((x / rect.width) - 0.5) * -50;
      card.style.transform = `rotateX(${rotateX}deg) rotateY(${rotateY}deg)`;

      // 白色炫光跟随鼠标
      const px = x / rect.width * 100;
      const py = y / rect.height * 100;
      shine.style.background = `radial-gradient(circle at ${px}% ${py}%, rgba(255,255,255,0.4), rgba(255,255,255,0))`;
      shine.style.opacity = 1;

      // 蓝色炫光增强
      card.classList.add("hovered");
    },
    resetCard(e) {
      const card = e.currentTarget.querySelector(".pc-card");
      const shine = card.querySelector(".pc-shine");

      card.style.transform = "rotateX(0deg) rotateY(0deg)";
      shine.style.opacity = 0;

      card.classList.remove("hovered");
    }
  }
};
</script>

<style scoped>
.pc-card-wrapper {
  perspective: 1000px;
  display: inline-block;
  margin: 20px;
}

.pc-card {
  width: 400px;
  height: 500px;
  background: linear-gradient(145deg,#1a2a6c,#3b7fff);
  border-radius: 20px;
  overflow: hidden;
  transform-style: preserve-3d;
  transition: transform 0.3s ease;
  position: relative;

  /* 蓝光脉冲 */
  box-shadow:
      0 0 10px rgba(59,167,255,0.3),
      0 0 20px rgba(59,167,255,0.2),
      0 0 30px rgba(59,167,255,0.1);
  animation: neonPulse 2.5s infinite alternate;
}

/* 鼠标悬浮时更强蓝光 */
.pc-card.hovered {
  animation: none;
  box-shadow:
      0 0 30px rgba(59,167,255,0.6),
      0 0 60px rgba(59,167,255,0.5),
      0 0 90px rgba(59,167,255,0.4);
}

/* 白色炫光 */
.pc-shine, .pc-glare {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  border-radius: 20px;
}
.pc-shine {
  mix-blend-mode: overlay;
  opacity: 0;
  transition: opacity 0.2s;
}
.pc-glare {
  background: radial-gradient(circle at center, rgba(255,255,255,0.4), rgba(255,255,255,0));
  opacity: 0;
  transition: opacity 0.2s;
}

/* 头像 */
.pc-avatar-content {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  position: relative;
}
.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  z-index: 2;
  position: relative;
}

/* 名字和用户名 */
.pc-content {
  text-align: center;
  margin-top: 15px;
}
.pc-details h3 {
  margin: 0;
  font-size: 1.5em;
  color: #fff;
}
.pc-details p {
  margin: 5px 0 0;
  color: #cce0ff;
  font-size: 1em;
}

/* 蓝光脉冲动画 */
@keyframes neonPulse {
  0% {
    box-shadow:
        0 0 10px rgba(59,167,255,0.3),
        0 0 20px rgba(59,167,255,0.2),
        0 0 30px rgba(59,167,255,0.1);
  }
  50% {
    box-shadow:
        0 0 20px rgba(59,167,255,0.5),
        0 0 40px rgba(59,167,255,0.4),
        0 0 60px rgba(59,167,255,0.3);
  }
  100% {
    box-shadow:
        0 0 10px rgba(59,167,255,0.3),
        0 0 20px rgba(59,167,255,0.2),
        0 0 30px rgba(59,167,255,0.1);
  }
}
</style>



