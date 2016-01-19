<?php

class Webalta
{
	# Функция парсинга выдачи из Webalta.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://webalta.ru/search?q=$query";
		$html = $F->GetHTML($url, 'webalta.ru');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('div[class="main-rnit"]') as $e)
			{
				$t = 'div[class="content-rnit-unit"] div[class="title"] a';
				$d = 'div[class="content-rnit-unit"]';

				if (isset($e->find($t, 0)->plaintext))
				{
					$title = $e->find($t, 0)->plaintext;
				}

				if (isset($e->find($d, 0)->innertext))
				{
					preg_match('|div>(.+?)<div|', $e->find($d, 0)->innertext, $matches);
					$description = (!empty($matches[1])) ? $matches[1] : '';
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
			$F->Error("Can't create outgoing request. Please check Webalta snippets plugin.");
		}

		return $snippets;
	}
}