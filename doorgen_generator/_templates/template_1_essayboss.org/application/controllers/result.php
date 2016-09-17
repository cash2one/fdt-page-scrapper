<?php

#===========================
# Подключение нужных файлов.
#===========================
require_once "base.php";

#=================================
# Контроллер страницы результатов.
#=================================
class Result extends Base
{
	# Свойства.
	private $page;

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
		$controller = $this->F->GetController($_SERVER['REQUEST_URI']);
		$this->page = $this->F->GetPage($controller);

		if (SUPPORT_32 == 'true')
		{
			if (isset($_POST[QUERY]) and !empty($_POST[QUERY]))
			{
				$query = $_POST[QUERY];
				header('Location: ' . $this->F->CreateUrl("/" . RESULT . "/$query"));
			}
		}
		else
		{
			if (isset($_POST[QUERY]) and !empty($_POST[QUERY]))
			{
				$this->F->View('404', array('T' => $this->T));

				exit();
			}
		}

		if (!empty($query))
		{
			$this->F->CheckQuery($query, array('T' => $this->T));

			$elements = $this->F->Run($query);
			$this->T = new Template($this->F, $query, $elements);

			if ($controller == 'search')
				$this->F->Ping($this->F->CreateUrl("/" . RESULT . "/$query"), $query);
		}
		else
		{
			$query = $this->F->Query();
			$elements = $this->F->GetCache($this->F->GetFile($query));
			$this->T = new Template($this->F, $query, $elements);

			if (in_array($controller, $this->F->GetFolders()))
				$this->page = "$controller/index";
			else
				$this->page = "result/index";
		}
	}

	# Виртуальный генератор HTML.
	protected function OnOutput()
	{
		$this->content = $this->Template($this->page, array('T' => $this->T));

		parent::OnOutput();
	}
}

/* End of file result.php */
/* Location: ./application/controllers/result.php */