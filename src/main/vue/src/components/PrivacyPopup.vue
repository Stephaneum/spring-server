<template>
    <div id="privacy-popup" class="z-depth-2 light-green lighten-4" :style="privacyStyle"
         style="animation: privacyAnim 1s ease-in-out 1 forwards; position:fixed; width:80%; left: 10vw; bottom: -100px; z-index:2000; padding: 20px; align-items: center; justify-content: space-between">
        <div style="display: flex; align-items: center">
            <i class="material-icons" style="font-size: 30px; margin-right: 10px">info</i>
            <span>
                Mit dem Zugang akzeptieren Sie die
                <a href="/impressum" class="green-text text-darken-4" target="_blank" style="text-decoration: underline">datenschutzrechtlichen Bestimmungen</a>
                sowie die Verwendung von Cookies auf unserer Webplattform.
            </span>
        </div>
        <a @click="accept" class="waves-effect waves-light btn green darken-4" style="margin-left: 15px; width: 100px">OK</a>
    </div>
</template>

<script>
export default {
    name: 'PrivacyPopup',
    data: () => ({
        privacyStyle: { 'display': 'none' }
    }),
    methods: {
        accept() {
            const time = 60 * 60 * 24 * 30; // 1 month
            document.cookie = 'privacyAccepted=1;path=/;SameSite=Lax;Max-Age=' + time;
            this.privacyStyle = { 'display': 'none' };
        }
    },
    mounted() {
        if(document.cookie.indexOf('privacyAccepted=1') === -1) {
            setTimeout(() => this.privacyStyle = { 'display': 'flex' }, 1000);
        } else {
            console.log('Privacy already accepted')
        }
    }
}
</script>

<style>
    @keyframes privacyAnim {
        0% {
            bottom: -100px;
        }
        100% {
            bottom: 20px;
        }
    }

    @media only screen and (max-width: 600px) {
        #privacy-popup {
            height:180px;
        }
    }

    @media only screen and (min-width: 601px) and (max-width: 1200px) {
        #privacy-popup {
            height:80px;
        }
    }

    @media only screen and (min-width: 1201px) {
        #privacy-popup {
            height:60px;
        }
    }
</style>