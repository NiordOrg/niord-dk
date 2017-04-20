<#include "common.ftl"/>
<#include "markings.ftl"/>

<#macro renderLightName lightParam defaultName="">
    <#if lightParam?? && lightParam.aton_name?has_content>
        ${lightParam.aton_name}
    <#else>
       ${defaultName}
    </#if>
</#macro>

<@defaultSubjectFieldTemplates/>

<field-template field="part.getDesc('da').details" format="html">
    <#list params.positions as pos>
        Fyret <@renderLightName lightParam=pos/> <@renderPositionList geomParam=pos lang="da"/>
        er væltet og udgør en sejladshindring.
    </#list>
    Fundamentet af fyret er <@renderMarkings markings=params.markings! lang="da" format="details" unmarkedText="ikke afmærket"/><br>
    Det tilrådes skibsfarten at holde godt klar af positionen.
</field-template>

<field-template field="part.getDesc('en').details" format="html">
    <#list params.positions as pos>
        The light <@renderLightName lightParam=pos/> <@renderPositionList geomParam=part lang="en"/>
        has been destroyed and makes an obstruction.
    </#list>
    The light is <@renderMarkings markings=params.markings! lang="en" format="details" unmarkedText="unmarked"/><br>
    Mariners are advised to keep well clear.
</field-template>

<#if promulgate('audio')>
    <field-template field="message.promulgation('audio').text" update="append">
        <@line>
            <#list params.positions as pos>
                Fyret <@renderLightName lightParam=pos/> t <@renderPositionList geomParam=part format="audio" lang="da"/>
                er væltet og udgør en sejladshindring.
            </#list>
            Fundamentet af fyret er <@renderMarkings markings=params.markings! lang="da" format="audio"  unmarkedText="ikke afmærket"/>
        </@line>
        <@line>
            Det tilrådes skibsfarten at holde godt klar af positionen.
        </@line>
    </field-template>
</#if>

<#if promulgate('navtex')>
    <field-template field="message.promulgation('navtex').text" update="append">
        <@line format="navtex">
            <#list params.positions as pos>
                LIGHT <@renderLightName lightParam=pos/> <@renderPositionList geomParam=part format="navtex" lang="en"/>
                DESTROYED.
            </#list>
            LIGHT <@renderMarkings markings=params.markings! lang="en" format="navtex"  unmarkedText="UNMARKED"/>
        </@line>
        <@line format="navtex">
            MARINERS ADVISED TO KEEP CLEAR.
        </@line>
    </field-template>
</#if>
