<#import "/spring.ftl" as spring/>
<#import "../components/loading.ftl" as loading/>
<#import "../components/toaster.ftl" as toaster/>
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
    <style>
        .backup-card {
            flex-basis: 400px;
            min-height: 600px;
            text-align: center;

            display: flex;
            flex-direction: column;
            justify-content: space-between;
        }

        .info-card {
            text-align: center;
            background-color: #dcedc8;
            color: black;
            border-radius: 20px;
            padding: 10px 10px 10px 10px;
            margin: 0 20px 0 20px;
            font-size: 1.2em;
            flex-shrink: 0;
        }
    </style>
</head>

<body>

<div style="display: flex; justify-content: center">
    <div style="width: 1400px; margin-bottom: 100px">
        <!-- title -->
        <div style="display: flex; justify-content: space-between; align-items: center; margin: 30px 0 0 100px">
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <img src="<@spring.url '/static/img/favicon.png' />" style="width: 50px"/>
                <h4 style="color: #396e3a; margin-left: 10px; padding-bottom: 5px">Backup-System</h4>
            </div>

            <a class="waves-effect waves-light btn teal darken-3" href="<@spring.url './logout' />">
                <i class="material-icons right">exit_to_app</i>Abmelden</a>
        </div>

        <!-- main card -->

        <div class="card" style="background-color: #f1f8e9; margin-top: 50px; padding: 20px">
            <div style="display: flex; justify-content: center; align-items: center;">

                <div class="info-card">
                    <b>Nächste automatische Sicherung:</b>
                    <br>
                    Samstag, 31.August 2019, um 4:30 Uhr
                </div>

                <div class="info-card">
                    <b>Sicherungspfad:</b>
                    <br>
                    ${backupLocation}
                </div>

                <div class="info-card">
                    <b>Gesamtgröße:</b>
                    <br>
                    3,7 GB
                </div>

                <div style="text-align: center; flex-basis: 500px">
                    <a class="backup-btn waves-effect waves-light btn-large green darken-3" style="margin-bottom: 0" href="<@spring.url './backup-all' />">
                        <i class="material-icons left">photo_camera</i>Backup erstellen
                    </a>
                    <p style="margin: 10px 0 0 0">Hierbei werden Backups von <b>allen</b> Modulen erstellt.</p>
                </div>

            </div>

        </div>

        <!-- cards -->
        <div style="display: flex; justify-content: space-between; margin-top: 50px">

            <#list modules as m>
                <div class="card backup-card">
                    <div>
                        <h4 style="text-align: center">${m.title}</h4>
                        <br>
                        <ul class="collection">
                            <#if m.backups?has_content>
                            <#list m.backups as b>
                                <li class="collection-item">
                                    <div style="display: flex; justify-content: space-between">
                                        <span>${b.name}</span>
                                        <span class="green-badge-light">${b.size}</span>
                                    </div>

                                </li>
                            </#list>
                            <#else>
                                <p>Keine Backups vorhanden</p>
                            </#if>
                        </ul>
                    </div>

                    <div>
                        <a class="waves-effect waves-light btn teal darken-3" href="${m.uploadURL}">
                            <i class="material-icons left">cloud_upload</i>Backup hochladen
                        </a>
                        <a class="waves-effect waves-light btn-large green darken-3"
                           style="margin: 30px 50px 50px 50px;font-size: 1.3em;" href="${m.backupURL}">
                            <i class="material-icons left">photo_camera</i>Backup erstellen
                        </a>

                    </div>
                </div>
            </#list>
        </div>
    </div>
</div>




<script src="<@spring.url '/static/js/jquery.min.js' />"></script>
<script src="<@spring.url '/static/js/materialize.min.js' />"></script>
<@loading.render/>
<@toaster.render/>
</body>
</html>