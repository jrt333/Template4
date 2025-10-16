<template>
  <footer class="app-foot">
    <!-- 提手 -->
    <div class="handle">
      <div class="handle-end left"></div>
      <div class="handle-end right"></div>
    </div>

    <!-- 袋子主体 -->
    <div class="bag-body">
      <div class="foot-bar">
        <!-- 左侧 logo -->
        <div class="brand">
          <img src="@/assets/logo.png" alt="logo" class="logo" />
          <span class="brand-text">BAG2BAG</span>
        </div>

        <!-- 中间链接：About Us居中 -->
        <div class="links-wrapper">
          <a href="#" class="link-left">Contact Us</a>
          <a href="#" class="link-center">About Us</a>
          <a href="#" class="link-right">Help Center</a>
        </div>

        <!-- 右侧版权 -->
        <div class="author">
          © {{ new Date().getFullYear() }} UOA Second Hand Exchange Web
        </div>
      </div>
    </div>
  </footer>
</template>

<script>
export default {
  name: "AppFoot"
}
</script>

<style scoped>
:root {
  --nav: #0c1240;
  --line: #3ba7ff;
}

.app-foot {
  width: 100%;
  margin-top: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 提手 */
.handle {
  width: 220px;
  height: 110px;
  border: 8px solid #14b8a6;
  border-bottom: none;
  border-radius: 110px 110px 0 0;
  position: relative;
  margin-bottom: -40px;
}


/* 袋子主体 */
.bag-body {
  background: var(--nav);
  width: 100%;
  box-shadow: 0 -4px 12px rgba(0,0,0,0.2);
  border-top-left-radius: 20px;
  border-top-right-radius: 20px;
}

/* 内部 bar */
.foot-bar {
  width: 90%;
  max-width: 1000px;
  margin: 0 auto;
  padding: 40px 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
  position: relative;
}

/* 左侧品牌 */
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}
.logo {
  width: 40px;
  height: auto;
}
.brand-text {
  font-weight: 900;
  font-size: 20px;
  letter-spacing: 1px;
}

/* 中间链接 wrapper，绝对居中 */
.links-wrapper {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 20px;
}

.links-wrapper a {
  color: #ffffff;
  text-decoration: none;
  font-size: 15px;
  font-weight: 500;
  transition: color 0.3s ease;
}
.links-wrapper a:hover {
  color: var(--line);
}

/* 右侧版权 */
.author {
  font-size: 13px;
  color: #cccccc;
}

/* Responsive */
@media (max-width: 768px) {
  .foot-bar {
    flex-direction: column;
    text-align: center;
  }
  .links-wrapper {
    position: static;
    transform: none;
    flex-direction: column;
    gap: 8px;
    margin: 10px 0;
  }
}
</style>

