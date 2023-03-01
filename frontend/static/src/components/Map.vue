<template>
  <div id="app">
    <l-map
      ref="map"
      :zoom="zoom"
      :center="center"
      :useGlobalLeaflet="false"
    >
      <l-tile-layer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution="&amp;copy OpenStreetMap contributors"
      />
      <l-marker v-for="(point, index) in points" :key="index" :lat-lng="point">
        <l-popup>{{ point.name }}</l-popup>
      </l-marker>
    </l-map>
  </div>
</template>

<script>
import { LMap, LTileLayer, LMarker, LPopup } from "@vue-leaflet/vue-leaflet";
import { ref } from "vue";

export default {
  name: "map-component",
  components: {
    LMap,
    LTileLayer,
    LMarker,
    LPopup,
  },
  setup() {
    const zoom = ref(6);
    const center = ref([54.5, -125]);
    const points = ref([
      { lat: 49.2827, lng: -123.1207, name: "Vancouver" },
      { lat: 48.4284, lng: -123.3656, name: "Victoria" },
      { lat: 50.6745, lng: -120.3273, name: "Kamloops" },
      { lat: 53.9171, lng: -122.7497, name: "Prince George" },
    ]);

    return {
      zoom,
      center,
      points,
    };
  },
};
</script>

<style lang="scss">
#app {
  height: 100vh;
}
</style>
