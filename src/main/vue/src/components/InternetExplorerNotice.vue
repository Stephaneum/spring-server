<template>
    <div style="padding: 10px; text-align: center" :style="ieStyle">
        Die Darstellung könnte fehlerhaft sein, weil Sie den Internet Explorer verwenden. Bitte wechseln Sie zu den Alternativen (z.B. Chrome, Firefox, Safari, Edge, etc.).
    </div>
</template>

<script>
export default {
    name: 'InternetExplorerNotice',
    data: () => ({
       ieStyle: { 'display': 'none' }
    }),
    mounted() {
        const ua = window.navigator.userAgent;
        const isIE = /MSIE|Trident/.test(ua);

        if ( isIE ) {
            this.ieStyle = { 'display': 'block' }
        }
    }
}
</script>

<style>

</style>