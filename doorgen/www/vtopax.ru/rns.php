<?php
preg_match("/\.php\?u=(.*)$/", $_SERVER['REQUEST_URI'], $matches);
header("HTTP/1.1 301 Moved Permanently");
header("Location: http://".$matches[1]."");
exit;
?> 