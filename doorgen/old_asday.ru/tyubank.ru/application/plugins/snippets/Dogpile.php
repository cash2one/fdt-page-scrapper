<?php

class Dogpile
{
	# Функция парсинга выдачи из Dogpile.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://www.dogpile.com/search/web?q=$query";
		$html = $F->GetHTML($url, 'www.dogpile.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('div[class="searchResult webResult"]') as $e)
			{
				$t = 'div[class="resultTitlePane"] a';
				$d = 'div[class="resultDescription"]';

				if (isset($e->find($t, 0)->plaintext))
				{
					$title = html_entity_decode($e->find($t, 0)->plaintext, ENT_QUOTES, 'UTF-8');
				}

				if (isset($e->find($d, 0)->plaintext))
				{
					$description = html_entity_decode($e->find($d, 0)->plaintext, ENT_QUOTES, 'UTF-8');
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
			$F->Error("Can't create outgoing request. Please check Dogpile snippets plugin.");
		}

		return $snippets;
	}
}