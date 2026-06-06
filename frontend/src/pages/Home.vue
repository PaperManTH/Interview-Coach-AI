<script setup lang="ts">
import logoPng from '@/assets/logo.png';
import settingPng from '@/assets/setting_icon.png';
import chatPng from '@/assets/chat_icon.png';
import voicePng from '@/assets/voice_icon.png';
import toolPng from '@/assets/tool_icon.png';
import dataPng from '@/assets/data_analysis.png';
import heroPng from '@/assets/study_process.png';
import bgPng from '@/assets/background.png';
import { ref, onMounted } from 'vue';
import { INTERVIEW_SCENES } from '@/types/scene';
import SceneCard from '@/components/scene/SceneCard.vue';
import { useRouter } from 'vue-router';
import BilingualText from '@/components/BilingualText.vue';
import { useAuthStore } from '@/stores/authStore';
import ResumeUpload from '@/components/resume/ResumeUpload.vue';

const router = useRouter();
const auth = useAuthStore();

const resumeUpload = ref<InstanceType<typeof ResumeUpload> | null>(null);
const resumeInfo = ref<{ name: string; size: number } | null>(null);

onMounted(() => {
  try {
    const raw = localStorage.getItem('icai:last-resume');
    if (raw) {
      const data = JSON.parse(raw);
      if (data?.name) resumeInfo.value = { name: data.name, size: data.size ?? 0 };
    }
  } catch { /* noop */ }
});

function onResumeUploaded(payload: { name: string; size: number }) {
  resumeInfo.value = { name: payload.name, size: payload.size };
}

function clearResume() {
  resumeInfo.value = null;
  try { localStorage.removeItem('icai:last-resume'); } catch { /* noop */ }
}

function humanSize(bytes: number): string {
  if (!bytes) return '-';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}
</script>

<template>
  <!-- 全宽背景层 -->
  <div class="page-wrapper" :style="{ '--bg-image': `url(${bgPng})` }">
    <!-- 内容区：居中 + 半透明玻璃态背景，与全宽背景图融合 -->
    <div class="home">
      <header class="nav">
        <div class="logo-wrap">
          <img :src="logoPng" class="logo-mark" alt="logo" />
          <div class="logo-text">
            <div class="logo-title">Interview Coach</div>
            <div class="logo-sub">AI 面试教练</div>
          </div>
        </div>
        <nav class="nav-actions">
          <button v-if="auth.isLoggedIn" class="nav-btn" @click="router.push('/settings')" title="API 配置">
            <img :src="settingPng" class="nav-icon" alt="settings" />
            <span class="nav-label">Settings</span>
          </button>
          <div v-if="auth.isLoggedIn" class="user-chip">
            <img v-if="auth.avatarUrl" :src="auth.avatarUrl" class="avatar" alt="" />
            <div v-else class="avatar fallback">{{ (auth.username || 'U').charAt(0).toUpperCase() }}</div>
            <span class="username">{{ auth.username || 'User' }}</span>
            <button class="logout-btn" @click="auth.logout()" title="Log out">↩</button>
          </div>
          <button v-else class="nav-btn primary" @click="router.push('/login')">登录 / Sign In</button>
        </nav>
      </header>

      <section class="hero">
        <div class="hero-tag">
          <span class="tag-dot"></span>
          <span>Real-time · AI Powered · English</span>
          <span class="tag-zh">实时对话 · 英语面试</span>
        </div>

        <h1 class="hero-title">
          <span class="hero-en">Practice Your English Interview</span>
          <span class="hero-accent">with an AI Coach</span>
        </h1>
        <p class="hero-desc">
          Sharpen your interviewing skills through natural, real-time conversations.  通过自然、实时的对话来磨练你的英语面试技巧。
        </p>

        <div v-if="!auth.isLoggedIn" class="hero-cta">
          <button class="btn-login" @click="router.push('/login')">
            <img :src="heroPng" class="cta-emoji" alt="" />
            <span>登录后开始面试练习</span>
            <span class="arrow">→</span>
          </button>
        </div>
      </section>

      <section class="features">
        <div class="feature-card">
          <div class="feature-icon"><img :src="chatPng" alt="" /></div>
          <div class="feature-text">
            <h3>Real-time Conversation</h3>
            <p>实时对话 · 流畅自然的英文面试交流</p>
          </div>
        </div>
        <div class="feature-card">
          <div class="feature-icon"><img :src="voicePng" alt="" /></div>
          <div class="feature-text">
            <h3>Voice Recognition</h3>
            <p>语音识别 · 说话即时获得反馈</p>
          </div>
        </div>
        <div class="feature-card">
          <div class="feature-icon"><img :src="toolPng" alt="" /></div>
          <div class="feature-text">
            <h3>Smart Scenarios</h3>
            <p>多场景模拟 · HR / 技术 / 压力面试全覆盖</p>
          </div>
        </div>
      </section>

      <section class="resume-section">
        <div class="resume-card">
          <div class="resume-icon-wrap">
            <img :src="dataPng" class="resume-icon" alt="resume" />
          </div>
          <div class="resume-text">
            <div class="resume-title">Upload Your Resume</div>
            <div class="resume-sub">上传简历 · 让 AI 面试官为你量身定制问题</div>
          </div>
          <div class="resume-action">
            <div v-if="resumeInfo" class="resume-info">
              <span class="resume-name" :title="resumeInfo.name">{{ resumeInfo.name }}</span>
              <span class="resume-meta">{{ humanSize(resumeInfo.size) }} · 已就绪</span>
              <button class="ghost-btn" @click="clearResume" title="移除">✕</button>
            </div>
            <button v-else class="primary-btn" @click="resumeUpload?.open()">
              <span>选择文件</span>
              <span class="btn-zh">Choose File</span>
            </button>
          </div>
        </div>
      </section>

      <section class="scenario-section">
        <div class="section-heading">
          <h2><span class="en">Choose Your Scenario</span><span class="zh">选择面试场景</span></h2>
          <p class="section-desc">Pick a practice mode below — follow-up questions will automatically adapt to your answers.</p>
        </div>

        <div class="grid">
          <SceneCard v-for="scene in INTERVIEW_SCENES" :key="scene.key" :scene="scene" />
        </div>
      </section>

      <footer class="footer">
        <p class="footer-en">Practice makes perfect · 🚀 Keep going!</p>
        <p class="footer-zh">熟能生巧 · 继续练习，你离 offer 只差一步</p>
      </footer>

      <ResumeUpload ref="resumeUpload" :hide-trigger="true" @uploaded="onResumeUploaded" />
    </div>
  </div>
</template>

<style scoped>
/* ============ 全宽背景层 ============ */
.page-wrapper {
  min-height: 100vh;
  background:
    var(--bg-image) center center / cover no-repeat fixed;
  /* 底部加渐变叠加，使内容区与背景自然过渡 */
  /* 背景图上方用深色渐变晕染，确保内容区卡片的亮色有足够对比 */
  position: relative;
}
.page-wrapper::before {
  content: '';
  position: fixed;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(15, 23, 42, 0.45) 0%, rgba(15, 23, 42, 0.2) 30%, rgba(15, 23, 42, 0.35) 100%);
  pointer-events: none;
  z-index: 0;
}

/* ============ 内容容器（半透明毛玻璃） ============ */
.home {
  position: relative;
  z-index: 1;
  max-width: min(1100px, 100%);
  margin: 0 auto;
  min-height: 100vh;
  padding: 0 clamp(16px, 4vw, 48px) 48px;
  /* 半透明底色 + 轻微 backdrop-filter，让背景图可见但不过度干扰阅读 */
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

/* ============ 顶部导航 ============ */
.nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0 24px;
}
.logo-wrap { display: flex; align-items: center; gap: 12px; }
.logo-mark {
  width: 63px; height: 63px;
  border-radius: 12px;
  object-fit: contain;
  display: block;
}
.logo-title { font-size: 16px; font-weight: 700; color: #0f172a; letter-spacing: -0.01em; }
.logo-sub   { font-size: 12px; color: #475569; }

.nav-actions { display: flex; align-items: center; gap: 10px; }
.nav-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid rgba(148, 163, 184, 0.3);
  color: #1e293b;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 150ms;
  backdrop-filter: blur(4px);
}
.nav-btn:hover { background: rgba(241, 245, 249, 0.8); border-color: rgba(148, 163, 184, 0.5); }
.nav-icon {
  width: 27px; height: 27px;
  object-fit: contain;
  display: block;
}

.nav-btn.primary {
  background: linear-gradient(135deg, #1e40af, #1e3a5f);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 2px 8px rgba(30, 64, 175, 0.35);
}
.nav-btn.primary:hover {
  box-shadow: 0 4px 14px rgba(30, 64, 175, 0.45);
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 6px 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid rgba(148, 163, 184, 0.3);
  backdrop-filter: blur(4px);
}
.avatar {
  width: 28px; height: 28px;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px rgba(148, 163, 184, 0.3);
  background: #f1f5f9;
  object-fit: cover;
}
.avatar.fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}
.username { font-size: 13px; font-weight: 600; color: #1e293b; }
.logout-btn {
  border: none; background: transparent;
  color: #64748b;
  width: 26px; height: 26px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
  transition: all 150ms;
}
.logout-btn:hover { background: rgba(239, 68, 68, 0.12); color: #dc2626; }

/* ============ Hero 区 ============ */
.hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 48px 0 36px;
}
.hero-tag {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  background: rgba(219, 234, 254, 0.45);
  color: #1e40af;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 24px;
  backdrop-filter: blur(6px);
}
.tag-dot {
  width: 6px; height: 6px; border-radius: 50%;
  background: #3b82f6;
  animation: pulse 1.6s infinite;
}
.tag-zh { color: #2563eb; opacity: 0.7; margin-left: 4px; }
@keyframes pulse { 0%,100% { opacity: 1; transform: scale(1); } 50% { opacity: 0.5; transform: scale(1.3); } }

.hero-title {
  margin: 0 0 16px;
  font-size: clamp(32px, 5vw, 52px);
  font-weight: 800;
  line-height: 1.15;
  color: #0f172a;
  letter-spacing: -0.02em;
}
.hero-en { display: block; }
.hero-accent {
  display: block;
  background: linear-gradient(135deg, #1e40af, #6366f1, #8b5cf6);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.hero-desc {
  max-width: 640px;
  margin: 0 auto;
  font-size: 16px;
  line-height: 1.65;
  color: #334155;
}

.hero-cta { margin-top: 28px; }
.btn-login {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 14px 28px;
  background: linear-gradient(135deg, #0f172a, #1e293b);
  color: #fff;
  border: none;
  border-radius: 14px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.25);
  transition: all 200ms cubic-bezier(0.16, 1, 0.3, 1);
}
.btn-login:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.35);
}
.btn-login .arrow {
  margin-left: 4px;
  transition: transform 200ms;
}
.btn-login:hover .arrow { transform: translateX(4px); }

.cta-emoji {
  width: 30px; height: 30px;
  object-fit: contain;
  display: inline-block;
}

/* ============ 特性卡片 ============ */
.features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  padding: 24px 0 8px;
}
.feature-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: rgba(255, 255, 255, 0.55);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 16px;
  backdrop-filter: blur(6px);
  transition: all 200ms cubic-bezier(0.16, 1, 0.3, 1);
}
.feature-card:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.7);
  box-shadow: 0 8px 20px -8px rgba(15, 23, 42, 0.12);
  border-color: rgba(148, 163, 184, 0.35);
}
.feature-icon {
  width: 63px; height: 63px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.12), rgba(99, 102, 241, 0.1));
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.feature-icon img {
  width: 42px; height: 42px;
  object-fit: contain;
  display: block;
}
.feature-text h3 { margin: 0 0 4px; font-size: 15px; font-weight: 700; color: #0f172a; }
.feature-text p  { margin: 0; font-size: 12.5px; color: #475569; line-height: 1.5; }

/* ============ 简历上传卡片 ============ */
.resume-section { padding: 28px 0 12px; }
.resume-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.5);
  border: 1.5px dashed rgba(59, 130, 246, 0.3);
  border-radius: 18px;
  backdrop-filter: blur(6px);
  transition: all 200ms;
}
.resume-card:hover {
  border-color: rgba(59, 130, 246, 0.55);
  transform: translateY(-1px);
  background: rgba(255, 255, 255, 0.65);
}
.resume-icon-wrap {
  width: 84px; height: 84px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15), rgba(99, 102, 241, 0.12));
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.resume-icon {
  width: 48px; height: 48px;
  object-fit: contain;
  display: block;
}
.resume-text { flex: 1; min-width: 0; }
.resume-title { font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 3px; }
.resume-sub   { font-size: 13px; color: #475569; }

.resume-action { display: flex; align-items: center; gap: 10px; }
.primary-btn {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #1e40af, #1e3a5f);
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(30, 64, 175, 0.3);
  transition: all 180ms;
  line-height: 1.1;
}
.primary-btn .btn-zh { font-size: 11px; opacity: 0.85; font-weight: 500; }
.primary-btn:hover { transform: translateY(-1px); box-shadow: 0 6px 18px rgba(30, 64, 175, 0.4); }

.resume-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  padding: 8px 10px;
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.25);
  border-radius: 12px;
  position: relative;
}
.resume-info .resume-name {
  font-size: 13px;
  font-weight: 700;
  color: #065f46;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.resume-info .resume-meta { font-size: 11px; color: #059669; }
.ghost-btn {
  position: absolute; top: 4px; right: 4px;
  width: 18px; height: 18px;
  border: none; border-radius: 50%;
  background: #fff;
  color: #059669;
  font-size: 11px;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: all 120ms;
}
.ghost-btn:hover { background: #fee2e2; color: #dc2626; }

/* ============ 场景选择 ============ */
.scenario-section { padding: 40px 0 16px; }
.section-heading { text-align: center; margin-bottom: 24px; }
.section-heading h2 { margin: 0 0 8px; }
.section-heading .en {
  display: block;
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.01em;
}
.section-heading .zh {
  display: block;
  font-size: 15px;
  color: #334155;
  margin-top: 4px;
  font-weight: 500;
}
.section-desc {
  margin: 8px auto 0;
  color: #475569;
  font-size: 14px;
  max-width: 540px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
}

/* ============ Footer ============ */
.footer {
  text-align: center;
  padding: 40px 0 0;
  margin-top: 24px;
  border-top: 1px solid rgba(148, 163, 184, 0.2);
}
.footer-en { margin: 0 0 4px; font-size: 13px; color: #334155; font-weight: 600; }
.footer-zh { margin: 0; font-size: 12px; color: #64748b; }

/* ============ 响应式 ============ */
@media (max-width: 880px) {
  .features { grid-template-columns: 1fr; }
  .grid     { grid-template-columns: 1fr; }
}

@media (max-width: 640px) {
  .home { padding: 0 16px 40px; background: rgba(255, 255, 255, 0.78); }
  .nav { padding: 16px 0 20px; }
  .logo-mark { width: 54px; height: 54px; }
  .logo-sub   { display: none; }
  .nav-label { display: none; }
  .hero { padding: 32px 0 24px; }
  .hero-desc { font-size: 14px; }
  .btn-login { padding: 12px 22px; font-size: 14px; }
  .resume-card {
    flex-direction: column;
    text-align: center;
    padding: 18px;
  }
  .resume-text { width: 100%; }
  .resume-action { width: 100%; justify-content: center; }
  .resume-info { align-items: center; text-align: center; }
  .section-heading .en { font-size: 20px; }
  .section-heading .zh { font-size: 13px; }
}
</style>
