<?php

class PlusNetwork
{
	# Функция парсинга выдачи из PlusNetwork.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://www.plusnetwork.com/?q=$query&lr=lang_$language&ie=utf-8&oe=utf-8";
		$html = $F->GetHTML($url, 'www.plusnetwork.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('ul[class="sch-list"] li[class="sch-itm"]') as $e)
			{
				$t = 'div[class="bd"] h3 a';
				$d = 'div[class="bd"]';

				if (isset($e->find($t, 0)->plaintext))
				{
					$title = $e->find($t, 0)->plaintext;
				}

				if (isset($e->find($d, 0)->innertext))
				{
					preg_match('|</h3>(.+?)<div|', $e->find($d, 0)->innertext, $matches);
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
			$F->Error("Can't create outgoing request. Please check PlusNetwork snippets plugin.");
		}

		return $snippets;
	}
}