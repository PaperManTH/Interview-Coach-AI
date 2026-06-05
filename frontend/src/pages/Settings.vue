<template>
  <div class="settings-container">
    <div class="settings-header">
      <div class="header-top">
        <button class="back-btn" @click="router.push('/')">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="m15 18-6-6 6-6"/>
          </svg>
          <span>返回</span>
        </button>
      </div>
      <h1>用户设置</h1>
      <p class="subtitle">配置您的语音识别、语音合成和AI模型参数</p>
    </div>

    <div class="settings-content">
      <!-- ASR 设置 -->
      <div class="config-section">
        <div class="section-header">
          <h2>语音识别 (ASR)</h2>
          <span v-if="hasAsrConfig" class="configured-badge">已配置</span>
        </div>
        
        <div class="form-group">
          <label for="asr-type">服务提供商</label>
          <select id="asr-type" v-model="localConfig.asrType">
            <option value="mock">测试模式 (Mock)</option>
            <option value="iflytek">讯飞 (iFlytek)</option>
            <option value="openai">OpenAI Whisper</option>
            <option value="azure">Azure Speech</option>
          </select>
        </div>

        <div class="form-group">
          <label for="asr-api-key">API Key</label>
          <input
            id="asr-api-key"
            type="password"
            v-model="localConfig.asrApiKey"
            placeholder="输入您的 API Key"
            :disabled="localConfig.asrType === 'mock'"
          />
          <div v-if="localConfig.asrType !== 'mock'" class="hint">
            测试模式无需 API Key
          </div>
        </div>

        <div class="form-group">
          <label for="asr-base-url">自定义 API 地址（可选）</label>
          <input
            id="asr-base-url"
            type="text"
            v-model="localConfig.asrBaseUrl"
            placeholder="留空使用默认地址"
          />
        </div>
      </div>

      <!-- TTS 设置 -->
      <div class="config-section">
        <div class="section-header">
          <h2>语音合成 (TTS)</h2>
          <span v-if="hasTtsConfig" class="configured-badge">已配置</span>
        </div>
        
        <div class="form-group">
          <label for="tts-type">服务提供商</label>
          <select id="tts-type" v-model="localConfig.ttsType">
            <option value="mock">测试模式 (Mock)</option>
            <option value="iflytek">讯飞 (iFlytek)</option>
            <option value="openai">OpenAI TTS</option>
            <option value="azure">Azure TTS</option>
          </select>
        </div>

        <div class="form-group">
          <label for="tts-api-key">API Key</label>
          <input
            id="tts-api-key"
            type="password"
            v-model="localConfig.ttsApiKey"
            placeholder="输入您的 API Key"
            :disabled="localConfig.ttsType === 'mock'"
          />
        </div>

        <div class="form-group">
          <label for="tts-voice">语音类型</label>
          <select id="tts-voice" v-model="localConfig.ttsVoice">
            <option value="female">女声</option>
            <option value="male">男声</option>
          </select>
        </div>
      </div>

      <!-- LLM 设置 -->
      <div class="config-section">
        <div class="section-header">
          <h2>AI 模型 (LLM)</h2>
          <span v-if="hasLlmConfig" class="configured-badge">已配置</span>
        </div>
        
        <div class="form-group">
          <label for="llm-type">服务提供商</label>
          <select id="llm-type" v-model="localConfig.llmType">
            <option value="mock">测试模式 (Mock)</option>
            <option value="openai">OpenAI</option>
            <option value="azure">Azure OpenAI</option>
            <option value="qianwen">阿里云千问</option>
            <option value="doubao">字节跳动豆包</option>
          </select>
        </div>

        <div class="form-group">
          <label for="llm-api-key">API Key</label>
          <input
            id="llm-api-key"
            type="password"
            v-model="localConfig.llmApiKey"
            placeholder="输入您的 API Key"
            :disabled="localConfig.llmType === 'mock'"
          />
        </div>

        <div class="form-group">
          <label for="llm-model">模型名称</label>
          <input
            id="llm-model"
            type="text"
            v-model="localConfig.llmModel"
            placeholder="例如: gpt-4o-mini"
          />
        </div>

        <div class="form-group">
          <label for="llm-base-url">自定义 API 地址（可选）</label>
          <input
            id="llm-base-url"
            type="text"
            v-model="localConfig.llmBaseUrl"
            placeholder="留空使用默认地址"
          />
        </div>
      </div>
    </div>

    <div class="settings-footer">
      <button class="btn btn-secondary" @click="resetForm">重置</button>
      <button class="btn btn-primary" @click="saveSettings" :disabled="isSaving">
        <span v-if="isSaving">保存中...</span>
        <span v-else>保存配置</span>
      </button>
    </div>

    <div v-if="message" :class="['message', messageType]">
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
.settings-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.settings-header {
  margin-bottom: 30px;
}

.header-top {
  margin-bottom: 16px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: transparent;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  color: #64748b;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.back-btn:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
  color: #334155;
}

.settings-header h1 {
  font-size: 24px;
  color: #333;
  margin: 0 0 8px 0;
}

.subtitle {
  color: #666;
  font-size: 14px;
}

.settings-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.config-section {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.section-header h2 {
  font-size: 18px;
  color: #333;
  margin: 0;
}

.configured-badge {
  background: #28a745;
  color: white;
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 12px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  font-size: 14px;
  color: #555;
  margin-bottom: 6px;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #007bff;
}

.form-group input:disabled {
  background: #e9ecef;
  cursor: not-allowed;
}

.hint {
  font-size: 12px;
  color: #888;
  margin-top: 4px;
}

.settings-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 30px;
}

.btn {
  padding: 10px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #0056b3;
}

.btn-primary:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.btn-secondary {
  background: #e9ecef;
  color: #333;
}

.btn-secondary:hover {
  background: #dee2e6;
}

.message {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  border-radius: 8px;
  color: white;
  font-size: 14px;
  z-index: 1000;
}

.message.success {
  background: #28a745;
}

.message.error {
  background: #dc3545;
}
</style>
