<?php

class MetaCrawler
{
	# Функция парсинга выдачи из MetaCrawler.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://www.metacrawler.com/search/web?q=$query&qlang=$language&ie=utf-8&oe=utf-8";
		$html = $F->GetHTML($url, 'www.metacrawler.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('div[id="webResults"] div[class="searchResult webResult"]') as $e)
			{
				$t = 'div[class="resultTitlePane"] a';
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
			$F->Error("Can't create outgoing request. Please check MetaCrawler snippets plugin.");
		}

		return $snippets;
	}
}