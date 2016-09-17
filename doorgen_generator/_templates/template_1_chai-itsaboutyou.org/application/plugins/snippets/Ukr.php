<?php

class Ukr
{
	# Функция парсинга выдачи из Ukr.
	public function Start($string, $language, $count, $F)
	{
		$get_count = 50;
		$snippets = array();
		$page = rand(1,20);
		$query = urlencode(mb_strtolower($string, 'UTF-8'));
		//$url = "http://search.ukr.net/yandex/search.php?search_mode=ordinal&lang=$language&engine=1&search_query=$query&q=$query";
		$url = "http://search.ukr.net/yandex/search.php?search_mode=advanced&date=&lang=&search_query=$query&spcctx=notfar&zone=all&wordforms=all&lang=$language&within=0&from_day=&from_month=&from_year=&to_day=24&to_month=5&to_year=2013&mime=all&site=&rstr=&ds=&numdoc=$get_count&page=$page";
		#echo "url : ".$url."<br/>";
		
		$html = $F->GetHTML($url, 'search.ukr.net');

		if (!is_bool($html))
		{
			$i = 0;
			foreach ($html->find('ol[class="results"] li') as $e)
			{
				$t = 'div[class="title"] a';
				$d = 'div[class="text"]';

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
			$html = null; 
			$e = null;
			unset($html, $e);
		}
		else
		{
			$F->Error("Can't create outgoing request. Please check Ukr snippets plugin.");
		}

		return $snippets;
	}
}