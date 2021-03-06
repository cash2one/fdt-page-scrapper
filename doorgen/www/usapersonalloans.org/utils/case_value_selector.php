<?php
class CaseValueSelector
{
	function getCityValueByNewsKey($con,$news_key)
	{
		$query_case_list = 	" SELECT DISTINCT c.city_name_latin  " .
							" FROM door_keys k, city c " .
							" WHERE 1 " .
							" AND k.city_id = c.city_id " .
							" AND k.key_value_latin = ? " .
							" LIMIT 1 ";
		
		if (!($stmt = mysqli_prepare($con,$query_case_list))) {
			echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		}
		//set values
		#echo "set value...";
		$id=1;
		if (!mysqli_stmt_bind_param($stmt, "s", $news_key)) {
			echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		}
		
		#echo "execute...";
		if (!mysqli_stmt_execute($stmt)){
			echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		}

		/* instead of bind_result: */
		#echo "get result...";
		if(!mysqli_stmt_bind_result($stmt, $city_name_latin)){
			echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		}
		
		//TODO ���������� ������ ���� ��������� �������, � ����� ��� �� ������� ��������� ������.
		if(mysqli_stmt_fetch($stmt)) {
		}
		mysqli_stmt_close($stmt);
		
		unset($stmt,$query_case_list,$id);
		
		return $city_name_latin;
	}
	
	function getCaseTitle($con,$reg_type,$region_name)
	{
		$result = array();
		if($reg_type == 2){
			#region name
			$query_case_list =	" SELECT DISTINCT c.case_code_value, c.case_value  " .
								" FROM cases c  " .
								" WHERE 1 " .
								" AND c.location_type_code_value = 2  " .
								" AND c.location_id = (SELECT region_id FROM region r WHERE r.region_name_latin = ?) " .
								" ORDER BY c.case_code_value ASC ";
		}else{
			#city name
			$query_case_list = 	" SELECT DISTINCT c.case_code_value, c.case_value  " .
								" FROM cases c  " .
								" WHERE 1 " .
								" AND c.location_type_code_value = 1  " .
								" AND c.location_id in (SELECT city_id FROM `city` r WHERE r.city_name_latin = ?) " .
								" ORDER BY c.case_code_value ASC ";
		}
		if (!($stmt = mysqli_prepare($con,$query_case_list))) {
			echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		}
		//set values
		#echo "set value...";
		$id=1;
		if (!mysqli_stmt_bind_param($stmt, "s", $region_name)) {
			echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		}
		
		#echo "execute...";
		if (!mysqli_stmt_execute($stmt)){
			echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		}

		/* instead of bind_result: */
		#echo "get result...";
		if(!mysqli_stmt_bind_result($stmt, $case_code_value, $case_value)){
			echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		}
		
		//TODO ���������� ������ ���� ��������� �������, � ����� ��� �� ������� ��������� ������.
		while(mysqli_stmt_fetch($stmt)) {
			$result[$case_code_value] = $case_value;
		}
		mysqli_stmt_close($stmt);
		
		unset($stmt,$query_case_list,$id,$case_value);
		
		return $result;
	}
}
?>