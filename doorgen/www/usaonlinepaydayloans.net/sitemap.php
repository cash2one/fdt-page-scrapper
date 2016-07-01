<?php

// open the file in a binary mode
$name = './sitemap_gen.xml';
$fp = fopen($name, 'rb');

// send the right headers
header("Content-Type: text/html");
header("Content-Length: " . filesize($name));

// dump the picture and stop the script
fpassthru($fp);
exit;

?>