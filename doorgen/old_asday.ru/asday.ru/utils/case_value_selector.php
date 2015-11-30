<?php
class CaseValueSelector
{
	function getCityValueByNewsKey($con,$news_key)
	{
		$query_case_list = "select c.city_name_latin from `city_page` cp,  `city` c where c.city_id = cp.city_id AND cp.city_page_key = LOWER(?)";
		
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
		
		//TODO Возвращать массив всех возможных падежей, а затем уже из массива извлекать нужное.
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
			$query_case_list = "select DISTINCT c.case_code_value, c.case_value from `case` c where c.location_type_code_value = 2 AND c.location_id = (select region_id from `region` r where r.region_name_latin like replace(LOWER(?),'-','_')) ORDER BY c.case_code_value ASC";
		}else{
			#city name
			$query_case_list = "select DISTINCT c.case_code_value, c.case_value from `case` c where c.location_type_code_value = 1 AND c.location_id in (select city_id from `city` r where r.city_name_latin like replace(LOWER(?),'-','_')) ORDER BY c.case_code_value ASC";
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
		
		//TODO Возвращать массив всех возможных падежей, а затем уже из массива извлекать нужное.
		while(mysqli_stmt_fetch($stmt)) {
			$result[$case_code_value] = $case_value;
		}
		mysqli_stmt_close($stmt);
		
		unset($stmt,$query_case_list,$id,$case_value);
		
		return $result;
	}
}
?>