/**
 * WebSocket 组合式函数
 */
import { ref, onMounted, onUnmounted } from 'vue';
import { wsService } from '@/services/websocket';
import type { MessageHandler, StatusHandler, ErrorHandler } from '@/services/websocket';

export function useWebSocket() {
  const isConnected = ref(false);
  const status = ref('disconnected');
  const error = ref<string | null>(null);
  
  let messageHandler: MessageHandler | null = null;
  let statusHandler: StatusHandler | null = null;
  let errorHandler: ErrorHandler | null = null;
  let heartbeatTimer: ReturnType<typeof setInterval> | null = null;

  const connect = async (userId: string) => {
    try {
      error.value = null;
      
      wsService.onMessage((data) => {
        messageHandler?.(data);
      });
      
      wsService.onStatusChange((newStatus) => {
        status.value = newStatus;
        statusHandler?.(newStatus);
      });
      
      wsService.onError((err) => {
        error.value = err;
        errorHandler?.(err);
      });

      await wsService.connect(userId);
      isConnected.value = true;
      status.value = 'connected';
      
      // 启动心跳
      startHeartbeat();
    } catch (e) {
      isConnected.value = false;
      status.value = 'error';
      error.value = e instanceof Error ? e.message : '连接失败';
    }
  };

  const disconnect = () => {
    stopHeartbeat();
    wsService.disconnect();
    isConnected.value = false;
    status.value = 'disconnected';
  };

  const sendText = (text: string) => {
    if (isConnected.value) {
      wsService.sendText(text);
    }
  };

  const sendAudio = (audioData: string) => {
    if (isConnected.value) {
      wsService.sendAudio(audioData);
    }
  };

  const onMessage = (handler: MessageHandler) => {
    messageHandler = handler;
  };

  const onStatusChange = (handler: StatusHandler) => {
    statusHandler = handler;
  };

  const onError = (handler: ErrorHandler) => {
    errorHandler = handler;
  };

  const startHeartbeat = () => {
    heartbeatTimer = setInterval(() => {
      if (wsService.isConnected()) {
        wsService.sendHeartbeat();
      }
    }, 30000);
  };

  const stopHeartbeat = () => {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer);
      heartbeatTimer = null;
    }
  };

  onUnmounted(() => {
    stopHeartbeat();
  });

  return {
    isConnected,
    status,
    error,
    connect,
    disconnect,
    sendText,
    sendAudio,
    onMessage,
    onStatusChange,
    onError,
  };
}
