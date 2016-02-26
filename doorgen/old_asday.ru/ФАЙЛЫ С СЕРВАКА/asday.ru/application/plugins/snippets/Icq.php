<?php

class Icq
{
	# Функция парсинга выдачи из Icq.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://search.icq.com/search/results.php?q=$query&search_mode=lang";
		$html = $F->GetHTML($url, 'search.icq.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('div[class="r2-6"]') as $e)
			{
				$t = 'a[class="r2-6-1a"]';
				$d = 'div[class="r2-6-2"]';

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
			$F->Error("Can't create outgoing request. Please check Icq snippets plugin.");
		}

		return $snippets;
	}
}