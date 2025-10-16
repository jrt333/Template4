<template>
  <div>
    <app-head :nickname-value="userInfo.nickname"
              :avatarValue="userInfo.avatar"></app-head>
    <app-body>
      <!-- User Information Section - Enhanced Header Style -->
      <div class="user-profile-section">
        <div class="user-profile-card">
          <!-- User Profile Header -->
          <div class="profile-header">
            <div class="profile-cover">
              <div class="cover-gradient"></div>
              <div class="header-decoration"></div>
            </div>

            <div class="profile-main">
              <div class="avatar-section">
                <el-upload
                    v-if="isSelf"
                    :action="apiBaseUrl + '/file/'"
                    :on-success="fileHandleSuccess"
                    :file-list="imgFileList"
                    accept="image/*"
                    :show-file-list="false"
                >
                  <div class="avatar-wrapper">
                    <el-image
                        class="user-avatar"
                        :src="userInfo.avatar"
                        fit="cover">
                      <div slot="error" class="avatar-placeholder">
                        <i class="el-icon-user-solid"></i>
                      </div>
                    </el-image>
                    <div class="avatar-overlay">
                      <i class="el-icon-camera"></i>
                      <span>Change</span>
                    </div>
                  </div>
                </el-upload>
                <div v-else class="avatar-wrapper">
                  <el-image class="user-avatar" :src="userInfo.avatar" fit="cover">
                    <div slot="error" class="avatar-placeholder">
                      <i class="el-icon-user-solid"></i>
                    </div>
                  </el-image>
                </div>
              </div>

              <div class="profile-info">
                <div class="name-and-rating">
                  <h1 class="user-nickname">{{userInfo.nickname || 'User name'}}</h1>
                  <div class="user-rating">
                    <div class="rating-stars">
                      <i
                          v-for="n in 5"
                          :key="n"
                          class="el-icon-star-on"
                          :class="starClass(n)"
                      ></i>
                    </div>
                      <span v-if="userInfo && Number(userInfo.rating) > 0">
                        {{ (Number(userInfo.rating || 0)).toFixed(2) }}
                    </span>
                      <span v-else class="text-gray-400">
                        No ratings yet
                    </span>
                  </div>
                </div>

                <div v-if="!isSelf" class="chat-bottom-bar">
                  <el-button type="primary" icon="el-icon-chat-dot-round" class="chat-bottom-btn" @click="goPrivateChat">
                    <button class="chat-cta" @click="goPrivateChat">
                    <span class="chat-cta__icon">
                      <i class="el-icon-chat-dot-round"></i>
                    </span>
                      <span class="chat-cta__label">Chat with him</span>
                    </button>
                  </el-button>
                </div>


                <div class="user-meta-grid">
                  <div class="meta-card">
                    <i class="el-icon-location detail-icon"></i>
                    <div>
                      <span class="meta-label">Country</span>
                      <span class="meta-value">{{userInfo.country || 'Wait To Upload'}}</span>
                    </div>
                  </div>

                  <div class="meta-card">
                    <i class="el-icon-school detail-icon"></i>
                    <div>
                      <span class="meta-label">Major</span>
                      <span class="meta-value">{{userInfo.major || 'Wait To Upload'}}</span>
                    </div>
                  </div>

                  <div class="meta-card">
                    <i class="el-icon-time detail-icon"></i>
                    <div>
                      <span class="meta-label">Degree</span>
                      <span class="meta-value">{{userInfo.degree || 'Wait To Upload'}}</span>
                    </div>
                  </div>
                </div>

                <div class="profile-actions">
                  <el-button type="primary" icon="el-icon-edit" @click="userInfoDialogVisible = true" v-if="isSelf" class="edit-profile-btn">
                    Edit Your Profile
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Edit Personal Information Dialog -->
      <el-dialog
          @close="finishEdit"
          title="Edit Personal Information"
          :visible.sync="userInfoDialogVisible"
          width="500px"
          class="user-edit-dialog">
        <div class="edit-form">
          <div class="form-section">
            <h4>Basic Information</h4>
            <div class="form-item">
              <label>Nickname</label>
              <el-input
                  v-model="userInfo.nickname"
                  :disabled="notUserNicknameEdit"
                  @change="saveUserNickname"
                  maxlength="20"
                  show-word-limit>
                <el-button slot="append" type="warning" icon="el-icon-edit"
                           @click="notUserNicknameEdit = false">Edit
                </el-button>
              </el-input>
            </div>

            <div class="form-item">
              <label>Country</label>
              <el-input v-model="userInfo.country" placeholder="Please enter your country" maxlength="30"></el-input>
            </div>

            <div class="form-item">
              <label>Major</label>
              <el-input v-model="userInfo.major" placeholder="Please enter your major" maxlength="50"></el-input>
            </div>

            <div class="form-item">
              <label>Degree</label>
              <el-select v-model="userInfo.degree" placeholder="Please select degree">
                <el-option label="Bachelor's" value="Bachelor's"></el-option>
                <el-option label="Master's" value="Master's"></el-option>
                <el-option label="PhD" value="PhD"></el-option>
                <el-option label="Other" value="Other"></el-option>
              </el-select>
            </div>
          </div>

          <div class="form-section">
            <h4>Password Change</h4>
            <div v-if="userPasswordEdit">
              <div class="form-item">
                <label>Current Password</label>
                <el-input v-model="userPassword1" show-password placeholder="Please enter current password"></el-input>
              </div>
              <div class="form-item">
                <label>New Password</label>
                <el-input v-model="userPassword2" show-password placeholder="Please enter new password"></el-input>
              </div>
              <div class="form-item">
                <label>Confirm New Password</label>
                <el-input v-model="userPassword3" show-password placeholder="Please enter new password again"></el-input>
              </div>
              <div class="password-actions">
                <el-button @click="cancelPasswordEdit">Cancel</el-button>
                <el-button type="primary" @click="savePassword">Confirm Change</el-button>
              </div>
            </div>
            <div v-else>
              <div class="form-item">
                <label>Password</label>
                <el-input
                    value="••••••••"
                    :disabled="true">
                  <el-button slot="append" type="warning" icon="el-icon-edit"
                             @click="userPasswordEdit = true">Change Password
                  </el-button>
                </el-input>
              </div>
            </div>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="userInfoDialogVisible=false">Close</el-button>
          <el-button type="primary" @click="saveUserInfo">Save Information</el-button>
        </span>
      </el-dialog>

      <!-- Items and Orders Management Section -->
      <div class="items-management-section">
        <div class="section-header">
          <h3>My Items and Orders</h3>
        </div>

        <el-tabs v-model="activeName" @tab-click="handleClick" class="custom-tabs">
          <el-tab-pane name="1">
            <span slot="label">
              <i class="el-icon-sell"></i>
              Want Sell
            </span>
          </el-tab-pane>
          <el-tab-pane name="2">
            <span slot="label">
              <i class="el-icon-refresh"></i>
              Want Exchange
            </span>
          </el-tab-pane>
          <el-tab-pane name="4" v-if="isSelf">
            <span slot="label">
              <i class="el-icon-star-on"></i>
              My Favorites
            </span>
          </el-tab-pane>
        </el-tabs>

        <div class="items-grid">
          <div v-if="dataList[activeName-1] && dataList[activeName-1].length === 0" class="empty-state">
            <i class="el-icon-box"></i>
            <p>No related data available</p>
          </div>

          <div v-for="(item,index) in dataList[activeName-1]"
               :key="index"
               class="item-card"
               @click="toDetails(activeName,item)">
            <div class="item-image">
              <el-image
                  :src="item.imgUrl"
                  fit="cover">
                <div slot="error" class="image-error">
                  <i class="el-icon-picture-outline"></i>
                </div>
              </el-image>
              <div class="item-status" v-if="activeName==='5'||activeName==='6'">
                <span class="status-badge" :class="'status-' + item.orderStatus">
                  {{orderStatus[item.orderStatus]}}
                </span>
              </div>
            </div>

            <div class="item-content">
              <h4 class="item-title">{{item.idleName}}</h4>
              <p class="item-description">{{item.idleDetails}}</p>
              <div class="item-meta">
                <span class="item-time">{{item.timeStr}}</span>
              </div>
              <div class="item-footer">
                <div class="item-price">￥{{item.idlePrice}}</div>
                <el-button
                    v-if="activeName==='3'||activeName==='4'"
                    :type="activeName==='4' ? 'warning' : 'danger'"
                    size="mini"
                    plain
                    @click.stop="handle(activeName,item,index)">
                  {{handleName[activeName-1]}}
                </el-button>
                <el-button
                    v-if="activeName==='1' && isSelf"
                    type="success"
                    size="mini"
                    plain
                    @click.stop="handleSell(item,index)">
                  Publish for Sale
                </el-button>
                <el-button
                    v-if="activeName==='2' && isSelf"
                    type="primary"
                    size="mini"
                    plain
                    @click.stop="handleExchange(item,index)">
                  Propose Exchange
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <app-foot></app-foot>
    </app-body>
  </div>
</template>

<script>
import AppHead from '../common/AppHeader.vue';
import AppBody from '../common/AppPageBody.vue'
import AppFoot from '../common/AppFoot.vue'
import { getApiBaseUrl } from '../../utils/env';

export default {
  name: "me",
  components: {
    AppHead,
    AppBody,
    AppFoot
  },
  props: {
    id: { type: [String, Number], default: null } // 供 /user/:id 使用
  },

  beforeRouteUpdate (to, from, next) {
    // 1) 取目標 id 與自己 id
    const toId = to.params && to.params.id ? String(to.params.id) : null
    const myId = this.myId != null ? String(this.myId) : null

    // 2) 清掉上一位使用者的畫面資料，避免殘留
    this.userInfo = {}
    this.$set(this.dataList, 0, [])
    this.$set(this.dataList, 1, [])
    try { sessionStorage.removeItem('viewUserProfile') } catch (e) {}

    // 3) 分流：看別人 or 看自己
    const load = (toId && myId && toId !== myId)
        ? this.loadOtherProfile(toId).then(() => Promise.all([
          this.getSellItems(), this.getExchangeItems()
        ]))
        : this.loadSelfProfile().then(() => Promise.all([
          this.getSellItems(), this.getExchangeItems(), this.getMyFavorite(true)
        ]))

    load.then(() => next()).catch(() => next())
  },

  computed: {
    myId () {
      var s = this.$store && this.$store.state && this.$store.state.user
      var g = this.$globalData && this.$globalData.userInfo
      return (s && (s.userId || s.id)) || (g && (g.userId || g.id)) || null
    },
    targetUserId () {
      return this.id != null ? String(this.id) : (this.myId != null ? String(this.myId) : null)
    },
    isSelf () {
      if (!this.targetUserId || !this.myId) return true
      return String(this.targetUserId) === String(this.myId)
    }
  },

  data() {
    return {
        apiBaseUrl: getApiBaseUrl(),
      pictureList: [],
      imgFileList:[],
      activeName: '1',
      handleName: ['Publish', 'Exchange', 'Delete', 'Remove Favorite', '', ''],
      dataList: [[], [], [], [], [], []], // Sell, Exchange, Delisted, Favorites, Sales, Purchases
      orderStatus: ['Pending Payment', 'Pending Shipment', 'Pending Receipt', 'Completed', 'Cancelled'],
      fallbackImg: 'https://dummyimage.com/600x400/e5e7eb/9ca3af&text=No+Image',
      userInfoDialogVisible: false,
      notUserNicknameEdit: true,
      userPasswordEdit: false,
      userPassword1: '',
      userPassword2: '',
      userPassword3: '',
      userInfo: {
        id: "", // 对应数据库 id 字段（BIGINT）
        account_number: "", // 对应数据库 account_number 字段
        Email_number: "", // 对应数据库 Email_number 字段
        UPI: "", // 对应数据库 UPI 字段
        avatar: "", // 对应数据库 avatar 字段
        nickname: "", // 对应数据库 nickname 字段
        sign_in_time: "", // 对应数据库 sign_in_time 字段
        major: "", // 对应数据库 major 字段
        country: "", // 对应数据库 National 字段（注意大写N）
        degree: "", // 对应数据库 degree 字段
        rating: "5.0" // 对应数据库 rating 字段
      }
    };
  },
  created: async function () {
    // 先確保拿到自己的登入資訊（但不要動 this.userInfo）
    await this.ensureUser();

    var routeId = this.id != null ? String(this.id) : null;
    var myId    = this.myId != null ? String(this.myId) : null;

    console.log('[me.vue created]', { routeId, myId, isSelf: this.isSelf });

    if (routeId && myId && routeId !== myId) {
      // --- 他人模式 ---
      console.log('[me.vue] 他人模式，呼叫 getUserById', routeId);
      await this.loadOtherProfile(routeId);     // 只把畫面用的 userInfo 換成對方
      await this.getSellItems();                // 用 targetUserId 拉對方清單
      await this.getExchangeItems();
    } else {
      // --- 自己模式 ---
      console.log('[me.vue] 自己模式，呼叫 loadSelfProfile');
      await this.loadSelfProfile();             // 把畫面用的 userInfo 設為自己
      await this.getSellItems();                // 用 targetUserId 拉自己的清單
      await this.getExchangeItems();
      await this.getMyFavorite(true);           // ✅ 只有自己時才拉
    }
  },

  methods: {
    async ensureUser () {
      // 只確保 $globalData.userInfo 補齊，別動 this.userInfo
      if (this.$globalData && this.$globalData.userInfo && (this.$globalData.userInfo.id || this.$globalData.userInfo.userId)) return;
      const r = await this.$api.getUserInfo();
      if (r && (r.status_code === 1 || r.code === 200 || r.success)) {
        this.$globalData.userInfo = r.data || r.result || r.body || {};
      }
    },

    async loadSelfProfile () {
      if (!this.$globalData || !this.$globalData.userInfo) {
        const r = await this.$api.getUserInfo();
        if (r && (r.status_code === 1 || r.code === 200 || r.success)) {
          this.$globalData.userInfo = r.data || r.result || r.body || {};
        }
      }
      this.userInfo = (this.$globalData && this.$globalData.userInfo) ? this.$globalData.userInfo : {};
      // 可加保底欄位
      this.userInfo.nickname = this.userInfo.nickname || 'User name';
    },

      starClass (n) {
          var r = (this.userInfo && this.userInfo.rating) ? Number(this.userInfo.rating) : 0;
          if (r >= n) return 'is-full';
          if (r >= n - 0.5) return 'is-half';
          return 'is-empty';
      },

    async loadOtherProfile (uid) {
      try {
        const res = await this.$api.getPublicUser({ id: uid })
        if (res && (res.status_code === 1 || res.code === 200)) {
          const d = res.data || res.result || res.body || {}
          this.userInfo = {
            id: d.id,
            avatar: d.avatar || '',
            nickname: d.nickname || 'User name',
            country: d.country || '',
            major: d.major || '',
            degree: d.degree || '',
            signInTime: d.signInTime || '',
            rating: d.rating || '',
            rating_count: d.rating_count || '',
          }
          return
        }
      } catch (e) {
        console.warn('loadOtherProfile failed', e)
      }
      this.userInfo = { id: Number(uid), nickname: 'User name' } // 保底
    },

    goPrivateChat () {
      // 安全取得 targetId（不使用 ?.）
      const targetId = this.targetUserId || this.userInfo.id
      const selfId   = this.myId

      if (!targetId) {
        if (this.$message && this.$message.error) {
          this.$message.error('找不到對方的ID，無法發起私聊');
        } else {
          alert('找不到對方的ID，無法發起私聊');
        }
        return;
      }

      // 跳轉到私聊頁
      try {
        this.$router.push({
          name: 'PrivateChat',
          query: { selfId: String(selfId), peerId: String(targetId) }  // ★ 關鍵
        }).catch(err => {
          // 忽略重複導航；其它錯誤印出來
          if (err && err.name !== 'NavigationDuplicated') console.error(err);
        });
      } catch (e) {
        this.$router.push({
          path: '/privateChat',
          query: { targetId: String(targetId) }
        });
      }
    },


    goDetails(id) {
      this.$router.push({ path: "/idle-details", query: { id } });
    },

    safePics(p) {
      if (Array.isArray(p)) return p;
      if (!p) return [];
      try {
        var v = JSON.parse(p);
        return Array.isArray(v) ? v : [String(v)];
      } catch (e) {
        return [String(p)];
      }
    },




    async handleClick(tab) {
      const name = tab.name;     // '1' | '2' | ...
      this.loading = true;
      try {
        if (name === '1') {
          // Want Sell
          this.getSellItems();             // 你這個方法已經會先清空 dataList[0]
        } else if (name === '2') {
          // Want Exchange
          this.getExchangeItems();         // 你這個方法已經會先清空 dataList[1]
        } else if (name === '3') {
          // My Delisted
          this.dataList[2] = [];           // 這個方法不會清空，先手動清掉
          this.getIdleItemData();          // 內部把 idleStatus===2 放到 dataList[2]
        } else if (name === '4') {
          // My Favorites
          this.dataList[3] = [];           // ★ 你的 getMyFavorite 目前不清空，要加這行避免重複
          this.getMyFavorite();
        } else if (name === '5') {
          // My Sales
          this.dataList[4] = [];           // ★ 同上
          this.getMySoldIdle();
        } else if (name === '6') {
          // My Purchases
          this.dataList[5] = [];           // ★ 同上
          this.getMyOrder();
        }
      } finally {
        this.loading = false;
      }
    },

    // 顯示sell 的内容//
    async getSellItems () {
      const uid = Number(this.targetUserId || 0);
      if (!uid) { this.$set(this.dataList, 0, []); return; }

      // 後端已支援 trade 參數（推薦）
      const res  = await this.$api.getAllIdleItem({ userId: uid, trade: 1 });
      const list = (res && Array.isArray(res.data)) ? res.data : [];
      const rows = list
          .filter(function(r){
            var userId = (uid !== undefined && uid !== null) ? uid : uid;
            return Number(userId) === uid;
          })
          .map((r) => {
            var trade = (r.idle_trade !== undefined && r.idle_trade !== null) ? r.idle_trade :
                (r.idleTrade !== undefined && r.idleTrade !== null) ? r.idleTrade : 1; // 沒傳就當 1
            var pics  = this.safePics((r.pictureList !== undefined && r.pictureList !== null) ? r.pictureList : r.picture_list);

            var obj = Object.assign({}, r);
            obj.idleTrade = Number(trade);
            obj.imgUrl    = pics[0] || '';
            return obj;
          })
          .filter(function(r){ return r.idleTrade === 1; });
      this.$set(this.dataList, 0, rows);
    },

    // 顯示exchange 的内容//
    async getExchangeItems () {
      const uid = Number(this.targetUserId || 0);
      if (!uid) { this.$set(this.dataList, 1, []); return; }

      const res  = await this.$api.getAllIdleItem({ userId: uid, trade: 2 });
      const list = (res && Array.isArray(res.data)) ? res.data : [];
      const rows = list
          .filter(function(r){
            var userId = (uid !== undefined && uid !== null) ? uid : uid;
            return Number(userId) === uid;
          })
          .map((r) => {
            var trade = (r.idle_trade !== undefined && r.idle_trade !== null) ? r.idle_trade :
                (r.idleTrade !== undefined && r.idleTrade !== null) ? r.idleTrade : 2; // 沒傳就當 2
            var pics  = this.safePics((r.pictureList !== undefined && r.pictureList !== null) ? r.pictureList : r.picture_list);

            var obj = Object.assign({}, r);
            obj.idleTrade = Number(trade);
            obj.imgUrl    = pics[0] || '';
            return obj;
          })
          .filter(function(r){ return r.idleTrade === 2; });
      this.$set(this.dataList, 1, rows);
    },




    // 顯示Favorite 的内容//
    async getMyFavorite(force = false) {
      console.log('[favorite] enter, force =', force);
      if (!force && this.dataList[3] && this.dataList[3].length) {
        console.log('[favorite] use cache:', this.dataList[3].length);
        return;
      }
      console.log('[favorite] calling GET /favorite/my');

      // 確認你的封裝會帶 cookie；若不確定，改用 axios + { withCredentials:true }
      const res  = await this.$api.getMyFavorite();
      const list = (res && Array.isArray(res.data)) ? res.data : [];

      const rows = list.map(f => {
        const item = f.idleItem || f.idle_item || {};
        const pics = this.safePics(item.pictureList || item.picture_list);
        const t = f.createTime || f.create_time || item.releaseTime || item.release_time || '';
        return {
          favoriteId: f.id || f.favoriteId,
          id: item.id,
          imgUrl: pics[0] || '',
          idleName: item.idleName || item.idle_name,
          idleDetails: item.idleDetails || item.idle_details,
          timeStr: (t || '').slice(0,10) + ' ' + (t || '').slice(11,19),
          idlePrice: item.idlePrice || item.idle_price,
        };
      });
      console.log('[favorite] rows =', rows.length);
      this.$set(this.dataList, 3, rows);
    },



    handleSell(item, index) {
      this.$confirm('Confirm to list this item for sale?', 'Confirmation', {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }).then(() => {
        // Handle sell logic here
        this.$message.success('Item listed for sale successfully!');
      });
    },
    handleExchange(item, index) {
      this.$confirm('Confirm to propose this item for exchange?', 'Confirmation', {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }).then(() => {
        // Handle exchange logic here
        this.$message.success('Exchange proposal submitted successfully!');
      });
    },

    getMySoldIdle(){
      this.$api.getMySoldIdle().then(res=>{
        if (res.status_code === 1){
          for (let i = 0; i < res.data.length; i++) {
            let pictureList = JSON.parse(res.data[i].idleItem.pictureList);
            this.dataList[4].push({
              id:res.data[i].id,
              imgUrl:pictureList.length > 0 ? pictureList[0] : '',
              idleName:res.data[i].idleItem.idleName,
              idleDetails:res.data[i].idleItem.idleDetails,
              timeStr:res.data[i].createTime.substring(0, 10) + " " + res.data[i].createTime.substring(11, 19),
              idlePrice:res.data[i].orderPrice,
              orderStatus:res.data[i].orderStatus
            });
          }
        }
      })
    },

    getMyOrder(){
      this.$api.getMyOrder().then(res=>{
        if (res.status_code === 1){
          for (let i = 0; i < res.data.length; i++) {
            let pictureList = JSON.parse(res.data[i].idleItem.pictureList);
            this.dataList[5].push({
              id:res.data[i].id,
              imgUrl:pictureList.length > 0 ? pictureList[0] : '',
              idleName:res.data[i].idleItem.idleName,
              idleDetails:res.data[i].idleItem.idleDetails,
              timeStr:res.data[i].createTime.substring(0, 10) + " " + res.data[i].createTime.substring(11, 19),
              idlePrice:res.data[i].orderPrice,
              orderStatus:res.data[i].orderStatus
            });
          }
        }
      })
    },

    getIdleItemData() {
      this.$api.getAllIdleItem().then(res => {
        if (res.status_code === 1) {
          for (let i = 0; i < res.data.length; i++) {
            res.data[i].timeStr = res.data[i].releaseTime.substring(0, 10) + " " + res.data[i].releaseTime.substring(11, 19);
            let pictureList = JSON.parse(res.data[i].pictureList);
            res.data[i].imgUrl = pictureList.length > 0 ? pictureList[0] : '';
            if (res.data[i].idleStatus === 2) {
              this.dataList[2].push(res.data[i]);
            }
          }
        }
      })
    },



    //保存以及修改個人用戶資料//
    saveUserNickname() {
      this.notUserNicknameEdit = true;
      this.$api.updateUserPublicInfo({
        id: this.userInfo.id, // 添加用户ID用于数据库更新
        nickname: this.userInfo.nickname
      }).then(res => {
        if (res.status_code === 1) {
          this.$globalData.userInfo.nickname = this.userInfo.nickname;
          this.$message.success('Nickname updated successfully!');
        } else {
          this.$message.error('Update failed: ' + res.msg);
        }
      }).catch(() => {
        this.$message.error('Update failed, please try again!');
      })
    },
    saveUserInfo() {
      // 验证必填字段
      if (!this.userInfo.nickname.trim()) {
        this.$message.error('Nickname cannot be empty!');
        return;
      }

      // 更新用户信息到数据库
      this.$api.updateUserPublicInfo({
        id: this.userInfo.id, // 用户ID，用于WHERE条件
        nickname: this.userInfo.nickname.trim(),
        major: this.userInfo.major.trim(),
        degree: this.userInfo.degree,
        country: this.userInfo.country.trim() // 注意大写N，匹配数据库字段名
      }).then(res => {
        if (res.status_code === 1) {
          // 更新全局用户信息
          this.$globalData.userInfo = {
            ...this.$globalData.userInfo,
            nickname: this.userInfo.nickname,
            major: this.userInfo.major,
            degree: this.userInfo.degree,
            country: this.userInfo.country // 注意大写N，匹配数据库字段名
          };
          this.$message.success('Information saved successfully!');
          this.userInfoDialogVisible = false;
        } else {
          this.$message.error('Save failed: ' + (res.msg || 'Unknown error'));
        }
      }).catch((error) => {
        console.error('Update user info error:', error);
        this.$message.error('Network error, please try again!');
      })
    },
    savePassword() {
      if (!this.userPassword1 || !this.userPassword2) {
        this.$message.error('Password cannot be empty!');
      } else if (this.userPassword2 !== this.userPassword3) {
        this.$message.error('The two passwords entered are inconsistent!');
      } else if (this.userPassword2.length < 6) {
        this.$message.error('New password length cannot be less than 6 characters!');
      } else {
        this.$api.updatePassword({
          id: this.userInfo.id, // 添加用户ID
          oldPassword: this.userPassword1,
          newPassword: this.userPassword2
        }).then(res => {
          if (res.status_code === 1) {
            this.userPasswordEdit = false;
            this.$message.success('Password changed successfully!');
            this.clearPasswordFields();
          } else {
            this.$message.error('Old password is incorrect, change failed!');
          }
        }).catch((error) => {
          console.error('Password update error:', error);
          this.$message.error('Password update failed, please try again!');
        })
      }
    },
    cancelPasswordEdit() {
      this.userPasswordEdit = false;
      this.clearPasswordFields();
    },
    clearPasswordFields() {
      this.userPassword1 = '';
      this.userPassword2 = '';
      this.userPassword3 = '';
    },
    finishEdit() {
      this.notUserNicknameEdit = true;
      this.userInfoDialogVisible = false;
      this.userPasswordEdit = false;
      this.clearPasswordFields();
    },
    toDetails(activeName, item) {
      if (activeName === '5'||activeName === '6') {
        this.$router.push({path: '/order', query: {id: item.id}});
      }else {
        this.$router.push({path: '/details', query: {id: item.id}});
      }
    },
    handle(activeName,item,index) {
      const confirmText = activeName === '3' ? 'Confirm to delete this item?' : 'Confirm to remove from favorites?';

      this.$confirm(confirmText, 'Confirmation', {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }).then(() => {
        if(activeName==='3'){
          this.$api.updateIdleItem({
            id:item.id,
            idleStatus:0
          }).then(res=>{
            if(res.status_code===1){
              this.dataList[2].splice(index,1);
              this.$message.success('Item has been deleted');
            }else {
              this.$message.error(res.msg)
            }
          });
        }else if(activeName==='4'){
          this.$api.deleteFavorite({
            id: item.favoriteId
          }).then(res=>{
            if(res.status_code===1){
              this.$message.success('Removed from favorites!');
              this.dataList[3].splice(index,1);
            }else {
              this.$message.error(res.msg)
            }
          })
        }
      });
    },
    fileHandleSuccess(response, file, fileList) {
      let imgUrl = response.data;
      this.imgFileList = [];
      this.$api.updateUserPublicInfo({
        id: this.userInfo.id, // 添加用户ID
        avatar: imgUrl
      }).then(res => {
        if (res.status_code === 1) {
          this.userInfo.avatar = imgUrl;
          this.$globalData.userInfo.avatar = imgUrl;
          this.$message.success('Avatar updated successfully!');
        } else {
          this.$message.error('Avatar update failed: ' + res.msg);
        }
      }).catch(() => {
        this.$message.error('Avatar update failed!');
      })
    },
  },
}
</script>

<style scoped>
/* User Profile Section Styles - Transparent glassmorphism design */
.user-profile-section {
  padding: 0;
  background: linear-gradient(

      #0c1240 0%,
      #0c1240 60%,
      #14b8a6 100%

  );
  margin-bottom: 30px;
  min-height: 400px;
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(30, 124, 142, 0.3);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  animation: cardRiseIn 0.8s cubic-bezier(0.165, 0.84, 0.44, 1);
}

@keyframes cardRiseIn {
  0% {
    opacity: 0;
    transform: translateY(40px) scale(0.95);
  }
  60% {
    opacity: 0.8;
    transform: translateY(-5px) scale(1.02);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.user-profile-card {
  background: transparent;
  border-radius: 0;
  padding: 0;
  box-shadow: none;
  height: 100%;
}

/* Profile Header Design */
.profile-header {
  position: relative;
  padding: 40px 30px 30px;
}

.cover-gradient {
  width: 100%;
  height: 100%;
  background: rgba(255,255,255,0.08);
}

.header-decoration {
  position: absolute;
  top: -50%;
  right: -20%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  border-radius: 50%;
}

.header-decoration::before {
  content: '';
  position: absolute;
  top: 20%;
  left: 20%;
  width: 60%;
  height: 60%;
  background: radial-gradient(circle, rgba(255,255,255,0.05) 0%, transparent 70%);
  border-radius: 50%;
}

.profile-main {
  position: relative;
  z-index: 2;
  margin-top: 60px;
  display: flex;
  align-items: flex-start;
  gap: 40px;
}

.avatar-section {
  flex-shrink: 0;
}

.avatar-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
  border: 4px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.avatar-wrapper:hover {
  transform: translateY(-8px) scale(1.05);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.5);
  border-color: rgba(255, 255, 255, 0.5);
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.user-avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: rgba(12, 18, 64, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: all 0.3s ease;
  border-radius: 50%;
  backdrop-filter: blur(10px);
}

.avatar-overlay i {
  font-size: 24px;
  margin-bottom: 4px;
}

.avatar-overlay span {
  font-size: 12px;
  font-weight: 600;
}

.profile-info {
  flex: 1;
  color: white;
}

.name-and-rating {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 25px;
}

.user-nickname {
  font-size: 36px;
  font-weight: 700;
  color: white;
  margin: 0;
  letter-spacing: -0.5px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.user-rating {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255,255,255,0.2);
  padding: 10px 18px;
  border-radius: 25px;
  backdrop-filter: blur(15px);
  border: 1px solid rgba(255,255,255,0.3);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.rating-stars {
  color: #ffd217;
  font-size: 16px;
  display: flex;
  gap: 2px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.rating-text {
  font-size: 16px;
  font-weight: 700;
  color: white;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.user-meta-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  margin-bottom: 25px;
}

.meta-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(15px);
  padding: 18px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.25);
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.meta-card:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
  border-color: rgba(255, 255, 255, 0.4);
}

.detail-icon {
  color: #ffd217;
  font-size: 20px;
  flex-shrink: 0;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.meta-card div {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meta-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.meta-value {
  font-size: 15px;
  color: white;
  font-weight: 700;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.profile-actions {
  display: flex;
  gap: 15px;
}

.edit-profile-btn {
  background: rgba(255, 255, 255, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.4);
  color: white;
  padding: 14px 28px;
  border-radius: 16px;
  font-weight: 700;
  font-size: 16px;
  backdrop-filter: blur(15px);
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.edit-profile-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.6);
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.3);
}

.edit-profile-btn:active {
  transform: translateY(-2px);
}

/* Edit Dialog Styles */
.user-edit-dialog .el-dialog {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 25px 50px rgba(30, 124, 142, 0.3);
  border: 1px solid rgba(30, 124, 142, 0.1);
}

.user-edit-dialog .el-dialog__header {
  background: #1e7c8e;
  color: white;
  padding: 25px 30px;
}

.user-edit-dialog .el-dialog__title {
  color: white;
  font-weight: 700;
  font-size: 20px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.user-edit-dialog .el-dialog__headerbtn .el-dialog__close {
  color: white;
  font-size: 20px;
}

.edit-form {
  padding: 30px;
  background: #f8f9ff;
}

.form-section {
  margin-bottom: 35px;
}

.form-section h4 {
  color: #0c1240;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 3px solid rgba(30, 124, 142, 0.2);
  font-size: 18px;
  font-weight: 700;
  letter-spacing: -0.3px;
}

.form-item {
  margin-bottom: 25px;
}

.form-item label {
  display: block;
  margin-bottom: 10px;
  color: #0c1240;
  font-weight: 700;
  font-size: 15px;
}

.form-item .el-input input {
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
  padding: 12px 15px;
  font-size: 15px;
}

.form-item .el-input input:focus {
  border-color: #1e7c8e;
  box-shadow: 0 0 0 4px rgba(30, 124, 142, 0.15);
}

.form-item .el-select {
  width: 100%;
}

.form-item .el-select .el-input__inner {
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  padding: 12px 15px;
  font-size: 15px;
}

.password-actions {
  display: flex;
  gap: 15px;
  margin-top: 25px;
}

.password-actions .el-button {
  border-radius: 12px;
  font-weight: 600;
  padding: 10px 20px;
  font-size: 14px;
}

.password-actions .el-button--primary {
  background: #1e7c8e;
  border-color: #1e7c8e;
}

.password-actions .el-button--primary:hover {
  background: #2ba3b8;
  border-color: #2ba3b8;
  transform: translateY(-2px);
}

/* Items Management Section Styles */
.items-management-section {
  padding: 0 30px 30px;
}

.section-header {
  margin-bottom: 30px;
}

.section-header h3 {
  color: #0c1240;
  font-size: 32px;
  margin: 0;
  font-weight: 800;
  letter-spacing: -0.8px;
  text-shadow: 0 2px 4px rgba(30, 124, 142, 0.1);
}

.custom-tabs {
  background: white;
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 10px 40px rgba(30, 124, 142, 0.12);
  border: 1px solid rgba(30, 124, 142, 0.08);
  animation: tabsRiseIn 0.6s ease-out 0.3s both;
}

@keyframes tabsRiseIn {
  0% {
    opacity: 0;
    transform: translateY(30px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.custom-tabs .el-tabs__header {
  margin-bottom: 25px;
}

.custom-tabs .el-tabs__item {
  font-size: 16px;
  font-weight: 700;
  padding: 18px 30px;
  color: #64748b;
  transition: all 0.3s ease;
  border-radius: 15px 15px 0 0;
  margin-right: 5px;
}

.custom-tabs .el-tabs__item:hover {
  color: #1e7c8e;
  background: rgba(30, 124, 142, 0.05);
  transform: translateY(-2px);
}

.custom-tabs .el-tabs__item.is-active {
  color: #1e7c8e;
  background: rgba(30, 124, 142, 0.1);
  box-shadow: 0 4px 15px rgba(30, 124, 142, 0.2);
}

.custom-tabs .el-tabs__item i {
  margin-right: 10px;
  font-size: 18px;
}

.custom-tabs .el-tabs__active-bar {
  background: #1e7c8e;
  height: 4px;
  border-radius: 2px;
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 25px;
  margin-top: 30px;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 100px 20px;
  color: #64748b;
}

.empty-state i {
  font-size: 80px;
  margin-bottom: 25px;
  display: block;
  color: #1e7c8e;
  opacity: 0.4;
}

.empty-state p {
  font-size: 20px;
  margin: 0;
  font-weight: 600;
}

.item-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(30, 124, 142, 0.12);
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
  cursor: pointer;
  border: 1px solid rgba(30, 124, 142, 0.08);
  animation: itemCardRise 0.6s ease-out;
  animation-fill-mode: both;
}

.item-card:nth-child(1) { animation-delay: 0.1s; }
.item-card:nth-child(2) { animation-delay: 0.2s; }
.item-card:nth-child(3) { animation-delay: 0.3s; }
.item-card:nth-child(4) { animation-delay: 0.4s; }
.item-card:nth-child(5) { animation-delay: 0.5s; }
.item-card:nth-child(6) { animation-delay: 0.6s; }

@keyframes itemCardRise {
  0% {
    opacity: 0;
    transform: translateY(40px) scale(0.9);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.item-card:hover {
  transform: translateY(-12px) scale(1.02);
  box-shadow: 0 25px 50px rgba(30, 124, 142, 0.2);
  border-color: rgba(30, 124, 142, 0.15);
}

.item-image {
  position: relative;
  height: 240px;
  overflow: hidden;
}

.item-image .el-image {
  width: 100%;
  height: 100%;
  transition: transform 0.4s ease;
}

.item-card:hover .item-image .el-image {
  transform: scale(1.1);
}

.image-error {
  width: 100%;
  height: 100%;
  background: #f8f9ff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1e7c8e;
  font-size: 40px;
  opacity: 0.6;
}

.item-status {
  position: absolute;
  top: 15px;
  right: 15px;
}

.status-badge {
  padding: 8px 16px;
  border-radius: 25px;
  font-size: 12px;
  font-weight: 700;
  color: white;
  backdrop-filter: blur(15px);
  box-shadow: 0 4px 15px rgba(0,0,0,0.3);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.status-0 { background: rgba(245, 108, 108, 0.9); }
.status-1 { background: rgba(230, 162, 60, 0.9); }
.status-2 { background: rgba(64, 158, 255, 0.9); }
.status-3 { background: rgba(103, 194, 58, 0.9); }
.status-4 { background: rgba(144, 147, 153, 0.9); }

.item-content {
  padding: 25px;
}

.item-title {
  font-size: 20px;
  font-weight: 800;
  color: #0c1240;
  margin: 0 0 12px 0;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  letter-spacing: -0.5px;
}

.item-description {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 18px 0;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  line-height: 1.6;
  font-weight: 500;
}

.item-meta {
  margin-bottom: 20px;
}

.item-time {
  font-size: 13px;
  color: #94a3b8;
  font-weight: 600;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-price {
  font-size: 22px;
  font-weight: 800;
  color: #1e7c8e;
  letter-spacing: -0.5px;
  text-shadow: 0 1px 3px rgba(30, 124, 142, 0.2);
}

.item-footer .el-button {
  border-radius: 12px;
  font-weight: 700;
  padding: 10px 18px;
  transition: all 0.3s ease;
}

.item-footer .el-button--success {
  background: #67c23a;
  border-color: #67c23a;
  color: white;
}

.item-footer .el-button--success:hover {
  background: #85ce61;
  border-color: #85ce61;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(103, 194, 58, 0.4);
}

.item-footer .el-button--primary {
  background: #1e7c8e;
  border-color: #1e7c8e;
  color: white;
}

.item-footer .el-button--primary:hover {
  background: #2ba3b8;
  border-color: #2ba3b8;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(30, 124, 142, 0.4);
}

.item-footer .el-button--warning {
  background: #e6a23c;
  border-color: #e6a23c;
  color: white;
}

.item-footer .el-button--warning:hover {
  background: #ebb563;
  border-color: #ebb563;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(230, 162, 60, 0.4);
}

.item-footer .el-button--danger {
  background: #f56c6c;
  border-color: #f56c6c;
  color: white;
}

.item-footer .el-button--danger:hover {
  background: #f78989;
  border-color: #f78989;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(245, 108, 108, 0.4);
}

/* Responsive Design */
@media (max-width: 768px) {
  .profile-main {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 25px;
    margin-top: 40px;
  }

  .profile-info {
    width: 100%;
  }

  .name-and-rating {
    justify-content: center;
    flex-direction: column;
    gap: 20px;
  }

  .user-nickname {
    font-size: 28px;
  }

  .user-meta-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .items-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
  }

  .user-profile-section {
    margin-bottom: 25px;
  }

  .profile-header {
    padding: 30px 20px 25px;
  }

  .items-management-section {
    padding: 0 20px 25px;
  }

  .custom-tabs {
    padding: 20px;
  }

  .custom-tabs .el-tabs__item {
    font-size: 14px;
    padding: 15px 20px;
  }
}

/* Enhanced Animation Effects */

/* Loading Animation for Dynamic Content */
.meta-card, .item-card {
  position: relative;
  overflow: hidden;
}

.meta-card::before, .item-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg,
  transparent 0%,
  rgba(255, 255, 255, 0.4) 50%,
  transparent 100%);
  transition: left 0.6s ease;
  z-index: 1;
}

.meta-card:hover::before, .item-card:hover::before {
  left: 100%;
}

.user-rating {
  position: relative;
}

/* Custom Scrollbar */
.items-grid::-webkit-scrollbar {
  width: 8px;
}

.items-grid::-webkit-scrollbar-track {
  background: rgba(30, 124, 142, 0.1);
  border-radius: 4px;
}

.items-grid::-webkit-scrollbar-thumb {
  background: rgba(30, 124, 142, 0.6);
  border-radius: 4px;
}

.items-grid::-webkit-scrollbar-thumb:hover {
  background: rgba(30, 124, 142, 0.8);
}
/* ====== Profile Card Compact（缩小版）====== */

/* 整体卡片高度和圆角、阴影缩小 */
.user-profile-section {
  min-height: 280px;            /* 原来 400px → 280px */
  margin-bottom: 20px;          /* 原 30px */
  border-radius: 16px;          /* 原 20px */
  box-shadow: 0 12px 24px rgba(30, 124, 142, 0.18); /* 更轻 */
}

/* 头部内边距缩小 */
.profile-header {
  padding: 24px 24px 20px;      /* 原 40px 30px 30px */
}

/* 主体块间距缩小 */
.profile-main {
  margin-top: 24px;             /* 原 60px */
  gap: 20px;                    /* 原 40px */
  align-items: center;
}

/* 头像缩小 */
.avatar-wrapper {
  width: 88px;                  /* 原 120px */
  height: 88px;
  border-width: 3px;            /* 原 4px */
  box-shadow: 0 6px 18px rgba(0,0,0,.35);
}

/* 标题和评分组缩小 */
.user-nickname { font-size: 28px; }         /* 原 36px */
.user-rating   { padding: 6px 12px; }        /* 原 10px 18px */
.rating-stars  { font-size: 14px; }          /* 原 16px */
.rating-text   { font-size: 14px; }          /* 原 16px */

/* 信息小卡更紧凑 */
.user-meta-grid {
  gap: 10px;                    /* 原 15px */
  margin-bottom: 16px;          /* 原 25px */
}
.meta-card {
  padding: 12px;                /* 原 18px */
  border-radius: 12px;          /* 原 16px */
  box-shadow: 0 3px 10px rgba(0,0,0,0.12);
}
.detail-icon  { font-size: 18px; }           /* 原 20px */
.meta-label   { font-size: 11px; }           /* 原 12px */
.meta-value   { font-size: 14px; }           /* 原 15px */

/* 操作按钮更轻更短 */
.edit-profile-btn {
  padding: 10px 16px;           /* 原 14px 28px */
  font-size: 14px;              /* 原 16px */
  border-radius: 12px;          /* 原 16px */
  box-shadow: 0 3px 10px rgba(0,0,0,0.18);
}

/* 视口缩小时进一步收紧：避免拥挤 */
@media (max-width: 1200px) {
  .user-profile-section { min-height: 260px; }
  .profile-header       { padding: 20px; }
  .profile-main         { margin-top: 20px; gap: 16px; }
}
@media (max-width: 768px) {
  .avatar-wrapper { width: 76px; height: 76px; }
  .user-nickname  { font-size: 24px; }
  .user-rating    { padding: 5px 10px; }
  .meta-card      { padding: 10px; }
}
/* ===== Items grid —— 紧凑版尺寸（只影响本页） ===== */
.items-management-section .items-grid {
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); /* 原 320px → 260px */
  gap: 16px;                                  /* 原 25px → 16px */
}

/* 卡片外观更轻一些 */
.items-management-section .item-card {
  border-radius: 14px;                         /* 原 20px */
  box-shadow: 0 6px 18px rgba(30,124,142,.10); /* 原来更重 */
  transition: transform .25s ease, box-shadow .25s ease;
}
.items-management-section .item-card:hover {
  transform: translateY(-6px) scale(1.01);     /* 原 -12px、1.02 */
  box-shadow: 0 12px 28px rgba(30,124,142,.16);
}

/* 关键：缩小图片区域高度（主导卡片高度） */
.items-management-section .item-image {
  height: 170px;                               /* 原 240px */
  overflow: hidden;
}

/* 内容区更紧凑 */
.items-management-section .item-content { padding: 16px; }       /* 原 25px */
.items-management-section .item-title   { font-size: 16px; margin-bottom: 8px; } /* 原 20px / 12px */
.items-management-section .item-description { font-size: 13px; margin-bottom: 12px; }
.items-management-section .item-time    { font-size: 12px; }
.items-management-section .item-price   { font-size: 18px; }     /* 原 22px */

/* 按钮更小更短 */
.items-management-section .item-footer .el-button {
  padding: 6px 10px;                           /* 原 10px 18px */
  font-size: 12px;                             /* 原 14px */
  border-radius: 10px;                         /* 原 12px */
}

/* 中小屏再收紧一点 */
@media (max-width: 1024px) {
  .items-management-section .item-image { height: 150px; }
}
@media (max-width: 640px) {
  .items-management-section .items-grid {
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 12px;
  }
  .items-management-section .item-image { height: 130px; }
  .items-management-section .item-content { padding: 14px; }
  .items-management-section .item-title { font-size: 15px; }
  .items-management-section .item-price { font-size: 16px; }
}
.items-management-section .item-image { height: auto; aspect-ratio: 4 / 3; }

:root { --nav: #0c1240; --accent: #0c1240; --line: #27a5ff; --muted: #e5e7eb; }

/* Cards (copied from homepage) */
.cards { display: grid; grid-template-columns: repeat(3, minmax(200px, 1fr)); gap: 24px; padding: 8px 32px 24px; }
.card { position: relative; background: #fff; border-radius: 14px; border: 6px solid #d6dee5; padding: 10px; height: 220px; cursor: pointer; }
.cover { width: 100%; height: 120px; border-radius: 8px; background: #f9fafb; }
.heart { position: absolute; top: 10px; right: 12px; background: #fff; border: 0; font-size: 18px; cursor: pointer; }
.price { position: absolute; left: 18px; top: 134px; background: #eef0f3; border-radius: 9999px; padding: 4px 10px; font-size: 12px; color: #111827; }
.name { position: absolute; left: 12px; right: 12px; bottom: 12px; background: var(--nav); color: #fff; border-radius: 8px; padding: 8px 12px; font-weight: 600; font-size: 14px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

@media (max-width: 1024px) { .cards { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 640px)  { .cards { grid-template-columns: 1fr; } }

.custom-tabs >>> .el-tabs__nav {
  display: flex;
  justify-content: space-between; /* 平分間距 */
  width: 100%;
}
.custom-tabs >>> .el-tabs__item {
  flex: 1; /* 每個 tab 等寬 */
  text-align: center; /* 文字置中 */
  margin: 0 !important; /* 移除預設 margin */
}

.rating-stars {
  display: flex;
  align-items: center;
}

.rating-stars i {
  font-size: 18px;
  margin-right: 2px;
  position: relative;
  /* ⚠️ 這裡不要設定 color，不然會蓋掉下面的動態顏色 */
}

/* 預設灰色（空星） */
.rating-stars .el-icon-star-on {
  color: #dcdfe6;
}

/* 整顆亮星（金色） */
.rating-stars .el-icon-star-on.is-full {
  color: #f7ba2a;
}

/* 半顆星：左半金右半灰 */
.rating-stars .el-icon-star-on.is-half {
  background: linear-gradient(90deg, #f7ba2a 50%, #dcdfe6 50%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.chat-bottom-bar{
  margin: 6px 0 18px;           /* 上、下留白；下边距就是和卡片的间距 */
}

/* 美化 Chat 按钮：胶囊、渐变、玻璃质感、轻微动效 */
:deep(.chat-bottom-btn){
  padding: 10px 18px;
  border-radius: 9999px;
  border: 1px solid rgba(255,255,255,.35);
  background: linear-gradient(135deg, rgba(39,197,255,.9), rgba(20,184,166,.9));
  color: #fff;
  font-weight: 700;
  letter-spacing: .2px;
  backdrop-filter: blur(8px);
  box-shadow: 0 8px 20px rgba(0,0,0,.25);
  transition: transform .18s ease, box-shadow .18s ease, filter .18s ease;
}

/* 按钮里的图标稍微大一点，与文字留点空 */
:deep(.chat-bottom-btn .el-icon-chat-dot-round){
  font-size: 16px;
  margin-right: 6px;
}

/* 悬停/按下效果 */
:deep(.chat-bottom-btn:hover){
  transform: translateY(-2px);
  box-shadow: 0 14px 28px rgba(32,180,190,.35);
  filter: brightness(1.02);
}
:deep(.chat-bottom-btn:active){
  transform: translateY(0);
  box-shadow: 0 8px 20px rgba(0,0,0,.25);
}

/* 小屏下给按钮多一点下边距，避免贴得太近 */
@media (max-width: 768px){
  .chat-bottom-bar{ margin-bottom: 22px; }
  :deep(.chat-bottom-btn){ padding: 10px 16px; }
}

/* 让按钮与下方卡片拉开距离，手机上也有余量 */
.chat-bottom-bar { margin: 8px 0 18px; }

/* 胶囊按钮主体 */
.chat-cta{
  display:inline-flex; align-items:center; gap:12px;
  padding:8px 14px 8px 10px;
  border:0; border-radius:9999px;
  background: linear-gradient(135deg,#5ac8fa,#34d399 70%);
  color:#0b1740; font-weight:800; letter-spacing:.2px;
  box-shadow: 0 8px 22px rgba(52, 211, 153, .28), 0 2px 8px rgba(90,200,250,.18);
  cursor:pointer; user-select:none; -webkit-tap-highlight-color:transparent;
  transform: translateZ(0); transition: transform .18s ease, box-shadow .18s ease, filter .18s ease;
}

/* 悬浮/按压反馈 */
.chat-cta:hover{ transform: translateY(-2px); box-shadow: 0 12px 28px rgba(52,211,153,.32), 0 4px 12px rgba(90,200,250,.20); filter: brightness(1.02); }
.chat-cta:active{ transform: translateY(0); box-shadow: 0 6px 16px rgba(52,211,153,.24), 0 2px 8px rgba(90,200,250,.16); }

/* 左侧独立圆形图标 */
.chat-cta__icon{
  width:36px; height:36px; border-radius:50%;
  display:inline-flex; align-items:center; justify-content:center;
  background: rgba(255,255,255,.85);
  color:#0ea5e9;            /* 图标颜色 */
  box-shadow: inset 0 0 0 2px rgba(255,255,255,.55), 0 2px 10px rgba(14,165,233,.25);
  transition: transform .18s ease, background .18s ease, color .18s ease;
}
.chat-cta:hover .chat-cta__icon{ transform: scale(1.05); background:#fff; }

/* 标签文字 */
.chat-cta__label{
  font-size:16px; font-weight:800; color:#0b1740;
  text-shadow: 0 1px 0 rgba(255,255,255,.35);
}

/* 高对比度/深色背景时给外缘一点发光 */
.chat-cta{
  outline: none;
  position: relative;
}
.chat-cta::after{
  content:''; position:absolute; inset:-2px; border-radius:inherit; pointer-events:none;
  box-shadow: 0 0 0 2px rgba(255,255,255,.12), 0 0 24px rgba(56,189,248,.25);
}

/* 深色主题下（你的头图区域）显得更通透一些 */
.user-profile-section .chat-cta{
  mix-blend-mode: normal;        /* 如果你喜欢更通透可以换成 screen */
}

/* 响应式：在窄屏上略微缩小 */
@media (max-width: 768px){
  .chat-cta{ padding:10px 14px 10px 12px; gap:10px; }
  .chat-cta__icon{ width:32px; height:32px; }
  .chat-cta__label{ font-size:15px; }
  .chat-bottom-bar{ margin: 6px 0 14px; }
}

</style>