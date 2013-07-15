<?php

class Bing
{
	# Функция парсинга выдачи из Bing.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://www.bing.com/search?q=$query+language:$language";
		$html = $F->GetHTML($url, 'www.bing.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('ul[id="wg0"] li') as $e)
			{
				$t = 'div[class="sa_cc"] div[class="sb_tlst"] h3 a';
				$d = 'div[class="sa_cc"] p';

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
			$F->Error("Can't create outgoing request. Please check Bing snippets plugin.");
		}

		return $snippets;
	}
}