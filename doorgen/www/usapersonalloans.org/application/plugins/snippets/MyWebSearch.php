<?php

class MyWebSearch
{
	# Функция парсинга выдачи из MyWebSearch.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();

		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		$url = "http://search.mywebsearch.com/mywebsearch/GGmain.jhtml?searchfor=$query";
		$html = $F->GetHTML($url, 'search.mywebsearch.com');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('div[class="algoLinkBox"]') as $e)
			{
				$t = 'a[class="pseudolink"]';
				$d = 'span[class="nDesc"]';

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
			$F->Error("Can't create outgoing request. Please check MyWebSearch snippets plugin.");
		}

		return $snippets;
	}
}