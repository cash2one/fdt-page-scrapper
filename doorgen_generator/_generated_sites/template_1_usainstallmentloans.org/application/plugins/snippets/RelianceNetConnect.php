<?php

class RelianceNetConnect
{
	# Функция парсинга выдачи из RelianceNetConnect.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://www.reliancenetconnect.co.in/search.php?q=$query&lr=lang_$language&ie=utf-8&oe=utf-8";
		$html = $F->GetHTML($url, 'www.reliancenetconnect.co.in');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('td[class="arial12black-n"]') as $e)
			{
				$t = 'a[class="ver13bld-b"]';
				$d = 'span[class="arial12black-n"]';

				if (isset($e->find($t, 0)->plaintext))
				{
					$title = html_entity_decode($e->find($t, 0)->plaintext, ENT_QUOTES, 'UTF-8');
				}

				if (isset($e->find($d, 0)->plaintext))
				{
					$description = html_entity_decode($e->find($d, 0)->plaintext, ENT_QUOTES, 'UTF-8');
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
			$F->Error("Can't create outgoing request. Please check RelianceNetConnect snippets plugin.");
		}

		return array_unique($snippets, SORT_REGULAR);
	}
}