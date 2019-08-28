<#macro render>
    <script type="text/javascript">
        document.addEventListener('DOMContentLoaded', function () {
            <#if toast??>
                <#if toast.content?has_content>
                M.toast({html: '${toast.title}<br>${toast.content}'});
                <#else>
                M.toast({html: '${toast.title}'});
                </#if>
            </#if>
        });
    </script>
</#macro>