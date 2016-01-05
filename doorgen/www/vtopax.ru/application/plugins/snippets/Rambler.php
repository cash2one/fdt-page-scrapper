<?php

class Rambler
{
	# Функция парсинга выдачи из Rambler.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$languages = array("ru" => "1", "en" => "2", "ua" => "3", "kk" => "4", "be" => "5", "tt" => "6", "fr" => "7", "de" => "8");
		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://nova.rambler.ru/search?query=$query&dlang=$languages[$language]";
		$html = $F->GetHTML($url, 'nova.rambler.ru');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('li[class="b-serp__list_item"]') as $e)
			{
				$t = 'h2[class="b-serp__list_item_title"]';
				$d = 'p[class="b-serp__list_item_snippet"]';

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
			$F->Error("Can't create outgoing request. Please check Rambler snippets plugin.");
		}

		return $snippets;
	}
}