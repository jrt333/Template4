<template>
  <div class="auth-page">
    <router-link class="back-btn" to="/" aria-label="Home">
      <i class="el-icon-arrow-left"></i><span>Home</span>
    </router-link>

    <div class="auth-card">
      <!-- 左側導流面板 -->
      <aside class="side">
        <div class="side-inner">
          <!-- 如為 .svg/.jpg 請改副檔名 -->
          <img class="bag" :src="require('@/assets/logo.png')" alt="BAG2BAG logo" />
          <div class="ask">Want to<br> <strong>Log in?</strong></div>
          <router-link class="cta" to="/login">Login →</router-link>
        </div>
      </aside>

      <!-- 裝飾圓環 -->
      <div class="ring"></div>

      <!-- 右側表單 -->
      <section class="form-wrap">
        <h1 class="title">Sign up</h1>

        <div class="field">
          <label>Nickname</label>
          <el-input v-model.trim="userInfo.nickname" placeholder="Your nickname" />
        </div>

        <div class="field">
          <label>UPI</label>
          <el-input v-model.trim="userInfo.accountNumber" placeholder="e.g. abcd123" />
        </div>

        <div class="field">
          <label>verification code</label>
          <el-input v-model.trim="emailCode" placeholder="Enter code">
            <template slot="append">
              <el-button
                  :disabled="codeBtnDisabled"
                  size="mini"
                  @click="sendCode"
              >{{ codeBtnText }}</el-button>
            </template>
          </el-input>
        </div>

        <div class="field">
          <label>Set password</label>
          <el-input placeholder="Enter your Password..." show-password maxlength="16"
                    v-model="userInfo.userPassword" class="sign-in-input" clearable>
            <template slot="prepend"><div class="el-icon-lock"></div></template>
          </el-input>
        </div>

        <div class="field">
          <label>Confirm password</label>
          <el-input placeholder="Confirm your Password..." show-password maxlength="16"
                    v-model="userPassword2" @keyup.enter.native="signIn"
                    class="sign-in-input" clearable>
            <template slot="prepend"><div class="el-icon-lock"></div></template>
          </el-input>
        </div>

        <div class="actions">
          <el-button type="primary" @click="signIn">submit</el-button>
        </div>
      </section>
    </div>
  </div>
</template>


<script>
import Logo from '@/assets/logo.png'
export default {
  name: "sign-in",
  data(){
    return{
      logo: Logo,
      userPassword2:'',
      emailCode:'',
      codeBtnText:'Click to get Code',
      codeBtnDisabled:false,
      countdown:60,
      timer:null,
      userInfo:{
        accountNumber:'',
        userPassword:'',
        nickname:''
      }
    };
  },
  methods:{
    toLogin(){
      this.$router.replace({path: '/login'});
    },
    sendCode(){
      if(!this.userInfo.accountNumber){
        this.$message.error('Please enter your UPI！');
        return;
      }
      this.$api.getEmailCode({upi:this.userInfo.accountNumber}).then(res=>{
        if(res.status_code===1){
          this.$message.success('code has been send');
          this.codeBtnDisabled=true;
          this.codeBtnText=this.countdown+'s';
          this.timer=setInterval(()=>{
            this.countdown--;
            this.codeBtnText=this.countdown+'s';
            if(this.countdown<=0){
              clearInterval(this.timer);
              this.countdown=60;
              this.codeBtnText='click to get code';
              this.codeBtnDisabled=false;
            }
          },1000);
        }else{
          this.$message.error(res.msg);
        }
      }).catch(()=>{
        this.$message.error('code send error');
      });
    },
    signIn(){
      if(this.userInfo.accountNumber&&this.userInfo.userPassword&&this.userInfo.nickname&&this.emailCode){
        if(this.userInfo.userPassword!==this.userPassword2){
          this.$message.error('please confirm your password!');
        }else {
          this.$api.signIn({...this.userInfo,code:this.emailCode}).then(res=>{
            if(res.status_code===1){
              this.$message({
                message: 'Sign In success！',
                type: 'success'
              });
              this.$router.replace({path: '/login'});
            }else {
              this.$message.error(res.msg);
            }
          }).catch(e=>{
            console.log(e);
            this.$message.error('Sign In error network error');
          })
        }
      }else{
        this.$message.error('Please enter all Information!');
      }
    }
  }
}
</script>

<style scoped>
:root { --nav:#0c1240; --line:#1597a8; }

.auth-page { display:grid; place-items:center; min-height:calc(100vh - 120px); padding:24px; background:#f5f6f8; }
.auth-card {
  position:relative; width:min(980px, 96%); min-height:420px; background:#fff;
  border-radius:20px; box-shadow:0 12px 28px rgba(0,0,0,.08);
  overflow:hidden; display:grid; grid-template-columns: 360px 1fr;
}

/* 左側深藍面板 */
.side { background:var(--nav); color:#fff; }
.side-inner { height:100%; display:flex; flex-direction:column; align-items:center; justify-content:center; gap:22px; padding:24px; }
.bag { height:80px; width:auto; opacity:.95; display:block; }
.ask { font-size:24px; line-height:1.25; text-align:center; }
.cta { display:inline-block; padding:10px 18px; background:#129fb0; color:#fff; border-radius:9999px; text-decoration:none; font-weight:700; }

/* 右側表單 */
.form-wrap { position:relative; padding:34px 32px 28px; background:#fff; }
.title { font-weight:800; color:var(--nav); margin:2px 0 18px; }
.field { margin-bottom:12px; }
.field label { display:block; font-size:13px; color:#374151; margin:0 0 6px 4px; }
::v-deep .el-input__inner { border-radius:9999px; }

/* 裝飾圓環 */
.ring {
  position:absolute; left:300px; top:50%; transform:translateY(-50%);
  width:220px; height:220px; border-radius:50%;
  border:14px solid #1597a8; border-left-color:transparent; opacity:.25;
}

/* 按鈕 */
.actions { display:flex; align-items:center; gap:14px; margin-top:10px; }
.submit { background:#129fb0; border-color:#129fb0; border-radius:9999px; padding:10px 20px; }
.ghost { color:#9da3ae; pointer-events:auto; text-decoration:none; }

@media (max-width: 860px) {
  .auth-card { grid-template-columns: 1fr; }
  .side { order:2; }
  .ring { display:none; }
}

.auth-page { position: relative; } /* 讓絕對定位以此為參考 */

.back-btn {
  position: absolute;
  top: 12px; left: 12px;
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 12px;
  background: #fff;
  color: var(--nav);
  border: 1px solid #e5e7eb;
  border-radius: 9999px;
  box-shadow: 0 6px 16px rgba(0,0,0,.08);
  cursor: pointer;
  font-weight: 600;
}
.back-btn:hover { background: #f7f7f9; }
.back-btn:active { transform: translateY(1px); }
.back-btn .el-icon-arrow-left { font-size: 16px; }
</style>
