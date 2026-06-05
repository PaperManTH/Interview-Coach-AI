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
        <h1>用户设置</h1>
        <p class="subtitle">配置您的语音识别、语音合成和AI模型参数</p>
      </div>
    </div>

    <div class="settings-grid">
      <div class="main-section">
        <div class="section-card voice-section">
          <div class="card-header">
            <div class="card-icon voice-icon">🎙️</div>
            <div class="card-title">语音设置</div>
          </div>
          
          <div class="card-body">
            <div class="config-group">
              <div class="group-header">
                <h3>语音识别 (ASR)</h3>
                <span v-if="hasAsrConfig" class="configured-badge">已配置</span>
              </div>
              
              <div class="form-row">
                <div class="form-group">
                  <label>服务提供商</label>
                  <select v-model="localConfig.asrType">
                    <option value="mock">测试模式 (Mock)</option>
                    <option value="iflytek">讯飞 (iFlytek)</option>
                    <option value="openai">OpenAI Whisper</option>
                    <option value="azure">Azure Speech</option>
                  </select>
                </div>
                
                <div class="form-group">
                  <label>API Key</label>
                  <input
                    type="password"
                    v-model="localConfig.asrApiKey"
                    placeholder="输入您的 API Key"
                    :disabled="localConfig.asrType === 'mock'"
                  />
                </div>
              </div>
              
              <div class="form-group">
                <label>自定义 API 地址（可选）</label>
                <input
                  type="text"
                  v-model="localConfig.asrBaseUrl"
                  placeholder="留空使用默认地址"
                />
              </div>
            </div>

            <div class="divider"></div>

            <div class="config-group">
              <div class="group-header">
                <h3>语音合成 (TTS)</h3>
                <span v-if="hasTtsConfig" class="configured-badge">已配置</span>
              </div>
              
              <div class="form-row">
                <div class="form-group">
                  <label>服务提供商</label>
                  <select v-model="localConfig.ttsType">
                    <option value="mock">测试模式 (Mock)</option>
                    <option value="iflytek">讯飞 (iFlytek)</option>
                    <option value="openai">OpenAI TTS</option>
                    <option value="azure">Azure TTS</option>
                  </select>
                </div>
                
                <div class="form-group">
                  <label>API Key</label>
                  <input
                    type="password"
                    v-model="localConfig.ttsApiKey"
                    placeholder="输入您的 API Key"
                    :disabled="localConfig.ttsType === 'mock'"
                  />
                </div>
              </div>
              
              <div class="form-group">
                <label>语音类型</label>
                <select v-model="localConfig.ttsVoice">
                  <option value="female">女声</option>
                  <option value="male">男声</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        <div class="section-card ai-section">
          <div class="card-header">
            <div class="card-icon ai-icon">🤖</div>
            <div class="card-title">AI 模型设置</div>
          </div>
          
          <div class="card-body">
            <div class="group-header">
              <h3>大语言模型 (LLM)</h3>
              <span v-if="hasLlmConfig" class="configured-badge">已配置</span>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label>服务提供商</label>
                <select v-model="localConfig.llmType">
                  <option value="mock">测试模式 (Mock)</option>
                  <option value="openai">OpenAI</option>
                  <option value="azure">Azure OpenAI</option>
                  <option value="qianwen">阿里云千问</option>
                  <option value="doubao">字节跳动豆包</option>
                </select>
              </div>
              
              <div class="form-group">
                <label>API Key</label>
                <input
                  type="password"
                  v-model="localConfig.llmApiKey"
                  placeholder="输入您的 API Key"
                  :disabled="localConfig.llmType === 'mock'"
                />
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label>模型名称</label>
                <input
                  type="text"
                  v-model="localConfig.llmModel"
                  placeholder="例如: gpt-4o-mini"
                />
              </div>
              
              <div class="form-group">
                <label>自定义 API 地址</label>
                <input
                  type="text"
                  v-model="localConfig.llmBaseUrl"
                  placeholder="留空使用默认地址"
                />
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="sidebar-section">
        <div class="section-card quick-info">
          <div class="card-header">
            <div class="card-icon info-icon">ℹ️</div>
            <div class="card-title">快速提示</div>
          </div>
          
          <div class="card-body tips-list">
            <div class="tip-item">
              <div class="tip-icon">🔒</div>
              <div class="tip-content">API Key 安全存储，不会被分享</div>
            </div>
            <div class="tip-item">
              <div class="tip-icon">🛠️</div>
              <div class="tip-content">测试时使用 Mock 模式，无需 API Key</div>
            </div>
            <div class="tip-item">
              <div class="tip-icon">💡</div>
              <div class="tip-content">自定义地址支持本地部署</div>
            </div>
          </div>
        </div>

        <div class="section-card status-card">
          <div class="card-header">
            <div class="card-icon status-icon">✅</div>
            <div class="card-title">配置状态</div>
          </div>
          
          <div class="card-body status-list">
            <div class="status-item" :class="{ configured: hasAsrConfig }">
              <span class="status-label">语音识别</span>
              <span class="status-value">{{ hasAsrConfig ? '已配置' : '未配置' }}</span>
            </div>
            <div class="status-item" :class="{ configured: hasTtsConfig }">
              <span class="status-label">语音合成</span>
              <span class="status-value">{{ hasTtsConfig ? '已配置' : '未配置' }}</span>
            </div>
            <div class="status-item" :class="{ configured: hasLlmConfig }">
              <span class="status-label">AI 模型</span>
              <span class="status-value">{{ hasLlmConfig ? '已配置' : '未配置' }}</span>
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

    <div v-if="message" :class="['toast', messageType]">
      {{ message }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getUserConfig, saveUserConfig, type UserConfig, type UserConfigRequest } from '../api/config';

const router = useRouter();

const userId = ref('current-user');

const localConfig = reactive<UserConfigRequest>({
  asrType: 'mock',
  asrApiKey: '',
  asrBaseUrl: '',
  ttsType: 'mock',
  ttsApiKey: '',
  ttsBaseUrl: '',
  ttsVoice: 'female',
  llmType: 'mock',
  llmApiKey: '',
  llmBaseUrl: '',
  llmModel: 'gpt-4o-mini',
});

const isSaving = ref(false);
const message = ref('');
const messageType = ref<'success' | 'error'>('success');

const hasAsrConfig = computed(() => {
  return localConfig.asrType !== 'mock' && !!localConfig.asrApiKey;
});

const hasTtsConfig = computed(() => {
  return localConfig.ttsType !== 'mock' && !!localConfig.ttsApiKey;
});

const hasLlmConfig = computed(() => {
  return localConfig.llmType !== 'mock' && !!localConfig.llmApiKey;
});

onMounted(async () => {
  const config = await getUserConfig(userId.value);
  if (config) {
    localConfig.asrType = config.asrType || 'mock';
    localConfig.asrBaseUrl = config.asrBaseUrl || '';
    localConfig.ttsType = config.ttsType || 'mock';
    localConfig.ttsBaseUrl = config.ttsBaseUrl || '';
    localConfig.ttsVoice = config.ttsVoice || 'female';
    localConfig.llmType = config.llmType || 'mock';
    localConfig.llmBaseUrl = config.llmBaseUrl || '';
    localConfig.llmModel = config.llmModel || 'gpt-4o-mini';
  }
});

function resetForm() {
  localConfig.asrType = 'mock';
  localConfig.asrApiKey = '';
  localConfig.asrBaseUrl = '';
  localConfig.ttsType = 'mock';
  localConfig.ttsApiKey = '';
  localConfig.ttsBaseUrl = '';
  localConfig.ttsVoice = 'female';
  localConfig.llmType = 'mock';
  localConfig.llmApiKey = '';
  localConfig.llmBaseUrl = '';
  localConfig.llmModel = 'gpt-4o-mini';
  message.value = '';
}

async function saveSettings() {
  isSaving.value = true;
  message.value = '';

  try {
    const result = await saveUserConfig(userId.value, localConfig);
    if (result) {
      message.value = '配置保存成功！';
      messageType.value = 'success';
    } else {
      message.value = '配置保存失败，请重试';
      messageType.value = 'error';
    }
  } catch (error) {
    message.value = '保存失败：网络错误';
    messageType.value = 'error';
  } finally {
    isSaving.value = false;
    setTimeout(() => {
      message.value = '';
    }, 3000);
  }
}
</script>

<style scoped>
.settings-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  padding: 0;
}

.settings-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 32px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
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

.back-btn:hover {
  background: #e2e8f0;
  color: #1e293b;
}

.header-content {
  flex: 1;
}

.header-content h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.subtitle {
  margin: 6px 0 0;
  font-size: 14px;
  color: #64748b;
}

.settings-grid {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px;
}

.main-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  border: 1px solid #e2e8f0;
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
  background: #fafafa;
}

.card-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.voice-icon {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
}

.ai-icon {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
}

.info-icon {
  background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
}

.status-icon {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.card-body {
  padding: 20px;
}

.config-group {
  margin-bottom: 0;
}

.group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.group-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #334155;
}

.configured-badge {
  padding: 4px 10px;
  background: #dcfce7;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
  color: #16a34a;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #475569;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  background: #fafafa;
  transition: all 0.2s;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #3b82f6;
  background: white;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-group input:disabled {
  background: #f1f5f9;
  color: #94a3b8;
  cursor: not-allowed;
}

.divider {
  height: 1px;
  background: #e2e8f0;
  margin: 20px 0;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.tip-item {
  display: flex;
  gap: 10px;
}

.tip-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.tip-content {
  font-size: 13px;
  color: #475569;
}

.status-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: #f8fafc;
  border-radius: 8px;
}

.status-item.configured {
  background: #f0fdf4;
}

.status-label {
  font-size: 13px;
  font-weight: 500;
  color: #334155;
}

.status-value {
  font-size: 12px;
  font-weight: 600;
  color: #94a3b8;
}

.status-item.configured .status-value {
  color: #22c55e;
}

.settings-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 32px;
  background: white;
  border-top: 1px solid #e2e8f0;
  box-shadow: 0 -1px 3px rgba(0, 0, 0, 0.04);
}

.footer-content {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  max-width: 1200px;
  margin: 0 auto;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.btn-primary:disabled {
  background: #94a3b8;
  cursor: not-allowed;
}

.btn-secondary {
  background: #f1f5f9;
  color: #475569;
}

.btn-secondary:hover {
  background: #e2e8f0;
}

.toast {
  position: fixed;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  padding: 14px 24px;
  border-radius: 10px;
  color: white;
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
}

.toast.success {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
}

.toast.error {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

@media (max-width: 1024px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }
  
  .sidebar-section {
    order: -1;
    display: grid;
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 768px) {
  .settings-header {
    padding: 16px 20px;
    flex-wrap: wrap;
  }
  
  .header-content {
    width: 100%;
    margin-top: 12px;
  }
  
  .header-content h1 {
    font-size: 18px;
  }
  
  .settings-grid {
    padding: 20px;
    gap: 16px;
  }
  
  .sidebar-section {
    grid-template-columns: 1fr;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .settings-footer {
    padding: 12px 16px;
  }
  
  .btn {
    padding: 10px 18px;
  }
}

@media (max-width: 480px) {
  .card-header {
    padding: 14px 16px;
  }
  
  .card-body {
    padding: 16px;
  }
  
  .btn {
    padding: 10px 14px;
  }
}
</style>
