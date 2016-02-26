<?php

#===========================
# Подключение нужных файлов.
#===========================
require_once "base.php";

#=============================
# Контроллер главной страницы.
#=============================
class Home extends Base
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

		if (strlen(KEYWORD) > 0)
		{
			$query = KEYWORD;
			$elements = $this->F->Run(KEYWORD);
		}
		else
		{
			$query = $this->F->Query();
			$elements = $this->F->GetCache($this->F->GetFile($query));
		}

		if (!empty($elements['snippets']))
		{
			$elements['snippets'] = $this->F->GetLinks($query, $elements['snippets']);
		}
		$this->T = new Template($this->F, $query, $elements);
	}

	# Виртуальный генератор HTML.
	protected function OnOutput()
	{
		$this->content = $this->Template('home', array('T' => $this->T));

		parent::OnOutput();
	}
}

/* End of file home.php */
/* Location: ./application/controllers/home.php */