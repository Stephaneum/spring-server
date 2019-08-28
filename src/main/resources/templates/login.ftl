<#-- @ftlvariable name="toast" type="de.stephaneum.backend.Toast" -->
<#-- @ftlvariable name="loginFailed" type="java.lang.Boolean" -->
<#-- @ftlvariable name="title" type="java.lang.String" -->

<#import "/spring.ftl" as spring/>
<#import "components/toaster.ftl" as toaster/>

<!DOCTYPE HTML>
<html lang="de">
<head>
    <title>${title}</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="<@spring.url '/static/img/favicon.png' />" />
    <link rel="apple-touch-icon" sizes="196x196" href="<@spring.url '/static/img/favicon.png' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/materialize.min.css' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/material-icons.css' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/style.css' />">
</head>

<body>

<div class="valign-wrapper" style="height: 100vh">
    <div style="margin: auto; max-width: 400px">

        <div class="center-align">
            <img src="<@spring.url '/static/img/logo-banner-green.png' />" style="width: 100%"/>
        </div>

        <div class="card" style="margin-top: 100px; padding: 5px 20px 40px 20px">
            <h5 class="center-align" style="padding: 10px; margin-bottom: 30px">${title}</h5>
            <#if loginFailed>
                <p class="center red-text" style="margin: 20px 0 20px 0">Login fehlgeschlagen</p>
            </#if>

            <form action="<@spring.url './login' />" method="POST">
                <div class="input-field">
                    <i class="material-icons prefix">vpn_key</i>
                    <label for="password">Passwort</label>
                    <input type="password" id="password" name="password" />
                </div>
                <div class="right-align">
                    <button type="submit" value="Login" class="btn waves-effect waves-light green darken-3">
                        Login
                        <i class="material-icons right">send</i>
                    </button>
                </div>
            </form>
        </div>

    </div>
</div>

<script src="<@spring.url '/static/js/jquery.min.js' />"></script>
<script src="<@spring.url '/static/js/materialize.min.js' />" ></script>
<script type="text/javascript">
    document.addEventListener('DOMContentLoaded', function() {
        M.AutoInit();
    });
</script>
<@toaster.render/>
</body>
</html>