<template>
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
            <div class="card-icon ai-icon">🤖</div>
            <div class="card-title-group">
              <div class="card-title">大语言模型 (LLM)</div>
              <span v-if="hasLlmConfig" class="configured-badge">已配置</span>
            </div>
          </div>
          <div class="card-body">
            <div class="form-row">
              <div class="form-group">
                <label>服务提供商</label>
                <select v-model="form.llmType">
                  <option v-for="p in LLM_PROVIDERS" :key="p.value" :value="p.value">{{ p.label }}</option>
                </select>
              </div>
              <div class="form-group">
                <label>API Key</label>
                <input
                  type="password"
                  v-model="form.llmApiKey"
                  placeholder="sk-xxx"
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
          </div>
        </div>

        <!-- ====== ASR 配置 ====== -->
        <div class="section-card">
          <div class="card-header">
            <div class="card-icon voice-icon">🎙️</div>
            <div class="card-title-group">
              <div class="card-title">语音识别 (ASR)</div>
              <span v-if="hasAsrConfig" class="configured-badge">已配置</span>
            </div>
          </div>
          <div class="card-body">
            <div class="form-row">
              <div class="form-group">
                <label>服务提供商</label>
                <select v-model="form.asrType">
                  <option v-for="p in ASR_PROVIDERS" :key="p.value" :value="p.value">{{ p.label }}</option>
                </select>
              </div>
              <div class="form-group">
                <label>API Key</label>
                <input
                  type="password"
                  v-model="form.asrApiKey"
                  placeholder="sk-xxx"
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
            <div class="card-icon sound-icon">🔊</div>
            <div class="card-title-group">
              <div class="card-title">语音合成 (TTS)</div>
              <span v-if="hasTtsConfig" class="configured-badge">已配置</span>
            </div>
          </div>
          <div class="card-body">
            <div class="form-row">
              <div class="form-group">
                <label>服务提供商</label>
                <select v-model="form.ttsType">
                  <option v-for="p in TTS_PROVIDERS" :key="p.value" :value="p.value">{{ p.label }}</option>
                </select>
              </div>
              <div class="form-group">
                <label>API Key</label>
                <input
                  type="password"
                  v-model="form.ttsApiKey"
                  placeholder="sk-xxx"
                  :disabled="form.ttsType === 'mock'"
                />
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>语音类型</label>
                <select v-model="form.ttsVoice">
                  <option v-for="v in TTS_VOICES" :key="v.value" :value="v.value">{{ v.label }}</option>
                </select>
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
            <div class="card-icon info-icon">💡</div>
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
            <div class="card-icon status-icon">📊</div>
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
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getUserConfig, saveUserConfig, type UserConfigRequest } from '../api/config';
import { LLM_PROVIDERS, ASR_PROVIDERS, TTS_PROVIDERS, TTS_VOICES } from '@/constants';

const router = useRouter();
const userId = ref('current-user');

const form = reactive<UserConfigRequest>({
  llmType: 'mock',
  llmApiKey: '',
  llmBaseUrl: '',
  llmModel: 'deepseek-chat',
  asrType: 'mock',
  asrApiKey: '',
  asrBaseUrl: '',
  ttsType: 'mock',
  ttsApiKey: '',
  ttsBaseUrl: '',
  ttsVoice: 'alloy',
});

const isSaving = ref(false);
const message = ref('');
const messageType = ref<'success' | 'error'>('success');

const hasLlmConfig = computed(() => form.llmType !== 'mock' && !!form.llmApiKey);
const hasAsrConfig = computed(() => form.asrType !== 'mock' && !!form.asrApiKey);
const hasTtsConfig = computed(() => form.ttsType !== 'mock' && !!form.ttsApiKey);

const llmStatusText = computed(() => hasLlmConfig.value ? `${form.llmType} · ${form.llmModel}` : '未配置');
const asrStatusText = computed(() => hasAsrConfig.value ? form.asrType : '未配置');
const ttsStatusText = computed(() => hasTtsConfig.value ? `${form.ttsType} · ${form.ttsVoice}` : '未配置');

onMounted(async () => {
  const config = await getUserConfig(userId.value);
  if (config) {
    form.llmType = config.llmType || 'mock';
    form.llmBaseUrl = config.llmBaseUrl || '';
    form.llmModel = config.llmModel || 'deepseek-chat';
    form.asrType = config.asrType || 'mock';
    form.asrBaseUrl = config.asrBaseUrl || '';
    form.ttsType = config.ttsType || 'mock';
    form.ttsBaseUrl = config.ttsBaseUrl || '';
    form.ttsVoice = config.ttsVoice || 'alloy';
  }
});

function resetForm() {
  form.llmType = 'mock';
  form.llmApiKey = '';
  form.llmBaseUrl = '';
  form.llmModel = 'deepseek-chat';
  form.asrType = 'mock';
  form.asrApiKey = '';
  form.asrBaseUrl = '';
  form.ttsType = 'mock';
  form.ttsApiKey = '';
  form.ttsBaseUrl = '';
  form.ttsVoice = 'alloy';
  message.value = '';
}

async function saveSettings() {
  isSaving.value = true;
  message.value = '';
  try {
    await saveUserConfig(userId.value, form);
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
.settings-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
}
.settings-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 32px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  background: #f1f5f9;
  border: none;
  border-radius: 8px;
  color: #475569;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.back-btn:hover { background: #e2e8f0; color: #1e293b; }
.header-content h1 { margin: 0; font-size: 20px; font-weight: 700; color: #1e293b; }
.subtitle { margin: 6px 0 0; font-size: 14px; color: #64748b; }

.settings-grid {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 24px;
  max-width: 1100px;
  margin: 0 auto;
  padding: 32px;
}
.main-section { display: flex; flex-direction: column; gap: 20px; }
.sidebar-section { display: flex; flex-direction: column; gap: 20px; }

.section-card {
  background: white;
  border-radius: 14px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  border: 1px solid #e2e8f0;
  overflow: hidden;
}
.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  border-bottom: 1px solid #f1f5f9;
  background: #fafafa;
}
.card-icon {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}
.ai-icon { background: linear-gradient(135deg, #fef3c7, #fde68a); }
.voice-icon { background: linear-gradient(135deg, #dbeafe, #bfdbfe); }
.sound-icon { background: linear-gradient(135deg, #fce7f3, #fbcfe8); }
.info-icon { background: linear-gradient(135deg, #e0e7ff, #c7d2fe); }
.status-icon { background: linear-gradient(135deg, #dcfce7, #bbf7d0); }
.card-title-group { display: flex; align-items: center; gap: 8px; flex: 1; }
.card-title { font-size: 15px; font-weight: 600; color: #1e293b; }
.configured-badge {
  padding: 2px 8px;
  background: #dcfce7;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
  color: #16a34a;
}
.card-body { padding: 18px 20px; }

.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.form-group { margin-bottom: 14px; }
.form-group:last-child { margin-bottom: 0; }
.form-group label { display: block; margin-bottom: 5px; font-size: 13px; font-weight: 500; color: #475569; }
.form-group input, .form-group select {
  width: 100%;
  padding: 9px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 13px;
  background: #fafafa;
  transition: all 0.2s;
  box-sizing: border-box;
}
.form-group input:focus, .form-group select:focus {
  outline: none;
  border-color: #3b82f6;
  background: white;
  box-shadow: 0 0 0 3px rgba(59,130,246,0.1);
}
.form-group input:disabled { background: #f1f5f9; color: #94a3b8; cursor: not-allowed; }

.tips-list { display: flex; flex-direction: column; gap: 12px; }
.tip-item { display: flex; gap: 8px; align-items: flex-start; }
.tip-num {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #eff6ff;
  color: #3b82f6;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 1px;
}
.tip-item span:last-child { font-size: 12px; color: #475569; line-height: 1.5; }

.status-list { display: flex; flex-direction: column; gap: 10px; }
.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: #f8fafc;
  border-radius: 8px;
}
.status-item.configured { background: #f0fdf4; }
.status-label { font-size: 12px; font-weight: 500; color: #334155; }
.status-value { font-size: 12px; color: #94a3b8; }
.status-item.configured .status-value { color: #22c55e; font-weight: 500; }

.settings-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 14px 32px;
  background: white;
  border-top: 1px solid #e2e8f0;
  box-shadow: 0 -1px 3px rgba(0,0,0,0.04);
}
.footer-content { display: flex; justify-content: flex-end; gap: 12px; max-width: 1100px; margin: 0 auto; }
.btn {
  padding: 11px 22px;
  border: none;
  border-radius: 9px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-primary { background: linear-gradient(135deg, #3b82f6, #2563eb); color: white; }
.btn-primary:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(59,130,246,0.3); }
.btn-primary:disabled { background: #94a3b8; cursor: not-allowed; }
.btn-secondary { background: #f1f5f9; color: #475569; }
.btn-secondary:hover { background: #e2e8f0; }

.toast {
  position: fixed;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  border-radius: 10px;
  color: white;
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  z-index: 1000;
}
.toast.success { background: linear-gradient(135deg, #22c55e, #16a34a); }
.toast.error { background: linear-gradient(135deg, #ef4444, #dc2626); }

@media (max-width: 900px) {
  .settings-grid { grid-template-columns: 1fr; }
  .sidebar-section { order: -1; display: grid; grid-template-columns: 1fr 1fr; }
}
@media (max-width: 600px) {
  .form-row { grid-template-columns: 1fr; }
  .sidebar-section { grid-template-columns: 1fr; }
  .settings-grid { padding: 16px; }
  .settings-header { padding: 14px 16px; }
}
</style>