<!-- TradeDialog.vue（重點版，Vue2） -->
<template>
  <!-- 用 v-if 保證每次打開都是乾淨的實例，避免舊值干擾 -->
  <el-dialog
      :visible.sync="innerVisible"
      append-to-body
      :modal-append-to-body="true"
      :lock-scroll="false"
      title="Edit Trade"
      width="420px"
      @close="handleClose"
  >
    <!-- ★ 一定要綁 :model="form" -->
    <el-form :model="form" label-width="100px">
      <el-form-item label="Time to meet">
        <!-- 如果要用字串存後端，使用 value-format -->
        <el-date-picker
            v-model="form.meetTime"
            type="datetime"
            placeholder="yyyy/MM/dd HH:mm"
            :editable="false"
        />
      </el-form-item>

      <el-form-item label="Place to meet">
        <el-input v-model.trim="form.meetPlace" placeholder="OGGB Main Gate"/>
      </el-form-item>

      <el-form-item v-if="idleTrade === 1" label="Price（$）">
        <el-input v-model="form.priceDollar" placeholder="e.g 12.5"/>
      </el-form-item>

      <el-form-item v-else label="Item to Exchange">
        <el-input
            type="textarea"
            v-model.trim="form.tradeItem"
            :rows="3"
            placeholder="item you want to exchange (e.g. PS5 controller, same value)"
        />
      </el-form-item>
    </el-form>

    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose">cancel</el-button>
      <el-button type="primary" native-type="button" @click="submit">send</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  name: 'TradeDialog',
  props: {
    visible: Boolean,
    itemId: [String, Number],
    sellerId: [String, Number],
    defaultPriceDollar: [String, Number],
    idleTrade: {            // ★ 新增，用來決定是 SELL 或 TRADE
      type: Number,
      default: 1            // 1 = Sell, 2 = Exchange
    }
  },

  data () {
    return {
      innerVisible: false,     // 用內部狀態配合 v-if，避免父層 re-render 影響輸入
      form: {
        meetTime: null,        // 字串（有 value-format）或 Date（二選一）
        meetPlace: '',
        priceDollar: '',        // 文字輸入更穩
        tradeItem: ''
      }
    }
  },
  watch: {
    visible (v) {
      // 打開時重置一次；關閉時不用動
      if (v) {
        this.resetWithDefaults()
        this.innerVisible = true
      }
    }
  },
  methods: {
    resetWithDefaults () {
      this.form.meetTime = null
      this.form.meetPlace = ''
      this.form.priceDollar =
          (this.defaultPriceDollar !== undefined && this.defaultPriceDollar !== null && this.defaultPriceDollar !== '')
              ? String(this.defaultPriceDollar)
              : ''
      this.form.tradeItem = ''
    },

    handleClose () {
      this.innerVisible = false
      // 通知父層關閉
      this.$emit('close')
    },

    submit () {
      var priceNum = parseFloat(this.form.priceDollar);
      if (isNaN(priceNum)) priceNum = 0;

      // 將 Date 轉成字串（你後端常用的 yyyy-MM-dd HH:mm:ss）
      var meet = this.form.meetTime;
      if (meet && Object.prototype.toString.call(meet) === '[object Date]') {
        const pad = n => (n < 10 ? '0' + n : '' + n);
        meet = meet.getFullYear() + '-' +
            pad(meet.getMonth() + 1) + '-' +
            pad(meet.getDate()) + ' ' +
            pad(meet.getHours()) + ':' +
            pad(meet.getMinutes()) + ':' +
            pad(meet.getSeconds());
      } else {
        meet = null;
      }

      // ★ 根據 idleTrade 不同送出不同 payload
      let payload = {
        itemId: this.itemId,
        sellerId: this.sellerId,
        meetTime: meet,
        meetPlace: this.form.meetPlace || ''
      }

      if (this.idleTrade === 1) {
        payload.priceCents = Math.round(priceNum * 100)
      } else {
        payload.tradeItem = this.form.tradeItem || ''
      }

      this.$emit('created', payload)
      this.$emit('close')
    }
  }
}
</script>



<style scoped>
.trade-dialog >>> .el-dialog__body {
  padding-top: 8px;
}
.form { display: grid; grid-template-columns: 1fr; grid-row-gap: 14px; }
.field .label { font-size: 13px; color: #666; margin-bottom: 6px; }
</style>
