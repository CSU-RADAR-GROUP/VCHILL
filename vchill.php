<?php
    header('Content-Type: application/x-java-jnlp-file; name=vchill.jnlp');
    header('Content-Disposition: attachment; filename=vchill.jnlp');
    header('Cache-Control: no-cache, must-revalidate');
    echo('<?xml version="1.0" encoding="UTF-8"?>'); ?> 
<jnlp spec="1.0+" codebase="http://chill.colostate.edu/java/">
    <information>
        <title>Java VChill</title>
        <homepage href="http://chill.colostate.edu/"/>
        <vendor>Colorado State University</vendor>
        <description>A virtual radar display and control program</description>
        <description kind="short">Virtual radar display</description>
        <offline-allowed/>
    </information>
    <resources>
        <j2se version="1.5+" max-heap-size="384m"/>
        <jar href="vchill.jar"/>
        <jar href="ascope.jar"/>
        <jar href="bookmark.jar"/>
        <jar href="cache.jar"/>
        <jar href="chill.jar"/>
        <jar href="color.jar"/>
        <jar href="connection.jar"/>
        <jar href="data.jar"/>
        <jar href="file.jar"/>
        <jar href="gui.jar"/>
        <jar href="map.jar"/>
        <jar href="netcdf.jar"/>
        <jar href="numdump.jar"/>
        <jar href="plot.jar"/>
        <jar href="radar.jar"/>
        <jar href="socket.jar"/>
        <jar href="lib/looks-2.1.4.jar"/>
        <jar href="lib/gif89encoder.jar"/>
        <jar href="lib/netcdf-2.2.22.jar" download="lazy"/>
        <jar href="lib/slf4j-jdk14.jar" download="lazy"/>
    </resources>
    <application-desc main-class="edu.colostate.vchill.Loader">
    <?php foreach ($_GET + $_POST as $key => $value) { if (!empty($value)) {
            echo("<argument>-$key</argument>");
            echo("<argument>$value</argument>\n");
    }} ?>
    </application-desc>
    <security><all-permissions/></security>
</jnlp>
