

<#include "../messages/message-support.ftl"/>

<html>
<head>
    <link rel="stylesheet" type="text/css" href="/css/templates/mail.css">

    <style>
        table.nw-table td {
            vertical-align: top;
            border-bottom: 1px solid #ddd;
            padding: 2mm;
        }
        table.nw-table th {
            text-align: left;
            border-bottom: 1px solid lightgray;
            padding: 1mm;
        }
    </style>
</head>
<body>

<h1>Aktive navigationsadvarsler</h1>

<table class="nw-table">
  <tr>
    <th>Dato</th>
    <th>ID</th>
    <th>Område</th>
    <th>Advarsel</th>
  </tr>
  <#list messages as msg>
    <#assign msgDesc=descForLang(msg, language)!>
    <tr>
        <td nowrap>${msg.publishDateFrom?string['dd-MM-yyyy']}</td>
        <td nowrap>
            <#if msg.shortId?has_content>${msg.shortId}</#if>
        </td>
        <td>
            <#if msg.areas?has_content>
                <@areaLineage area=msg.areas[0] />
                <#if msgDesc?? && msgDesc.vicinity?has_content>
                    - ${msgDesc.vicinity}
                </#if>
            </#if>
        </td>
        <td>
            <#if msg.parts?has_content>
                <#list msg.parts as part>
                    <#assign partDesc=descForLang(part, language)!>
                    <#if partDesc?? && partDesc.details?has_content>
                        ${partDesc.details}<br>
                    </#if>
                </#list>
            </#if>
        </td>
    </tr>
  </#list>
</table>

</body>
</html>

