<?PHP
$FileBazaTXT="bazalink.txt";
$temlpfile="backtemplate.html";
$mypage404=false; //если true, то статус 404, иначе просто пустая страничка.

// считываем шаблон
if(file_exists($temlpfile)){
	$fh=fopen($temlpfile, 'r', false);
   	$temlp = fread($fh, filesize($temlpfile));
	fclose($fh);
}else $temlp='';

// проверяем полученный GET-параметр
if(isset($_GET['id'])){
	$idsite=substr(trim($_GET['id']),0,60);
 	if(!preg_match("/^[a-z\-0-9.]*$/i", $idsite)){
          $idsite=''; }
}else $idsite='';

$strLink='';
if((file_exists($FileBazaTXT))&&(strlen($idsite)>0)){
	$fh=fopen($FileBazaTXT, 'r', false);
	while (!feof($fh)) {
		$buf = trim(fgets($fh));
		if((strpos($buf,'[#*HOST=')!==false)&&(strpos($buf,$idsite)==8))
		{
           	$strLink='';
           	$buf='';
			while ((!feof($fh))AND(strpos($buf,'[#*HOST=')===false)) {
				$buf = fgets($fh);
				if (strpos($buf,'[#*HOST=')===false){$strLink.=$buf;}
			}
			break;
		}
	}
   	fclose($fh);
}

if((strlen($temlp)>0)AND(strlen($strLink)>0)){ // если есть шаблон и код ссылки
	$result=str_replace("!linkdata!", $strLink,$temlp);
	echo $result;
}elseif($mypage404){
header("HTTP/1.0 404 Not Found");
header("Status: 404 Not Found");
echo <<<END
<html xmlns="http://www.w3.org/1999/xhtml" dir="ltr" lang="ru">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Страница не найдена!</title>
</head>
<body>
<h1>Страница не найдена! 404 ошибка.</h1>
<br><br>
Вернитесь на главную страницу сайта или воспользуйтесь поиском!
</body></html>
END;
}else{
echo <<<END
<html xmlns="http://www.w3.org/1999/xhtml" dir="ltr" lang="ru">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Партнерские ссылки</title>
</head>
<body>
END;
echo $strLink;
echo '</body></html>';
}

?>
