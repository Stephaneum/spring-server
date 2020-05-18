<template>
    <div class="card" style="background-color: #dcedc8">
        <div class="card-content">
            <div style="display: table; width: 100%; table-layout: fixed;">
                <div style="display: table-cell; width: 80px; vertical-align: top" class="hide-on-small-only">
                    <b>Liveticker:</b>
                </div>
                <div class="marquee" style="display: table-cell; overflow: hidden">
                    <span :style="{ 'animation-duration': speed+'s' }">
                        {{ text }} {{ text }} {{ text }} {{ text }} {{ text }}
                    </span>
                </div>
            </div>
        </div>
    </div>

</template>

<script>
export default {
    name: 'LiveTicker',
    props: ['text'],
    computed: {
        speed() {
            if(!this.text)
                return 1;

            return Math.max(this.text.length / 10, 5);
        }
    }
}
</script>

<style>
    .marquee {
        max-width: 100vw; /* iOS braucht das */
        white-space: nowrap;
        overflow: hidden;
        /* hier evtl. noch font-size, color usw. */
    }

    .marquee span {
        display: inline-block;
        animation: marquee 10s linear infinite;
    }

    /* Optional: mouseover (oder Tipp auf dem Touchscreen) pausiert die Laufschrift */
    .marquee span:hover {
        animation-play-state: paused
    }

    /* Make it move */
    @keyframes marquee {
        0%   { transform: translateX(0); }
        100% { transform: translateX(-20%); }
    }
</style>