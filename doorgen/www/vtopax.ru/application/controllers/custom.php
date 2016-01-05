<?php

#===========================
# Подключение нужных файлов.
#===========================
require_once "base.php";

#=============================
# Контроллер обычной страницы.
#=============================
class Custom extends Base
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

		$controller = $this->F->GetController($_SERVER['REQUEST_URI']);

		$this->F->View($controller, array('T' => $this->T));
	}

	# Виртуальный генератор HTML.
	protected function OnOutput(){}
}

/* End of file custom.php */
/* Location: ./application/controllers/custom.php */