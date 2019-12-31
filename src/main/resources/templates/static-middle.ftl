<#import "/spring.ftl" as spring/>
<#import "components/vue-loader.ftl" as vueLoader/>
<#import "components/menu.ftl" as menu/>
<#import "components/logos.ftl" as logos/>
<#import "components/quick-links.ftl" as quickLinks/>
<#import "components/footer.ftl" as footer/>

<!DOCTYPE HTML>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="/static/img/favicon.png" />
    <link rel="apple-touch-icon" sizes="196x196" href="/static/img/favicon.png">
    <link rel="stylesheet" type="text/css" href="/static/css/materialize.min.css">
    <link rel="stylesheet" type="text/css" href="/static/css/material-icons.css">
    <link rel="stylesheet" type="text/css" href="/static/css/style.css">
    <style>
        [v-cloak] {
            display: none;
        }
    </style>
    ${head}
</head>

<body>

<@vueLoader.blank/>
<div id="app" style="display: flex; align-items: center; flex-direction: column; min-height: 100vh" v-cloak>
    <nav-menu :menu="info.menu" :user="info.user" :plan="info.plan" :unapproved="info.unapproved"></nav-menu>

    <div id="main-row" class="row" style="min-height: 100vh; margin-top: 30px">

        <div class="row" style="margin: 0">
            <div class="col m4 l2 hide-on-small-only"></div>
            <div class="col s12 m8">
                <div style="display: flex; align-items: center;">
                    <a href="/" class="green-text" style="margin-left: 20px">Startseite</a>
                    <i class="material-icons">chevron_right</i>
                    <a href="#" class="green-text">${title}</a>
                </div>
            </div>
            <div class="col m2 hide-on-med-and-down"></div>
        </div>

        <div class="row">
            <div class="col m4 l2 hide-on-small-only">
                <br/><br/>
                <quick-links :plan="info.plan"></quick-links>
            </div>

            <div class="col s12 m8">
                <div class="card-panel white">
                    ${body}
                </div>
            </div>

            <div class="col m2 hide-on-med-and-down">
                <br/><br/>
                <logos :history="info.history" :eu-sa="info.euSa"></logos>
            </div>
        </div>
    </div>

    <stephaneum-footer :copyright="info.copyright"></stephaneum-footer>
</div>

<script src="/static/js/materialize.min.js" ></script>
<script src="/static/js/axios.min.js" ></script>
<script src="/static/js/vue.min.js" ></script>
<@menu.render/>
<@logos.render/>
<@quickLinks.render/>
<@footer.render/>
<script type="text/javascript">
    M.AutoInit();

    var app = new Vue({
        el: '#app',
        data: {
            info: {}
        },
        mounted: function() {
            axios.get('/api/info')
                .then((res) => {
                    if(res.data) {
                        this.info = res.data;
                    } else {
                        M.toast({html: 'Interner Fehler.'});
                    }
                });
        }
    });
</script>
</body>
</html>