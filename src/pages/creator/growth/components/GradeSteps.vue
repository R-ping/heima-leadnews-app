<template>
  <div class="grade-steps">
    <div class="info-section">
      <div class="current-power">
        <span class="power-value">{{ currentPower }}</span>
        <span class="power-label">掘力值</span>
      </div>
      <span class="power-detail-link" @click="$emit('show-detail')">掘力值明细 ›</span>
    </div>

    <div class="progress-tip">
      已解锁 <span class="highlight">{{ unlockedCount }}</span> 项权益，
      还需 <span class="highlight">{{ nextLevelNeed }}</span> 掘力值解锁下一等级
    </div>

    <div class="steps-container">
      <div
        v-for="level in levels"
        :key="level.level"
        class="step-item"
        :class="{
          'current': level.level === currentLevel,
          'unlocked': level.level < currentLevel,
          'locked': level.level > currentLevel
        }"
        :style="{ height: getStepHeight(level.level) + 'px' }"
        @click="handleLevelClick(level)"
      >
        <div class="step-content">
          <span class="level-label">LV.{{ level.level }}</span>
        </div>
        <div v-if="level.level === currentLevel" class="current-badge">
          当前等级
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'GradeSteps',
  props: {
    currentPower: { type: Number, default: 0 },
    currentLevel: { type: Number, default: 1 },
    unlockedCount: { type: Number, default: 0 },
    nextLevelNeed: { type: Number, default: 0 },
    levels: {
      type: Array,
      default: () => [
        { level: 1, minScore: 0, maxScore: 39 },
        { level: 2, minScore: 40, maxScore: 279 },
        { level: 3, minScore: 280, maxScore: 1799 },
        { level: 4, minScore: 1800, maxScore: 5499 },
        { level: 5, minScore: 5500, maxScore: 27999 },
        { level: 6, minScore: 28000, maxScore: 74999 },
        { level: 7, minScore: 75000, maxScore: 139999 },
        { level: 8, minScore: 140000, maxScore: 99999999 }
      ]
    }
  },
  methods: {
    getStepHeight(level) {
      const heights = [60, 80, 100, 120, 140, 160, 180, 200]
      return heights[level - 1] || 60
    },
    handleLevelClick(level) {
      this.$emit('click-level', level)
    }
  }
}
</script>

<style lang="less" scoped>
@import '../../layout/styles/variables.less';

.grade-steps {
  width: 100%;

  .info-section {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 12px;

    .current-power {
      display: flex;
      align-items: baseline;
      gap: 6px;

      .power-value {
        font-size: 36px;
        font-weight: 700;
        color: @brandBlue;
      }

      .power-label {
        font-size: 14px;
        color: @textMuted;
      }
    }

    .power-detail-link {
      font-size: 13px;
      color: @brandBlue;
      cursor: pointer;

      &:hover {
        text-decoration: underline;
      }
    }
  }

  .progress-tip {
    font-size: 13px;
    color: @textMuted;
    margin-bottom: 24px;

    .highlight {
      color: @brandBlue;
      font-weight: 600;
    }
  }

  .steps-container {
    display: flex;
    align-items: flex-end;
    gap: 8px;
    padding: 20px 0;
    border-bottom: 2px solid #e8e8e8;
  }

  .step-item {
    flex: 1;
    background: #f5f7fa;
    border-radius: 8px 8px 0 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-end;
    padding-bottom: 12px;
    cursor: pointer;
    transition: all 0.3s ease;
    position: relative;

    &:hover {
      background: #e8f3ff;
    }

    &.current {
      background: linear-gradient(180deg, @brandBlue 0%, #4a9eff 100%);

      .level-label {
        color: #fff;
      }

      .current-badge {
        position: absolute;
        top: -28px;
        left: 50%;
        transform: translateX(-50%);
        background: #ff6b35;
        color: #fff;
        font-size: 11px;
        padding: 3px 10px;
        border-radius: 10px;
        white-space: nowrap;
      }
    }

    &.unlocked:not(.current) {
      background: linear-gradient(180deg, #69b1ff 0%, #91caff 100%);

      .level-label {
        color: #fff;
      }
    }

    &.locked {
      background: #f0f0f0;
      opacity: 0.6;

      .level-label {
        color: @textMuted;
      }
    }

    .step-content {
      .level-label {
        font-size: 14px;
        font-weight: 600;
        color: @textPrimary;
      }
    }
  }
}

@media screen and (max-width: 768px) {
  .grade-steps {
    .info-section {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }

    .steps-container {
      gap: 4px;
    }

    .step-item {
      .step-content {
        .level-label {
          font-size: 12px;
        }
      }
    }
  }
}
</style>
