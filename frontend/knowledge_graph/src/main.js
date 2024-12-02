import Vue from 'vue';
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import App from './App.vue';
import axios from 'axios';
import router from './router/index';

Vue.use(ElementUI);
Vue.prototype.$axios = axios;
Vue.prototype.$isVisable = true;

new Vue({
  router,
  render: h => h(App),
}).$mount('#app');
