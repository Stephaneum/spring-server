<template>
    <div style="text-align: center">
        <div style="display:inline-block;border: 2px solid white;" class="z-depth-1">
            <l-map ref="map" style="width: 400px; height: 300px" :zoom="17" :center.sync="center">
                <l-tile-layer :url="url" :attribution="attribution" />
                <l-marker ref="markerA" :lat-lng="posA" @add="$nextTick(() => $event.target.openPopup())">
                    <l-popup>
                        Kreativwerkstatt Aschersleben
                    </l-popup>
                </l-marker>
            </l-map>
        </div>
        <div class="remove-margin-left" style="display:inline-block;vertical-align:top;margin-left:30px">
            <div id="standort-title">
                <br/>
                <h5>Unser Standort</h5>
                <br/>
            </div>
            <div @click="toA" class="card-panel location" :class="active === posA ? ['location-active'] : []">
                <b>Kreativwerkstatt Aschersleben</b><br/>
                Wilhelmstraße 21-23<br/>
                06449 Aschersleben
            </div>
        </div>
    </div>
</template>

<script>
    import { Icon, latLng } from "leaflet";
    import { LMap, LTileLayer, LMarker, LPopup } from "vue2-leaflet";

    const posA = latLng(51.7534381, 11.4622746);

    // fix marker icon
    delete Icon.Default.prototype._getIconUrl;
    Icon.Default.mergeOptions({
        iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
        iconUrl: require('leaflet/dist/images/marker-icon.png'),
        shadowUrl: require('leaflet/dist/images/marker-shadow.png'),
    });

export default {
    name: 'Locations',
    components: {
        LMap, LTileLayer, LMarker, LPopup
    },
    data: () => ({
        center: posA,
        url: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
        attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors',
        posA: posA,
        active: posA
    }),
    methods: {
        toA() {
            this.active = posA;
            this.$refs.map.mapObject.flyTo(posA);
            this.$refs.markerA.mapObject.openPopup();
        }
    },
    mounted() {
        this.$refs.map.mapObject.scrollWheelZoom.disable();
    }
}
</script>

<style scoped>
    .location {
        display:inline-block;
        background-color: white;
        cursor: pointer;
        max-width: 300px;
    }

    .location.location-active {
        background-color: #A9D9EA;
        cursor: pointer;
    }

    .location:hover {
        background-color: #7FC5DF;
    }
</style>