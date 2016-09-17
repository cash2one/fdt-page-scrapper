<?php

class RuYahoo
{
	# Функция парсинга выдачи из RuYahoo.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://ru.search.yahoo.com/search?ei=UTF-8&vl=lang_$language&p=$query";
		$html = $F->GetHTML($url, 'ru.search.yahoo.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('div[class="res"]') as $e)
			{
				$t = 'a[class="yschttl"]';
				$d = 'div[class="abstr"]';

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
			$F->Error("Can't create outgoing request. Please check RuYahoo snippets plugin.");
		}

		return $snippets;
	}
}