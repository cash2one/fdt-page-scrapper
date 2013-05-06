<?php
class CaseValueSelector
{
	function getCaseTitle($con,$reg_type,$case,$region_name)
	{
		$result = array();
		$query_case_list = "select c.case_code_value, c.case_value from `doorgen_banks`.`case` c where c.location_type_code_value = ? AND c.location_id = (select region_id from `doorgen_banks`.`region` r where r.region_name_latin like replace(LOWER(?),'-','_')) ORDER BY c.case_code_value ASC";
		if (!($stmt = mysqli_prepare($con,$query_case_list))) {
			echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		}
		//set values
		#echo "set value...";
		$id=1;
		if (!mysqli_stmt_bind_param($stmt, "dds", $reg_type,$case,$region_name)) {
			echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		}
		
		#echo "execute...";
		if (!mysqli_stmt_execute($stmt)){
			echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		}

		/* instead of bind_result: */
		#echo "get result...";
		if(!mysqli_stmt_bind_result($stmt, $case_code_value, $case_region_name)){
			echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		}
		
		//TODO Возвращать массив всех возможных падежей, а затем уже из массива извлекать нужное.
		while(mysqli_stmt_fetch($stmt)) {
			$result[$case_code_value] = $case_region_name;
		}
		mysqli_stmt_close($stmt);
		
		return $result;
	}
}
?>