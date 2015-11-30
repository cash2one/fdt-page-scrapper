<?php

#===========================
# Подключение нужных файлов.
#===========================
require_once "controller.php";
require_once "application/models/functions.php";
require_once "application/models/template.php";

#===========================
# Базовый класс контроллера.
#===========================
abstract class Base extends Controller
{
	# Свойства.
	protected $content;
	protected $F;
	protected $T;

	# Конструктор.
	function __construct(){}

	# Виртуальный обработчик запроса.
	protected function OnInput()
	{
		$this->content = '';
		$this->F = new Functions();
		$this->T = new Template($this->F);
	}

	# Виртуальный генератор HTML.
	protected function OnOutput()
	{
		$page = $this->Template('base', array('content' => $this->content, 'T' => $this->T));

		echo $page;
	}
}

/* End of file base.php */
/* Location: ./application/contollers/base.php */