<?php

error_reporting(E_ALL ^ E_NOTICE);

include('get_articles_list.php');

exec("/usr/bin/java -Duser.timezone=\"EST\" -cp \"/home/user/soft/java/*\" com.fdt.doorgen.key.pooler.util.SiteMapGenerator txt/urls.txt 20 200 \"./sitemap_gen.xml\" articles_posted.txt");

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