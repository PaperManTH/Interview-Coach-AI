import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router, { injectPinia as injectPiniaToRouter } from './router';
import './assets/styles/main.css';

const app = createApp(App);
const pinia = createPinia();
app.use(pinia);
app.use(router);

// 路由守卫需要同一个 Pinia 实例
injectPiniaToRouter(pinia);

app.mount('#app');
