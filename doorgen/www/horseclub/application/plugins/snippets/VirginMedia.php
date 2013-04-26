<?php

class VirginMedia
{
	# Функция парсинга выдачи из VirginMedia.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://search.virginmedia.com/results/?q=$query";
		$html = $F->GetHTML($url, 'search.virginmedia.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('div[class="result"]') as $e)
			{
				$t = 'a[class="title"]';
				$d = 'span[class="description"]';

				if (isset($e->find($t, 0)->plaintext))
				{
					$title = $e->find($t, 0)->plaintext;
				}

				if (isset($e->find($d, 0)->plaintext))
				{
					$description = $e->find($d, 0)->plaintext;
				}

				if ($i < $count)
				{
					if (!empty($title) and !empty($description))
					{
						$snippets[$i]['title'] = trim($title);
						$snippets[$i]['description'] = trim($description);
						$i++;
					}
				}
			}

			$html->clear();
			$html = null; $e = null;
			unset($html, $e);
		}
		else
		{
			$F->Error("Can't create outgoing request. Please check VirginMedia snippets plugin.");
		}

		return $snippets;
	}
}