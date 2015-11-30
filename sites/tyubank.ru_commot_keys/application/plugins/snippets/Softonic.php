<?php

class Softonic
{
	# Функция парсинга выдачи из Softonic.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://search.softonic.com/default/default?q=$query&uil=$language";
		$html = $F->GetHTML($url, 'search.softonic.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('ul[class="search_results"] li') as $e)
			{
				$t = 'h3 a';
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
			$F->Error("Can't create outgoing request. Please check Softonic snippets plugin.");
		}

		return $snippets;
	}
}