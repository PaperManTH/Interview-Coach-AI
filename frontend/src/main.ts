import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import './assets/styles/main.css';

// 应用入口：注册 Pinia 状态管理、Vue Router，挂载到 #app
const app = createApp(App);
app.use(createPinia());
app.use(router);
app.mount('#app');
