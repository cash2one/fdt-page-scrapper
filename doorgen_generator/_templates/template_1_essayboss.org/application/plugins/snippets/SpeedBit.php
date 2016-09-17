<?php

class SpeedBit
{
	# Функция парсинга выдачи из SpeedBit.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://home.speedbit.com/search.aspx?q=$query&lr=lang_$language&ie=utf-8&oe=utf-8";
		$html = $F->GetHTML($url, 'www.home.speedbit.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('ol li[class="unitres"]') as $e)
			{
				$t = 'h3[class="restitle"] a';
				$d = 'div[class="results"]';

				if (isset($e->find($t, 0)->plaintext))
				{
					$title = $e->find($t, 0)->plaintext;
				}

				if (isset($e->find($d, 0)->innertext))
				{
					preg_match('|(.+?)<br|', $e->find($d, 0)->innertext, $matches);
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
			$F->Error("Can't create outgoing request. Please check SpeedBit snippets plugin.");
		}

		return $snippets;
	}
}