<?php 

error_reporting(E_ALL ^ E_NOTICE);

require_once "application/models/functions_decode.php";
require_once "application/libraries/parser.php";
require_once "utils/title_generator.php";
require_once "utils/case_value_selector.php";
require_once "utils/config.php";
require_once "index.php";


#echo "Opening connection...<br>";

//fetch regions
$con=mysqli_connect(DB_HOST,DB_USER_NAME,DB_USER_PWD,DB_NAME);
#echo "Connecting...";
if (mysqli_connect_errno())
{
	#echo "Failed to connect to MySQL: " . mysqli_connect_error();
}

mysqli_query($con,"set character_set_client='utf8'");
mysqli_query($con,"set character_set_results='utf8'");
mysqli_query($con,"set collation_connection='utf8_general_ci'");

#echo "Connection is opened<br>";

$template = file_get_contents("tmpl_main.html");

$tmpl_inner = file_get_contents("tmpl_404.html");

$template  = fillCategoryList($con, $template);

$page_meta_robots="<meta name=\"robots\" content=\"noindex,follow\" />";

$template=preg_replace("/\[MAIN_BLOCK\]/",$tmpl_inner, $template);

$template=preg_replace("/\[PAGE_H1\]/", PAGE_404_H1, $template);
$template=preg_replace("/\[PAGE_H2\]/", PAGE_404_H2, $template);

$template=preg_replace("/\[TITLE\]/", PAGE_404_TITLE, $template);
$template=preg_replace("/\[DESCRIPTION\]/", PAGE_404_META_DESCRIPTION, $template);
$template=preg_replace("/\[KEYWORDS\]/", PAGE_404_META_KEYWORDS, $template);

$template=preg_replace("/\[CITY_PLACENAME\]/", $page_meta_placename, $template);
$template=preg_replace("/\[CITY_PPOSITION\]/", $page_meta_position, $template);
$template=preg_replace("/\[CITY_REGION\]/", $page_meta_region, $template);
$template=preg_replace("/\[META_ICBM\]/", $page_meta_icbm, $template);
$template=preg_replace("/\[META_ROBOTS\]/", $page_meta_robots, $template);

$template=preg_replace("/\[CANONICAL_LINK\]/", "", $template);

mysqli_close($con);

echo $template;

?>

