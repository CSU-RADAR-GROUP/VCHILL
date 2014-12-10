<?php
    header('Content-Type: application/x-java-jnlp-file; name=proxy.jnlp');
    header('Content-Disposition: attachment; filename=proxy.jnlp');
    header('Cache-Control: no-cache, must-revalidate');
    echo('<?xml version="1.0" encoding="UTF-8"?>'); ?> 
<jnlp spec="1.0+" codebase="http://chill.colostate.edu/java/">
    <information>
        <title>Java VChill Proxy</title>
        <homepage href="http://chill.colostate.edu/"/>
        <vendor>Colorado State University</vendor>
        <description>A proxy server for use with the VChill virtual radar display program</description>
        <description kind="short">CHILL protocol proxy</description>
    </information>
    <resources>
        <j2se version="1.5+" max-heap-size="512m" java-vm-args="-ea"/>
        <jar href="proxy.jar"/>
        <jar href="cache.jar"/>
        <jar href="chill.jar"/>
        <jar href="color.jar"/>
        <jar href="data.jar"/>
        <jar href="gui.jar"/>
        <jar href="socket.jar"/>
        <jar href="vchill.jar"/>
        <jar href="lib/looks-2.1.4.jar"/>
    </resources>
    <application-desc main-class="edu.colostate.vchill.proxy.ArchiveProxy">
    <?php foreach ($_GET + $_POST as $key => $value) { if (!empty($value)) {
        echo("<argument>-$key</argument>");
        echo("<argument>$value</argument>\n");
    }} ?>
        <argument>-gui</argument>
        <argument>on</argument>
    </application-desc>
    <security><all-permissions/></security>
</jnlp>
