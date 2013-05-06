<?php
class CaseValueSelector
{
	function getCaseTitle($con,$reg_type,$case,$region_name)
	{
		$query_case_list = "select c.case_value from `doorgen_banks`.`case` c where c.location_type_code_value = ? AND c.case_code_value = ? and c.location_id = (select region_id from `doorgen_banks`.`region` r where r.region_name_latin like replace(LOWER(?),'-','_'))";
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
		if(!mysqli_stmt_bind_result($stmt, $case_region_name)){
			echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		}
		
		if (mysqli_stmt_fetch($stmt)) {
		}
		mysqli_stmt_close($stmt);
		
		return $case_region_name;
	}
}
?>