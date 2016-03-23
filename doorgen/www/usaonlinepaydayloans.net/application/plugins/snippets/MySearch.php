<?php

class MySearch
{
	# Функция парсинга выдачи из MySearch.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://www.mysearch.com/search/GGmain.jhtml?searchfor=$query&lr=lang_$language&ie=utf-8&oe=utf-8";
		$html = $F->GetHTML($url, 'www.mysearch.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('div[class="algoLinkBox"]') as $e)
			{
				$t = 'a[class="pseudolink"]';
				$d = 'span[class="nDesc"]';

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
			$F->Error("Can't create outgoing request. Please check MySearch snippets plugin.");
		}

		return $snippets;
	}
}