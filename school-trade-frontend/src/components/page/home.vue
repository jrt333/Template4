<template>
  <div class="index-page">
    <app-head />
    <!-- Search bar -->
    <section class="search-row">
      <div class="searchbar">
        <input
            v-model.trim="keyword"
            placeholder="Search  Computer Sci"
            @keyup.enter="onSearch"
        />
        <button class="searchbtn" @click="onSearch" aria-label="search">🔍</button>
      </div>
    </section>

    <div class="content-container">
      <!-- 活动横幅卡片 -->
      <section class="activity-banner container">
        <div class="banner-grid">
          <div
              v-for="(banner, idx) in banners"
              :key="idx"
              class="banner-card"
          >
            <img :src="banner.img" :alt="banner.title" />
            <div class="banner-title">{{ banner.title }}</div>
          </div>
        </div>
      </section>
      <!-- Category tiles -->
      <section class="cats">
        <button
            v-for="c in cats"
            :key="c.key"
            class="cat"
            @click="goCategory(c)"
        >
            <span class="cat-icon">
              <img :src="c.img" :alt="c.label" />
            </span>
          <span class="cat-text">{{ c.label }}</span>
        </button>
      </section>

      <!-- Section header + Sort -->
      <div class="section-header">
        <h3 class="section-title">You May Like</h3>
        <div class="sort-row" role="group" aria-label="Sort by">
          <span class="sort-label">Sort by:</span>

          <!-- 两个 Price（升序/降序） -->
          <button
              type="button"
              class="sort-chip"
              :class="{ active: sortKey === 'priceAsc' }"
              @click="setSort('priceAsc')"
          >Price: Low → High</button>

          <button
              type="button"
              class="sort-chip"
              :class="{ active: sortKey === 'priceDesc' }"
              @click="setSort('priceDesc')"
          >Price: High → Low</button>

          <!-- 时间：Newest / Oldest -->
          <button
              type="button"
              class="sort-chip"
              :class="{ active: sortKey === 'timeDesc' }"
              @click="setSort('timeDesc')"
          >Newest</button>

          <button
              type="button"
              class="sort-chip"
              :class="{ active: sortKey === 'timeAsc' }"
              @click="setSort('timeAsc')"
          >Oldest</button>

          <!-- 可选：回到默认 -->
          <button
              type="button"
              class="sort-chip"
              :class="{ active: sortKey === 'default' }"
              @click="setSort('default')"
          >Default</button>
        </div>

      </div>

      <!-- Recommended cards -->
      <section class="cards">
        <article
            v-for="(idle, index) in idleList"
            :key="idle.id || index"
            class="card"
            @click="toDetails(idle)"
        >
          <el-image class="cover" :src="idle.imgUrl" fit="cover">
            <template #error>
              <div class="image-slot"><i class="el-icon-picture-outline"></i>无图</div>
            </template>
          </el-image>

          <button
              class="heart"
              :class="{ 'is-liked': isLiked(idle) }"
              @click.stop="toggleWishlist(idle)"
              :aria-pressed="isLiked(idle)"
              aria-label="wishlist"
          >
            {{ isLiked(idle) ? '♥' : '♡' }}
          </button>

          <div class="price">NZ$ {{ formatPrice(idle.idlePrice) }}</div>
          <div class="name">{{ idle.idleName || 'Item name' }}</div>
        </article>
      </section>

      <!-- Pagination -->
      <div class="pager">
        <el-pagination
            background
            :page-size="40"
            layout="prev, pager, next, jumper"
            :current-page.sync="currentPage"
            :total="totalItem"
            :title="isAuthed() ? '' : '請先登入'"
            @current-change="handleCurrentChange"
        />
      </div>
    </div>
    <app-foot />
  </div>
</template>

<script>
import AppHead from '../common/AppHeader.vue'
import AppBody from '../common/AppPageBody.vue'
import AppFoot from '../common/AppFoot.vue'
import IUniv    from '../../assets/book.png'
import ITech    from '../../assets/computer.png'
import IClothes from '../../assets/hood.png'
import ISports  from '../../assets/sports.png'
import ILiving  from '../../assets/sofa.png'
import IOther   from '../../assets/application.png'

export default {
  name: 'index',
  components: { AppHead, AppFoot },
  data () {
    return {
      sortKey: 'default',
      labelName: '0',
      idleList: [],
      currentPage: 1,
      totalItem: 0,
      keyword: '',
      likedMap: {},
      favIdByIdle: {},
      cats: [
        { key: 'univ',    label: 'University', value: '1', img: IUniv },
        { key: 'tech',    label: 'Tech',       value: '2', img: ITech },
        { key: 'clothes', label: 'Clothes',    value: '3', img: IClothes },
        { key: 'sports',  label: 'Sports',     value: '4', img: ISports },
        { key: 'living',  label: 'Living',     value: '5', img: ILiving },
        { key: 'other',   label: 'Other',      value: '6', img: IOther}
      ],
      banners: [
        { img: require('@/assets/Specialoffer.png'), link: '/activity/1' },
        { img: require('@/assets/summersale.jpg'), link: '/activity/2' },
        { img: require('@/assets/Backtoschool.jpg'), link: '/activity/3' }
      ]
    }
  },
  created () {
    this.syncFromRoute()
    this.findIdleTiem(this.currentPage)
  },
  watch: {
    '$route.query' () {
      this.syncFromRoute()
      this.findIdleTiem(this.currentPage)
    }
  },
  async mounted() {
    await this.initFavorites();
  },
  methods: {
    onSearch () {
      if (!this.keyword) return
      this.$router.push({ path: '/search', query: { searchValue: this.keyword } })
    },
    setSort(key) {
      if (this.sortKey === key) return;
      this.sortKey = key;
      this.handleSortChange(); // 复用你已有的逻辑：更新路由参数并触发排序/请求
    },

    goCategory (c) {
      this.labelName = String(c.value)
      this.handleClick()
    },
    formatPrice (p) {
      const n = Number(p || 0)
      return n.toFixed(2)
    },
    goBanner(banner) {
      if (banner.link) this.$router.push(banner.link)
    },
    isAuthed() {
      try {
        const raw = localStorage.getItem('user');
        if (!raw) return false;
        const obj = JSON.parse(raw);
        return !!obj;
      } catch (e) {
        return false;
      }
    },

    // ===== 資料讀取，沿用你原本的 API 命名 =====
    syncFromRoute () {
      this.currentPage = Number(this.$route.query.page || 1);
      this.labelName = (this.$route.query.labelName !== undefined) ? String(this.$route.query.labelName) : '0';
      this.sortKey = this.$route.query.sort || 'default';
      this.currentPage = Number(this.$route.query.page || 1)
      this.labelName = (this.$route.query.labelName !== undefined)
          ? String(this.$route.query.labelName)
          : '0'
    },
    findIdleTiem(page) {
      const loading = this.$loading({
        lock: true, text: 'Loading', spinner: 'el-icon-loading', background: 'rgba(0,0,0,0)'
      });

      const labelNum = Number(this.labelName || 0);
      const isAll = !(isFinite(labelNum) && labelNum > 0);
      const api = isAll ? this.$api.findIdleTiem : this.$api.findIdleTiemByLable;

      const params = {
        page,
        nums: 40,
        idleLabel: isAll ? undefined : labelNum,
        sort: this.sortKey // 传给后端（后端若不支持则前端会再排序）
      };

      api(params).then(res => {
        const data = (res && res.data) ? res.data : res;
        // 兼容后端不同字段命名
        const rawList = (data && Array.isArray(data.list)) ? data.list
            : (data && Array.isArray(data.data)) ? data.data
                : (Array.isArray(data) ? data : []);
        const list = rawList || [];

        this.idleList = list.map(it => {
          let pictures = [];
          try { pictures = JSON.parse(it.pictureList || '[]') } catch(e){}
          const timeStr = it.releaseTime ? (it.releaseTime.substring(0,10) + ' ' + it.releaseTime.substring(11,19)) : '';
          return {
            ...it,
            timeStr,
            imgUrl: pictures[0] || '',
            user: it.user || {}
          }
        });

        this.totalItem = Number((data && data.count) || 0);

        // 如果后端没有做排序，前端再根据 sortKey 做一次本地排序（作为后备）
        if (this.sortKey && this.sortKey !== 'default') {
          this.applySort();
        }
      }).catch(e => {
        console.error(e);
        this.idleList = [];
        this.totalItem = 0;
      }).finally(() => loading.close());
    },
    handleSortChange() {
      // 排序改变，回到第一页
      this.currentPage = 1;
      this.$router.replace({ query: { page: 1, labelName: this.labelName, sort: this.sortKey } });
      this.findIdleTiem(this.currentPage);
    },

    handleClick () {
      const q = { page: 1 }
      if (Number(this.labelName) > 0) q.labelName = this.labelName
      this.$router.replace({ query: q })
    },
    handleCurrentChange (val) {
      this.currentPage = val;
      const q = { page: val };
      if (Number(this.labelName) > 0) q.labelName = this.labelName;
      if (this.sortKey && this.sortKey !== 'default') q.sort = this.sortKey;
      this.$router.replace({ query: q });
      this.findIdleTiem(val);
    },
    toDetails (idle) {
      this.$router.push({ path: '/details', query: { id: idle.id } })
    },

    async initFavorites() {
      try {
        const res = await this.$api.getMyFavorite({});
        // 规范化到数组
        let list = [];

        // 常见后端返回结构兼容（优先级从具体到宽泛）
        if (!res) {
          list = [];
        } else if (Array.isArray(res)) {
          list = res;
        } else if (Array.isArray(res.data)) {
          list = res.data;
        } else if (res.data && Array.isArray(res.data.data)) {
          list = res.data.data;
        } else if (res.data && Array.isArray(res.data.list)) {
          list = res.data.list;
        } else if (Array.isArray(res.data && res.data.items)) {
          list = res.data.items;
        } else {
          // 最后防御：如果 res.data 是对象但其属性里面包含数组（尝试自动抽取）
          if (res && typeof res === 'object' && !Array.isArray(res)) {
            // no-op: 保持 list = []
          }
        }

        // 如果需要调试实际返回结构，可以临时打开下面这行：
        // console.debug('initFavorites response normalized list:', list, 'rawRes:', res);

        const liked = {};
        const mapFav = {};
        (list || []).forEach(function (it) {
          // 兼容不同命名：favorite record 的主键可能叫 id / favoriteId / favId
          const favoriteId =
              (it && typeof it.id !== 'undefined') ? it.id :
                  (it && typeof it.favoriteId !== 'undefined') ? it.favoriteId :
                      (it && typeof it.favId !== 'undefined') ? it.favId : null;

          // 这条收藏对应的商品 id 可能叫 idleId 或 id
          const idleId = it ? (it.idleId != null ? it.idleId : (it.itemId != null ? it.itemId : it.id)) : null;

          if (idleId != null) {
            liked[idleId] = true;
            if (favoriteId != null) mapFav[idleId] = favoriteId;
          }
        });

        this.likedMap = liked;
        this.favIdByIdle = mapFav;
      } catch (e) {
        // 未登入也可能進來，這裡不用彈錯
        console.error(e);
      }
    },

    isLiked(idle) {
      var id = (idle && (idle.id != null ? idle.id : idle.idleId));
      return !!(this.likedMap && this.likedMap[id]);
    },

    _getCode(res) {
      if (!res) return 0;

      // 1) 已解包：{status_code, code, message/msg, data...}
      if (typeof res.status_code !== 'undefined') return res.status_code;
      if (typeof res.code !== 'undefined') return res.code;

      // 2) Axios 樣式：{ data: {...}, status }
      if (res.data) {
        if (typeof res.data.status_code !== 'undefined') return res.data.status_code;
        if (typeof res.data.code !== 'undefined') return res.data.code;
      }
      if (typeof res.status !== 'undefined') return res.status;

      return 0;
    },
    _getMsg(res, fallback) {
      if (!res) return fallback || '';
      if (res.message) return res.message;
      if (res.msg) return res.msg;
      if (res.data && (res.data.message || res.data.msg)) return res.data.message || res.data.msg;
      return fallback || '';
    },

    async toggleWishlist(idle) {
      // 0) 取 id
      var idleId = (idle && idle.id != null) ? idle.id : (idle && idle.idleId != null ? idle.idleId : null);
      if (idleId == null) {
        this.$message && this.$message.warning && this.$message.warning('Item without an ID');
        return;
      }

      // 1) 登入檢查
      if (!this.isAuthed || !this.isAuthed()) {
        if (this.$message && this.$message.warning) this.$message.warning('Please Login'); else alert('Please Login');
        var back = (this.$route && this.$route.fullPath) ? this.$route.fullPath : '/';
        this.$router && this.$router.push && this.$router.push({ path: '/login', query: { redirect: back }});
        return;
      }

      // 2) 狀態/回滾快照
      var wasLiked = !!(this.likedMap && this.likedMap[idleId]);
      var prevLiked  = this.likedMap    ? Object.assign({}, this.likedMap)    : {};
      var prevFavMap = this.favIdByIdle ? Object.assign({}, this.favIdByIdle) : {};

      try {
        if (wasLiked) {
          // —— 取消喜歡 ——
          var favoriteId = this.favIdByIdle ? this.favIdByIdle[idleId] : null;
          if (favoriteId == null && typeof this.initFavorites === 'function') {
            await this.initFavorites();
            favoriteId = this.favIdByIdle ? this.favIdByIdle[idleId] : null;
          }
          if (favoriteId == null) throw new Error('No corresponding Favorite record found');

          // 樂觀更新
          var next = Object.assign({}, this.likedMap || {}); delete next[idleId]; this.likedMap = next;
          var nextFav = Object.assign({}, this.favIdByIdle || {}); delete nextFav[idleId]; this.favIdByIdle = nextFav;

          if (!this.$api || typeof this.$api.deleteFavorite !== 'function') throw new Error('Interface not registered：deleteFavorite');
          var res = await this.$api.deleteFavorite({ id: favoriteId });

          var code = this._getCode(res);
          if (!(code === 1 || code === 200 || code === 204)) {
            throw new Error(this._getMsg(res, 'Remove Fail'));
          }
          this.$message && this.$message.success && this.$message.success('Remove From Your Favorites');

        } else {
          // —— 加入喜歡 ——
          var liked2 = Object.assign({}, this.likedMap || {}); liked2[idleId] = true; this.likedMap = liked2;

          if (!this.$api || typeof this.$api.addFavorite !== 'function') throw new Error('Interface not registered：addFavorite');
          var res2 = await this.$api.addFavorite({ idleId: idleId });

          var code2 = this._getCode(res2);
          // 成功容忍 1 / 200 / 201
          if (!(code2 === 1 || code2 === 200 || code2 === 201)) {
            throw new Error(this._getMsg(res2, 'Add Fail'));
          }

          if (typeof this.initFavorites === 'function') await this.initFavorites();
          this.$message && this.$message.success && this.$message.success('Add to Your Favorites');
        }
      } catch (e) {
        // 回滾
        this.likedMap = prevLiked;
        this.favIdByIdle = prevFavMap;

        var status = e && e.response ? e.response.status : 0;
        var msg = this._getMsg(e && e.response && e.response.data ? e.response.data : e, e && e.message);

        if (status === 401 || /未登錄|未登录|請先登入|請先登录|not\s*login/i.test(msg || '')) {
          var back2 = (this.$route && this.$route.fullPath) ? this.$route.fullPath : '/';
          this.$router && this.$router.push && this.$router.push({ path: '/login', query: { redirect: back2 }});
          return;
        }

        this.$message && this.$message.error ? this.$message.error(msg || 'please try again later') : alert(msg || 'please try again later');
        console.error(e);
      }
    },
    // 排序
    handleSort() {
      if (!this.idleList || this.idleList.length === 0) return;

      switch(this.sortKey) {
        case 'priceAsc':
          this.idleList.sort((a, b) => (Number(a.idlePrice) || 0) - (Number(b.idlePrice) || 0));
          break;
        case 'priceDesc':
          this.idleList.sort((a, b) => (Number(b.idlePrice) || 0) - (Number(a.idlePrice) || 0));
          break;
        case 'timeAsc':
          this.idleList.sort((a, b) => new Date(a.releaseTime) - new Date(b.releaseTime));
          break;
        case 'timeDesc':
          this.idleList.sort((a, b) => new Date(b.releaseTime) - new Date(a.releaseTime));
          break;
        default:
          // 默认顺序可以重新拉接口，也可以不变
          this.findIdleTiem(this.currentPage);
          break;
      }
    },
    // 本地排序（作为后备），不会再次拉接口，避免递归
    applySort() {
      if (!this.idleList || !Array.isArray(this.idleList) || this.idleList.length === 0) return;

      switch(this.sortKey) {
        case 'priceAsc':
          this.idleList.sort((a, b) => (Number(a.idlePrice) || 0) - (Number(b.idlePrice) || 0));
          break;
        case 'priceDesc':
          this.idleList.sort((a, b) => (Number(b.idlePrice) || 0) - (Number(a.idlePrice) || 0));
          break;
        case 'timeAsc':
          this.idleList.sort((a, b) => new Date(a.releaseTime) - new Date(b.releaseTime));
          break;
        case 'timeDesc':
          this.idleList.sort((a, b) => new Date(b.releaseTime) - new Date(a.releaseTime));
          break;
        default:
          // 不做任何操作
          break;
      }
    }

  }
}

</script>

<style scoped>
:root {
  --nav:#0c1240;
  --accent: #0c1240;
  --line: #27a5ff;
  --muted: #e5e7eb;
}

/* Search */
.search-row {
  display: flex;
  justify-content: center;
  margin-top: 0px;
  padding-top: 20px;
}
.searchbar {
  display: flex; width: min(1000px, 92%);
  background: #e6f5ff; border: 1px solid #d9dee7; border-radius: 9999px;
  box-shadow: 0 2px 8px rgba(0,0,0,.06); overflow: hidden;
}
.searchbar input { flex: 1; padding: 20px 16px; border: 0; outline: none; font-size: 20px; }
.searchbtn { width: 56px; border: 0; background: #fff; cursor: pointer; font-size: 18px; }
.searchbtn:hover { background: #e6f5ff; }

/* 全局内容容器 */
.content-container {
  max-width: 1500px;
  margin: 0 auto;
  padding: 0 150px;
}

/* 活动横幅 */
.activity-banner {
  margin: 32px 0 24px;
}
.banner-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.banner-card {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.banner-card img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}
.banner-title {
  position: absolute;
  bottom: 12px;
  left: 12px;
  color: #fff;
  font-size: 20px;
  font-weight: 600;
  text-shadow: 0 2px 6px rgba(0,0,0,0.4);
}

/* 分类 */
.cats {
  display: grid;
  grid-template-columns: repeat(6, minmax(100px, 1fr));
  gap: 16px;
  padding: 24px 0;
}
.cat {
  background: var(--nav); color: #fff; border-radius: 16px; height: 92px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  box-shadow: 0 4px 10px rgba(0,0,0,.06); border: 2px solid rgba(255,255,255,.06);
}
.cat:hover { transform: translateY(-2px); transition: transform .15s ease; }
.cat-icon { display: grid; place-items: center; margin-bottom: 6px; }
.cat-icon img { width: 34px; height: 34px; object-fit: contain; filter: invert(1); }
.cat-text { font-size: 14px; }

/* Section title */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between; /* 左右分布 */
  margin: 16px 0; /* 上下间距 */
}

.section-title {
  font-weight: 700;
  font-size: 20px;
}

/* 商品卡片 */
.cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  padding: 8px 0 24px;
}
.card {
  position: relative;
  background: #fff;
  border-radius: 14px;
  border: 6px solid #d6dee5;
  padding: 10px;
  height: 220px;
  cursor: pointer;
}
.cover { width: 100%; height: 120px; border-radius: 8px; background: #f9fafb; }
.heart {
  position: absolute;
  top: 8px;
  left: 8px;
  width: 32px;
  height: 32px;
  border: 0;
  border-radius: 50%;
  background: rgba(255,255,255,0.9);
  font-size: 18px;
  line-height: 32px;
  cursor: pointer;
  transition: transform .12s ease;
}
.heart:hover { transform: scale(1.06); }
.heart.is-liked { color: #e0245e; background: #fff; }
.price {
  position: absolute;
  left: 18px;
  top: 134px;
  background: #eef0f3;
  border-radius: 9999px;
  padding: 4px 10px;
  font-size: 12px;
  color: #111827;
}
.name {
  position: absolute;
  left: 12px;
  right: 12px;
  bottom: 12px;
  background: var(--nav);
  color: #fff;
  border-radius: 8px;
  padding: 8px 12px;
  font-weight: 600;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 分页 */
.pager { display: flex; justify-content: center; padding: 8px 0 32px; }

/* 排序行 */
/* 排序行（展开按钮，胶囊风格） */
.sort-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px 14px;
}

.sort-label {
  font-size: 16px;
  color: #0c1240;
  font-weight: 600;
  margin-right: 2px;
}

/* 胶囊按钮：白底 + 蓝边 + 圆角 */
.sort-chip {
  appearance: none;
  background: #fff;                   /* 白底 */
  color: #0c1240;                      /* 蓝字（接近TradeMe风格） */
  border: 2px solid #0c1240;           /* 蓝边 */
  padding: 10px 16px;                  /* 稍大手感 */
  border-radius: 9999px;               /* 圆弧 */
  cursor: pointer;
  font-size: 16px;                     /* 字体更大 */
  font-weight: 600;                    /* 粗一点 */
  line-height: 1;
  user-select: none;
  transition:
      background-color .15s ease,
      color .15s ease,
      border-color .15s ease,
      box-shadow .15s ease,
      transform .05s ease;
}

.sort-chip:hover {
  background: #f0f7ff;                 /* 悬停轻微浅蓝 */
  box-shadow: 0 2px 6px rgba(10,102,194,0.12);
}

.sort-chip:active {
  transform: scale(0.98);
}

/* 选中态：蓝底白字 */
.sort-chip.active {
  background: #0c1240;
  color: #fff;
  border-color: #0c1240;
  box-shadow: 0 3px 10px rgba(10,102,194,0.25);
}

/* 键盘可访问性 */
.sort-chip:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px rgba(10,102,194,0.35);
}

/* 如果保留了旧的 <select> 作为兜底，这里继续隐藏它 */
.sort-row select {
  display: none;
}
.section-header .sort-row {
  gap: 10px 12px;              /* 间距稍微收紧 */
}

.section-header .sort-label {
  font-size: 18px;             /* 放大标签 */
  font-weight: 700;
  color: #0c1240;
  margin-right: 8px;
}

/* 胶囊按钮：更薄、更短、更精致 */
.section-header .sort-chip {
  padding: 7px 12px;           /* 从 10x16 缩到 7x12：更短更细 */
  font-size: 14px;             /* 字体略小一点，整体更秀气 */
  border-width: 1px;           /* 边框更细 */
  border-radius: 9999px;
  background: #fff;
  color: #0c1240;
  border-color: #0c1240;
  box-shadow: none;            /* 默认去掉阴影，显得干净 */
}

.section-header .sort-chip:hover {
  background: #f2f7ff;         /* 轻微悬停反馈 */
}

/* 选中态：保持高亮但更克制 */
.section-header .sort-chip.active {
  background: #0c1240;
  color: #fff;
  border-color: #0c1240;
  box-shadow: 0 2px 6px rgba(10,102,194,0.18);
}

/* 小屏再紧一点（可选） */
@media (max-width: 640px) {
  .section-header .sort-chip {
    padding: 6px 10px;
    font-size: 13.5px;
  }
  .section-header .sort-label {
    font-size: 16px;
  }
}
/* 让排序胶囊完全跟随 --line 颜色 */
.section-header .sort-chip {
  color: #0c1240;
  border-color: #0c1240;
  background: #fff;
}

.section-header .sort-chip:hover {
  /* 浅一点的悬停底色（用 --line 的 10% 透明度） */
  background: rgba(39,165,255,.10); /* 若你把 --line 改了颜色，这里也一起换数值 */
  /* 更稳妥：如果你知道 --line 的十六进制，比如 #3AA8FF，就写 rgba(58,168,255,.10) */
}

.section-header .sort-chip.active {
  background: #0c1240;
  border-color: #0c1240;
  color: #fff;
  box-shadow: 0 2px 8px rgba(39,165,255,.25);
}
/* ========== 自适应布局 for Section Header + Sort ========== */
/* 宽屏：标题在左、排序在右 */
.section-header {
  display: grid;
  grid-template-columns: 1fr auto;   /* 左:标题  右:排序 */
  align-items: center;
  gap: 12px;
}

/* 排序行：允许换行，间距适中 */
.sort-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px 12px;
  margin-top: 4px;
}

/* 胶囊按钮（保持与站点浅蓝一致） */
.sort-chip {
  padding: 7px 12px;                 /* 稍微短一点 */
  font-size: 14px;
  border: 1px solid #0c1240;
  color: #0c1240;
  background: #fff;
  border-radius: 9999px;
  cursor: pointer;
  white-space: nowrap;               /* 不在按钮内部换行 */
}
.sort-chip:hover { background: rgba(39,165,255,.10); }
.sort-chip.active {
  background: #0c1240;
  border-color: #0c1240;
  color: #fff;
  box-shadow: 0 2px 8px rgba(39,165,255,.25);
}

/* 中屏：排序整行显示到标题下面，减少拥挤 */
@media (max-width: 1024px) {
  .section-header {
    grid-template-columns: 1fr;      /* 只有一列：标题在上，排序在下 */
  }
  .sort-row {
    justify-content: flex-start;     /* 贴左排 */
    gap: 8px 10px;
  }
}

/* 小屏：按钮更小一点；排序横向滑动，避免挤成多行 */
@media (max-width: 640px) {
  .sort-chip { padding: 6px 10px; font-size: 13.5px; }
  .sort-row {
    flex-wrap: nowrap;               /* 不换行 */
    overflow-x: auto;                /* 可横向滑动 */
    -webkit-overflow-scrolling: touch;
    padding-bottom: 6px;             /* 留点手指滑动空间 */
  }
  .sort-chip { flex: 0 0 auto; }     /* 每个按钮占自身宽度 */
  .sort-label { display: none; }     /* 节省横向空间：隐藏“Sort by:”文字 */
}

/* 可选：容器在窄屏时减小两侧留白，避免整体拥挤 */
@media (max-width: 1200px) {
  .content-container { padding: 0 60px; }
}
@media (max-width: 768px) {
  .content-container { padding: 0 20px; }
}

/* 响应式 */
@media (max-width: 1024px) {
  .cats { grid-template-columns: repeat(3, 1fr); }
  .cards { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .cats { grid-template-columns: repeat(2, 1fr); }
  .cards { grid-template-columns: 1fr; }
}
</style>


