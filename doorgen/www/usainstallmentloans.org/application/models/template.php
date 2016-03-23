<?php

class Template
{
	# Свойства.
	private $F;
	public $query;
	private $elements;

	# Конструктор.
	public function __construct($F = null, $query = null, $elements = null)
	{
		$this->F = $F;
		$this->query = $query;
		$this->elements = $elements;
	}

	# Клон.
	private function __clone(){}

	# Функцию получения заголовка страницы.
	public function getTitle()
	{
		$page = $this->F->GetController($_SERVER['REQUEST_URI']);
		if (!empty($page))
		{
			if (empty($this->query))
				$title = TITLE;
			else
				$title = mb_strtoupper(mb_substr($this->query, 0, 1, 'UTF-8'), 'UTF-8') . mb_substr(mb_convert_case($this->query, MB_CASE_LOWER, 'UTF-8'), 1, mb_strlen($this->query), 'UTF-8') . ' - ' . TITLE;
		}
		else
		{
			$title = TITLE;
		}

		return $title;
	}

	# Функция получения корневого каталога сайта.
	public function getBase()
	{
		if (isset($_POST['referer']) and !empty($_POST['referer']))
		{
			$base = '<base href="http://' . $_POST['referer'] . '">' . "\n";
		}
		else
		{
			$base = '<base href="' . $this->F->host . '">' . "\n";
		}

		return $base;
	}

	# Функцию получения мета-тега description.
	public function getDescription()
	{
		if (!empty($this->elements['snippets']))
		{
			$content = $this->elements['snippets'][0]['description'] . ' - ' . $this->elements['snippets'][0]['title'];
			$description = '<meta name="description" content="' . htmlentities($content, ENT_QUOTES, 'UTF-8') . '">' . "\n";
		}
		else
		{
			$description = '<meta name="description" content="">' . "\n";
		}

		return $description;
	}

	# Функцию получения мета-тега keywords.
	public function getKeywords()
	{
		if (!empty($this->query))
		{
			$content = str_replace(' ', ', ', $this->query);
			$keywords = '<meta name="keywords" content="' . $content . '">' . "\n";
		}
		else
		{
			$keywords = '<meta name="keywords" content="">' . "\n";
		}

		return $keywords;
	}

	# Функция получения ссылки на ico файл.
	public function getICON($file)
	{
		$link = '<link type="image/x-icon" rel="shortcut icon" href="' . $this->F->host . '/templates/' . TEMPLATE . '/' . $file . '">' . "\n";

		return $link;
	}

	# Функция получения ссылки на css файл.
	public function getCSS($file)
	{
		$link = '<link type="text/css" rel="stylesheet" href="' . $this->F->host . '/templates/' . TEMPLATE . '/css/' . $file . '">' . "\n";

		return $link;
	}

	# Функция получения ссылки на js файл.
	public function getJS($file)
	{
		$link = '<script type="text/javascript" src="' . $this->F->host . '/templates/' . TEMPLATE . '/js/' . $file . '"></script>' . "\n";

		return $link;
	}

	# Функция получения ссылки на rss ленту.
	public function getRSS()
	{
		if (!empty($this->query))
		{
			$page = $this->F->GetController($_SERVER['REQUEST_URI']);

			if (empty($page))
				$rss = $this->F->CreateUrl('/rss');
			else
				$rss = $this->F->CreateUrl('/rss/' . urlencode($this->query));

			return $rss;
		}
	}

	# Функция получения ссылки на js файл редиректа.
	public function getLIB()
	{
		$link = '<script type="text/javascript" src="' . $this->F->host . '/application/libraries/lib.js"></script>' . "\n";

		return $link;
	}

	# Функция получения src ссылки на картинку шаблона.
	public function getIMG($file)
	{
		$src = $this->F->host . '/templates/' . TEMPLATE . '/img/' . $file;

		return $src;
	}

	# Функция получения текущего запроса.
	public function getQuery()
	{
		return $this->query;
	}

	# Функция получения случайных запросов.
	public function getQueries($count, $tag = null, $route = null, $class = null)
	{
		$requests = '';
		$queries = $this->F->GetQueries($count);

		if (!empty($queries))
		{
			foreach($queries as $query)
			{
				if (!empty($tag))
				{
					if (!empty($route))
						$requests .= '<' . $tag . ' class="query ' . $class . '"><a href="' . $this->F->CreateUrl("/$route/$query") . '">' . $query . '</a></' . $tag . '>' . "\n";
					else
						$requests .= '<' . $tag . ' class="query ' . $class . '"><a href="' . $this->F->CreateUrl("/" . RESULT . "/$query") . '">' . $query . '</a></' . $tag . '>' . "\n";
				}
				else
				{
					if (!empty($route))
						$requests .= '<a class="' . $class . '" href="' . $this->F->CreateUrl("/$route/$query") . '">' . $query . '</a>' . "\n";
					else
						$requests .= '<a class="' . $class . '" href="' . $this->F->CreateUrl("/" . RESULT . "/$query") . '">' . $query . '</a>' . "\n";
				}
			}
		}

		return $requests;
	}

	# Функция получения сниппета.
	public function getSnippet($key, $tag = null, $route = null, $class = null)
	{
		$object = new stdClass();

		if (isset($this->elements['snippets'][$key]))
		{
			$snippet = $this->elements['snippets'][$key];

			if (empty($snippet['link']))
			{
				$url = $this->getExternalUrl();
				if ($url)
					$title = '<' . $tag . ' class="without title ' . $class . '" title="' . $snippet['words'] . '" url="' . $url . '">' . $snippet['title'] . '</' . $tag . '>' . "\n";
				else
					$title = '<' . $tag . ' class="without title ' . $class . '" title="' . $snippet['words'] . '">' . $snippet['title'] . '</' . $tag . '>' . "\n";
			}
			else
			{
				if (empty($route))
					$title = '<' . $tag . ' class="title ' . $class . '">' . $snippet['link'] . '</' . $tag . '>' . "\n";
				else
					if (count($this->elements['snippets']) - 1 == $key)
						$title = '<' . $tag . ' class="title ' . $class . '">' . $snippet['link'] . '</' . $tag . '>' . "\n";
					else
						$title = '<' . $tag . ' class="title ' . $class . '"><a href="' . $this->F->CreateUrl("/$route/" . $snippet['words']) . '" title="' . $snippet['title'] . '">' . $snippet['title'] . '</a></' . $tag . '>' . "\n";
			}

			if (!empty($tag))
				$description = '<' . $tag . ' class="description ' . $class . '">' . $snippet['description'] . '</' . $tag . '>' . "\n";
			else
				$description = $snippet['description'] . "\n";

			$object->{'title'} = $title;
			$object->{'description'} = $description;
		}
		else
		{
			$object->{'title'} = '';
			$object->{'description'} = '';
		}

		return $object;
	}

	# Функция получения картинки.
	public function getImage($key, $tag = null, $class = null)
	{
		$object = new stdClass();

		if (isset($this->elements['images'][$key]))
		{
			$image = $this->elements['images'][$key];
			$small = '<img class="' . $class . '" src="' . $this->F->GetImage($image['small']) . '" alt="' . $image['title'] . '" title="' . $image['title'] . '">' . "\n";
			$large = '<img class="' . $class . '" src="' . $this->F->GetImage($image['large']) . '" alt="' . $image['title'] . '" title="' . $image['title'] . '">' . "\n";

			if (!empty($tag))
				$frame = '<' . $tag . ' class="image ' . $class . '"><a href="#i' . $key . '" rel="prettyPhoto[i]">' . $small . '</a></' . $tag . '><div id="i' . $key . '" style="display:none;">' . $large . '</div>' . "\n";
			else
				$frame = '<a href="#i' . $key . '" rel="prettyPhoto">' . $small . '</a><div id="i' . $key . '" style="display:none;">' . $large . '</div>' . "\n";

			$object->{'frame'} = $frame;
			$object->{'title'} = $image['title'];
			$object->{'small'} = $small;
			$object->{'large'} = $large;
			$object->{'srcs'} = $image['small'];
			$object->{'srcl'} = $image['large'];
		}
		else
		{
			$object->{'frame'} = '';
			$object->{'title'} = '';
			$object->{'small'} = '';
			$object->{'large'} = '';
			$object->{'srcs'} = '';
			$object->{'srcl'} = '';
		}

		return $object;
	}

	# Функция получения видео.
	public function getVideo($key, $tag = null, $class = null)
	{
		$object = new stdClass();

		if (isset($this->elements['videos'][$key]))
		{
			$video = $this->elements['videos'][$key];
			$image = '<img class="' . $class . '" src="' . $this->F->GetImage($video['src']) . '" alt="' . $video['title'] . '" title="' . $video['title'] . '">' . "\n";

			if (!empty($tag))
				$frame = '<' . $tag . ' class="video ' . $class . '"><a href="#v' . $key . '" rel="prettyPhoto[v]">' . $image . '</a></' . $tag . '><div id="v' . $key . '" style="display:none;"><iframe width="640" height="480" src="' . $video['url'] . '" frameborder="0" allowfullscreen></iframe></div>' . "\n";
			else
				$frame = '<a href="#v' . $key . '" rel="prettyPhoto[v]">' . $image . '</a><div id="v' . $key . '" style="display:none;"><iframe width="640" height="480" src="' . $video['url'] . '" frameborder="0" allowfullscreen></iframe></div>' . "\n";

			$object->{'frame'} = $frame;
			$object->{'title'} = $video['title'];
			$object->{'image'} = $image;
			$object->{'url'} = $video['url'];
		}
		else
		{
			$object->{'frame'} = '';
			$object->{'title'} = '';
			$object->{'image'} = '';
			$object->{'url'} = '';
		}

		return $object;
	}

	# Функция получения комментария.
	public function getComment($key, $tag = null, $class = null)
	{
		$object = new stdClass();

		if (isset($this->elements['comments'][$key]))
		{
			$comment = $this->elements['comments'][$key];

			if (!empty($tag))
				$avatar = '<' . $tag . ' class="' . $class . '"><img src="' . $this->F->GetImage($this->F->host . '/' . $comment['avatar']) . '"></' . $tag . '>';
			else
				$avatar = '<img src="' . $this->F->GetImage($this->F->host . '/' . $comment['avatar']) . '">';

			if (!empty($tag))
				$nick = '<' . $tag . ' class="' . $class . '">' . $comment['nick'] . '</' . $tag . '>';
			else
				$nick = $comment['nick'];

			if (!empty($tag))
				$text = '<' . $tag . ' class="' . $class . '">' . $comment['text'] . '</' . $tag . '>';
			else
				$text = $comment['text'];

			$object->{'avatar'} = $avatar;
			$object->{'nick'} = $nick;
			$object->{'text'} = $text;
		}
		else
		{
			$object->{'avatar'} = '';
			$object->{'nick'} = '';
			$object->{'text'} = '';
		}

		return $object;
	}

	# Функция подключения файла.
	public function incFile($file)
	{
		if(file_exists("templates/" . TEMPLATE . "/userinc/$file") and filesize("templates/" . TEMPLATE . "/userinc/$file") > 0)
		{
			require "templates/" . TEMPLATE . "/userinc/$file";
		}
	}

	# Функция получения случайного url запроса.
	public function getUrl($route = null)
	{
		$query = $this->F->Query();
		if(!empty($query))
		{
			if (!empty($route))
				$url = $this->F->CreateUrl("/$route/$query");
			else
			{
				$url = $this->F->CreateUrl("/" . RESULT . "/$query");
			}

			return $url;
		}
	}

	# Функция получения внешней ссылки с текущим запросом.
	public function getExternalUrl()
	{
		$url = $this->F->Url($this->query);

		return $url;
	}

	# Функция получения url на страницу.
	public function Page($name)
	{
		$url = $this->F->CreateUrl("/$name");

		return $url;
	}
}