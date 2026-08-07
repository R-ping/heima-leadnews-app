<template>
  <div class="benefit-carousel">
    <div class="section-header">
      <span class="section-title">{{ title }}</span>
    </div>

    <div class="carousel-wrapper">
      <button
        class="arrow-btn left"
        :disabled="currentIndex <= 0"
        @click="slideLeft"
      >
        <i class="el-icon-arrow-left"></i>
      </button>

      <div class="carousel-container" ref="container">
        <div
          class="carousel-track"
          :style="{ transform: `translateX(-${currentIndex * cardWidth}px)` }"
        >
          <div
            v-for="(card, index) in visibleCards"
            :key="card.privId || index"
            class="benefit-card"
            :class="{
              'unlocked': card.unlocked,
              'locked': !card.unlocked
            }"
          >
            <div class="card-icon">
              <img v-if="card.icon" :src="card.icon" :alt="card.title">
              <span v-else class="icon-placeholder">🎁</span>
            </div>
            <div class="card-content">
              <div class="card-title">{{ card.title }}</div>
              <div class="card-desc">{{ getDesc(card) }}</div>
            </div>
            <div v-if="!card.unlocked" class="lock-overlay">
              <i class="el-icon-lock"></i>
            </div>
          </div>
        </div>
      </div>

      <button
        class="arrow-btn right"
        :disabled="currentIndex >= maxIndex"
        @click="slideRight"
      >
        <i class="el-icon-arrow-right"></i>
      </button>
    </div>

    <div class="carousel-tip">
      <span v-if="selectedLevel && selectedLevel <= currentLevel">
        当前等级已解锁 {{ unlockedCount }} 项权益
      </span>
      <span v-else class="next-level-tip">
        还需 {{ needPower }} 掘力值即可解锁本等级
      </span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BenefitCarousel',
  props: {
    title: { type: String, default: '我的权益' },
    cards: { type: Array, default: () => [] },
    currentLevel: { type: Number, default: 1 },
    selectedLevel: { type: Number, default: 1 },
    currentPower: { type: Number, default: 0 },
    cardWidth: { type: Number, default: 240 },
    visibleCount: { type: Number, default: 3 }
  },
  data() {
    return {
      currentIndex: 0
    }
  },
  computed: {
    visibleCards() {
      return this.cards.map(card => ({
        ...card,
        unlocked: card.level <= this.selectedLevel
      }))
    },
    maxIndex() {
      return Math.max(0, this.visibleCards.length - this.visibleCount)
    },
    unlockedCount() {
      return this.visibleCards.filter(c => c.unlocked).length
    },
    needPower() {
      const nextLevelConfig = this.getLevelConfig(this.selectedLevel)
      if (nextLevelConfig) {
        return Math.max(0, nextLevelConfig.minScore - this.currentPower)
      }
      return 0
    }
  },
  methods: {
    slideLeft() {
      if (this.currentIndex > 0) {
        this.currentIndex--
      }
    },
    slideRight() {
      if (this.currentIndex < this.maxIndex) {
        this.currentIndex++
      }
    },
    jumpToLevel(level) {
      const firstIndex = this.cards.findIndex(c => c.level === level)
      if (firstIndex >= 0) {
        this.currentIndex = Math.min(firstIndex, this.maxIndex)
      }
    },
    getDesc(card) {
      if (card.desc && card.desc.length > 0) {
        return card.desc[0]?.desc_content || ''
      }
      return ''
    },
    getLevelConfig(level) {
      const levels = [
        { level: 1, minScore: 0 },
        { level: 2, minScore: 40 },
        { level: 3, minScore: 280 },
        { level: 4, minScore: 1800 },
        { level: 5, minScore: 5500 },
        { level: 6, minScore: 28000 },
        { level: 7, minScore: 75000 },
        { level: 8, minScore: 140000 }
      ]
      return levels.find(l => l.level === level)
    }
  }
}
</script>

<style lang="less" scoped>
@import '../../layout/styles/variables.less';

.benefit-carousel {
  width: 100%;

  .section-header {
    margin-bottom: 16px;

    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: @textPrimary;
    }
  }

  .carousel-wrapper {
    position: relative;
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .arrow-btn {
    width: 36px;
    height: 36px;
    border: none;
    background: #f5f7fa;
    border-radius: 50%;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;

    &:hover:not(:disabled) {
      background: @brandBlue;
      color: #fff;
    }

    &:disabled {
      opacity: 0.4;
      cursor: not-allowed;
    }

    i {
      font-size: 16px;
    }
  }

  .carousel-container {
    flex: 1;
    overflow: hidden;
    padding: 8px 0;
  }

  .carousel-track {
    display: flex;
    gap: 16px;
    transition: transform 0.3s ease;
  }

  .benefit-card {
    width: 240px;
    min-height: 140px;
    padding: 16px;
    border-radius: 12px;
    background: #f5f7fa;
    border: 2px solid transparent;
    transition: all 0.3s;
    flex-shrink: 0;
    position: relative;

    &.unlocked {
      background: linear-gradient(135deg, #fffbe6 0%, #fff7cc 100%);
      border-color: #ffd666;
    }

    &.locked {
      background: #f5f5f5;
      opacity: 0.7;
    }

    .card-icon {
      width: 40px;
      height: 40px;
      margin-bottom: 12px;

      img {
        width: 100%;
        height: 100%;
        object-fit: contain;
      }

      .icon-placeholder {
        font-size: 28px;
      }
    }

    .card-content {
      .card-title {
        font-size: 14px;
        font-weight: 600;
        color: @textPrimary;
        margin-bottom: 8px;
      }

      .card-desc {
        font-size: 12px;
        color: @textMuted;
        line-height: 1.5;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
    }

    .lock-overlay {
      position: absolute;
      top: 12px;
      right: 12px;
      font-size: 16px;
      color: @textMuted;
    }
  }

  .carousel-tip {
    margin-top: 16px;
    padding: 12px 16px;
    background: #f5f7fa;
    border-radius: 8px;
    font-size: 13px;
    color: @textPrimary;

    .next-level-tip {
      color: @brandBlue;
      font-weight: 500;
    }
  }
}

@media screen and (max-width: 768px) {
  .benefit-carousel {
    .benefit-card {
      width: 160px;
      min-height: 120px;

      .card-content {
        .card-title {
          font-size: 13px;
        }

        .card-desc {
          font-size: 11px;
        }
      }
    }
  }
}
</style>
