<?php
function getKeyInfo($con,$page_key)
{
	$result_array = array();
	$query_case_list = "SELECT key_value, key_value_latin, unix_timestamp(posted_time) posted_time FROM page WHERE key_value_latin = ?";
	if (!($stmt = mysqli_prepare($con,$query_case_list))) {
		#echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	//set values
	#echo "set value...";
	$id=1;
	if (!mysqli_stmt_bind_param($stmt, "s", $page_key)) {
		#echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		#echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	/* instead of bind_result: */
	#echo "get result...";
	if(!mysqli_stmt_bind_result($stmt, $key_value,$key_value_latin,$posted_time)){
		#echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	if(mysqli_stmt_fetch($stmt)) {
		$result_array = array(	"key_value"=>$key_value,
						"key_value_latin"=>$key_value_latin,
						"posted_time"=>$posted_time
					);	
	}else{
		#echo "Fetching results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		#print_r(error_get_last());
	}
	
	mysqli_stmt_close($stmt);
	
	return $result_array;
}

function getPageInfo($con,$page_url)
{
	$result_array = array();
	$query_case_list = "SELECT cp.cached_page_id, cp.cached_page_title, cp.cached_page_meta_keywords, cp.cached_page_meta_description, cp.cached_time FROM `cached_page` cp WHERE 1 AND cp.cached_page_url = ?";
	if (!($stmt = mysqli_prepare($con,$query_case_list))) {
		#echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	//set values
	#echo "set value...";
	$id=1;
	if (!mysqli_stmt_bind_param($stmt, "s", $page_url)) {
		#echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		#echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}

	/* instead of bind_result: */
	#echo "get result...";
	if(!mysqli_stmt_bind_result($stmt, $cached_page_id, $cached_page_title, $cached_page_meta_keywords, $cached_page_meta_description, $cached_time)){
		#echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
	}
	
	if(mysqli_stmt_fetch($stmt)) {
		$result_array = array(	
						"cached_page_id"=>$cached_page_id,
						"cached_page_title"=>$cached_page_title,
						"cached_page_meta_keywords"=>$cached_page_meta_keywords,
						"cached_page_meta_description"=>$cached_page_meta_description, 
						"cached_time"=>$cached_time
					);	
	}else{
		#echo "Fetching results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		#print_r(error_get_last());
	}
		
	mysqli_stmt_close($stmt);
	
	return $result_array;
}

function savePageInfo($conn,$page_url, $title, $keywords, $description)
{
	#echo "Saving page info..<br/>";
	$query_case_list = "INSERT INTO cached_page (cached_page_url, cached_page_title, cached_page_meta_keywords, cached_page_meta_description, cached_time) VALUES (?,?,?,?,now())";
	if (!($stmt = mysqli_prepare($conn,$query_case_list))) {
		#echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		#echo "End prepare field.";
	}
	//set values
	#echo "set value...";
	if (!mysqli_stmt_bind_param($stmt, "ssss", $page_url,$title,$keywords,$description)) {
		#echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		#print_r(error_get_last());
	}
	
	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		#echo "Saving failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		#print_r(error_get_last());
	}

	$conn->commit();
	
	mysqli_stmt_close($stmt);
}

function fillSnippetsContent($template, $key_value, $conn, $page_url){
	$snippets_array = array();
	
	#scrap snippets for page
	scrapPageSnippets($snippets_array, $key_value, $conn, $page_url);
	
	$SNIPPET_BLOCK_1 = "<div class='wrap border-bot-1'><img src='[SNIPPET_IMG_SMALL_[INDEX]]' alt=''><p class='text-1 top-2 p3'><h2>[SNIPPET_TITLE_[INDEX]]</h2></p><p>[SNIPPET_CONTENT_[INDEX]]</p><br/></div>";
	$start_block_index = 1;
	for($i=1; $i <=3; $i++){
		if(isset($snippets_array[$i-1])){
			$template=preg_replace("/\[SNIPPET_BLOCK_1_".$start_block_index."\]/", preg_replace("/\[INDEX\]/", $i, $SNIPPET_BLOCK_1), $template);
			$start_block_index++;
		}
	}
	
	$SNIPPET_BLOCK_2 = "<div class='wrap'><div class='number'>[NUMBER]</div><p class='extra-wrap border-bot-1'><span class='clr-1'><h2>[SNIPPET_TITLE_[INDEX]]</h2></span><br>[SNIPPET_CONTENT_[INDEX]]</p></div>";
	$start_block_index = 1;
	for($i=4; $i <=6; $i++){
		if(isset($snippets_array[$i-1])){
			$template=preg_replace("/\[SNIPPET_BLOCK_2_".$start_block_index."\]/", preg_replace("/\[INDEX\]/", $i, preg_replace("/\[NUMBER\]/", $start_block_index, $SNIPPET_BLOCK_2)), $template);
			$start_block_index++;
		}
	}
	
	$SNIPPET_BLOCK_3 = "<div class='wrap border-bot-1'><img src='[SNIPPET_IMG_SMALL_[INDEX]]' alt='' class='img-indent'><p class='extra-wrap'><span class='clr-1'><h2>[SNIPPET_TITLE_[INDEX]]</h2></span><br>[SNIPPET_CONTENT_[INDEX]]</p></div>";
	$start_block_index = 1;
	for($i=7; $i <=9; $i++){
		if(isset($snippets_array[$i-1])){
			$template=preg_replace("/\[SNIPPET_BLOCK_3_".$start_block_index."\]/", preg_replace("/\[INDEX\]/", $i, $SNIPPET_BLOCK_3), $template);
			$start_block_index++;
		}
	}
	
	//clear empty blocks
	for($i=1; $i <=3; $i++){
		for($j=1; $j <=3; $j++){
			$template=preg_replace("/\[SNIPPET_BLOCK_".$i."_".$j."\]/", "", $template);
		}
	}
	
	for($i=0; $i < 9; $i++){
		if(isset($snippets_array[$i])){
			$template=preg_replace("/\[SNIPPET_TITLE_".($i+1)."\]/", $snippets_array[$i]["title"], $template);
			$template=preg_replace("/\[SNIPPET_CONTENT_".($i+1)."\]/", $snippets_array[$i]["description"], $template);
			$template=preg_replace("/\[SNIPPET_IMG_LARGE_".($i+1)."\]/", isset($snippets_array[$i]["large"])?$snippets_array[$i]["large"]:"", $template);
			$template=preg_replace("/\[SNIPPET_IMG_SMALL_".($i+1)."\]/", isset($snippets_array[$i]["small"])?$snippets_array[$i]["small"]:"", $template);
		}else{
			$template=preg_replace("/\[SNIPPET_TITLE_".($i+1)."\]/", "", $template);
			$template=preg_replace("/\[SNIPPET_CONTENT_".($i+1)."\]/", "", $template);
			$template=preg_replace("/\[SNIPPET_IMG_LARGE_".($i+1)."\]/", "", $template);
			$template=preg_replace("/\[SNIPPET_IMG_SMALL_".($i+1)."\]/", "", $template);
		}
	}
	
	unset($snippets_array);
	
	return $template;
}

function scrapPageSnippets(&$snippets_array, $key_value, $conn, $page_url){
	$function = new Functions;
	$snippet_extractor = new Google;
	$google_image = new ImagesGoogle;

	$snippets_array = getPageSnippets($conn,$page_url);
	
	if(count($snippets_array) == 0){
		#echo "Saving snippets.";
		$rand_index_array = array();
		$index = 0;
		while(count($rand_index_array) < 3){
			$rand_value = rand(0,8);
			if(!in_array($rand_value,$rand_index_array)){
				$rand_index_array[$index] = $rand_value;
				$index++;
			}
		}
		
		$snippet_image_array = $google_image->Start($key_value,count($rand_index_array),$function);
		$snippet_array = array();
		while(!$snippet_array){
			$snippet_array = $snippet_extractor->Start($key_value,'ru',count($rand_index_array),$function);
		}
		
		for($i=0; $i < count($rand_index_array); $i++){
			$snippets_array[$rand_index_array[$i]]['title'] = preg_replace('/ {0,}\.{2,}/','.',$snippet_array[$i]['title']);
			$snippets_array[$rand_index_array[$i]]['description'] = preg_replace('/ {0,}\.{2,}/','.',$snippet_array[$i]['description']);
			if($snippet_image_array && $snippet_image_array[$i]){
				$snippets_array[$rand_index_array[$i]]['small'] = $snippet_image_array[$i]['small'];
				$snippets_array[$rand_index_array[$i]]['large'] = $snippet_image_array[$i]['large'];
			}
		}
		
		savePageSnippets($conn, $page_url, $snippets_array);
	}
}

function savePageSnippets($conn, $page_url, $snippets_array)
{	
	#echo "Saving snippets procdedure..";
	#var_dump($snippets_array);
	for($i = 0; $i < 9; $i++){
		if(isset($snippets_array[$i])){
			
			$query_case_list = "INSERT INTO snippets (cached_page_id, snippets_index, snippets_title, snippets_content, snippets_image_large, snippets_image_small, created_time) SELECT cp.cached_page_id,?,?,?,?,?,now() FROM cached_page cp WHERE cp.cached_page_url = ?";
			if (!($stmt = mysqli_prepare($conn,$query_case_list))) {
				#echo "savePageSnippets: Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
				#print_r(error_get_last());
			}
			//set values
			#echo "set value...";
			if (!mysqli_stmt_bind_param($stmt, "dsssss", $i, $snippets_array[$i]["title"],$snippets_array[$i]["description"],$snippets_array[$i]["large"],$snippets_array[$i]["small"], $page_url)) {
				#echo "savePageSnippets: Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
				#print_r(error_get_last());
			}
			
			#echo "execute...";
			if (!mysqli_stmt_execute($stmt)){
				#echo "savePageSnippets: Saving failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
				#print_r(error_get_last());
			}

			#echo "commit...";
			$conn->commit();
			
			mysqli_stmt_close($stmt);
		}
	}
}

function getPageSnippets($conn,$page_url)
{
	$snippets_array = array();
	$query_case_list = "select snp.snippets_index, snp.cached_page_id, snp.snippets_title, snp.snippets_content, snp.snippets_image_large, snp.snippets_image_small from snippets snp where snp.cached_page_id IN (select cp.cached_page_id from cached_page cp where cp.cached_page_url = ?)";
	if (!($stmt = mysqli_prepare($conn,$query_case_list))) {
		#echo "Prepare failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		#print_r(error_get_last());
	}
	//set values
	#echo "set value...";
	if (!mysqli_stmt_bind_param($stmt, "s", $page_url)) {
		#echo "Binding parameters failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		#print_r(error_get_last());
	}
	
	#echo "execute...";
	if (!mysqli_stmt_execute($stmt)){
		#echo "Execution failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		#print_r(error_get_last());
	}

	/* instead of bind_result: */
	#echo "get result...";
	if(!mysqli_stmt_bind_result($stmt, $snippets_index, $cached_page_id, $snippets_title, $snippets_content, $snippets_image_large, $snippets_image_small)){
		#echo "Getting results failed: (" . mysqli_connect_errno() . ") " . mysqli_connect_error()."<br>";
		#print_r(error_get_last());
	}
	
	while(mysqli_stmt_fetch($stmt)) {
		$snippets_array[$snippets_index] = array(	
						"cached_page_id"=>$cached_page_id,
						"title"=>$snippets_title,
						"description"=>$snippets_content, 
						"large"=>$snippets_image_large,
						"small"=>$snippets_image_small
					);	
	}
		
	mysqli_stmt_close($stmt);
	
	return $snippets_array;
}

?>