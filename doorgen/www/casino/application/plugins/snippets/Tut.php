<?php

class Tut
{
	# Функция парсинга выдачи из Tut.
	public function Start($string, $language, $count, $F)
	{
		$snippets = array();
		$page = rand(1,20);
		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		#$url = "http://search.tut.by/?str=$query";
		$url = "http://search.tut.by/?status=1&$language=1&encoding=1&page=$page&how=rlv&query=$query";
		$html = $F->GetHTML($url, 'search.tut.by');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('li[class="sBCRItem"]') as $e)
			{
				$t = 'h3';
				$d = 'div[class="sBCRText"]';

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
			$F->Error("Can't create outgoing request. Please check Tut snippets plugin.");
		}

		return $snippets;
	}
}