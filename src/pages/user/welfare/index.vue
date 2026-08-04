<template>
    <div class="welfare-page">
        <div class="art-top" v-if="!isDesktop"><HomeBar/></div>

        <div class="welfare-content">
            <!-- User Info Bar -->
            <div class="user-info-bar">
                <div class="user-info-section">
                    <span class="user-nickname">{{ userInfo.name || '用户' }}</span>
                    <span class="user-level-badge">{{ userInfo.level || 'JY.1' }}</span>
                </div>
                <span class="info-separator">|</span>
                <div class="user-info-section ore-section">
                    <span class="info-value ore-value">{{ userInfo.ore || 0 }}</span>
                    <span class="ore-icon">&#xf06d;</span>
                    <span class="ore-question">&#xf059;</span>
                </div>
            </div>

            <!-- Sub Tabs -->
            <div class="sub-tabs">
                <div
                    v-for="tab in subTabs"
                    :key="tab.key"
                    class="tab-item"
                    :class="{ active: activeTab === tab.key }"
                    @click="switchTab(tab)"
                >
                    {{ tab.label }}
                </div>
            </div>

            <!-- Exchange Main Area -->
            <div class="exchange-main">
                <!-- Category Tabs -->
                <div class="category-tabs">
                    <div
                        v-for="cat in categories"
                        :key="cat.value"
                        class="category-item"
                        :class="{ active: activeCategory === cat.value }"
                        @click="switchCategory(cat.value)"
                    >
                        {{ cat.label }}
                    </div>
                </div>

                <!-- Goods Grid -->
                <div class="goods-grid" v-if="goodsList.length > 0">
                    <div
                        v-for="goods in goodsList"
                        :key="goods.id"
                        class="goods-card"
                        @click="openExchangeDialog(goods)"
                    >
                        <div class="goods-image">
                            <img :src="goods.image" :alt="goods.name" v-if="goods.image" />
                            <div class="goods-image-placeholder" v-else>&#xf03e;</div>
                        </div>
                        <div class="goods-info">
                            <div class="goods-name">{{ goods.name }}</div>
                            <div class="goods-meta">
                                <span class="goods-ore">&#xf06d; {{ goods.ore }}</span>
                                <span class="goods-stock" :class="{ 'stock-low': goods.stock > 0 && goods.stock <= 10, 'stock-empty': goods.stock <= 0 }">
                                    {{ goods.stock > 0 ? '库存 ' + goods.stock : '已兑完' }}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="empty-state" v-else-if="!loading">
                    <div class="empty-icon">&#xf11a;</div>
                    <div class="empty-text">暂无可用商品</div>
                </div>

                <!-- Loading -->
                <div class="loading-state" v-if="loading">
                    <div class="loading-spinner"></div>
                    <div class="loading-text">加载中...</div>
                </div>

                <!-- Pagination -->
                <div class="pagination" v-if="totalPages > 1">
                    <div
                        class="page-btn"
                        :class="{ disabled: currentPage <= 1 }"
                        @click="goPage(currentPage - 1)"
                    >
                        &#xf104; 上一页
                    </div>
                    <div class="page-info">{{ currentPage }} / {{ totalPages }}</div>
                    <div
                        class="page-btn"
                        :class="{ disabled: currentPage >= totalPages }"
                        @click="goPage(currentPage + 1)"
                    >
                        下一页 &#xf105;
                    </div>
                </div>
            </div>

            <!-- Exchange Records -->
            <div class="records-section">
                <div class="records-header">
                    <span class="records-title">我的兑换记录</span>
                    <span class="records-count" v-if="exchangeRecords.length > 0">共 {{ exchangeRecords.length }} 条</span>
                </div>
                <div class="records-list" v-if="exchangeRecords.length > 0">
                    <div
                        v-for="record in exchangeRecords"
                        :key="record.id"
                        class="record-item"
                    >
                        <div class="record-goods">
                            <div class="record-goods-image">
                                <img :src="record.goodsImage" :alt="record.goodsName" v-if="record.goodsImage" />
                                <div class="record-goods-placeholder" v-else>&#xf03e;</div>
                            </div>
                            <div class="record-goods-info">
                                <div class="record-goods-name">{{ record.goodsName }}</div>
                                <div class="record-time">{{ record.createTime }}</div>
                            </div>
                        </div>
                        <div class="record-ore">&#xf06d; {{ record.ore }}</div>
                        <div class="record-status" :class="'status-' + record.status">
                            {{ record.status === 'SUCCESS' ? '兑换成功' : record.status === 'PENDING' ? '处理中' : record.status === 'FAILED' ? '兑换失败' : record.status }}
                        </div>
                    </div>
                </div>
                <div class="empty-state" v-else-if="!loadingRecords">
                    <div class="empty-icon">&#xf0e0;</div>
                    <div class="empty-text">暂无兑换记录</div>
                </div>
                <div class="loading-state" v-if="loadingRecords">
                    <div class="loading-spinner"></div>
                    <div class="loading-text">加载中...</div>
                </div>
            </div>

            <!-- Exchange Confirmation Dialog -->
            <div class="dialog-overlay" v-if="showDialog" @click.self="closeDialog">
                <div class="dialog-box">
                    <div class="dialog-header">
                        <span class="dialog-title">确认兑换</span>
                        <span class="dialog-close" @click="closeDialog">&times;</span>
                    </div>
                    <div class="dialog-body" v-if="selectedGoods">
                        <div class="dialog-goods-image">
                            <img :src="selectedGoods.image" :alt="selectedGoods.name" v-if="selectedGoods.image" />
                            <div class="dialog-goods-placeholder" v-else>&#xf03e;</div>
                        </div>
                        <div class="dialog-goods-name">{{ selectedGoods.name }}</div>
                        <div class="dialog-goods-desc" v-if="selectedGoods.description">{{ selectedGoods.description }}</div>
                        <div class="dialog-ore-cost">
                            需要消耗 <span class="ore-highlight">&#xf06d; {{ selectedGoods.ore }}</span> 矿石
                        </div>
                        <div class="dialog-balance">
                            当前矿石：<span class="ore-highlight">&#xf06d; {{ userInfo.ore || 0 }}</span>
                        </div>
                        <div class="dialog-warning" v-if="(userInfo.ore || 0) < selectedGoods.ore">
                            矿石不足，无法兑换
                        </div>
                    </div>
                    <div class="dialog-footer">
                        <div class="dialog-btn dialog-btn-cancel" @click="closeDialog">取消</div>
                        <div
                            class="dialog-btn dialog-btn-confirm"
                            :class="{ disabled: (userInfo.ore || 0) < (selectedGoods ? selectedGoods.ore : 0) || exchanging }"
                            @click="confirmExchange"
                        >
                            {{ exchanging ? '兑换中...' : '确认兑换' }}
                        </div>
                    </div>
                </div>
            </div>

            <!-- Footer -->
            <div class="welfare-footer">
                用户协议 · 法律声明 &copy;{{ new Date().getFullYear() }} 稀土掘金
            </div>
        </div>
    </div>
</template>

<script>
import HomeBar from '@/components/bars/home_bar'
import Utils from '@/utils/env'
import store from '@/stores/store'
import { getGoodsList, getGoodsDetail, doExchange, getMyExchanges } from '@/apis/welfare'
import { toast } from '@/utils/toast'

export default {
    name: 'UserWelfare',
    components: { HomeBar },
    data() {
        return {
            activeTab: 'exchange',
            subTabs: [
                { key: 'checkin', label: '每日签到' },
                { key: 'growth', label: '成长等级' },
                { key: 'lottery', label: '幸运抽奖' },
                { key: 'exchange', label: '福利兑换' },
                { key: 'harvest', label: '我的收获' }
            ],
            categories: [
                { label: '全部', value: '' },
                { label: '实物', value: 'PHYSICAL' },
                { label: '虚拟', value: 'VIRTUAL' },
                { label: '公益', value: 'COMMONWEAL' }
            ],
            activeCategory: '',
            userInfo: {
                name: '',
                level: '',
                ore: 0
            },
            goodsList: [],
            currentPage: 1,
            totalPages: 1,
            pageSize: 12,
            loading: false,
            exchangeRecords: [],
            loadingRecords: false,
            showDialog: false,
            selectedGoods: null,
            exchanging: false
        }
    },
    computed: {
        isDesktop() {
            return Utils.isDesktop()
        }
    },
    mounted() {
        this.loadGoods()
        this.loadExchanges()
        this.loadUserInfo()
    },
    methods: {
        loadUserInfo() {
            const userInfo = store.state.userInfo || {}
            this.userInfo = {
                name: userInfo.nickName || '用户',
                level: 'JY.1',
                ore: 0
            }
        },
        async loadGoods() {
            this.loading = true
            try {
                const res = await getGoodsList({
                    type: this.activeCategory,
                    page: this.currentPage,
                    size: this.pageSize
                })
                if (res && res.code === 200 && res.data) {
                    const data = res.data
                    this.goodsList = data.list || data.records || []
                    if (data.total != null) {
                        this.totalPages = Math.ceil(data.total / this.pageSize)
                    }
                    if (data.pages != null) {
                        this.totalPages = data.pages
                    }
                } else {
                    this.goodsList = []
                }
            } catch (error) {
                toast('加载商品列表失败，请稍后重试', 2)
                this.goodsList = []
            } finally {
                this.loading = false
            }
        },
        async loadExchanges() {
            this.loadingRecords = true
            try {
                const res = await getMyExchanges({ page: 1, size: 20 })
                if (res && res.code === 200 && res.data) {
                    const data = res.data
                    this.exchangeRecords = data.list || data.records || []
                } else {
                    this.exchangeRecords = []
                }
            } catch (error) {
                this.exchangeRecords = []
            } finally {
                this.loadingRecords = false
            }
        },
        switchTab(tab) {
            if (tab.key === 'checkin') {
                this.$router.push('/user/checkin')
                return
            }
            if (tab.key === 'growth') {
                this.$router.push('/user/growth')
                return
            }
            if (tab.key === 'lottery') {
                toast('幸运抽奖功能开发中', 2)
                return
            }
            if (tab.key === 'exchange') {
                return
            }
            if (tab.key === 'harvest') {
                toast('我的收获功能开发中', 2)
                return
            }
        },
        switchCategory(category) {
            this.activeCategory = category
            this.currentPage = 1
            this.loadGoods()
        },
        goPage(page) {
            if (page < 1 || page > this.totalPages) return
            this.currentPage = page
            this.loadGoods()
        },
        async openExchangeDialog(goods) {
            try {
                const res = await getGoodsDetail(goods.id)
                if (res && res.code === 200 && res.data) {
                    this.selectedGoods = res.data
                } else {
                    this.selectedGoods = goods
                }
            } catch (error) {
                this.selectedGoods = goods
            }
            this.showDialog = true
        },
        closeDialog() {
            this.showDialog = false
            this.selectedGoods = null
        },
        async confirmExchange() {
            if (!this.selectedGoods) return
            if ((this.userInfo.ore || 0) < this.selectedGoods.ore) {
                toast('矿石不足，无法兑换', 2)
                return
            }
            this.exchanging = true
            try {
                const res = await doExchange({ goodsId: this.selectedGoods.id })
                if (res && res.code === 200) {
                    toast('兑换成功！', 2)
                    this.closeDialog()
                    this.userInfo.ore = (this.userInfo.ore || 0) - this.selectedGoods.ore
                    await this.loadExchanges()
                    await this.loadGoods()
                } else {
                    toast(res && res.message ? res.message : '兑换失败', 2)
                }
            } catch (error) {
                toast('兑换失败，请稍后重试', 2)
            } finally {
                this.exchanging = false
            }
        }
    }
}
</script>

<style lang="less" scoped>
@import '../../../styles/common';

.welfare-page {
    min-height: 100vh;
    background: #f5f7fa;
}

.welfare-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 24px 24px;
}

// User Info Bar
.user-info-bar {
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fff;
    border-radius: 0 0 12px 12px;
    padding: 16px 24px;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    flex-wrap: wrap;
    gap: 12px;
}

.user-info-section {
    display: flex;
    align-items: center;
    gap: 6px;
}

.user-nickname {
    font-size: 15px;
    color: #1a1a1a;
    font-weight: 500;
}

.user-level-badge {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 10px;
    background: linear-gradient(135deg, #fa8c16, #ffc069);
    color: #fff;
    font-size: 12px;
    font-weight: 600;
    line-height: 1.5;
}

.info-separator {
    color: #e8e8e8;
    font-size: 14px;
}

.info-value {
    font-size: 18px;
    font-weight: 700;
    color: #fa8c16;
}

.ore-section {
    .ore-value {
        font-size: 18px;
    }

    .ore-icon {
        font-family: fontawesome;
        font-size: 16px;
        color: #fa8c16;
    }

    .ore-question {
        font-family: fontawesome;
        font-size: 14px;
        color: #ccc;
        cursor: pointer;
        margin-left: 2px;
    }
}

// Sub Tabs
.sub-tabs {
    display: flex;
    background: #fff;
    border-radius: 12px;
    padding: 0 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    margin-bottom: 16px;
}

.tab-item {
    padding: 16px 20px;
    font-size: 15px;
    color: #666;
    cursor: pointer;
    position: relative;
    transition: color 0.2s;

    &:hover {
        color: #1e80ff;
    }

    &.active {
        color: #1e80ff;
        font-weight: 600;

        &::after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 50%;
            transform: translateX(-50%);
            width: 28px;
            height: 3px;
            background: #1e80ff;
            border-radius: 2px;
        }
    }
}

// Exchange Main Area
.exchange-main {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    margin-bottom: 16px;
}

// Category Tabs
.category-tabs {
    display: flex;
    gap: 8px;
    margin-bottom: 20px;
    border-bottom: 1px solid #f0f2f5;
    padding-bottom: 12px;
}

.category-item {
    padding: 8px 20px;
    font-size: 14px;
    color: #666;
    cursor: pointer;
    border-radius: 16px;
    transition: all 0.2s;

    &:hover {
        color: #1e80ff;
        background: rgba(30, 128, 255, 0.06);
    }

    &.active {
        color: #fff;
        background: #1e80ff;
        font-weight: 500;
    }
}

// Goods Grid
.goods-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
}

.goods-card {
    background: #fafafa;
    border-radius: 12px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.2s;
    border: 1px solid #f0f0f0;

    &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
        border-color: #e0e0e0;
    }
}

.goods-image {
    width: 100%;
    aspect-ratio: 1;
    overflow: hidden;
    background: #f5f5f5;

    img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
}

.goods-image-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-family: fontawesome;
    font-size: 48px;
    color: #ddd;
}

.goods-info {
    padding: 12px;
}

.goods-name {
    font-size: 14px;
    color: #1a1a1a;
    font-weight: 500;
    margin-bottom: 8px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.goods-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.goods-ore {
    font-family: fontawesome;
    font-size: 14px;
    color: #fa8c16;
    font-weight: 500;
}

.goods-stock {
    font-size: 12px;
    color: #999;

    &.stock-low {
        color: #fa8c16;
    }

    &.stock-empty {
        color: #ff4d4f;
    }
}

// Empty State
.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 60px 0;
}

.empty-icon {
    font-family: fontawesome;
    font-size: 64px;
    color: #e8e8e8;
    margin-bottom: 16px;
}

.empty-text {
    font-size: 15px;
    color: #ccc;
}

// Loading State
.loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 40px 0;
}

.loading-spinner {
    width: 32px;
    height: 32px;
    border: 3px solid #f0f0f0;
    border-top-color: #1e80ff;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
    margin-bottom: 12px;
}

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}

.loading-text {
    font-size: 14px;
    color: #999;
}

// Pagination
.pagination {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16px;
    margin-top: 24px;
    padding-top: 20px;
    border-top: 1px solid #f0f2f5;
}

.page-btn {
    padding: 8px 16px;
    border: 1px solid #e8e8e8;
    border-radius: 6px;
    font-size: 13px;
    color: #666;
    cursor: pointer;
    transition: all 0.2s;
    font-family: fontawesome;

    &:hover:not(.disabled) {
        border-color: #1e80ff;
        color: #1e80ff;
    }

    &.disabled {
        color: #ccc;
        cursor: not-allowed;
        border-color: #f0f0f0;
    }
}

.page-info {
    font-size: 14px;
    color: #666;
}

// Records Section
.records-section {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    margin-bottom: 16px;
}

.records-header {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    gap: 12px;
}

.records-title {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
}

.records-count {
    font-size: 13px;
    color: #999;
}

.records-list {
    display: flex;
    flex-direction: column;
}

.record-item {
    display: flex;
    align-items: center;
    padding: 14px 0;
    border-bottom: 1px solid #f2f3f5;
    gap: 12px;

    &:last-child {
        border-bottom: none;
    }
}

.record-goods {
    display: flex;
    align-items: center;
    gap: 12px;
    flex: 1;
    min-width: 0;
}

.record-goods-image {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    overflow: hidden;
    flex-shrink: 0;
    background: #f5f5f5;

    img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
}

.record-goods-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-family: fontawesome;
    font-size: 20px;
    color: #ddd;
}

.record-goods-info {
    flex: 1;
    min-width: 0;
}

.record-goods-name {
    font-size: 14px;
    color: #1a1a1a;
    font-weight: 500;
    margin-bottom: 4px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.record-time {
    font-size: 12px;
    color: #999;
}

.record-ore {
    font-family: fontawesome;
    font-size: 14px;
    color: #fa8c16;
    font-weight: 500;
    white-space: nowrap;
}

.record-status {
    padding: 4px 12px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 500;
    white-space: nowrap;

    &.status-SUCCESS {
        background: #f6ffed;
        color: #52c41a;
    }

    &.status-PENDING {
        background: #fff7e6;
        color: #fa8c16;
    }

    &.status-FAILED {
        background: #fff2f0;
        color: #ff4d4f;
    }
}

// Dialog
.dialog-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
}

.dialog-box {
    background: #fff;
    border-radius: 16px;
    width: 420px;
    max-width: 90vw;
    max-height: 80vh;
    overflow-y: auto;
    box-shadow: 0 16px 48px rgba(0, 0, 0, 0.15);
}

.dialog-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px 24px 0;
}

.dialog-title {
    font-size: 18px;
    font-weight: 600;
    color: #1a1a1a;
}

.dialog-close {
    font-size: 24px;
    color: #999;
    cursor: pointer;
    line-height: 1;

    &:hover {
        color: #666;
    }
}

.dialog-body {
    padding: 20px 24px;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
}

.dialog-goods-image {
    width: 120px;
    height: 120px;
    border-radius: 12px;
    overflow: hidden;
    margin-bottom: 16px;
    background: #f5f5f5;

    img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
}

.dialog-goods-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-family: fontawesome;
    font-size: 48px;
    color: #ddd;
}

.dialog-goods-name {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
    margin-bottom: 8px;
}

.dialog-goods-desc {
    font-size: 13px;
    color: #999;
    margin-bottom: 16px;
    line-height: 1.5;
}

.dialog-ore-cost {
    font-size: 15px;
    color: #666;
    margin-bottom: 8px;
}

.dialog-balance {
    font-size: 14px;
    color: #666;
    margin-bottom: 12px;
}

.ore-highlight {
    font-family: fontawesome;
    color: #fa8c16;
    font-weight: 600;
}

.dialog-warning {
    font-size: 13px;
    color: #ff4d4f;
    background: #fff2f0;
    padding: 8px 16px;
    border-radius: 8px;
    width: 100%;
}

.dialog-footer {
    display: flex;
    gap: 12px;
    padding: 0 24px 20px;
}

.dialog-btn {
    flex: 1;
    padding: 12px 0;
    border-radius: 8px;
    font-size: 15px;
    font-weight: 500;
    text-align: center;
    cursor: pointer;
    transition: all 0.2s;
}

.dialog-btn-cancel {
    border: 1px solid #e8e8e8;
    color: #666;

    &:hover {
        border-color: #d0d0d0;
        color: #333;
    }
}

.dialog-btn-confirm {
    background: #1e80ff;
    color: #fff;

    &:hover:not(.disabled) {
        background: #4096ff;
    }

    &.disabled {
        background: #b0d0ff;
        cursor: not-allowed;
    }
}

// Footer
.welfare-footer {
    text-align: center;
    font-size: 12px;
    color: #ccc;
    padding: 16px 0;
}

// Responsive
@media screen and (max-width: 768px) {
    .welfare-content {
        padding: 0 12px 12px;
    }

    .user-info-bar {
        padding: 12px 16px;
        gap: 8px;
    }

    .user-info-section {
        gap: 4px;
    }

    .info-value {
        font-size: 16px;
    }

    .sub-tabs {
        padding: 0 12px;
        overflow-x: auto;
    }

    .tab-item {
        padding: 14px 12px;
        font-size: 13px;
        white-space: nowrap;
    }

    .exchange-main {
        padding: 16px;
    }

    .category-tabs {
        overflow-x: auto;
        padding-bottom: 8px;
    }

    .category-item {
        padding: 6px 14px;
        font-size: 13px;
        white-space: nowrap;
    }

    .goods-grid {
        grid-template-columns: repeat(2, 1fr);
        gap: 12px;
    }

    .goods-info {
        padding: 10px;
    }

    .goods-name {
        font-size: 13px;
    }

    .records-section {
        padding: 16px;
    }

    .record-item {
        flex-wrap: wrap;
        padding: 12px 0;
    }

    .record-goods {
        width: 100%;
    }

    .dialog-box {
        width: 90vw;
    }

    .dialog-body {
        padding: 16px;
    }

    .dialog-goods-image {
        width: 100px;
        height: 100px;
    }
}
</style>