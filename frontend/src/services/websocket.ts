/**
 * WebSocket 服务
 */
import { WS_BASE_URL, API_ENDPOINTS } from '@/constants';

export type MessageHandler = (data: any) => void;
export type StatusHandler = (status: string) => void;
export type ErrorHandler = (error: string) => void;

export class WebSocketService {
  private ws: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000;
  private userId: string = '';
  private messageHandler: MessageHandler | null = null;
  private statusHandler: StatusHandler | null = null;
  private errorHandler: ErrorHandler | null = null;

  /**
   * 连接到 WebSocket
   */
  connect(userId: string): Promise<void> {
    return new Promise((resolve, reject) => {
      this.userId = userId;
      const url = `${WS_BASE_URL}${API_ENDPOINTS.WS_INTERVIEW}?userId=${userId}`;
      
      console.log('[WS] 连接中:', url);
      this.ws = new WebSocket(url);

      this.ws.onopen = () => {
        console.log('[WS] 连接成功');
        this.reconnectAttempts = 0;
        resolve();
      };

      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          this.handleMessage(data);
        } catch (e) {
          console.error('[WS] 消息解析失败:', e);
        }
      };

      this.ws.onerror = (error) => {
        console.error('[WS] 连接错误:', error);
        this.errorHandler?.('WebSocket 连接错误');
        reject(error);
      };

      this.ws.onclose = (event) => {
        console.log('[WS] 连接关闭: code=', event.code, 'reason=', event.reason);
        if (event.code !== 1000) {
          this.attemptReconnect();
        }
      };
    });
  }

  /**
   * 处理收到的消息
   */
  private handleMessage(data: any) {
    switch (data.type) {
      case 'status':
        this.statusHandler?.(data.status);
        break;
      case 'text':
      case 'message':
        this.messageHandler?.(data);
        break;
      case 'error':
        this.errorHandler?.(data.message);
        break;
      default:
        this.messageHandler?.(data);
    }
  }

  /**
   * 发送文本消息
   */
  sendText(text: string) {
    this.send({ type: 'text', content: text });
  }

  /**
   * 发送音频数据
   */
  sendAudio(audioData: string) {
    this.send({ type: 'audio', data: audioData });
  }

  /**
   * 发送心跳
   */
  sendHeartbeat() {
    this.send({ type: 'heartbeat', timestamp: Date.now() });
  }

  /**
   * 发送消息
   */
  private send(data: object) {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data));
    }
  }

  /**
   * 尝试重连
   */
  private attemptReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.log('[WS] 最大重连次数已达，放弃重连');
      return;
    }

    this.reconnectAttempts++;
    const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1);
    console.log(`[WS] ${delay}ms 后尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

    setTimeout(() => {
      this.connect(this.userId).catch(() => {});
    }, delay);
  }

  /**
   * 断开连接
   */
  disconnect() {
    this.ws?.close(1000, '用户主动断开');
    this.ws = null;
  }

  /**
   * 设置消息处理器
   */
  onMessage(handler: MessageHandler) {
    this.messageHandler = handler;
  }

  /**
   * 设置状态处理器
   */
  onStatusChange(handler: StatusHandler) {
    this.statusHandler = handler;
  }

  /**
   * 设置错误处理器
   */
  onError(handler: ErrorHandler) {
    this.errorHandler = handler;
  }

  /**
   * 检查是否已连接
   */
  isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN;
  }
}

// 导出单例
export const wsService = new WebSocketService();
