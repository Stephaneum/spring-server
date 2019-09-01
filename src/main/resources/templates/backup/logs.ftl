<#import "/spring.ftl" as spring/>
<#setting locale="de_DE">
<#setting number_format="computer">

<!DOCTYPE HTML>
<html lang="de">
<head>
    <title>Backup</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="<@spring.url '/static/img/favicon.png' />"/>
    <link rel="apple-touch-icon" sizes="196x196" href="<@spring.url '/static/img/favicon.png' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/materialize.min.css' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/material-icons.css' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/style.css' />">
</head>

<body>

<div style="display: flex; justify-content: center">
    <div style="width: 1300px; margin-bottom: 100px">
        <!-- title -->
        <div style="display: flex; justify-content: space-between; align-items: center; margin: 30px 50px 100px 50px">
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <img src="<@spring.url '/static/img/favicon.png' />" style="width: 50px"/>
                <h4 style="color: #396e3a; margin-left: 10px; padding-bottom: 5px">Backup-System</h4>
            </div>

            <a class="waves-effect waves-light btn teal darken-3" href="<@spring.url './logout' />">
                <i class="material-icons right">exit_to_app</i>Abmelden</a>
        </div>

        <div id="log-container" class="card" style="min-height: 60vh; padding: 30px">
            <p style="margin: 0 0 20px 0; font-size: 2em">Konsolenausgabe</p>
            <div id="log">
                ${logs}
            </div>
            <a id="log-back-btn" class="waves-effect waves-light btn green darken-3" style="display: none; margin-top: 30px" href="<@spring.url './admin' />">
                <i class="material-icons left">arrow_back</i>Zurück</a>
        </div>
    </div>

</div>
<script src="<@spring.url '/static/js/jquery.min.js' />"></script>
<script src="<@spring.url '/static/js/materialize.min.js' />"></script>
<script src="<@spring.url '/static/js/backup-logs.js' />"></script>
<script type="text/javascript">
    document.addEventListener('DOMContentLoaded', function () {
        M.AutoInit();
        initLogs('<@spring.url './log-data'/>')
    });
</script>
</body>
</html>