<?php

	set_time_limit(600);

	require_once "../application/models/functions.php";

	$F = new Functions('../');

	if (!isset($_COOKIE['password']) or $_COOKIE['password'] !== PASSWORD)
	{
		exit('Please login to your dashboard!');
	}

	require_once "../application/libraries/createzip.php";
	require_once "../application/libraries/createzipfromdir.php";

	$Z = new createDirZip;

	$kids = $F->GetContents('txt/kids.txt');
	if (!empty($kids))
	{
		foreach($kids as $kid)
		{
			$index = '<?php

	$id = (isset($_GET[\'id\'])) ? $_GET[\'id\'] : \'\';

	$ch = curl_init();
	$url = "' . $F->host . '/$id";
	curl_setopt($ch, CURLOPT_URL, $url);
	curl_setopt($ch, CURLOPT_POSTFIELDS, "referer=' . $kid . '");
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	$html = curl_exec($ch);
	curl_close($ch);

	$extension = pathinfo($id, PATHINFO_EXTENSION);
	switch ($extension)
	{
		case "jpg":
			header("Content-Type: image/jpeg");

		case "gif":
			header("Content-Type: image/gif");

		case "png":
			header("Content-Type: image/png");

		case "css":
			header("Content-Type: text/css; charset=utf-8");

		default:
			header("Content-Type: text/html; charset=utf-8");
	}

	echo $html;

?>';

			$base = explode('/', $kid, 2);
			$base = isset($base[1]) ? $base[1] : '';

			$htaccess = <<<CODE
RewriteEngine On
RewriteBase /$base

RewriteCond %{REQUEST_FILENAME} !-d
RewriteCond %{REQUEST_FILENAME} !-f

RewriteRule index.php.* - [L]

RewriteCond %{REQUEST_FILENAME} !-d
RewriteCond %{REQUEST_FILENAME} !-f

RewriteRule ^(.*) index.php?id=$1
CODE;

			if (!is_dir("../content/kids/$kid/"))
			{
				mkdir("../content/kids/$kid/", 0755, true);
			}

			file_put_contents("../content/kids/$kid/index.php", $index);
			file_put_contents("../content/kids/$kid/.htaccess", $htaccess);
		}

		$Z->get_files_from_folder('../content/kids/', '');
		if(!file_put_contents('../content/kids.zip', $Z->getZippedfile(), LOCK_EX))
		{
			$F->Error("Can't create or write in file kids.zip in the content directory.");
		}
		else
		{
			$file = '../content/kids.zip';
			if (file_exists($file))
			{
				header('Content-Description: File Transfer');
				header('Content-Type: application/x-zip');
				header('Content-Type: application/x-zip-compressed; name=' . basename('kids.zip'));
				header('Content-Disposition: attachment; filename=' . basename('kids.zip'));
				header('Content-Transfer-Encoding: binary');
				header('Content-Length: ' . filesize($file));
				flush();
				readfile($file);
				unlink($file);
			}
		}

		$F->RemoveDirectory('../content/kids');
	}
	else
	{
		exit('File txt/kids.txt does not exist or is empty!');
	}