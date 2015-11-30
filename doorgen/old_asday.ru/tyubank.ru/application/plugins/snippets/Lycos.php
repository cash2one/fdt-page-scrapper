<?php

class Lycos
{
	# Функция парсинга выдачи из Lycos.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://search.lycos.com/web?q=$query&lr=lang_$language&ie=utf-8&oe=utf-8";
		$html = $F->GetHTML($url, 'search.lycos.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('ul[id="webResults"] li[class="result overflow"]') as $e)
			{
				$t = 'a div[class="resultText"] h4';
				$d = 'a div[class="resultText"] p';

				if (isset($e->find($t, 0)->plaintext))
				{
					$title = $e->find($t, 0)->plaintext;
				}

				if (isset($e->find($d, 1)->plaintext))
				{
					$description = $e->find($d, 1)->plaintext;
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
			$F->Error("Can't create outgoing request. Please check Lycos snippets plugin.");
		}

		return $snippets;
	}
}