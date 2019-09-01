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

        .action-btn {
            margin: 0 10px 0 10px;
        }
    </style>
</head>

<body>

<div style="display: flex; justify-content: center">
    <div style="width: 1300px; margin-bottom: 100px">
        <!-- title -->
        <div style="display: flex; justify-content: space-between; align-items: center; margin: 30px 50px 0 50px">
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <img src="<@spring.url '/static/img/favicon.png' />" style="width: 50px"/>
                <h4 style="color: #396e3a; margin-left: 10px; padding-bottom: 5px">Backup-System</h4>
            </div>

            <a class="waves-effect waves-light btn teal darken-3" href="<@spring.url './logout' />">
                <i class="material-icons right">exit_to_app</i>Abmelden</a>
        </div>

        <!-- main card -->

        <div class="card" style="background-color: #f1f8e9; margin-top: 50px; padding: 20px;display: flex; justify-content: center; align-items: center;">
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
                ${totalSize}
            </div>

            <div style="text-align: center; flex-basis: 500px">
                <a class="backup-btn waves-effect waves-light btn-large green darken-3" style="margin: 5px 0 0 0" href="<@spring.url './backup' />">
                    <i class="material-icons left">photo_camera</i>Backup erstellen
                </a>
                <p style="margin: 10px 0 0 0">Hierbei werden Backups von <b>allen</b> Modulen erstellt.</p>
            </div>
        </div>

        <!-- cards -->
        <div style="display: flex; justify-content: space-between; margin-top: 50px">

            <#list modules as m>
                <div class="card backup-card">
                    <div>
                        <h4 style="text-align: center">${m.title}</h4>
                        <br>
                        <#if m.backups?has_content>
                        <ul class="collapsible" style="box-shadow: none !important;">
                            <#list m.backups as b>
                                <li>
                                    <div class="collapsible-header">
                                        <div style="display: flex; justify-content: space-between; width: 100%;">
                                            <span>${b.name}</span>
                                            <span class="green-badge-light">${b.size}</span>
                                        </div>
                                    </div>
                                    <div class="collapsible-body">
                                        <div style="display: flex; justify-content: center; align-items: center">
                                            <i class="material-icons" style="display: inline-block; margin: 0 10px 10px 0; font-size: 2em;">subdirectory_arrow_right</i>
                                            <span style="margin-right: 10px">Aktionen:</span>
                                            <a class="action-btn tooltipped waves-effect waves-light btn green darken-3" data-tooltip="Download" data-position="bottom"
                                               href="<@spring.url './download/' + m.code + '/' + b.name />">
                                                <i class="material-icons">arrow_downward</i></a>
                                            <a class="action-btn tooltipped waves-effect waves-light btn yellow darken-3" data-tooltip="Wiederherstellen" data-position="bottom"
                                               href="<@spring.url './restore/' + m.code + '/' + b.name />">
                                                <i class="material-icons">restore</i></a>
                                            <a class="action-btn tooltipped waves-effect waves-light btn red darken-3" data-tooltip="Löschen" data-position="bottom"
                                               href="<@spring.url './delete/' + m.code + '/' + b.name />">
                                                <i class="material-icons">delete</i></a>
                                        </div>

                                    </div>
                                </li>
                            </#list>
                        </ul>
                        <#else>
                        <div>
                            <p class="green-badge-light" style="display: inline-block; font-size: 1em">Keine Backups vorhanden</p>
                        </div>
                        </#if>
                    </div>

                    <div>
                        <a class="waves-effect waves-light btn teal darken-3" href="upload-${m.code}">
                            <i class="material-icons left">cloud_upload</i>Backup hochladen
                        </a>
                        <a class="waves-effect waves-light btn-large green darken-3"
                           style="margin: 30px 50px 50px 50px;font-size: 1.3em;" href="backup-${m.code}">
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
<script type="text/javascript">

    document.addEventListener('DOMContentLoaded', function () {
        M.AutoInit();
    });

</script>
<@loading.render/>
<@toaster.render/>
</body>
</html>