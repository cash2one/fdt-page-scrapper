<?php

	set_time_limit(600);

	require_once '../application/models/functions.php';

	$F = new Functions('../');

	if (!isset($_COOKIE['password']) or $_COOKIE['password'] !== PASSWORD)
	{
		exit('Please login to your dashboard!');
	}

	$keywords = $F->getContents('txt/keywords.txt');
	if (!empty($keywords))
	{
		$infile = count($keywords);

		$keys = array('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f');
		for ($i = 0; $i < count($keys); $i++)
		{
			$files[$keys[$i]] = array_fill_keys($keys, '');
		}

		foreach ($keywords as $keyword)
		{
			$query = $F->Clear($keyword);
			$md5 = md5($query);
			$string = array('md5'		=> $md5,
							'query'		=> $query,
							'translit'	=> $F->Convert($query));

			$files[$md5[0]][$md5[1]][] = json_encode($string);

			if (isset($_GET['type']))
			{
				switch($_GET['type'])
				{
					case 'html':
						$urls[] = '<a href="' . CreateUrl($F, $query, $md5) . '">' . $query . '</a>';
						break;

					case 'code':
						$urls[] = '[url=' . CreateUrl($F, $query, $md5) . ']' . $query . '[/url]';
						break;

					case 'farm':
						$urls[] = CreateUrl($F, $query, $md5) . " $query";
						break;
				}
			}
			else
			{
				$urls[] = CreateUrl($F, $query, $md5);
			}
		}

		unset($keywords);

		$count = 0;
		for ($i = 0; $i < count($keys); $i++)
		{
			for ($j = 0; $j < count($keys); $j++)
			{
				if (!empty($files[$keys[$i]][$keys[$j]]))
				{
					$file = "../content/base/$keys[$i]/$keys[$j].txt";
					$content = array_merge(file($file, FILE_IGNORE_NEW_LINES), $files[$keys[$i]][$keys[$j]]);
					$keywords = array_unique($content);

					if (!file_put_contents($file, implode("\r\n", $keywords) . "\r\n"))
					{
						$F->Error("Can't write in file $file.");
					}
					else
					{
						$count += count($content) - count($keywords);
					}
				}
			}
		}

		unset($files);

		if (class_exists('DOMDocument'))
		{
			$xml = new DOMDocument('1.0', 'UTF-8');
			$xml->formatOutput = true;
			$uri = $xml->appendChild($xml->createElementNS('http://www.sitemaps.org/schemas/sitemap/0.9', 'urlset'));
			foreach ($urls as $url)
			{
				$urn = $uri->appendChild($xml->createElement('url'));
				$loc = $urn->appendChild($xml->createElement('loc'));
				$loc->appendChild($xml->createTextNode($url));
			}
			$xml->save('../content/sitemap.xml');
		}

		if (!file_put_contents('../content/urls.log', implode("\r\n", $urls) . "\r\n"))
		{
			$F->Error("Can't create or write in file urls.log in the content directory.");
		}
		else
		{
			$inbase = $infile - $count;

			echo "Added $inbase/$infile keywords to queries base.<br><a href=\"$F->host/content/urls.log\">Download</a>";
		}

		unset($urls);
	}
	else
	{
		exit('File txt/keywords.txt does not exist or is empty!');
	}

	function CreateUrl($F, $query, $md5)
	{
		$url = $F->host;

		switch(FU_STATUS)
		{
			case 'ON':

				switch(substr(FU_TYPE, 0, 4))
				{
					case 'FULL':

						$url .= "/" . RESULT . "/" . $F->Convert($query) . "-" . substr($md5, 0, 2) . substr(FU_TYPE, 4);
						break;

					case 'RAND':

						$url .= "/" . RESULT . "/$md5" . substr(FU_TYPE, 4);
						break;
				}

				break;

			case 'OFF':

				$url .= "/?" . CONTROLLER . "=" . RESULT . "&" . QUERY . "=" . urlencode($query);
				break;
		}

		return $url;
	}