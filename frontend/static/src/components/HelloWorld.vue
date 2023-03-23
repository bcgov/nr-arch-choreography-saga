<script setup lang="ts">/** Props */
defineProps<{
  msg: string;
}>();
import {useSocketStore} from '../store';

const socketStore = useSocketStore();
</script>

<script lang="ts">
import {
  getCurrentInstance,
  defineComponent,
  type ComponentInternalInstance,
} from 'vue';

export default defineComponent({
  name: 'HelloWorld',
  mounted() {
    const {proxy} = getCurrentInstance() as ComponentInternalInstance;
    setTimeout(() => {
      if (proxy == null) return;
      // @ts-ignore
      proxy.$connect();
    }, 100);
  },
  data() {
    return {
      headers: ['id', 'type', 'source', 'payloadVersion', 'data', 'subject', 'createdBy', 'updatedBy', 'createdAt', 'updatedAt'],
      items: [],
    }
  }
});
</script>
<template>
  <v-container class="fill-height" style="display: flex; align-items: center; justify-content: center;">
    <table v-if="socketStore.getMessages.length > 0">
      <thead>
      <th v-for="key in Object.keys(socketStore.getMessages[0])" v-bind:key="key">{{ key }}</th>
      </thead>
      <tbody>
      <tr v-for="data in socketStore.getMessages" v-bind:key="data">
        <td v-for="cell in Object.values(data)" v-bind:key="cell">{{ cell }}</td>
      </tr>

      </tbody>
    </table>
  </v-container>
</template>

<style scoped>
table {
  border-collapse: collapse;
  width: 80%;
  border: 0.5px solid #ccc;
}

th,
td {
  text-align: left;
  padding: 2px;
  color: #333;
  border-right: 0.5px solid #ccc;
}
th:last-child,
td:last-child {
  border-right: none;
}

tr:nth-child(even) {
  background-color: #f2f2f2;
}

tr:not(:last-child) {
  border-bottom: 2px solid #ddd;
}
</style>
