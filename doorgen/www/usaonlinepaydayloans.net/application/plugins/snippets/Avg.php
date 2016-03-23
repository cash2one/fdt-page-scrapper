<?php

class Avg
{
	# Функция парсинга выдачи из Avg.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://search.avg.com/search?q=$query&lang=$language";
		$html = $F->GetHTML($url, 'search.avg.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('ul[class="res-list"] li') as $e)
			{
				$t = 'h2 a';
				$d = 'p';

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
			$F->Error("Can't create outgoing request. Please check Avg snippets plugin.");
		}

		return $snippets;
	}
}