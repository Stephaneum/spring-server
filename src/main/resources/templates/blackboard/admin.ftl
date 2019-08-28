<#import "/spring.ftl" as spring/>
<#import "../components/loading.ftl" as loading/>
<#import "../components/toaster.ftl" as toaster/>
<#setting locale="de_DE">
<#setting number_format="computer">

<!DOCTYPE HTML>
<html lang="de">
<head>
    <title>Blackboard</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="<@spring.url '/static/img/favicon.png' />"/>
    <link rel="apple-touch-icon" sizes="196x196" href="<@spring.url '/static/img/favicon.png' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/materialize.min.css' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/material-icons.css' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/style.css' />">
</head>

<body>

<div class="valign-wrapper" style="height: 100vh">
    <div style="margin: auto">
        <div class="center-align" style="max-width: 400px; margin: auto; margin-top: 50px">
            <img src="<@spring.url '/static/img/logo-banner-green.png' />" style="width: 100%"/>
        </div>

        <div class="row">
            <div class="col m6 left-align">
                <a class="waves-effect waves-light btn green darken-3" href="<@spring.url './' />" target="_blank">
                    <i class="material-icons right">star</i>Live-Version</a>
            </div>
            <div class="col m6 right-align">
                <a class="waves-effect waves-light btn green darken-3" href="<@spring.url './logout' />">
                    <i class="material-icons right">exit_to_app</i>Abmelden</a>
            </div>
        </div>
        <div class="card" style="width: 1250px; min-height: 500px; margin: 15px 0 15px 0; padding: 5px 20px 20px 20px">
            <h4 style="margin-bottom: 20px">Blackboard</h4>

            <ul class="collection">
                <#list boards as b>
                    <li class="collection-item <#if !b.visible>grey lighten-3</#if>">
                        <div class="row" style="margin: 0">
                            <div class="col m6" style="font-size: 1.4em; overflow: hidden;padding: 10px">
                                <div style="display: flex; justify-content: space-between">
                                    <div style="display:inline-block; flex-shrink: 0; width: 60px">
                                        <span class="board-sec-class green-badge" id="board-sec-${b.id}"
                                              style="visibility: hidden"></span>
                                    </div>
                                    <div style="display:inline-block; flex-shrink: 0; width: 80px">
                                        <span class="text-hover" style="display: inline-block"
                                              onclick="updateDuration(${b.id}, '${b.type.string}',${b.duration});$('#modal-duration').modal('open');">(${b.duration}s)</span>
                                    </div>
                                    <div style="flex: 1; display:inline-block;">
                                        <#if b.type == "PLAN">
                                            <span style="margin-left: 10px">[ Vertretungsplan ]</span>
                                        <#elseif b.type == "TEXT">
                                            <span class="text-hover" style="white-space: nowrap;"
                                                  onclick="updateRename(${b.id}, '${b.type.string}','${b.value}');$('#modal-rename').modal('open');">${b.valueWithoutBreaks}</span>
                                        <#elseif b.type == "PDF" || b.type == "IMG">
                                            <span style="margin-left: 10px">[ <#if b.uploaded>${b.fileName}<#else>leer</#if> ]</span>
                                            <form action="<@spring.url './upload/' + b.id />" method="POST" enctype="multipart/form-data" style="display: inline-block">
                                                <input name="file" type="file" id="upload-file-${b.id}" onchange="loading(); this.form.submit()" style="display: none">
                                                <a class="waves-effect waves-light btn-small green darken-3 margin-1"
                                                   onclick="document.getElementById('upload-file-${b.id}').click();"><i
                                                            class="material-icons left">arrow_upward</i>Hochladen</a>
                                            </form>
                                        </#if>
                                    </div>
                                </div>
                            </div>
                            <div class="col m3 right-align">
                                <form action="<@spring.url './type/' + b.id />" method="GET">
                                    <select name="type" onchange="this.form.submit()" class="browser-default">
                                        <#list types as t>
                                            <option value="${t}" <#if b.type == t>selected</#if>
                                                    style="z-index: 9999">${t.string}</option>
                                        </#list>
                                    </select>
                                </form>
                            </div>
                            <div class="col m3 right-align">

                                <a class="tooltipped waves-effect waves-light btn darken-4 margin-1"
                                   href="<@spring.url './move-up/' + b.id />"
                                   data-tooltip="Reihenfolge: nach oben" data-position="top"><i
                                            class="material-icons">arrow_upward</i></a>

                                <a class="tooltipped waves-effect waves-light btn darken-4 margin-1"
                                   href="<@spring.url './move-down/' + b.id />"
                                   data-tooltip="Reihenfolge: nach unten" data-position="top"><i
                                            class="material-icons">arrow_downward</i></a>

                                <a class="tooltipped waves-effect waves-light btn green darken-4 margin-1"
                                   href="<@spring.url './toggle-visibility/' + b.id />"
                                   data-tooltip="Sichtbar: ja/nein" data-position="top"><i
                                            class="material-icons"><#if b.visible>visibility<#else>visibility_off</#if></i></a>

                                <a class="tooltipped waves-effect waves-light btn red darken-4 margin-1"
                                   onclick="updateDelete(${b.id}, '${b.type.string}');$('#modal-delete').modal('open');"
                                   data-tooltip="Löschen" data-position="top"><i
                                            class="material-icons">delete</i></a>

                            </div>
                        </div>
                    </li>
                </#list>
            </ul>

            <div class="center-align">
                <a style="margin-top: 25px" class="waves-effect waves-light btn green darken-3"
                   href="<@spring.url './add' />"><i
                            class="material-icons right">add</i>Neuer Eintrag</a>
            </div>

        </div>
        <div id="active-counter" style="text-align: center; color: #808080">aktive Blackboards: ?</div>
    </div>
</div>

<div id="modal-delete" class="modal">
    <div class="modal-content">
        <h4 id="modal-delete-title">Wirklich löschen?</h4>
        <p id="modal-delete-text"></p>
    </div>
    <div class="modal-footer">
        <a href="#!" onclick="$('#modal-delete').modal('close')" class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
        <a id="modal-delete-action" href="" class="modal-close waves-effect waves-red btn red darken-4">Löschen</a>
    </div>
</div>

<div id="modal-rename" class="modal">
    <form id="modal-rename-form" action="<@spring.url './rename' />" method="GET">
        <div class="modal-content">
            <h4 id="modal-rename-title"></h4>
            <br/>

            <div class="input-field">
                <i class="material-icons prefix">edit</i>
                <label for="modal-rename-input">Textinhalt</label>
                <textarea id="modal-rename-input" name="value" class="materialize-textarea">

                </textarea>
            </div>
        </div>
        <div class="modal-footer">
            <a href="#!" onclick="$('#modal-rename').modal('close')"
               class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
            <button type="submit" class="btn waves-effect waves-light green darken-3">
                Speichern
                <i class="material-icons left">save</i>
            </button>
        </div>
    </form>
</div>

<div id="modal-duration" class="modal">
    <form id="modal-duration-form" action="" method="GET">
        <div class="modal-content">
            <h4 id="modal-duration-title"></h4>
            <br/>

            <div class="input-field">
                <i class="material-icons prefix">timer</i>
                <label for="modal-duration-input">Dauer in Sekunden</label>
                <input name="duration" type="number" id="modal-duration-input" min="1" max="3600"/>
            </div>
        </div>
        <div class="modal-footer">
            <a href="#!" onclick="$('#modal-duration').modal('close')"
               class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
            <button type="submit" value="Login" class="btn waves-effect waves-light green darken-3">
                Speichern
                <i class="material-icons left">save</i>
            </button>
        </div>
    </form>
</div>

<script src="<@spring.url '/static/js/jquery.min.js' />"></script>
<script src="<@spring.url '/static/js/materialize.min.js' />"></script>
<script src="<@spring.url '/static/js/blackboard-admin.js' />"></script>
<script type="text/javascript">

    document.addEventListener('DOMContentLoaded', function () {
        M.AutoInit();
        initBlackboardAdmin('<@spring.url './info'/>')
    });

    function updateDelete(boardId, boardType) {
        document.getElementById("modal-delete-action").setAttribute('href', "<@spring.url './delete/' />" + boardId);
        document.getElementById("modal-delete-title").innerHTML = boardType + " wirklich löschen?";
        document.getElementById("modal-delete-text").innerHTML = boardType + " wird gelöscht. Dieser Vorgang kann nicht rückgangig gemacht werden.";
    }

    function updateRename(boardId, boardType, boardValue) {
        document.getElementById("modal-rename-title").innerHTML = boardType + " bearbeiten";
        document.getElementById("modal-rename-form").setAttribute('action', "<@spring.url './rename/' />" + boardId);
        document.getElementById("modal-rename-input").innerHTML = boardValue.replace(/<br\s*[\/]?>/gi, '\n');
        M.textareaAutoResize(document.getElementById("modal-rename-input")); // auto resize text area
    }

    function updateDuration(boardId, boardType, boardDuration) {
        document.getElementById("modal-duration-title").innerHTML = "Dauer bearbeiten (" + boardType + ")";
        document.getElementById("modal-duration-form").setAttribute('action', "<@spring.url './duration/' />" + boardId);
        document.getElementById("modal-duration-input").value = boardDuration;
        M.updateTextFields();
    }

</script>
<@loading.render/>
<@toaster.render/>
</body>
</html>