<?php
    header('Content-Type: application/x-java-jnlp-file; name=proxyshutdown.jnlp');
    header('Content-Disposition: attachment; filename=proxyshutdown.jnlp');
    header('Cache-Control: no-cache, must-revalidate');
    echo('<?xml version="1.0" encoding="UTF-8"?>'); ?> 
<jnlp spec="1.0+" codebase="http://chill.colostate.edu/java/">
    <information>
        <title>Java VChill Proxy Shutdown</title>
        <homepage href="http://chill.colostate.edu/"/>
        <vendor>Colorado State University</vendor>
        <description>A program to shut down a running VChill proxy server</description>
        <description kind="short">CHILL proxy shutdown utility</description>
        <offline-allowed/>
    </information>
    <resources>
        <j2se version="1.4+"/>
        <jar href="proxy.jar"/>
        <jar href="socket.jar"/>
        <jar href="vchill.jar"/>
    </resources>
    <application-desc main-class="edu.colostate.vchill.proxy.ProxyShutdown">
    <?php foreach ($_GET + $_POST as $key => $value) { if (!empty($value)) {
        echo("<argument>-$key</argument>");
        echo("<argument>$value</argument>\n");
    }} ?>
    </application-desc>
    <security><all-permissions/></security>
</jnlp>
