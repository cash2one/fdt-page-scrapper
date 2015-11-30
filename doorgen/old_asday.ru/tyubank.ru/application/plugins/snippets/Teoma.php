<?php

class Teoma
{
	# Функция парсинга выдачи из Teoma.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://www.teoma.com/web?q=$query&lr=lang_$language&ie=utf-8&oe=utf-8";
		$html = $F->GetHTML($url, 'www.teoma.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('div[class="pad pr10 rwid pltbx"]') as $e)
			{
				$t = 'div a';
				$d = 'div[class="T1"]';

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
			$F->Error("Can't create outgoing request. Please check Teoma snippets plugin.");
		}

		return $snippets;
	}
}