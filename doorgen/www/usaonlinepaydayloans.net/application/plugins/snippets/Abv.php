<?php

class Abv
{
	# Функция парсинга выдачи из Abv.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://search.abv.bg/search.php?q=$query&lr=lang_$language&ie=utf-8&oe=utf-8";
		$html = $F->GetHTML($url, 'search.abv.bg');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('div[id="srr_left"] div') as $e)
			{
				$t = 'a[class="st"]';
				$d = $e->innertext;

				if (isset($e->find($t, 0)->plaintext))
				{
					$title = $e->find($t, 0)->plaintext;
				}

				if (isset($d))
				{
					preg_match('|</a>(.+?)<span|', $d, $matches);
					$description = (!empty($matches[1])) ? strip_tags($matches[1]) : '';
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
			$F->Error("Can't create outgoing request. Please check Abv snippets plugin.");
		}

		return $snippets;
	}
}