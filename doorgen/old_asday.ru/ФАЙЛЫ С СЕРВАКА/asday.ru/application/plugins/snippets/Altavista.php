<?php

class Altavista
{
	# Функция парсинга выдачи из Altavista.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://search.yahoo.com/search?p=$query&vl=lang_$language&ei=utf-8";
		$html = $F->GetHTML($url, 'search.yahoo.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('ol li') as $e)
			{
				$t = 'div[class="res"] div h3 a';
				$d = 'div[class="res"] div[class="abstr"]';

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
			$F->Error("Can't create outgoing request. Please check Altavista snippets plugin.");
		}

		return $snippets;
	}
}