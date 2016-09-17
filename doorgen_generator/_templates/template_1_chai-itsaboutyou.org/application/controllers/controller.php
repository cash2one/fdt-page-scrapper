<?php

#============================
# Основной класс контроллера.
#============================
abstract class Controller
{
	# Конструктор.
	function __construct(){}

	# Полная обработка HTTP запроса.
	public function Request()
	{
		$this->OnInput();
		$this->OnOutput();
	}

	# Виртуальный обработчик запроса.
	protected function OnInput(){}

	# Виртуальный генератор HTML.
	protected function OnOutput(){}

	# Запрос произведен методом GET?
	protected function IsGet()
	{
		return $_SERVER['REQUEST_METHOD'] == 'GET';
	}

	# Запрос произведен методом POST?
	protected function IsPost()
	{
		return $_SERVER['REQUEST_METHOD'] == 'POST';
	}

	# Генерация HTML шаблона.
	protected function Template($filename, $vars = array())
	{
		foreach ($vars as $key => $value)
		{
			$$key = $value;
		}

		ob_start();
		require "templates/" . TEMPLATE . "/$filename.php";
		return ob_get_clean();
	}
}

/* End of file controller.php */
/* Location: ./application/controllers/controller.php */