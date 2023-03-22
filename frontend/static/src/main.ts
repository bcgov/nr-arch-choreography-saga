/**
 * Vue3 Main script
 */

// Load vue core
import {createApp} from 'vue';
import router from './router';
import store from './store';

// @ts-ignore
// Load Vuetify
import vuetify from './plugins/vuetify';

// Load Layout vue.
import App from './App.vue';
import {useSocketStoreWithOut} from "@/store/socket-store";
// eslint-disable-next-line import/no-unresolved
import vuenativesocket from 'vue-native-websocket-vue3';

const socketStoreWithOut = useSocketStoreWithOut();
/** Register Vue */
const vue = createApp(App);
vue.use(
  vuenativesocket,
  `wss://${window.location.hostname}'/api/socket`, // 'ws://localhost:3000/api/socket'
  {
    store: socketStoreWithOut,
    format: "json",
    connectManually: true,
    reconnection: true,
    reconnectionAttempts: 500,
    reconnectionDelay: 3000
  }
);
vue.use(router);
vue.use(store);
vue.use(vuetify);

// Run!
router.isReady().then(() => {
  vue.mount('#app');
});
export default vue;
