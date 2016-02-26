<?php

class I
{
	# Функция парсинга выдачи из I.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$string = mb_strtolower($string, 'UTF-8');
		$query = urlencode(mb_convert_encoding($string, 'Windows-1251', 'UTF-8'));
		$url = "http://search.i.ua/?q=$query";
		$html = $F->GetHTML($url, 'search.i.ua');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('ol[class="List"] li') as $e)
			{
				$t = 'p[class="larger"]';
				$d = 'p';

				if (isset($e->find($t, 0)->plaintext))
				{
					$title = mb_convert_encoding($e->find($t, 0)->plaintext, 'UTF-8', 'Windows-1251');
				}

				if (isset($e->find($d, 1)->plaintext))
				{
					$description = mb_convert_encoding($e->find($d, 1)->plaintext, 'UTF-8', 'Windows-1251');
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
			$F->Error("Can't create outgoing request. Please check I snippets plugin.");
		}

		return $snippets;
	}
}