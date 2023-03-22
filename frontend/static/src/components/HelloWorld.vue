<script setup lang="ts">
/** Props */
defineProps<{
  msg: string;
}>();

</script>

<script lang="ts">
import {getCurrentInstance, defineComponent, type ComponentInternalInstance} from "vue";

export default defineComponent({
  name: "HelloWorld",
  mounted() {
    const {proxy} = getCurrentInstance() as ComponentInternalInstance;
    setTimeout(() => {
      if (proxy == null) return;
      // @ts-ignore
      proxy.$connect();
    }, 100);
  }
});
</script>
<template>
  <v-container class="fill-height">

  </v-container>
</template>

<style scoped>
.logo:hover {
  will-change: filter;
  filter: drop-shadow(0 0 1em #2196f3aa);
}
</style>
