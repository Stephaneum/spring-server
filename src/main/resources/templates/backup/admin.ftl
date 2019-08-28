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
            min-height: 70vh;
            margin: 0 50px 0 50px;
            text-align: center;

            display: flex;
            flex-direction: column;
            justify-content: space-between;
        }

        .info-card {
            text-align: center;
            background-color: #f1f8e9;
            color: black;
            border-radius: 20px;
            padding: 10px 10px 10px 10px;
            margin: 0 20px 0 20px;
            font-size: 1.2em;
        }

        .backup-btn {
            margin: 0 50px 50px 50px;
            font-size: 1.3em;
        }
    </style>
</head>

<body>

<div style="margin: auto; width: 1500px">
    <!-- title -->
    <div style="display: flex; justify-content: space-between; align-items: center; margin: 30px 100px 0 100px">
        <div style="display: flex; justify-content: space-between; align-items: center;">
            <img src="<@spring.url '/static/img/favicon.png' />" style="width: 50px"/>
            <h4 style="color: #396e3a; margin-left: 10px; padding-bottom: 5px">Backup-System</h4>
        </div>

        <a class="waves-effect waves-light btn green darken-3" href="<@spring.url './logout' />">
            <i class="material-icons right">exit_to_app</i>Abmelden</a>
    </div>

    <!-- cards -->
    <div style="display: flex; justify-content: center; margin-top: 50px">

        <!-- main card -->
        <div class="card backup-card" style="background-color: #dcedc8; text-align: center">

            <div>
                <h4 style="text-align: center">Allgemein</h4>
                <br>
                <div class="info-card">
                    <b>Nächste automatische Sicherung:</b>
                    <br>
                    Samstag, 31.August 2019, um 4:30 Uhr
                </div>
                <br>
                <div class="info-card">
                    <b>Sicherungspfad:</b>
                    <br>
                    ${backupLocation}
                </div>
                <br>
                <div class="info-card">
                    <b>Gesamtgröße der Sicherungen:</b>
                    <br>
                    3,7 GB
                </div>
            </div>

            <div>
                <a class="backup-btn waves-effect waves-light btn-large green darken-3" style="margin-bottom: 0" href="<@spring.url './backup-all' />">
                    <i class="material-icons left">photo_camera</i>Backup erstellen
                </a>
                <p>Hierbei werden Backups von <b>allen</b> Modulen erstellt.</p>
            </div>
        </div>

        <div class="card backup-card">
            <div>
                <h4 style="text-align: center">Homepage</h4>
                <br>
                <ul class="collection">
                    <#list backupHomepage as b>
                        <li class="collection-item">
                            <div style="display: flex; justify-content: space-between">
                                <span>${b.name}</span>
                                <span class="green-badge">${b.size}</span>
                            </div>

                        </li>
                    </#list>
                </ul>
            </div>
            <a class="backup-btn waves-effect waves-light btn-large green darken-3" href="<@spring.url './backup-homepage' />">
                <i class="material-icons left">photo_camera</i>Backup erstellen
            </a>
        </div>

        <div class="card backup-card">
            <div>
                <h4 style="text-align: center">Moodle</h4>
                <br>
                <ul class="collection">
                    <#list backupMoodle as b>
                        <li class="collection-item">
                            <div style="display: flex; justify-content: space-between">
                                <span>${b.name}</span>
                                <span class="green-badge">${b.size}</span>
                            </div>

                        </li>
                    </#list>
                </ul>
            </div>
            <a class="backup-btn waves-effect waves-light btn-large green darken-3" href="<@spring.url './backup-moodle' />">
                <i class="material-icons left">photo_camera</i>Backup erstellen
            </a>
        </div>
    </div>
</div>



<script src="<@spring.url '/static/js/jquery.min.js' />"></script>
<script src="<@spring.url '/static/js/materialize.min.js' />"></script>
<@loading.render/>
<@toaster.render/>
</body>
</html>