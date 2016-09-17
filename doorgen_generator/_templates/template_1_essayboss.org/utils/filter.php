<?php

	set_time_limit(600);

	require_once '../application/models/functions.php';

	$F = new Functions('../');

	if (!isset($_COOKIE['password']) or $_COOKIE['password'] !== PASSWORD)
	{
		exit('Please login to your dashboard!');
	}

	$files = glob('../content/base/*/*', GLOB_NOSORT);
	if (!empty($files))
	{
		$strings = array();
		foreach ($files as $file)
		{
			if (filesize($file) > 0)
			{
				$array = file($file, FILE_IGNORE_NEW_LINES);
				foreach ($array as $value)
				{
					$strings[] = $value;
				}
			}
		}

		$before = count($strings);

		$words = $F->GetContents('txt/stop-words.txt');
		if (!empty($words))
		{
			foreach ($words as $w => $word)
			{
				if (preg_match('|^[*]|', $word))
					$words[$w] = str_replace('*', '', $word) . '$';
				if (preg_match('|[*]$|', $word))
					$words[$w] = '^' . str_replace('*', '', $word);
			}

			$word = implode('|', $words);

			foreach ($strings as $s => $string)
			{
				$query = @json_decode($string)->query;

				$pattern = mb_strtolower($word, 'UTF-8');
				$subject = mb_strtolower($query, 'UTF-8');

				if (preg_match("/$pattern/", $subject))
				{
					unset($strings[$s]);
				}
			}

			unset($words, $word);
		}

		$after = count($strings);

		$keys = array('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f');
		for ($i = 0; $i < count($keys); $i++)
		{
			$files[$keys[$i]] = array_fill_keys($keys, '');
		}

		$keywords = array();
		foreach ($strings as $string)
		{
			$md5 = @json_decode($string)->md5;
			$files[$md5[0]][$md5[1]][] = $string;
			$keywords[] = @json_decode($string)->query;
		}

		unset($strings);

		if ($before > $after)
		{
			for ($i = 0; $i < count($keys);$i++)
			{
				for ($j = 0; $j < count($keys);$j++)
				{
					if (!empty($files[$keys[$i]][$keys[$j]]))
					{
						$file = "../content/base/$keys[$i]/$keys[$j].txt";
						if (!file_put_contents($file, implode("\r\n", $files[$keys[$i]][$keys[$j]]) . "\r\n"))
						{
							$F->Error("Can't write in file $file.");
						}
					}
				}
			}
		}

		unset($files);

		if (!file_put_contents('../content/keywords.txt', implode("\r\n", $keywords)))
		{
			$F->Error("Can't create or write in file keywords.txt in the content directory.");
		}
		else
		{
			$file = '../content/keywords.txt';
			if (file_exists($file))
			{
				header('Content-Description: File Transfer');
				header('Content-Type: text/plain');
				header('Content-Disposition: attachment; filename=' . basename('keywords.txt'));
				header('Content-Transfer-Encoding: binary');
				header('Content-Length: ' . filesize($file));
				flush();
				readfile($file);
				unlink($file);
			}
		}

		unset($keywords);
	}