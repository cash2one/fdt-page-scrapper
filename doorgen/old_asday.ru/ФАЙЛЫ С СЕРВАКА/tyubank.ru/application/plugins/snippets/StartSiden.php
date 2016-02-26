<?php

class StartSiden
{
	# Функция парсинга выдачи из StartSiden.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://www.startsiden.no/sok/index.html?lr=lang_$language&q=$query";
		$html = $F->GetHTML($url, 'www.startsiden.no');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('ol[class="searchresults mainresults"] li') as $e)
			{
				$t = 'h3 a';
				$d = 'p[class="description"]';

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
			$F->Error("Can't create outgoing request. Please check StartSiden snippets plugin.");
		}

		return $snippets;
	}
}