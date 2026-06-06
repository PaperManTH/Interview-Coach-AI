<template>
  <div class="page-wrapper" :style="{ '--bg-image': `url(${bgPng})` }">
  <div class="settings-page">
    <div class="settings-header">
      <button class="back-btn" @click="router.push('/')">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="m15 18-6-6 6-6"/>
        </svg>
        <span>返回</span>
      </button>
      <div class="header-content">
        <h1>API 配置</h1>
        <p class="subtitle">LLM / ASR / TTS 三组 Key 各自独立配置，互不干扰</p>
      </div>
    </div>

    <div class="settings-grid">
      <div class="main-section">
        <!-- ====== LLM 配置 ====== -->
        <div class="section-card">
          <div class="card-header">
            <div class="card-icon ai-icon"><img :src="aiPng" alt="" /></div>
            <div class="card-title-group">
              <div class="card-title">大语言模型 (LLM)</div>
              <span v-if="hasLlmConfig" class="configured-badge">已配置</span>
            </div>
          </div>
          <div class="card-body">
            <div class="form-row">
              <div class="form-group">
                <label>服务提供商</label>
                <CustomSelect v-model="form.llmType" :options="LLM_PROVIDERS" />
              </div>
              <div class="form-group">
                <label>API Key</label>
                <input
                  type="password"
                  :value="llmApiKeyDisplay"
                  @input="onLlmApiKeyInput"
                  @focus="onLlmApiKeyFocus"
                  @blur="onLlmApiKeyBlur"
                  placeholder="DeepSeek API Key (sk-xxx)"
                  :disabled="form.llmType === 'mock'"
                />
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>模型名称</label>
                <input v-model="form.llmModel" placeholder="例如 deepseek-chat / qwen-plus / gpt-4o-mini" />
              </div>
              <div class="form-group">
                <label>自定义 API 地址（可选）</label>
                <input v-model="form.llmBaseUrl" placeholder="留空使用默认地址" />
              </div>
            </div>
            <div v-if="form.llmType === 'azure'" class="form-group">
              <label>Azure Region <span style="color:#94a3b8">(必填)</span></label>
              <input v-model="form.llmRegion" placeholder="例如 eastasia、eastus" />
            </div>
          </div>
        </div>

        <!-- ====== ASR 配置 ====== -->
        <div class="section-card">
          <div class="card-header">
            <div class="card-icon voice-icon"><img :src="voicePng" alt="" /></div>
            <div class="card-title-group">
              <div class="card-title">语音识别 (ASR)</div>
              <span v-if="hasAsrConfig" class="configured-badge">已配置</span>
            </div>
          </div>
          <div class="card-body">
            <div class="form-row">
              <div class="form-group">
                <label>服务提供商</label>
                <CustomSelect v-model="form.asrType" :options="ASR_PROVIDERS" />
              </div>
              <div class="form-group">
                <label>API Key</label>
                <input
                  type="password"
                  :value="asrApiKeyDisplay"
                  @input="onAsrApiKeyInput"
                  @focus="onAsrApiKeyFocus"
                  @blur="onAsrApiKeyBlur"
                  placeholder="讯飞ASR: appid:apikey:apisecret"
                  :disabled="form.asrType === 'mock'"
                />
              </div>
            </div>
            <div class="form-group">
              <label>自定义 API 地址（可选）</label>
              <input v-model="form.asrBaseUrl" placeholder="留空使用默认地址" />
            </div>
          </div>
        </div>

        <!-- ====== TTS 配置 ====== -->
        <div class="section-card">
          <div class="card-header">
            <div class="card-icon sound-icon"><img :src="soundPng" alt="" /></div>
            <div class="card-title-group">
              <div class="card-title">语音合成 (TTS)</div>
              <span v-if="hasTtsConfig" class="configured-badge">已配置</span>
            </div>
          </div>
          <div class="card-body">
            <div class="form-row">
              <div class="form-group">
                <label>服务提供商</label>
                <CustomSelect v-model="form.ttsType" :options="TTS_PROVIDERS" />
              </div>
              <div class="form-group">
                <label>API Key</label>
                <input
                  type="password"
                  :value="ttsApiKeyDisplay"
                  @input="onTtsApiKeyInput"
                  @focus="onTtsApiKeyFocus"
                  @blur="onTtsApiKeyBlur"
                  placeholder="讯飞TTS: appid:apikey:apisecret"
                  :disabled="form.ttsType === 'mock'"
                />
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>语音类型</label>
                <CustomSelect v-model="form.ttsVoice" :options="currentTtsVoices" />
              </div>
              <div class="form-group">
                <label>自定义 API 地址（可选）</label>
                <input v-model="form.ttsBaseUrl" placeholder="留空使用默认地址" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ====== 侧边栏 ====== -->
      <div class="sidebar-section">
        <div class="section-card quick-info">
          <div class="card-header">
            <div class="card-icon info-icon"><img :src="infoPng" alt="" /></div>
            <div class="card-title">温馨提示</div>
          </div>
          <div class="card-body tips-list">
            <div class="tip-item">
              <span class="tip-num">1</span>
              <span>Key 加密保存在本地，仅用于调用 API</span>
            </div>
            <div class="tip-item">
              <span class="tip-num">2</span>
              <span>Mock 模式无需 Key，适合体验流程</span>
            </div>
            <div class="tip-item">
              <span class="tip-num">3</span>
              <span>LLM / ASR / TTS 可以各自用不同厂商</span>
            </div>
            <div class="tip-item">
              <span class="tip-num">4</span>
              <span>自定义地址支持代理和本地部署</span>
            </div>
          </div>
        </div>

        <div class="section-card status-card">
          <div class="card-header">
            <div class="card-icon status-icon"><img :src="statusPng" alt="" /></div>
            <div class="card-title">配置状态</div>
          </div>
          <div class="card-body status-list">
            <div class="status-item" :class="{ configured: hasLlmConfig }">
              <span class="status-label">🤖 LLM</span>
              <span class="status-value">{{ llmStatusText }}</span>
            </div>
            <div class="status-item" :class="{ configured: hasAsrConfig }">
              <span class="status-label">🎙️ ASR</span>
              <span class="status-value">{{ asrStatusText }}</span>
            </div>
            <div class="status-item" :class="{ configured: hasTtsConfig }">
              <span class="status-label">🔊 TTS</span>
              <span class="status-value">{{ ttsStatusText }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="settings-footer">
      <div class="footer-content">
        <button class="btn btn-secondary" @click="resetForm">重置</button>
        <button class="btn btn-primary" @click="saveSettings" :disabled="isSaving">
          <span v-if="isSaving">保存中...</span>
          <span v-else>保存配置</span>
        </button>
      </div>
    </div>

    <div v-if="message" :class="['toast', messageType]">{{ message }}</div>
  </div>
  </div>
</template>

<script setup lang="ts">
import aiPng from '@/assets/chat_icon.png';
import voicePng from '@/assets/voice_icon.png';
import soundPng from '@/assets/tool_icon.png';
import infoPng from '@/assets/data_analysis.png';
import statusPng from '@/assets/study_process.png';
import bgPng from '@/assets/background.png';
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { getUserConfig, saveUserConfig, type UserConfigRequest } from '../api/config';
import { LLM_PROVIDERS, ASR_PROVIDERS, TTS_PROVIDERS, TTS_VOICES, IFLYTEK_CN_VOICES, IFLYTEK_EN_VOICES } from '@/constants';
import CustomSelect from '@/components/ui/CustomSelect.vue';

const router = useRouter();
const authStore = useAuthStore();
const userId = computed(() => authStore.userId || 'current-user');

const form = reactive<UserConfigRequest>({
  llmType: 'mock',
  llmApiKey: '',
  llmBaseUrl: '',
  llmModel: 'deepseek-chat',
  llmRegion: '',
  asrType: 'mock',
  asrApiKey: '',
  asrBaseUrl: '',
  asrRegion: '',
  ttsType: 'mock',
  ttsApiKey: '',
  ttsBaseUrl: '',
  ttsVoice: 'alloy',
  ttsRegion: '',
});

const isSaving = ref(false);
const message = ref('');
const messageType = ref<'success' | 'error'>('success');

/** 脱敏占位符；用于标识"已配置但用户未修改"的状态。 */
const MASK_PLACEHOLDER = '*********************';

/** 后端不返回 apiKey，只有 configured 布尔值。用标记位控制输入框脱敏。 */
const llmKeyConfigured = ref(false);
const asrKeyConfigured = ref(false);
const ttsKeyConfigured = ref(false);

/** 用户是否主动清除了 Key（focus 后 key 清空，表示想重新输入）。 */
const llmEditing = ref(false);
const asrEditing = ref(false);
const ttsEditing = ref(false);

// ---- 视图显示 ----

const llmApiKeyDisplay = computed(() => {
  if (llmEditing.value) return form.llmApiKey;
  if (llmKeyConfigured.value && !form.llmApiKey) return MASK_PLACEHOLDER;
  return form.llmApiKey;
});
const asrApiKeyDisplay = computed(() => {
  if (asrEditing.value) return form.asrApiKey;
  if (asrKeyConfigured.value && !form.asrApiKey) return MASK_PLACEHOLDER;
  return form.asrApiKey;
});
const ttsApiKeyDisplay = computed(() => {
  if (ttsEditing.value) return form.ttsApiKey;
  if (ttsKeyConfigured.value && !form.ttsApiKey) return MASK_PLACEHOLDER;
  return form.ttsApiKey;
});

const currentTtsVoices = computed(() => {
  if (form.ttsType === 'iflytek') {
    return [...IFLYTEK_CN_VOICES, ...IFLYTEK_EN_VOICES];
  }
  return TTS_VOICES;
});

// ---- 交互事件 ----

function onLlmApiKeyInput(e: Event) {
  form.llmApiKey = (e.target as HTMLInputElement).value;
}
function onLlmApiKeyFocus() {
  llmEditing.value = true;
  if (form.llmApiKey === MASK_PLACEHOLDER) form.llmApiKey = '';
}
function onLlmApiKeyBlur() {
  llmEditing.value = false;
}

function onAsrApiKeyInput(e: Event) {
  form.asrApiKey = (e.target as HTMLInputElement).value;
}
function onAsrApiKeyFocus() {
  asrEditing.value = true;
  if (form.asrApiKey === MASK_PLACEHOLDER) form.asrApiKey = '';
}
function onAsrApiKeyBlur() {
  asrEditing.value = false;
}

function onTtsApiKeyInput(e: Event) {
  form.ttsApiKey = (e.target as HTMLInputElement).value;
}
function onTtsApiKeyFocus() {
  ttsEditing.value = true;
  if (form.ttsApiKey === MASK_PLACEHOLDER) form.ttsApiKey = '';
}
function onTtsApiKeyBlur() {
  ttsEditing.value = false;
}

const hasLlmConfig = computed(() => llmKeyConfigured.value && form.llmType !== 'mock');
const hasAsrConfig = computed(() => asrKeyConfigured.value && form.asrType !== 'mock');
const hasTtsConfig = computed(() => ttsKeyConfigured.value && form.ttsType !== 'mock');

const llmStatusText = computed(() => hasLlmConfig.value ? `${form.llmType} · ${form.llmModel}` : '未配置');
const asrStatusText = computed(() => hasAsrConfig.value ? form.asrType : '未配置');
const ttsStatusText = computed(() => hasTtsConfig.value ? `${form.ttsType} · ${form.ttsVoice}` : '未配置');

onMounted(async () => {
  const config = await getUserConfig(userId.value);
  if (config) {
    llmKeyConfigured.value = config.llmConfigured || false;
    asrKeyConfigured.value = config.asrConfigured || false;
    ttsKeyConfigured.value = config.ttsConfigured || false;

    form.llmType = config.llmType || 'mock';
    form.llmBaseUrl = config.llmBaseUrl || '';
    form.llmModel = config.llmModel || 'deepseek-chat';
    form.llmRegion = config.llmRegion || '';
    form.asrType = config.asrType || 'mock';
    form.asrBaseUrl = config.asrBaseUrl || '';
    form.asrRegion = config.asrRegion || '';
    form.ttsType = config.ttsType || 'mock';
    form.ttsBaseUrl = config.ttsBaseUrl || '';
    form.ttsVoice = config.ttsVoice || 'alloy';
    form.ttsRegion = config.ttsRegion || '';
  }
});

function resetForm() {
  form.llmType = 'mock';
  form.llmApiKey = '';
  form.llmBaseUrl = '';
  form.llmModel = 'deepseek-chat';
  form.llmRegion = '';
  form.asrType = 'mock';
  form.asrApiKey = '';
  form.asrBaseUrl = '';
  form.asrRegion = '';
  form.ttsType = 'mock';
  form.ttsApiKey = '';
  form.ttsBaseUrl = '';
  form.ttsVoice = 'alloy';
  form.ttsRegion = '';
  llmKeyConfigured.value = false;
  asrKeyConfigured.value = false;
  ttsKeyConfigured.value = false;
  llmEditing.value = false;
  asrEditing.value = false;
  ttsEditing.value = false;
  message.value = '';
}

async function saveSettings() {
  isSaving.value = true;
  message.value = '';

  // 构建请求体：脱敏占位符替换为空串，后端保持旧 Key
  const payload: UserConfigRequest = { ...form };
  if (payload.llmApiKey === MASK_PLACEHOLDER) payload.llmApiKey = '';
  if (payload.asrApiKey === MASK_PLACEHOLDER) payload.asrApiKey = '';
  if (payload.ttsApiKey === MASK_PLACEHOLDER) payload.ttsApiKey = '';

  try {
    await saveUserConfig(userId.value, payload);

    // 保存成功后更新标记
    llmKeyConfigured.value = form.llmType !== 'mock' && !!(form.llmApiKey || llmKeyConfigured.value);
    asrKeyConfigured.value = form.asrType !== 'mock' && !!(form.asrApiKey || asrKeyConfigured.value);
    ttsKeyConfigured.value = form.ttsType !== 'mock' && !!(form.ttsApiKey || ttsKeyConfigured.value);

    // 清除编辑状态，让 Key 位置回显 ****
    llmEditing.value = false;
    asrEditing.value = false;
    ttsEditing.value = false;

    message.value = '配置保存成功！';
    messageType.value = 'success';
  } catch (error) {
    message.value = '保存失败：网络错误';
    messageType.value = 'error';
  } finally {
    isSaving.value = false;
    setTimeout(() => { message.value = ''; }, 3000);
  }
}
</script>

<style scoped>
/* ============ 全宽背景层（与 Home 一致） ============ */
.page-wrapper {
  min-height: 100vh;
  background: var(--bg-image) center center / cover no-repeat fixed;
  position: relative;
}
.page-wrapper::before {
  content: '';
  position: fixed;
  inset: 0;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.45) 0%, rgba(15, 23, 42, 0.2) 30%, rgba(15, 23, 42, 0.35) 100%);
  pointer-events: none;
  z-index: 0;
}

/* ============ 内容容器（半透明毛玻璃） ============ */
.settings-page {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  padding-bottom: 100px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

/* ============ 顶部导航条 ============ */
.settings-header {
  display: flex;
  align-items: center;
  gap: 20px;
  max-width: min(1100px, 100%);
  margin: 0 auto;
  padding: 20px clamp(16px, 4vw, 48px);
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
}
.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 16px;
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 10px;
  color: #475569;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 150ms;
  backdrop-filter: blur(4px);
}
.back-btn:hover {
  background: rgba(241, 245, 249, 0.8);
  border-color: rgba(148, 163, 184, 0.5);
  color: #1e293b;
  transform: translateX(-2px);
}
.header-content h1 { margin: 0; font-size: 19px; font-weight: 700; color: #0f172a; letter-spacing: -0.01em; }
.subtitle { margin: 4px 0 0; font-size: 13px; color: #64748b; }

/* ============ 主布局网格 ============ */
.settings-grid {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 24px;
  max-width: min(1100px, 100%);
  margin: 0 auto;
  padding: 28px clamp(16px, 4vw, 48px) 0;
}
.main-section { display: flex; flex-direction: column; gap: 20px; }
.sidebar-section { display: flex; flex-direction: column; gap: 20px; }

/* ============ 卡片通用 ============ */
.section-card {
  background: rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  transition: all 200ms cubic-bezier(0.16, 1, 0.3, 1);
}
.section-card:hover {
  background: rgba(255, 255, 255, 0.62);
  border-color: rgba(148, 163, 184, 0.3);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 22px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.12);
  background: rgba(248, 250, 252, 0.5);
}
.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  padding: 8px;
  box-sizing: border-box;
}
.card-icon img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}
.ai-icon     { background: linear-gradient(135deg, rgba(251, 191, 36, 0.2), rgba(245, 158, 11, 0.15)); }
.voice-icon  { background: linear-gradient(135deg, rgba(59, 130, 246, 0.18), rgba(99, 102, 241, 0.12)); }
.sound-icon  { background: linear-gradient(135deg, rgba(236, 72, 153, 0.18), rgba(217, 70, 239, 0.12)); }
.info-icon   { background: linear-gradient(135deg, rgba(99, 102, 241, 0.18), rgba(139, 92, 246, 0.12)); }
.status-icon { background: linear-gradient(135deg, rgba(34, 197, 94, 0.18), rgba(16, 185, 129, 0.12)); }

.card-title-group { display: flex; align-items: center; gap: 10px; flex: 1; }
.card-title { font-size: 15px; font-weight: 700; color: #0f172a; }

.configured-badge {
  padding: 3px 10px;
  background: rgba(16, 185, 129, 0.12);
  border: 1px solid rgba(16, 185, 129, 0.2);
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  color: #059669;
}

.card-body { padding: 20px 22px; }

/* ============ 表单组件 ============ */
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.form-row + .form-row { margin-top: 14px; }
.form-group { margin-bottom: 14px; }
.form-group:last-child { margin-bottom: 0; }
.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #475569;
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

.form-group input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 10px;
  font-size: 13.5px;
  font-family: inherit;
  background: rgba(255, 255, 255, 0.6);
  color: #0f172a;
  transition: all 150ms cubic-bezier(0.16, 1, 0.3, 1);
  box-sizing: border-box;
  backdrop-filter: blur(4px);
}

.form-group input:focus {
  outline: none;
  border-color: #1e40af;
  background: rgba(255, 255, 255, 0.85);
  box-shadow: 0 0 0 3px rgba(30, 64, 175, 0.1);
}
.form-group input:disabled {
  background: rgba(241, 245, 249, 0.5);
  color: #94a3b8;
  cursor: not-allowed;
}

/* ============ 侧边栏 - 温馨提示 ============ */
.tips-list { display: flex; flex-direction: column; gap: 13px; }
.tip-item {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}
.tip-num {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.18), rgba(99, 102, 241, 0.1));
  color: #1e40af;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 1px;
}
.tip-item span:last-child {
  font-size: 12.5px;
  color: #334155;
  line-height: 1.55;
}

/* ============ 侧边栏 - 配置状态 ============ */
.status-list { display: flex; flex-direction: column; gap: 10px; }
.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 11px 14px;
  background: rgba(241, 245, 249, 0.5);
  border-radius: 10px;
  transition: all 150ms;
}
.status-item.configured { background: rgba(16, 185, 129, 0.08); border: 1px solid rgba(16, 185, 129, 0.15); }
.status-label { font-size: 12px; font-weight: 600; color: #334155; letter-spacing: 0.03em; }
.status-value { font-size: 12px; color: #94a3b8; }
.status-item.configured .status-value { color: #059669; font-weight: 600; }

/* ============ 底部操作栏 ============ */
.settings-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 14px 0;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-top: 1px solid rgba(148, 163, 184, 0.15);
  z-index: 50;
}
.footer-content {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  max-width: min(1100px, 100%);
  margin: 0 auto;
  padding: 0 clamp(16px, 4vw, 48px);
}
.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 11px 24px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 150ms cubic-bezier(0.16, 1, 0.3, 1);
  white-space: nowrap;
}
.btn-primary {
  background: linear-gradient(135deg, #1e40af, #1e3a5f);
  color: #fff;
  box-shadow: 0 2px 8px rgba(30, 64, 175, 0.3);
}
.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(30, 64, 175, 0.4);
}
.btn-primary:active:not(:disabled) { transform: translateY(0); }
.btn-primary:disabled { background: #94a3b8; cursor: not-allowed; box-shadow: none; transform: none; }
.btn-secondary {
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.3);
  color: #475569;
  backdrop-filter: blur(4px);
}
.btn-secondary:hover {
  background: rgba(241, 245, 249, 0.7);
  border-color: rgba(148, 163, 184, 0.5);
  color: #1e293b;
}

/* ============ Toast 通知 ============ */
.toast {
  position: fixed;
  bottom: 90px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 26px;
  border-radius: 12px;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.18);
  z-index: 1000;
  animation: slide-up 280ms cubic-bezier(0.16, 1, 0.3, 1);
}
.toast.success { background: linear-gradient(135deg, #059669, #047857); }
.toast.error   { background: linear-gradient(135deg, #dc2626, #b91c1c); }

@keyframes slide-up {
  from { opacity: 0; transform: translateX(-50%) translateY(12px); }
  to   { opacity: 1; transform: translateX(-50%) translateY(0); }
}

/* ============ 响应式 ============ */
@media (max-width: 900px) {
  .settings-grid { grid-template-columns: 1fr; }
  .sidebar-section { order: -1; display: grid; grid-template-columns: 1fr 1fr; }
}

@media (max-width: 640px) {
  .form-row { grid-template-columns: 1fr; }
  .sidebar-section { grid-template-columns: 1fr; }
  .settings-header { padding: 14px 20px; }
  .settings-grid { padding: 20px 16px 0; }
  .back-btn { padding: 8px 12px; font-size: 12px; }
  .header-content h1 { font-size: 17px; }
  .card-header { padding: 14px 16px; }
  .card-body { padding: 16px; }
  .card-icon { width: 40px; height: 40px; padding: 6px; border-radius: 10px; }
}
</style>
