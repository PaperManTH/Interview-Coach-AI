// WebSocket 客户端封装
export interface WebSocketMessage {
  id: string;
  type: string;
  sender: string;
  receiver: string;
  content: string;
  timestamp: number;
  sessionId?: string;
}

export interface WebSocketConfig {
  userId: string;
  url?: string;
  reconnectDelay?: number;
  maxReconnectAttempts?: number;
}

export type MessageHandler = (message: WebSocketMessage) => void;
export type ConnectionHandler = (connected: boolean) => void;

class WebSocketClient {
  private ws: WebSocket | null = null;
  private url: string;
  private userId: string;
  private reconnectDelay: number;
  private maxReconnectAttempts: number;
  private reconnectAttempts: number = 0;
  private messageHandlers: MessageHandler[] = [];
  private connectionHandlers: ConnectionHandler[] = [];
  private isConnected: boolean = false;
  private heartbeatInterval: number | null = null;
  private pendingMessages: WebSocketMessage[] = [];

  constructor(config: WebSocketConfig) {
    this.userId = config.userId;
    this.url = config.url || `/ws/interview?userId=${config.userId}`;
    this.reconnectDelay = config.reconnectDelay || 5000;
    this.maxReconnectAttempts = config.maxReconnectAttempts || 5;
  }

  connect(): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      return;
    }

    this.ws = new WebSocket(this.url);

    this.ws.onopen = () => {
      console.log('[WebSocket] 连接成功');
      this.isConnected = true;
      this.reconnectAttempts = 0;
      this.notifyConnection(true);
      this.startHeartbeat();
      this.sendPendingMessages();
    };

    this.ws.onmessage = (event: MessageEvent) => {
      try {
        const message: WebSocketMessage = JSON.parse(event.data);
        this.notifyMessage(message);
      } catch (error) {
        console.error('[WebSocket] 消息解析失败:', error);
      }
    };

    this.ws.onerror = (error: Event) => {
      console.error('[WebSocket] 连接错误:', error);
    };

    this.ws.onclose = (event: CloseEvent) => {
      console.log('[WebSocket] 连接关闭:', event.code, event.reason);
      this.isConnected = false;
      this.stopHeartbeat();
      this.notifyConnection(false);

      if (event.code !== 1000 && this.reconnectAttempts < this.maxReconnectAttempts) {
        this.scheduleReconnect();
      }
    };
  }

  disconnect(): void {
    if (this.ws) {
      this.ws.close(1000, '主动断开');
      this.ws = null;
    }
    this.stopHeartbeat();
  }

  send(message: WebSocketMessage): void {
    if (!this.isConnected || !this.ws) {
      this.pendingMessages.push(message);
      return;
    }

    try {
      const json = JSON.stringify(message);
      this.ws.send(json);
    } catch (error) {
      console.error('[WebSocket] 发送消息失败:', error);
      this.pendingMessages.push(message);
    }
  }

  sendText(content: string): void {
    const message: WebSocketMessage = {
      id: this.generateId(),
      type: 'TEXT',
      sender: this.userId,
      receiver: '',
      content,
      timestamp: Date.now()
    };
    this.send(message);
  }

  sendChat(content: string): void {
    const message: WebSocketMessage = {
      id: this.generateId(),
      type: 'CHAT',
      sender: this.userId,
      receiver: '',
      content,
      timestamp: Date.now()
    };
    this.send(message);
  }

  sendHeartbeat(): void {
    const message: WebSocketMessage = {
      id: this.generateId(),
      type: 'HEARTBEAT',
      sender: this.userId,
      receiver: '',
      content: 'ping',
      timestamp: Date.now()
    };
    this.send(message);
  }

  onMessage(handler: MessageHandler): void {
    this.messageHandlers.push(handler);
  }

  onConnection(handler: ConnectionHandler): void {
    this.connectionHandlers.push(handler);
  }

  removeMessageHandler(handler: MessageHandler): void {
    const index = this.messageHandlers.indexOf(handler);
    if (index > -1) {
      this.messageHandlers.splice(index, 1);
    }
  }

  removeConnectionHandler(handler: ConnectionHandler): void {
    const index = this.connectionHandlers.indexOf(handler);
    if (index > -1) {
      this.connectionHandlers.splice(index, 1);
    }
  }

  isConnectedState(): boolean {
    return this.isConnected;
  }

  getUserId(): string {
    return this.userId;
  }

  private notifyMessage(message: WebSocketMessage): void {
    this.messageHandlers.forEach(handler => handler(message));
  }

  private notifyConnection(connected: boolean): void {
    this.connectionHandlers.forEach(handler => handler(connected));
  }

  private scheduleReconnect(): void {
    this.reconnectAttempts++;
    console.log(`[WebSocket] 尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
    
    setTimeout(() => {
      this.connect();
    }, this.reconnectDelay);
  }

  private startHeartbeat(): void {
    this.stopHeartbeat();
    this.heartbeatInterval = window.setInterval(() => {
      this.sendHeartbeat();
    }, 30000); // 每30秒发送心跳
  }

  private stopHeartbeat(): void {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval);
      this.heartbeatInterval = null;
    }
  }

  private sendPendingMessages(): void {
    this.pendingMessages.forEach(message => this.send(message));
    this.pendingMessages = [];
  }

  private generateId(): string {
    return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }
}

// 创建单例实例
let instance: WebSocketClient | null = null;

export function createWebSocketClient(config: WebSocketConfig): WebSocketClient {
  instance = new WebSocketClient(config);
  return instance;
}

export function getWebSocketClient(): WebSocketClient | null {
  return instance;
}

export function destroyWebSocketClient(): void {
  if (instance) {
    instance.disconnect();
    instance = null;
  }
}
