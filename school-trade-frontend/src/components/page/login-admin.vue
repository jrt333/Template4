<template>
  <div class="auth-page">
    <!-- 返回主页 -->
    <router-link class="back-btn" to="/">‹ Home</router-link>

    <div class="auth-card">
      <!-- 左侧导流区：保持与 login.vue 同结构/氛围 -->
      <aside class="auth-left">
        <div class="brand">
          <img class="brand-logo" :src="logo" alt="logo" />
          <h3>Admin portal</h3>
          <p class="muted">Staff only</p>
        </div>
        <router-link class="ghost" to="/login">← User Login</router-link>
      </aside>

      <!-- 右侧表单区 -->
      <section class="auth-right">
        <h1 class="title">Admin Login</h1>
        <p class="sub">Sign in to the management console</p>

        <el-form :model="form" label-position="top">
          <el-form-item label="Username">
            <el-input v-model.trim="form.username" />
          </el-form-item>
          <el-form-item label="Password">
            <el-input v-model.trim="form.password" type="password" show-password />
          </el-form-item>
            <el-button type="primary" :loading="loading" @click="onSubmit">Login</el-button>
        </el-form>
          <p class="err" v-if="err">{{ err }}</p>
      </section>
    </div>
  </div>
</template>

<script>
import Logo from "@/assets/logo.png";

export default {
  name: 'LoginAdmin',
  data() {
    return {
      logo: Logo,
      form: {username: '', password: ''},
      loading: false,
      err: ''
    }
  },
  methods: {
    onSubmit () {
      if (this.loading) return
      this.err = ''
      if (!this.form.username || !this.form.password) {
        this.err = '请输入账号与密码'
        return
      }
      this.loading = true

      // GET + params，字段名按你接口：accountNumber / adminPassword
      this.$api.adminLogin({
        accountNumber: this.form.username,
        adminPassword: this.form.password
      })
          .then((res) => {
              if (res && res.status_code === 1) {
                  this.$router.replace('/platform-admin').catch(() => {})
                  return
              }
              this.err = (res && res.msg) || 'Login failed'
          })
          .catch((error) => {
              (error && error.msg) ||
              (error && error.response && error.response.data && error.response.data.msg)
              this.err = msg || 'Login failed'
          })
          .finally(() => {
              this.loading = false
          })
    }
  }
}
</script>

<style scoped>
/* 页面背景与容器 */
.auth-page { min-height: 100vh; display: grid; place-items: center; background: #f3f4f6; padding: 32px; }
.auth-card { width: 960px; background: #fff; border-radius: 16px; box-shadow: 0 20px 60px rgba(0,0,0,.12);
  display: grid; grid-template-columns: 1fr 1.2fr; overflow: hidden; }

/* 左侧导流 */
.auth-left { background: #07133B; color: #fff; padding: 28px; display: grid; gap: 18px; align-content: center; }
.brand { text-align: left; display: grid; gap: 8px; }
.brand-logo { width: 72px; height: 72px; object-fit: contain; filter: drop-shadow(0 10px 24px rgba(0,0,0,.25)); }
.muted { opacity: .85; }
.ghost { color: #fff; opacity: .95; text-decoration: none; display: inline-block; border: 1px solid rgba(255,255,255,.35);
  padding: 8px 12px; border-radius: 10px; width: fit-content; margin-top: 8px; }

/* 右侧表单 */
.auth-right { padding: 36px; display: grid; gap: 14px; align-content: center; }
.title { font-size: 28px; font-weight: 800; }
.sub { color: #6b7280; margin-top: -6px; }
.form :deep(.el-form-item__label) { font-size: 13px; color: #6b7280; }
.submit { width: 100%; margin-top: 4px; }
.err { color: #ef4444; font-size: 12px; margin-top: 10px; }

/* 返回按钮 */
.back-btn { position: absolute; left: 16px; top: 16px; padding: 8px 12px; background: #fff; border: 1px solid #e5e7eb;
  border-radius: 999px; text-decoration: none; color: #111827; box-shadow: 0 6px 16px rgba(0,0,0,.08); }

/* 小屏处理：隐藏左栏，改为单栏表单 */
@media (max-width: 860px) {
  .auth-card { grid-template-columns: 1fr; }
  .auth-left { display: none; }
}
</style>
