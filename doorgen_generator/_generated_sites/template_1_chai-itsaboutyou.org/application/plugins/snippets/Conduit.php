<?php

class Conduit
{
	# Функция парсинга выдачи из Conduit.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://search.conduit.com/Results.aspx?q=$query&lr=lang_$language&ie=utf-8&oe=utf-8";
		$html = $F->GetHTML($url, 'www.search.conduit.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('div[id="organic_results"] div[class="ads_wrapper"]') as $e)
			{
				$t = 'div[class="title"] a';
				$d = 'span[class="text"] span';

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
			$F->Error("Can't create outgoing request. Please check Conduit snippets plugin.");
		}

		return $snippets;
	}
}