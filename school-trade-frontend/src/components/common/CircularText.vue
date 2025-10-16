<template>
  <div
      class="circular-text"
      :class="{ spinning: isSpinning }"
      :style="circularStyle"
      @mouseenter="handleHoverStart"
      @mouseleave="handleHoverEnd"
  >
    <span
        v-for="(letter, i) in letters"
        :key="i"
        :style="getLetterStyle(i)"
    >
      {{ letter }}
    </span>
  </div>
</template>

<script>
export default {
  name: 'CircularText',
  props: {
    text: { type: String, required: true },
    spinDuration: { type: Number, default: 20 }, // 秒
    onHover: { type: String, default: 'speedUp' }
  },
  data() {
    return {
      letters: [],
      currentDuration: 20,
      isSpinning: true,
      scale: 1
    }
  },
  computed: {
    circularStyle() {
      return {
        animationDuration: `${this.currentDuration}s`,
        transform: `scale(${this.scale})`
      }
    }
  },
  mounted() {
    this.letters = Array.from(this.text)
    this.currentDuration = this.spinDuration
  },
  methods: {
    handleHoverStart() {
      switch (this.onHover) {
        case 'slowDown':
          this.currentDuration = this.spinDuration * 2
          this.scale = 1
          break
        case 'speedUp':
          this.currentDuration = this.spinDuration / 4
          this.scale = 1
          break
        case 'pause':
          this.isSpinning = false
          this.scale = 1
          return
        case 'goBonkers':
          this.currentDuration = this.spinDuration / 20
          this.scale = 0.8
          break
        default:
          this.currentDuration = this.spinDuration
          this.scale = 1
      }
      this.isSpinning = true
    },
    handleHoverEnd() {
      this.currentDuration = this.spinDuration
      this.scale = 1
      this.isSpinning = true
    },
    getLetterStyle(i) {
      const rotationDeg = (360 / this.letters.length) * i
      const factor = Math.PI / this.letters.length
      const x = factor * i
      const y = factor * i
      return {
        transform: `rotateZ(${rotationDeg}deg) translate3d(${x}px, ${y}px, 0)`,
        WebkitTransform: `rotateZ(${rotationDeg}deg) translate3d(${x}px, ${y}px, 0)`
      }
    }
  }
}
</script>

<style>
.circular-text {
  margin: 0 auto;
  border-radius: 50%;
  width: 200px;
  height: 200px;
  position: relative;
  font-weight: 900;
  color: #fff;
  text-align: center;
  cursor: pointer;
  transform-origin: 50% 50%;
}

.circular-text span {
  position: absolute;
  display: inline-block;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  font-size: 20px;
  transition: all 0.5s cubic-bezier(0, 0, 0, 1);
}

/* 旋转动画 */
.spinning {
  animation-name: rotateCircle;
  animation-timing-function: linear;
  animation-iteration-count: infinite;
}

@keyframes rotateCircle {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
