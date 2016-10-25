<?php

error_reporting(E_ALL ^ E_NOTICE);

exec("/usr/bin/java -Duser.timezone=\"EST\" -cp \"/home/user/soft/java/*\" com.fdt.doorgen.key.pooler.util.SiteMapGenerator txt/urls.txt 900 1000 \"./sitemap_gen.xml\"");

$outputFile ="sitemap_gen.xml";

// open the file in a binary mode
$fp = fopen($outputFile, 'rb');

// send the right headers
header("Content-Type: text/xml");
header("Content-Length: " . filesize($outputFile));

// dump the picture and stop the script
fpassthru($fp);

exit;

?>