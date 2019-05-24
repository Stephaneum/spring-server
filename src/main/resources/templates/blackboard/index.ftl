<#import "/spring.ftl" as spring/>

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

<div id="title">
    <p style="color: white; margin: 5px; font-size: 24pt">${date}</p>
</div>

<#list indexes as i>
    <div style="padding: 50px">
        <img id="logo" src="<@spring.url '/blackboard/img/' + i />" style="width: 100%" alt="plan" />
    </div>
</#list>


<script src="<@spring.url '/static/js/scroller.js' />"></script>
</body>
</html>