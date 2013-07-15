<?php

class Meta
{
	# Функция парсинга выдачи из Meta.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://meta.ua/search.asp?q=$query";
		$html = $F->GetHTML($url, 'meta.ua');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('table[class="li"]') as $e)
			{
				$t = 'a[class="title"]';
				$d = 'div[class="quote"]';

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
			$F->Error("Can't create outgoing request. Please check Meta snippets plugin.");
		}

		return $snippets;
	}
}