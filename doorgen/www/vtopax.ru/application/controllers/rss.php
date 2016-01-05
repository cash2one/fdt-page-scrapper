<?php

#===========================
# Подключение нужных файлов.
#===========================
require_once "base.php";

#===============================
# Контроллер страницы RSS ленты.
#===============================
class Rss extends Base
{
	# Конструктор.
	function __construct()
	{
		parent::__construct();
	}

	# Виртуальный обработчик запроса.
	protected function OnInput()
	{
		parent::OnInput();

		$query = $this->F->GetQuery($_SERVER['REQUEST_URI']);
		if (empty($query))
		{
			$query = $this->F->Query();
		}

		$this->F->CheckQuery($query, array('T' => $this->T));

		$elements = $this->F->GetCache($this->F->GetFile($query));
		$rss = $this->F->RSS($query, $elements['snippets']);

		header('Content-Type: application/rss+xml; charset=UTF-8');
		print_r($rss);
	}

	# Виртуальный генератор HTML.
	protected function OnOutput(){}
}

/* End of file rss.php */
/* Location: ./application/controllers/rss.php */