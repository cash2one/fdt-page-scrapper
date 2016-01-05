<?php

class Ask
{
	# Функция парсинга выдачи из Ask.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://www.ru.ask.com/web?q=$query&ie=utf-8&oe=utf-8";
		$html = $F->GetHTML($url, 'www.ru.ask.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('div[id="lindm"] div[class="tsrc_tled"]') as $e)
			{
				$t = 'div[class="ptbs"] div a';
				$d = 'div[class="ptbs"] div[class="txt3 abstract"]';

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
			$F->Error("Can't create outgoing request. Please check Ask snippets plugin.");
		}

		return $snippets;
	}
}