<?php

	$url = base64_decode($_SERVER['QUERY_STRING']);
	$now = gmdate("D, d M Y H:i:s", time());
	$expire = gmdate("D, d M Y H:i:s", time() + 2592000);

	header("Cache-Control: private, max-age=2592000");
	header("Pragma: private");
	header("Last-Modified: $now GMT");
	header("Expires: $expire GMT");

	if (isset($_SERVER['HTTP_IF_MODIFIED_SINCE']))
	{
		header('Last-Modified: ' . $_SERVER['HTTP_IF_MODIFIED_SINCE'], true, 304);
		exit;
	}

	$extension = pathinfo($url, PATHINFO_EXTENSION);
	switch ($extension)
	{
		case 'jpg':
			$type = 'image/jpeg';
			break;

		case 'gif':
			$type = 'image/gif';
			break;

		case 'png':
			$type = 'image/png';
			break;

		default:
			$type = 'image/jpeg';
			break;
	}

	header("Content-Type: $type");
	readfile($url);