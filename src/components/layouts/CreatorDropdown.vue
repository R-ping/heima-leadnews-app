<template>
    <div class="creator-dropdown" @click.stop>
        <div class="dropdown-menu-section">
            <div class="dropdown-item" @click="handleNavigate('/creator/publish', true)">
                <span class="dropdown-icon">&#xf040;</span>
                <span class="dropdown-label">写文章</span>
            </div>
            <div class="dropdown-item" @click="handleNavigate('/pins')">
                <span class="dropdown-icon">&#xf142;</span>
                <span class="dropdown-label">发沸点</span>
            </div>
            <div class="dropdown-item" @click="handleNavigate('/creator/content?tab=draft')">
                <span class="dropdown-icon">&#xf0f6;</span>
                <span class="dropdown-label">草稿箱</span>
            </div>
            <div class="dropdown-item" :class="{ disabled: !coursePermission.hasPermission }" @click="handleCourseClick">
                <span class="dropdown-icon" v-html="coursePermission.hasPermission ? '&#xf02d;' : '&#xf023;'"></span>
                <span class="dropdown-label">写小册</span>
            </div>
        </div>
    </div>
</template>

<script>
import { permission } from '@/utils/permission'
import { toast } from '@/utils/toast'

export default {
    name: 'CreatorDropdown',
    data() {
        return {
            coursePermission: {
                hasPermission: true,
                powerLevel: 0,
                requiredLevel: 0
            }
        }
    },
    created() {
        this.loadCoursePermission()
    },
    methods: {
        async loadCoursePermission() {
            try {
                const res = await permission.canCreateCourse()
                this.coursePermission = res
            } catch (e) {
                this.coursePermission = { hasPermission: false, powerLevel: 0, requiredLevel: 9 }
            }
        },
        handleNavigate(path, isNewWindow) {
            this.$emit('navigate', path, isNewWindow)
            this.$emit('close')
        },
        handleCourseClick() {
            if (!this.coursePermission.hasPermission) {
                toast('当前逐力值等级，未达到写小册要求', 2)
                return
            }
            this.handleNavigate('/course/publish')
        }
    }
}
</script>

<style lang="less" scoped>
@import '../../styles/common';

.creator-dropdown {
    position: absolute;
    top: 100%;
    left: 0;
    margin-top: 8px;
    background-color: #ffffff;
    border-radius: 8px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
    width: 160px;
    z-index: 200;
    overflow: hidden;
    animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(-4px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.dropdown-menu-section {
    padding: 4px 0;
}

.dropdown-item {
    display: flex;
    align-items: center;
    padding: 10px 16px;
    cursor: pointer;
    transition: background-color 0.2s, color 0.2s;
    gap: 10px;
}

.dropdown-item:hover {
    background-color: #f5f7fa;
    color: @mian-color;
}

.dropdown-item:hover .dropdown-icon {
    color: @mian-color;
}

.dropdown-item.disabled {
    color: #c0c4cc;
    cursor: not-allowed;
}

.dropdown-item.disabled:hover {
    background-color: transparent;
    color: #c0c4cc;
}

.dropdown-item.disabled:hover .dropdown-icon {
    color: #c0c4cc;
}

.dropdown-icon {
    font-family: fontawesome;
    font-size: 14px;
    width: 16px;
    text-align: center;
    color: #515767;
    transition: color 0.2s;
}

.dropdown-label {
    font-size: 14px;
    color: #333;
    flex: 1;
    transition: color 0.2s;
}

.dropdown-item.disabled .dropdown-label {
    color: #c0c4cc;
}
</style>