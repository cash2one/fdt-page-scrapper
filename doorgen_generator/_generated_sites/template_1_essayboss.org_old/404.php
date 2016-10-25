<?php 

error_reporting(E_ALL ^ E_NOTICE);
echo "<HTML><HEAD><title> 404 Error Page</title></HEAD><BODY><p align=\"center\"><h1>Error 404</h1><br>Page Not Found<p>";

$ip = $_SERVER["REMOTE_ADDR"];
$requri = $_SERVER["REQUEST_URI"];
$servname = $_SERVER["SERVER_NAME"];
$combine = $ip . " tried to load " . $servname . $requri ;
$httpref = $_SERVER["HTTP_REFERER"];
$httpagent = $_SERVER["HTTP_USER_AGENT"];
$today = date("D M j Y g:i:s a T");
echo $today;
$note = "You are in a wrong page!" ;
$message = $today . '<br>' . $combine . '<br> User Agent = ' . $httpagent . '<h2>' . $note . '</h2> <br> ' . $httpref .'  Visit our <a href="http://'.$servname.'">Home Page </a>';
echo $message;

echo "</BODY></HTML>";


?>

