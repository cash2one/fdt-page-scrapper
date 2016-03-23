<?php

class InfoSpace
{
	# Функция парсинга выдачи из InfoSpace.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://www.infospace.com/search/web?q=$query&qlang=$language&ie=utf-8&oe=utf-8";
		$html = $F->GetHTML($url, 'www.infospace.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('div[class="searchResult webResult"]') as $e)
			{
				$t = 'div[class="resultTitlePane"] a[class="resultTitle"]';
				$d = 'div[class="resultDescription"]';

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
			$F->Error("Can't create outgoing request. Please check InfoSpace snippets plugin.");
		}

		return $snippets;
	}
}