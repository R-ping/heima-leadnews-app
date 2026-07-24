<template>
    <div class="confirm-modal-overlay" @click.self="handleCancel">
        <div class="confirm-modal-card">
            <div class="confirm-modal-title">{{ title }}</div>
            <div class="confirm-modal-content">{{ content }}</div>
            <div class="confirm-modal-actions">
                <button class="confirm-modal-btn confirm-modal-btn--cancel" @click="handleCancel">{{ cancelText }}</button>
                <button class="confirm-modal-btn confirm-modal-btn--confirm" @click="handleConfirm">{{ confirmText }}</button>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'ConfirmModal',
    props: {
        title: {
            type: String,
            default: ''
        },
        content: {
            type: String,
            default: ''
        },
        confirmText: {
            type: String,
            default: '确定'
        },
        cancelText: {
            type: String,
            default: '取消'
        }
    },
    methods: {
        handleConfirm() {
            this.$emit('confirm')
        },
        handleCancel() {
            this.$emit('cancel')
        }
    }
}
</script>

<style lang="less" scoped>
.confirm-modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.45);
    z-index: 1000;
    display: flex;
    align-items: center;
    justify-content: center;
}

.confirm-modal-card {
    background: #fff;
    border-radius: 16px;
    width: 400px;
    padding: 32px 0 0;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.confirm-modal-title {
    font-size: 18px;
    font-weight: 600;
    color: #1A1A1A;
    text-align: center;
    padding: 0 24px;
}

.confirm-modal-content {
    font-size: 14px;
    color: #8C8C8C;
    text-align: center;
    margin: 12px 0;
    padding: 0 24px;
}

.confirm-modal-actions {
    display: flex;
    justify-content: center;
    gap: 16px;
    padding: 0 24px 32px;
}

.confirm-modal-btn {
    border-radius: 20px;
    padding: 6px 24px;
    font-size: 14px;
    cursor: pointer;
    border: none;
    outline: none;
    transition: all 0.2s;

    &--cancel {
        background: transparent;
        border: 1px solid #D9D9D9;
        color: #666;

        &:hover {
            border-color: #999;
            color: #333;
        }
    }

    &--confirm {
        background: #E65A5A;
        color: #fff;

        &:hover {
            background: #D94A4A;
        }
    }
}
</style>