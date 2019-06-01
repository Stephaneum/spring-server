<#import "/spring.ftl" as spring/>
<#setting number_format="computer">

<!DOCTYPE HTML>
<html lang="de">
<head>
    <title>Blackboard</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="<@spring.url '/static/img/favicon.png' />">
    <link rel="apple-touch-icon" sizes="196x196" href="<@spring.url '/static/img/favicon.png' />">
    <style type="text/css">
        body {
            overflow: hidden;
        }

        #title {
            background-color: #1b5e20;
            position: fixed;
            top: 25px;
            right: 25px;
            z-index: 999;
            border-radius: 15px;
            padding: 10px 20px 10px 20px;
            text-align: center
        }
    </style>
</head>

<body style="margin: 0">

<#if active.type == "PLAN" || active.type == "PDF">
    <#if active.type == "PLAN">
        <div id="title">
            <p style="color: white; margin: 5px; font-size: 24pt">${pdfTitle}</p>
        </div>
    </#if>
    <#list pdfIndexes as i>
        <div style="padding: 50px">
            <img src="<@spring.url '/blackboard/img/' + active.id + '/' + i />" style="width: 100%" />
        </div>
    </#list>

    <script src="<@spring.url '/static/js/scroller.js' />"></script>
<#elseif active.type == "IMG">
    <div style="width: 100vw; height: 100vh; background-image: url(<@spring.url '/blackboard/img/' + active.id />); background-size: contain; background-repeat: no-repeat; background-position: center;">
    </div>
<#elseif active.type == "TEXT">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/materialize.min.css' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/material-icons.css' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/style.css' />">

    <div style="height: 100vh; display: flex; align-items: center; justify-content: center">
        <div class="center-align">

            <img src="<@spring.url '/static/img/logo-banner-green.png' />" style="height: 10vh; margin-bottom: 5vh"/>

            <div class="card" style="width: 90vw; height: 65vh; display: flex; align-items: center; justify-content: center">
                <p id="blackboard-text">${active.value}</p>
            </div>

        </div>

    </div>
<#else>
</#if>

<script src="<@spring.url '/static/js/jquery.min.js' />"></script>
<script src="<@spring.url '/static/js/materialize.min.js' />"></script>
<script src="<@spring.url '/static/js/blackboard.js' />"></script>
<script type="text/javascript">
    window.scrollTo(0, 0);
    document.addEventListener('DOMContentLoaded', function() {
        initBlackboard('<@spring.url '/blackboard/timestamp'/>', ${active.lastUpdate?long})
    });
</script>
</body>
</html>