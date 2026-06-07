<script setup lang="ts">
/**
 * 用户下拉菜单组件
 * 
 * 点击用户头像后显示下拉菜单，包含：
 * - 个人主页
 * - 面试历史
 * - 设置
 * - 退出登录
 */
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

const router = useRouter();
const auth = useAuthStore();

const isOpen = ref(false);

function toggleMenu() {
  isOpen.value = !isOpen.value;
}

function closeMenu() {
  isOpen.value = false;
}

function navigateTo(path: string) {
  closeMenu();
  router.push(path);
}

function handleLogout() {
  closeMenu();
  auth.logout();
  localStorage.removeItem('auth_token');
  router.push('/');
}

// 点击外部关闭菜单
function handleClickOutside(event: MouseEvent) {
  const target = event.target as HTMLElement;
  if (!target.closest('.user-dropdown')) {
    closeMenu();
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});
</script>

<template>
  <div class="user-dropdown">
    <button class="avatar-btn" @click="toggleMenu" :aria-expanded="isOpen">
      <img
        v-if="auth.avatarUrl"
        :src="auth.avatarUrl"
        class="avatar-img"
        alt="用户头像"
      />
      <div v-else class="avatar-placeholder">
        {{ auth.username?.charAt(0).toUpperCase() || 'U' }}
      </div>
    </button>

    <Transition name="dropdown">
      <div v-if="isOpen" class="dropdown-menu">
        <div class="menu-header">
          <div class="user-info">
            <span class="username">{{ auth.username || '用户' }}</span>
          </div>
        </div>

        <div class="menu-divider"></div>

        <div class="menu-items">
          <button class="menu-item" @click="navigateTo('/profile')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <span>个人主页</span>
          </button>

          <button class="menu-item" @click="navigateTo('/history')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
            <span>面试历史</span>
            <span class="badge">新</span>
          </button>

          <button class="menu-item" @click="navigateTo('/settings')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="3"/>
              <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>
            </svg>
            <span>设置</span>
          </button>
        </div>

        <div class="menu-divider"></div>

        <button class="menu-item logout" @click="handleLogout">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16 17 21 12 16 7"/>
            <line :x1="21" :y1="12" :x2="9" :y2="12"/>
          </svg>
          <span>退出登录</span>
        </button>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.user-dropdown {
  position: relative;
}

.avatar-btn {
  padding: 0;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 150ms;
}

.avatar-btn:hover {
  transform: scale(1.05);
}

.avatar-img {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.avatar-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #1e40af, #1e3a5f);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  border: 2px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 200px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  z-index: 1000;
  overflow: hidden;
}

.menu-header {
  padding: 14px 16px;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.menu-divider {
  height: 1px;
  background: rgba(148, 163, 184, 0.15);
}

.menu-items {
  padding: 8px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 10px 16px;
  background: transparent;
  border: none;
  color: #475569;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 150ms;
  text-align: left;
}

.menu-item:hover {
  background: rgba(30, 64, 175, 0.08);
  color: #1e40af;
}

.menu-item svg {
  flex-shrink: 0;
}

.badge {
  padding: 2px 6px;
  background: rgba(16, 185, 129, 0.15);
  border-radius: 4px;
  font-size: 10px;
  font-weight: 700;
  color: #059669;
}

.menu-item.logout {
  color: #dc2626;
}

.menu-item.logout:hover {
  background: rgba(239, 68, 68, 0.08);
  color: #b91c1c;
}

/* Transition */
.dropdown-enter-active {
  transition: all 200ms cubic-bezier(0.16, 1, 0.3, 1);
}

.dropdown-leave-active {
  transition: all 150ms ease-in;
}

.dropdown-enter-from {
  opacity: 0;
  transform: translateY(-8px) scale(0.95);
}

.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px) scale(0.98);
}

/* Responsive */
@media (max-width: 768px) {
  .dropdown-menu {
    width: 180px;
  }
}
</style>