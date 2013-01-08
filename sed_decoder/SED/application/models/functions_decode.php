<?php
class Functions
{
    public $_abfbd198884bed65b698b74d62c54cd47f44e048;
    
    public $_22b5520d76531e17f66ecd8a4c3c3980a5464e65570c2ae698cd887d;
    function __construct($www_folder = '')
    {
        require_once $www_folder . 'config.php';
        require_once $www_folder . 'application/libraries/parser.php';
        $this->Base($www_folder);
        $this->host = 'http://' . $_SERVER['HTTP_HOST'] . ROOT;
        $this->path = $www_folder;
    }
    
    
    private function __clone()
    {
    }
    
    
    private function Base($base_ww_folder)
    {
        $content_labels_array = array(
            'base',
            'cache'
        );
        foreach ($content_labels_array as $content_source) {
            if (!is_dir($base_ww_folder . "content/$content_source")) {
                if (@!mkdir($base_ww_folder . "content/$content_source", 0755)) {
                    exit("Невозможно создать папку $content_source. Установите 777 права доступа на папку content.");
                }
            }
        }
        $hex_labels_array = array(
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'a',
            'b',
            'c',
            'd',
            'e',
            'f'
        );
        for ($i = 0; $i < count($hex_labels_array); $i++) {
            if (!is_dir($base_ww_folder . "content/base/$hex_labels_array[$i]")) {
                if (mkdir($base_ww_folder . "content/base/$hex_labels_array[$i]", 0755)) {
                    foreach ($hex_labels_array as $hex_element) {
                        touch($base_ww_folder . "content/base/$hex_labels_array[$i]/$hex_element.txt");
                    }
                }
            }
        }
    }
    
    private function BaseWriter($_9e34c7cd15222976fd4def6b8fb0500c715c9b0665fbd0c797fe91bb2155e17f2e9e917a96aeb49dae219b8beaee0068fcfb5d9b5a70064e658faf12fd44013c, $_af523a2d8918ae60810bbc6b30799223)
    {
        $_9e34c7cd15222976fd4def6b8fb0500c715c9b0665fbd0c797fe91bb2155e17f2e9e917a96aeb49dae219b8beaee0068fcfb5d9b5a70064e658faf12fd44013c = $this->Clear($_9e34c7cd15222976fd4def6b8fb0500c715c9b0665fbd0c797fe91bb2155e17f2e9e917a96aeb49dae219b8beaee0068fcfb5d9b5a70064e658faf12fd44013c);
        $_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8 = md5($_9e34c7cd15222976fd4def6b8fb0500c715c9b0665fbd0c797fe91bb2155e17f2e9e917a96aeb49dae219b8beaee0068fcfb5d9b5a70064e658faf12fd44013c);
        $json_values_array = array(
            'md5' => $_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8,
            'query' => $_9e34c7cd15222976fd4def6b8fb0500c715c9b0665fbd0c797fe91bb2155e17f2e9e917a96aeb49dae219b8beaee0068fcfb5d9b5a70064e658faf12fd44013c,
            'translit' => $this->Convert($_9e34c7cd15222976fd4def6b8fb0500c715c9b0665fbd0c797fe91bb2155e17f2e9e917a96aeb49dae219b8beaee0068fcfb5d9b5a70064e658faf12fd44013c)
        );
        $_091db10488a55a1e6d20b517be60b85b345b47cce4d475df91b6325b62aa98cb = file("content/base/$_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8[0]/$_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8[1].txt", FILE_IGNORE_NEW_LINES);
        if (!in_array(json_encode($json_values_array), $_091db10488a55a1e6d20b517be60b85b345b47cce4d475df91b6325b62aa98cb)) {
            file_put_contents("content/base/$_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8[0]/$_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8[1].txt", json_encode($json_values_array) . "", FILE_APPEND | LOCK_EX);
        }
        if (ONLY_EXISTING_KEYWORDS == 'false') {
            foreach ($_af523a2d8918ae60810bbc6b30799223 as $_cac236123dd68b35ed08f1be3b9479a8e2d16980) {
                $_9e34c7cd15222976fd4def6b8fb0500c715c9b0665fbd0c797fe91bb2155e17f2e9e917a96aeb49dae219b8beaee0068fcfb5d9b5a70064e658faf12fd44013c = $_cac236123dd68b35ed08f1be3b9479a8e2d16980['words'];
                if (!empty($_9e34c7cd15222976fd4def6b8fb0500c715c9b0665fbd0c797fe91bb2155e17f2e9e917a96aeb49dae219b8beaee0068fcfb5d9b5a70064e658faf12fd44013c)) {
                    $_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8 = md5($_9e34c7cd15222976fd4def6b8fb0500c715c9b0665fbd0c797fe91bb2155e17f2e9e917a96aeb49dae219b8beaee0068fcfb5d9b5a70064e658faf12fd44013c);
                    $json_values_array                                 = array(
                        'md5' => $_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8,
                        'query' => $_9e34c7cd15222976fd4def6b8fb0500c715c9b0665fbd0c797fe91bb2155e17f2e9e917a96aeb49dae219b8beaee0068fcfb5d9b5a70064e658faf12fd44013c,
                        'translit' => $this->Convert($_9e34c7cd15222976fd4def6b8fb0500c715c9b0665fbd0c797fe91bb2155e17f2e9e917a96aeb49dae219b8beaee0068fcfb5d9b5a70064e658faf12fd44013c)
                    );
                    $_091db10488a55a1e6d20b517be60b85b345b47cce4d475df91b6325b62aa98cb = file("content/base/$_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8[0]/$_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8[1].txt", FILE_IGNORE_NEW_LINES);
                    if (!in_array(json_encode($json_values_array), $_091db10488a55a1e6d20b517be60b85b345b47cce4d475df91b6325b62aa98cb)) {
                        file_put_contents("content/base/$_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8[0]/$_124700b96720d08c6c125908c8d1f253a60c5915b7e69b3a1f3554be142d11a8[1].txt", json_encode($json_values_array) . "", FILE_APPEND | LOCK_EX);
                    }
                }
            }
        }
    }
    public function CheckQuery($_a986c8931418c3e11b53dae8b42eec29cef321a0a52500bb, $_d88696a833553e3d75affcbefb72fbbf)
    {
        $_5ce19675f36a08ad9e5f914bbe2e79e92842ccc3afe7b62d49267fc693967eb4 = md5($_a986c8931418c3e11b53dae8b42eec29cef321a0a52500bb);
        $json_values_array = array(
            'md5' => $_5ce19675f36a08ad9e5f914bbe2e79e92842ccc3afe7b62d49267fc693967eb4,
            'query' => $_a986c8931418c3e11b53dae8b42eec29cef321a0a52500bb,
            'translit' => $this->Convert($_a986c8931418c3e11b53dae8b42eec29cef321a0a52500bb)
        );
        $_0ceb14a690a0d94a59f362047ea1d6c548b42204                         = file("content/base/$_5ce19675f36a08ad9e5f914bbe2e79e92842ccc3afe7b62d49267fc693967eb4[0]/$_5ce19675f36a08ad9e5f914bbe2e79e92842ccc3afe7b62d49267fc693967eb4[1].txt", FILE_IGNORE_NEW_LINES);
        if (SUPPORT_32 == 'false') {
            if (!in_array(json_encode($json_values_array), $_0ceb14a690a0d94a59f362047ea1d6c548b42204)) {
                $this->View('404', $_d88696a833553e3d75affcbefb72fbbf);
                exit;
            }
        }
    }
    public function CheckUrl($url_for_check)
    {
        if (strpos($url_for_check, '?') == 1)
            return true;
        else
            return false;
    }
    public function Clear($str_for_clear)
    {
        $chars_for_replace_array = array(
            '+',
            '—',
            '.',
            ',',
            '=',
            '?',
            '%',
            ';',
            ':',
            '^',
            '$',
            '#',
            '!',
            '@',
            '№',
            '_',
            '/',
            '|',
            '[',
            ']',
            '{',
            '}',
            '&',
            '*',
            '(',
            ')',
            '<',
            '>',
            '-',
            '"',
            '»',
            '«',
            '~',
            '`',
            "'",
            'amp',
            'nbsp',
            'quot',
            '®',
            '©',
            '™',
            '•',
            '♥',
            '☆',
            '·',
            '›',
            ''
        );
        $str_for_clear = str_replace($chars_for_replace_array, ' ', $str_for_clear);
        return preg_replace('|s+|', ' ', trim($str_for_clear));
    }
    public function Convert($string_for_decode)
    {
        $string_for_decode = mb_strtolower($this->Clear($string_for_decode), 'UTF-8');
        $chars_mapping_array = array(
            'а' => 'a',
            'б' => 'b',
            'в' => 'v',
            'г' => 'g',
            'д' => 'd',
            'е' => 'e',
            'ё' => 'e',
            'ж' => 'zh',
            'з' => 'z',
            'и' => 'i',
            'й' => 'y',
            'к' => 'k',
            'л' => 'l',
            'м' => 'm',
            'н' => 'n',
            'о' => 'o',
            'п' => 'p',
            'р' => 'r',
            'с' => 's',
            'т' => 't',
            'у' => 'u',
            'ф' => 'f',
            'х' => 'h',
            'ц' => 'c',
            'ч' => 'ch',
            'ш' => 'sh',
            'щ' => 'sch',
            'ь' => '',
            'ы' => 'y',
            'ъ' => '',
            'э' => 'e',
            'ю' => 'yu',
            'я' => 'ya'
        );
        return str_replace(' ', '-', strtr($string_for_decode, $chars_mapping_array));
    }
    
    private function CreateCache($_bce54819, $_04d4ebf41f750ecc954f20b5a40037124c27570162678b90dd4fb5f54cb2cf3f9286bb2296c4a155c8b92e9ef77d120f720f64ba901ec6847912c5e7691e6581)
    {
        if (!file_exists($_bce54819)) {
            file_put_contents($_bce54819, json_encode($_04d4ebf41f750ecc954f20b5a40037124c27570162678b90dd4fb5f54cb2cf3f9286bb2296c4a155c8b92e9ef77d120f720f64ba901ec6847912c5e7691e6581));
        }
    }
    public function CreateUrl($_6e9bd5804d79ab41904a7ceec2ff0457f1bea6701a885b88)
    {
        if (isset($_POST['referer']) and !empty($_POST['referer'])) {
            $referer_url_value = $_POST['referer'];
            $kids_lines = $this->GetContents('txt/kids.txt');
            if (in_array($referer_url_value, $kids_lines)) {
                $referer_http_url = "http://$referer_url_value";
            }
        } else {
            $referer_http_url = $this->host;
        }
        $query_value = $this->GetQuery($_6e9bd5804d79ab41904a7ceec2ff0457f1bea6701a885b88);
        $controller_value = $this->GetController($_6e9bd5804d79ab41904a7ceec2ff0457f1bea6701a885b88);
        switch (FU_STATUS) {
            case 'ON':
                if (!empty($query_value)) {
                    switch (substr(FU_TYPE, 0, 4)) {
                        case 'FULL':
                            $referer_http_url .= "/$controller_value/" . $this->Convert($query_value) . "-" . substr(md5($query_value), 0, 2) . substr(FU_TYPE, 4);
                            break;
                        case 'RAND':
                            $referer_http_url .= "/$controller_value/" . md5($query_value) . substr(FU_TYPE, 4);
                            break;
                    }
                } else {
                    $referer_http_url .= "/$controller_value";
                }
                break;
            case 'OFF':
                if (!empty($query_value)) {
                    $referer_http_url .= !empty($controller_value) ? "/?" . CONTROLLER . "=$controller_value&" . QUERY . "=" . urlencode($query_value) : "";
                } else {
                    $referer_http_url .= !empty($controller_value) ? "/?" . CONTROLLER . "=$controller_value" : "";
                }
                break;
        }
        return $referer_http_url;
    }
    public function Dublier($_0b1dff4d63eba63737e7c5623c3884389416cdce736a31c91b87a309780145c0458f6c8fd9b42cc1)
    {
        foreach ($_0b1dff4d63eba63737e7c5623c3884389416cdce736a31c91b87a309780145c0458f6c8fd9b42cc1 as $_e456821cbc62b1a87f6e6c8a96e0e3e4) {
            $_3771ab040630eed7161ba000bcc361e04f4e5edf8a17d7015c6e086edc0f7e6372411effce8f7d0f66dd96d70849ef714e45dfafebb2a7a7801e67d70bbcf5aa[] = $_e456821cbc62b1a87f6e6c8a96e0e3e4['title'];
        }
        $_1372a58ef375e7947c1879169593de04739c009049d958a48bf1d7a2 = array_keys(array_unique($_3771ab040630eed7161ba000bcc361e04f4e5edf8a17d7015c6e086edc0f7e6372411effce8f7d0f66dd96d70849ef714e45dfafebb2a7a7801e67d70bbcf5aa));
        $_a98235b1e79b5558fa8f96dfb57bb12b                         = array();
        foreach ($_0b1dff4d63eba63737e7c5623c3884389416cdce736a31c91b87a309780145c0458f6c8fd9b42cc1 as $_f1d403294c64c19ce0eba621719c15a0 => $_e456821cbc62b1a87f6e6c8a96e0e3e4) {
            if (in_array($_f1d403294c64c19ce0eba621719c15a0, $_1372a58ef375e7947c1879169593de04739c009049d958a48bf1d7a2)) {
                $_a98235b1e79b5558fa8f96dfb57bb12b[] = $_e456821cbc62b1a87f6e6c8a96e0e3e4;
            }
        }
        return array_values($_a98235b1e79b5558fa8f96dfb57bb12b);
    }
    public function Error($error_msg)
    {
        $error_log_file_path = $this->path . 'content/errors.log';
        if (file_exists($error_log_file_path)) {
            $lines = file($error_log_file_path);
            if (count($lines) >= 1000) {
                unlink($error_log_file_path);
            }
        }
        $current_date = gmdate("d.m.y H:i:s", time() + 10800);
        file_put_contents($error_log_file_path, "$current_date - $error_msg", FILE_APPEND | LOCK_EX);
    }
    private function Filter($_48a53cf6b064de94dc2cb47968394dc6)
    {
        $stop_words_array = $this->GetContents('txt/stop-words.txt');
        if (!empty($stop_words_array)) {
            foreach ($stop_words_array as $stop_word_index => $stop_word_value) {
                if (preg_match('|^[*]|', $stop_word_value))
                    $stop_words_array[$stop_word_index] = str_replace('*', '', $stop_word_value) . '$';
                if (preg_match('|[*]$|', $stop_word_value))
                    $stop_words_array[$stop_word_index] = '^' . str_replace('*', '', $stop_word_value);
            }
            $stop_word_value = implode('|', $stop_words_array);
        }
        foreach ($_48a53cf6b064de94dc2cb47968394dc6 as $_c771d53a60957f9483ecd2f19406c4efcf8b77b3a47ff22d => $_b5802d64dc3214e02f00716b3f1460df2840456628713ae4c6dadf2fb247c91f) {
            $_23dabaf7bf78d57dd387f3b980ca0857968cb6b5 = str_replace('...', '', $_b5802d64dc3214e02f00716b3f1460df2840456628713ae4c6dadf2fb247c91f['title']);
            $stop_words_array = explode(' ', $this->Clear($_23dabaf7bf78d57dd387f3b980ca0857968cb6b5));
            $stop_words_array = array_slice($stop_words_array, 0, WORDS);
            $stop_words_array = implode(' ', $stop_words_array);
            $_48a53cf6b064de94dc2cb47968394dc6[$_c771d53a60957f9483ecd2f19406c4efcf8b77b3a47ff22d]['title'] = $_23dabaf7bf78d57dd387f3b980ca0857968cb6b5;
            $_48a53cf6b064de94dc2cb47968394dc6[$_c771d53a60957f9483ecd2f19406c4efcf8b77b3a47ff22d]['words'] = $stop_words_array;
            if (!empty($stop_word_value)) {
                $_77ae380258ac12b2c117cc78c99eed17 = mb_strtolower($stop_word_value, 'UTF-8');
                $_c921d1aa8e5e8c89848aff9d136fb9c9 = mb_strtolower($stop_words_array, 'UTF-8');
                if (preg_match("/$_77ae380258ac12b2c117cc78c99eed17/", $_c921d1aa8e5e8c89848aff9d136fb9c9)) {
                    $_48a53cf6b064de94dc2cb47968394dc6[$_c771d53a60957f9483ecd2f19406c4efcf8b77b3a47ff22d]['words'] = '';
                }
            }
        }
        return $_48a53cf6b064de94dc2cb47968394dc6;
    }
    public function GetCache($cache_file_path)
    {
        if (file_exists($cache_file_path)) {
            touch($cache_file_path);
            $cache_file_path = file_get_contents($cache_file_path);
            $json_array_values = json_decode($cache_file_path, true);
            return $json_array_values;
        }
    }
    public function GetContent($content_file_path)
    {
        $content_lines = $this->getContents($content_file_path);
        if (!empty($content_lines)) {
            $rand_line = array_rand($content_lines);
            return $content_lines[$rand_line];
        }
    }
    public function GetContents($file_name)
    {
        $path_to_file = $this->path . $file_name;
        if (file_exists($path_to_file) and filesize($path_to_file) > 0) {
            $lines = file($path_to_file, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
            return array_values(array_unique($lines));
        }
    }
    public function GetController($_7d51ecf6483ef83dad413133b992d964408af3d924936683079ae94ebb29f9f0)
    {
        if (strlen(ROOT) > 0 AND strstr($_7d51ecf6483ef83dad413133b992d964408af3d924936683079ae94ebb29f9f0, ROOT)) {
            $_7d51ecf6483ef83dad413133b992d964408af3d924936683079ae94ebb29f9f0 = substr($_7d51ecf6483ef83dad413133b992d964408af3d924936683079ae94ebb29f9f0, strlen(ROOT));
        }
        $_c664bf14d074e1e853aab333954ca37b2dd9935d = '';
        if ($this->CheckUrl($_7d51ecf6483ef83dad413133b992d964408af3d924936683079ae94ebb29f9f0) == true)
            $_d2c90c806c279c9556583e7582a5b041 = array_values(explode('&', $_7d51ecf6483ef83dad413133b992d964408af3d924936683079ae94ebb29f9f0));
        else
            $_d2c90c806c279c9556583e7582a5b041 = array_values(array_filter(explode('/', $_7d51ecf6483ef83dad413133b992d964408af3d924936683079ae94ebb29f9f0)));
        if (!empty($_d2c90c806c279c9556583e7582a5b041[0])) {
            if (strrchr($_d2c90c806c279c9556583e7582a5b041[0], '=')) {
                $_d2c90c806c279c9556583e7582a5b041[0] = substr(strrchr($_d2c90c806c279c9556583e7582a5b041[0], '='), 1);
            }
            $_c664bf14d074e1e853aab333954ca37b2dd9935d = $_d2c90c806c279c9556583e7582a5b041[0];
        }
        return $_c664bf14d074e1e853aab333954ca37b2dd9935d;
    }
    public function GetFile($_2a63ed6a7953b76bfde59bf048629621a88a175cc5f8ca7e)
    {
        if (!empty($_2a63ed6a7953b76bfde59bf048629621a88a175cc5f8ca7e)) {
            $_dc6fa59d3c5c707ac5f380ccb815aeb2f00b671352726e3aa6ff9808 = md5($_2a63ed6a7953b76bfde59bf048629621a88a175cc5f8ca7e);
            switch (CACHE) {
                case 'index':
                    $_830c64617d808c22cbac79542c109a508a7e41a1 = "content/cache/$_dc6fa59d3c5c707ac5f380ccb815aeb2f00b671352726e3aa6ff9808.json";
                    break;
                case 'disc':
                    $_acb85238 = implode('/', str_split($_dc6fa59d3c5c707ac5f380ccb815aeb2f00b671352726e3aa6ff9808, 1));
                    $_b8cb84225212fa57a73e186766644e9369e02c9925d03b656e0e2103c7e73ae36735d33fa1b8cceefe78afa8064a882c6e1cab3608a9cf1bb6ae427eec04f3e7 = pathinfo($_acb85238, PATHINFO_DIRNAME);
                    $_646d8196 = "content/cache/$_b8cb84225212fa57a73e186766644e9369e02c9925d03b656e0e2103c7e73ae36735d33fa1b8cceefe78afa8064a882c6e1cab3608a9cf1bb6ae427eec04f3e7";
                    if (!is_dir($_646d8196)) {
                        if (!mkdir($_646d8196, 0755, true)) {
                            $this->Error("Can't create directory $_646d8196.");
                        }
                    }
                    $_dc6fa59d3c5c707ac5f380ccb815aeb2f00b671352726e3aa6ff9808 = pathinfo($_acb85238, PATHINFO_FILENAME);
                    $_830c64617d808c22cbac79542c109a508a7e41a1                 = "$_646d8196/$_dc6fa59d3c5c707ac5f380ccb815aeb2f00b671352726e3aa6ff9808.json";
                    break;
            }
            return $_830c64617d808c22cbac79542c109a508a7e41a1;
        }
    }
    public function GetFolders()
    {
        $extra_folders_in_tempate_folder_array = array();
        $standart_folders_array = array(
            'userinc',
            'img',
            'css',
            'js'
        );
        $template_folder_array = glob("templates/" . TEMPLATE . "/*", GLOB_ONLYDIR);
        foreach ($template_folder_array as $template_folder) {
            $filename_value = pathinfo($template_folder, PATHINFO_FILENAME);
            if (!in_array($filename_value, $standart_folders_array)) {
                $extra_folders_in_tempate_folder_array[] = $filename_value;
            }
        }
        return $extra_folders_in_tempate_folder_array;
    }
    public function GetHTML($url_value, $host_value)
    {
        $browsers_lines = $this->GetContent('txt/browsers.txt');
        if (in_array('curl', get_loaded_extensions())) {
            $curl_descriptor = curl_init();
            curl_setopt($curl_descriptor, CURLOPT_URL, $url_value);
            curl_setopt($curl_descriptor, CURLOPT_RETURNTRANSFER, 1);
            curl_setopt($curl_descriptor, CURLOPT_USERAGENT, $browsers_lines);
            $proxy_list = $this->GetContent('txt/proxies.txt');
            if ($proxy_list) {
                curl_setopt($curl_descriptor, CURLOPT_PROXY, $proxy_list);
                curl_setopt($curl_descriptor, CURLOPT_PROXYTYPE, CURLPROXY_SOCKS5);
            }
            curl_setopt($curl_descriptor, CURLOPT_TIMEOUT, 15);
            $url_content = curl_exec($curl_descriptor);
            curl_close($curl_descriptor);
            $html_dom = str_get_html($url_content);
            if (is_bool($html_dom)) {
                $this->Error("Proxy $proxy_list does not work. Please remove it.");
            }
        } else {
            $request_params_array = array(
                'http' => array(
                    'method' => "GET",
                    'header' => "Host: $host_value" . "User-Agent: $browsers_lines"
                )
            );
            $html_file_content = stream_context_create($request_params_array);
            $html_dom = file_get_html($url_value, false, $html_file_content);
        }
        return $html_dom;
    }
    public function GetImage($image_content)
    {
        $encoded_image = $this->host . '/image.php?' . base64_encode($image_content);
        return $encoded_image;
    }
    public function GetKeyword()
    {
        $_01f300c5 = implode($this->GetQueries(1));
        return $_01f300c5;
    }
    public function GetLinks($_af331011bd23561cfa616cc345bf104468315b5b, $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc)
    {
        $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc = array_reverse($_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc);
        $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc = array_slice($_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc, 0, COUNT_SNIPPETS);
        if (STOP_GROWING == 'false') {
            switch (SUBJECT_LIMITATION) {
                case 'true':
                    $_2b55ce0a9dc42f0e103a7146cdcd8b8b = explode(' ', $_af331011bd23561cfa616cc345bf104468315b5b);
                    foreach ($_2b55ce0a9dc42f0e103a7146cdcd8b8b as $_4eff36155b347beceb090be634ed874dce1a1738a95d3b1c22bf7437b80af3974c2656ac6fcb04d5 => $_2c8b871e52d4e5f5db5ff84a82a45327e20df77edef961c4b6fa0e9c3d97ce5b) {
                        if (strlen($_2c8b871e52d4e5f5db5ff84a82a45327e20df77edef961c4b6fa0e9c3d97ce5b) < 4) {
                            unset($_2b55ce0a9dc42f0e103a7146cdcd8b8b[$_4eff36155b347beceb090be634ed874dce1a1738a95d3b1c22bf7437b80af3974c2656ac6fcb04d5]);
                        }
                    }
                    $_4de56d696b7ba50c59dc97ef762bdb0dc270fbe29c9f5ab1326a9e5fed02f911c28eb0ce39b618afdc73f7fd5fa96837e2f7bf0c0727739dea33902dd78fa03d = 0;
                    foreach ($_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc as $_f272a53b0ef1c576ff83d5cbdb2278970c863fb8 => $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3) {
                        if ($_4de56d696b7ba50c59dc97ef762bdb0dc270fbe29c9f5ab1326a9e5fed02f911c28eb0ce39b618afdc73f7fd5fa96837e2f7bf0c0727739dea33902dd78fa03d <= COUNT_LINKS) {
                            foreach ($_2b55ce0a9dc42f0e103a7146cdcd8b8b as $_2c8b871e52d4e5f5db5ff84a82a45327e20df77edef961c4b6fa0e9c3d97ce5b) {
                                if (preg_match('|' . mb_strtolower($_2c8b871e52d4e5f5db5ff84a82a45327e20df77edef961c4b6fa0e9c3d97ce5b, 'UTF-8') . '|', mb_strtolower($_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['title'], 'UTF-8'))) {
                                    if (ONLY_EXISTING_KEYWORDS == 'false') {
                                        if (!empty($_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['words'])) {
                                            $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[$_f272a53b0ef1c576ff83d5cbdb2278970c863fb8]['link'] = '<a href="' . $this->CreateUrl("/" . RESULT . "/" . $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['words']) . '" title="' . $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['title'] . '">' . $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['title'] . '</a>';
                                        }
                                    } else {
                                        $_5fcf2c9c13847fbe85391de6c646fa24                                                                      = $this->GetKeyword();
                                        $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[$_f272a53b0ef1c576ff83d5cbdb2278970c863fb8]['link']  = '<a href="' . $this->CreateUrl("/" . RESULT . "/$_5fcf2c9c13847fbe85391de6c646fa24") . '" title="' . $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['title'] . '">' . $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['title'] . '</a>';
                                        $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[$_f272a53b0ef1c576ff83d5cbdb2278970c863fb8]['words'] = $_5fcf2c9c13847fbe85391de6c646fa24;
                                    }
                                    $_4de56d696b7ba50c59dc97ef762bdb0dc270fbe29c9f5ab1326a9e5fed02f911c28eb0ce39b618afdc73f7fd5fa96837e2f7bf0c0727739dea33902dd78fa03d++;
                                }
                            }
                        } else {
                            if (ONLY_EXISTING_KEYWORDS == 'true') {
                                $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[$_f272a53b0ef1c576ff83d5cbdb2278970c863fb8]['words'] = '';
                            }
                        }
                    }
                    break;
                case 'false':
                    $_4de56d696b7ba50c59dc97ef762bdb0dc270fbe29c9f5ab1326a9e5fed02f911c28eb0ce39b618afdc73f7fd5fa96837e2f7bf0c0727739dea33902dd78fa03d = 0;
                    foreach ($_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc as $_f272a53b0ef1c576ff83d5cbdb2278970c863fb8 => $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3) {
                        if ($_4de56d696b7ba50c59dc97ef762bdb0dc270fbe29c9f5ab1326a9e5fed02f911c28eb0ce39b618afdc73f7fd5fa96837e2f7bf0c0727739dea33902dd78fa03d < COUNT_LINKS) {
                            if (ONLY_EXISTING_KEYWORDS == 'false') {
                                if (!empty($_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['words'])) {
                                    $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[$_f272a53b0ef1c576ff83d5cbdb2278970c863fb8]['link'] = '<a href="' . $this->CreateUrl("/" . RESULT . "/" . $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['words']) . '" title="' . $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['title'] . '">' . $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['title'] . '</a>';
                                }
                            } else {
                                $_5fcf2c9c13847fbe85391de6c646fa24                                                                      = $this->GetKeyword();
                                $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[$_f272a53b0ef1c576ff83d5cbdb2278970c863fb8]['link']  = '<a href="' . $this->CreateUrl("/" . RESULT . "/$_5fcf2c9c13847fbe85391de6c646fa24") . '" title="' . $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['title'] . '">' . $_7a5102c880c00f0398bb8aa0b9f0c15e04f2a2b2968b329c39110f16d2bcdda3['title'] . '</a>';
                                $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[$_f272a53b0ef1c576ff83d5cbdb2278970c863fb8]['words'] = $_5fcf2c9c13847fbe85391de6c646fa24;
                            }
                            $_4de56d696b7ba50c59dc97ef762bdb0dc270fbe29c9f5ab1326a9e5fed02f911c28eb0ce39b618afdc73f7fd5fa96837e2f7bf0c0727739dea33902dd78fa03d++;
                        } else {
                            if (ONLY_EXISTING_KEYWORDS == 'true') {
                                $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[$_f272a53b0ef1c576ff83d5cbdb2278970c863fb8]['words'] = '';
                            }
                        }
                    }
                    break;
            }
        }
        $_f1ea666ca26a11a5e7854267b5e642adf79b94df1a720349 = $this->GetContent('txt/external_linking.txt');
        if (EXTERNAL_LINKING == 'true' and !empty($_f1ea666ca26a11a5e7854267b5e642adf79b94df1a720349)) {
            if (ONLY_EXISTING_KEYWORDS == 'false') {
                if (!empty($_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[0]['words'])) {
                    $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[0]['link'] = '<a href="' . $_f1ea666ca26a11a5e7854267b5e642adf79b94df1a720349 . '?' . CONTROLLER . '=' . RESULT . '&amp;' . QUERY . '=' . urlencode($_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[0]['words']) . '" title="' . $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[0]['title'] . '">' . $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[0]['title'] . '</a>';
                }
            } else {
                $_5fcf2c9c13847fbe85391de6c646fa24                             = $this->GetKeyword();
                $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[0]['link']  = '<a href="' . $_f1ea666ca26a11a5e7854267b5e642adf79b94df1a720349 . '?' . CONTROLLER . '=' . RESULT . '&amp;' . QUERY . '=' . urlencode($_5fcf2c9c13847fbe85391de6c646fa24) . '" title="' . $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[0]['title'] . '">' . $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[0]['title'] . '</a>';
                $_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc[0]['words'] = $_5fcf2c9c13847fbe85391de6c646fa24;
            }
        }
        return array_reverse($_9d333506902e2269952a26ae595c223c7e6c184e5ab1dfcc);
    }
    public function GetPage($folder_name)
    {
        $folders_array = $this->GetFolders();
        if (in_array($folder_name, $folders_array)) {
            $php_file_descriptor_list = glob("templates/" . TEMPLATE . "/$folder_name/*.php", GLOB_NOSORT);
            if (empty($php_file_descriptor_list)) {
                $php_file_descriptor_list = glob("templates/" . TEMPLATE . "/result/*.php", GLOB_NOSORT);
                $folder_name = 'result';
            }
        } else {
            $php_file_descriptor_list  glob("templates/" . TEMPLATE . "/result/*.php", GLOB_NOSORT);
            $folder_name = 'result';
        }
        $php_file_names_list_not_index = array();
        foreach ($php_file_descriptor_list as $php_file_descriptor) {
            $php_file_name = pathinfo($php_file_descriptor, PATHINFO_FILENAME);
            if ($php_file_name !== 'index') {
                $php_file_names_list_not_index[] = $php_file_name;
            }
        }
        $php_file_name = $php_file_names_list_not_index[array_rand($php_file_names_list_not_index)];
        return "$folder_name/$php_file_name";
    }
	
    public function GetPages()
    {
        $php_file_name_list = array();
        $predefined_names = array(
            'base',
            'home'
        );
        $php_file_descriptor_list = glob('templates/' . TEMPLATE . '/*.php', GLOB_NOSORT);
        foreach ($php_file_descriptor_list as $php_file_descriptor) {
            $php_file_name = pathinfo($php_file_descriptor, PATHINFO_FILENAME);
            if (!in_array($php_file_name, $predefined_names)) {
                $php_file_name_list[] = $php_file_name;
            }
        }
        return $php_file_name_list;
    }
    public function GetPluginObject($plugin_name, $plugin_folder)
    {
        require_once $this->path . "application/plugins/$plugin_folder/$plugin_name.php";
        $my_reflection_class = new ReflectionClass($plugin_name);
        if ($my_reflection_class->getConstructor() === NULL) {
            $my_plugin = new $plugin_name();
            return $my_plugin;
        }
    }
    public function GetQueries($input_query_list)
    {
        $query_descriptor_file_array = glob('content/base/*/*', GLOB_NOSORT);
        if (!empty($query_descriptor_file_array)) {
            $query_descriptor_file_with_not_zero_size_copy = array();
            foreach ($query_descriptor_file_array as $query_descriptor_file) {
                if (filesize($query_descriptor_file) > 0) {
                    $query_descriptor_file_with_not_zero_size_copy[] = $query_descriptor_file;
                }
            }
            if (!empty($query_descriptor_file_with_not_zero_size_copy)) {
                $output_query_list = array();
                while (count($output_query_list) < $input_query_list) {
                    $query_descriptor_file = $query_descriptor_file_with_not_zero_size_copy[array_rand($query_descriptor_file_with_not_zero_size_copy)];
                    $query_file_lines = file($query_descriptor_file, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
                    shuffle($query_file_lines);
                    foreach ($query_file_lines as $query_file_line) {
                        if (count($output_query_list) < $input_query_list) {
                            $output_query_list[] = json_decode($query_file_line)->query;
                        }
                    }
                }
                return $output_query_list;
            }
        }
    }
    public function GetQuery($_ceda46bee2c569e571611d5db5f7efd6)
    {
        if (strlen(ROOT) > 0 AND strstr($_ceda46bee2c569e571611d5db5f7efd6, ROOT)) {
            $_ceda46bee2c569e571611d5db5f7efd6 = substr($_ceda46bee2c569e571611d5db5f7efd6, strlen(ROOT));
        }
        $_f2f68331352ee0eecc83761341db7c7e6196ac02fbcee5ca = '';
        if ($this->CheckUrl($_ceda46bee2c569e571611d5db5f7efd6) == true)
            $_4681ef5daa26f8844d1f99e2573cdaec198fd2fc5ee06c96562c883733185b5d = array_values(explode('&', $_ceda46bee2c569e571611d5db5f7efd6));
        else
            $_4681ef5daa26f8844d1f99e2573cdaec198fd2fc5ee06c96562c883733185b5d = array_values(array_filter(explode('/', $_ceda46bee2c569e571611d5db5f7efd6)));
        if (!empty($_4681ef5daa26f8844d1f99e2573cdaec198fd2fc5ee06c96562c883733185b5d[1])) {
            $_ceda46bee2c569e571611d5db5f7efd6 = urldecode($_4681ef5daa26f8844d1f99e2573cdaec198fd2fc5ee06c96562c883733185b5d[1]);
            if (strrchr($_ceda46bee2c569e571611d5db5f7efd6, '=')) {
                $_ceda46bee2c569e571611d5db5f7efd6                         = substr($_ceda46bee2c569e571611d5db5f7efd6, strpos($_ceda46bee2c569e571611d5db5f7efd6, '=') + 1);
                $_b8712f74cdd4afc1c234e77dc24ff20b12514c0819c298e2b2516768 = md5($_ceda46bee2c569e571611d5db5f7efd6);
                $_e3d63daa2aa73234783102599da0d78f42aa4d8e8162e569         = file("content/base/$_b8712f74cdd4afc1c234e77dc24ff20b12514c0819c298e2b2516768[0]/$_b8712f74cdd4afc1c234e77dc24ff20b12514c0819c298e2b2516768[1].txt", FILE_IGNORE_NEW_LINES);
                foreach ($_e3d63daa2aa73234783102599da0d78f42aa4d8e8162e569 as $_15221ea5d38666c733c106fdcde63a2ad061a99f82590db4e9ff061a) {
                    if ($_ceda46bee2c569e571611d5db5f7efd6 == json_decode($_15221ea5d38666c733c106fdcde63a2ad061a99f82590db4e9ff061a)->query) {
                        $_f2f68331352ee0eecc83761341db7c7e6196ac02fbcee5ca = json_decode($_15221ea5d38666c733c106fdcde63a2ad061a99f82590db4e9ff061a)->query;
                    }
                }
            }
            if (strrchr($_ceda46bee2c569e571611d5db5f7efd6, '.')) {
                $_ceda46bee2c569e571611d5db5f7efd6 = substr($_ceda46bee2c569e571611d5db5f7efd6, 0, strrpos($_ceda46bee2c569e571611d5db5f7efd6, '.'));
            }
            if (preg_match('|[a-z0-9]{32}|', $_ceda46bee2c569e571611d5db5f7efd6)) {
                if (strrchr($_ceda46bee2c569e571611d5db5f7efd6, '-')) {
                    $_ceda46bee2c569e571611d5db5f7efd6 = substr(strrchr($_ceda46bee2c569e571611d5db5f7efd6, '-'), 1);
                }
                $_b8712f74cdd4afc1c234e77dc24ff20b12514c0819c298e2b2516768 = $_ceda46bee2c569e571611d5db5f7efd6;
                $_e3d63daa2aa73234783102599da0d78f42aa4d8e8162e569         = file("content/base/$_b8712f74cdd4afc1c234e77dc24ff20b12514c0819c298e2b2516768[0]/$_b8712f74cdd4afc1c234e77dc24ff20b12514c0819c298e2b2516768[1].txt", FILE_IGNORE_NEW_LINES);
                foreach ($_e3d63daa2aa73234783102599da0d78f42aa4d8e8162e569 as $_15221ea5d38666c733c106fdcde63a2ad061a99f82590db4e9ff061a) {
                    if ($_ceda46bee2c569e571611d5db5f7efd6 == json_decode($_15221ea5d38666c733c106fdcde63a2ad061a99f82590db4e9ff061a)->md5) {
                        $_f2f68331352ee0eecc83761341db7c7e6196ac02fbcee5ca = json_decode($_15221ea5d38666c733c106fdcde63a2ad061a99f82590db4e9ff061a)->query;
                    }
                }
            } else {
                if (strrchr($_ceda46bee2c569e571611d5db5f7efd6, '-')) {
                    $_b8712f74cdd4afc1c234e77dc24ff20b12514c0819c298e2b2516768 = substr(strrchr($_ceda46bee2c569e571611d5db5f7efd6, '-'), 1);
                    $_ceda46bee2c569e571611d5db5f7efd6                         = substr($_ceda46bee2c569e571611d5db5f7efd6, 0, -3);
                    $_e3d63daa2aa73234783102599da0d78f42aa4d8e8162e569         = file("content/base/$_b8712f74cdd4afc1c234e77dc24ff20b12514c0819c298e2b2516768[0]/$_b8712f74cdd4afc1c234e77dc24ff20b12514c0819c298e2b2516768[1].txt", FILE_IGNORE_NEW_LINES);
                    foreach ($_e3d63daa2aa73234783102599da0d78f42aa4d8e8162e569 as $_15221ea5d38666c733c106fdcde63a2ad061a99f82590db4e9ff061a) {
                        if ($_ceda46bee2c569e571611d5db5f7efd6 == json_decode($_15221ea5d38666c733c106fdcde63a2ad061a99f82590db4e9ff061a)->translit) {
                            $_f2f68331352ee0eecc83761341db7c7e6196ac02fbcee5ca = json_decode($_15221ea5d38666c733c106fdcde63a2ad061a99f82590db4e9ff061a)->query;
                        }
                    }
                } else {
                    $_f2f68331352ee0eecc83761341db7c7e6196ac02fbcee5ca = $_ceda46bee2c569e571611d5db5f7efd6;
                }
            }
        }
        return $_f2f68331352ee0eecc83761341db7c7e6196ac02fbcee5ca;
    }
    private function GetSnippets($snippet_array, $key_words, $new_snippets_count)
    {
        foreach (glob('application/plugins/snippets/*.php') as $snippet_name) {
            $snippet_name = pathinfo($snippet_name, PATHINFO_FILENAME);
            if (!in_array($snippet_name, $snippet_array)) {
                $extra_snippet_array[] = $snippet_name;
            }
        }
        $snippet_name = $extra_snippet_array[array_rand($extra_snippet_array)];
        $my_snippet = $this->GetPluginObject($snippet_name, 'snippets');
        $new_snippets_count = COUNT_SNIPPETS - $new_snippets_count;
        $snippets_array = $my_snippet->Start($key_words, LANGUAGE, $new_snippets_count, $this);
        return $snippets_array;
    }
    public function Ping($url_for_ping, $query_for_ping)
    {
        if (strlen(PINGERS) > 0) {
            $pingers_array = explode(' | ', PINGERS);
            foreach ($pingers_array as $pinger_name) {
                $my_pinger = $this->GetPluginObject($pinger_name, 'pingers');
                $my_pinger->Start($url_for_ping, $query_for_ping, $this);
            }
        }
    }
    public function Query()
    {
        switch (CACHE) {
            case 'index':
                $json_cache_index_file_list = glob("content/cache/*.json", GLOB_NOSORT);
                if (!empty($json_cache_index_file_list)) {
                    $random_json_cache_index_file_list = $json_cache_index_file_list[array_rand($json_cache_index_file_list)];
                    $cache_file_name         = pathinfo($random_json_cache_index_file_list, PATHINFO_FILENAME);
                    $query_string         = $this->GetQuery("/result/$cache_file_name");
                    return $query_string;
                }
                break;
            case 'disc':
                $cache_file_name = $this->GetContent('content/cache/disc.txt');
                if (!empty($cache_file_name)) {
                    $query_string = $this->GetQuery("/result/$cache_file_name");
                    return $query_string;
                }
                break;
        }
    }
    
    public function RSS($_b135f9591e66b399d6a9b1bcbb1f9150527760a5, $_1dfceae0a1cd7543e410f34db3b0b65d44c39e95745e0dd33c010be5) 	
    { 		
    	$_8a88c4a1ce3156189a97f170038ed0b7cf58a16284ec24ed69118866b1811cdba98a2466f2892917775399b29b0fa8ff = $this->CreateUrl("/rss/$_b135f9591e66b399d6a9b1bcbb1f9150527760a5"); 		
    	$_8a88c4a1ce3156189a97f170038ed0b7cf58a16284ec24ed69118866b1811cdba98a2466f2892917775399b29b0fa8ff = htmlspecialchars($_8a88c4a1ce3156189a97f170038ed0b7cf58a16284ec24ed69118866b1811cdba98a2466f2892917775399b29b0fa8ff); 		
    	$_08335878eca8e1419deaf97039c57339 = TITLE;  		
    	$_09c0b1a8eeef438163f6b502752f38378698a831e742f8f7c2237dbb = <<<CONTENT><?xml version="1.0" encoding="UTF-8"?><rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom"><channel><title>$_b135f9591e66b399d6a9b1bcbb1f9150527760a5</title><link>$this->host</link><atom:link type="application/rss+xml" rel="self" href="$_8a88c4a1ce3156189a97f170038ed0b7cf58a16284ec24ed69118866b1811cdba98a2466f2892917775399b29b0fa8ff"/><description>$_08335878eca8e1419deaf97039c57339</description><language>ru-ru</language>
CONTENT; 		
	foreach ($_1dfceae0a1cd7543e410f34db3b0b65d44c39e95745e0dd33c010be5 as $_16caf1ac00028d20199526210e6b0fcc => $_020900cf) 		
	{ 			
		$_808920afa560ce4bc492a5965526b3dc6c2ae8c8 = htmlspecialchars($_020900cf['title']); 			
		$_eb23bf1a1c14db64e38470ba72858a6265079a0674d8a680 = $this->GetKeyword(); 			
		$_a21b7b546f5a845ef2da69a0d873f07a = $this->CreateUrl("/" . RESULT . "/$_eb23bf1a1c14db64e38470ba72858a6265079a0674d8a680"); 			
		$_77f82a67 = htmlspecialchars($_a21b7b546f5a845ef2da69a0d873f07a); 			
		$_08335878eca8e1419deaf97039c57339 = htmlspecialchars($_020900cf['description']); 			
		$_b2ccea514538dfca259ddf4e365bfed7 = $this->host . "/#$_16caf1ac00028d20199526210e6b0fcc";  			
		$_09c0b1a8eeef438163f6b502752f38378698a831e742f8f7c2237dbb .= <<<CONTENT<item><title>$_808920afa560ce4bc492a5965526b3dc6c2ae8c8</title><link>$_77f82a67</link><description>$_08335878eca8e1419deaf97039c57339</description><guid>$_b2ccea514538dfca259ddf4e365bfed7</guid></item>
CONTENT; 		
	}  		
	$_09c0b1a8eeef438163f6b502752f38378698a831e742f8f7c2237dbb .= <<<CONTENT</channel></rss>
CONTENT; 		
	return $_09c0b1a8eeef438163f6b502752f38378698a831e742f8f7c2237dbb; 	
	}  		
    
    public function Redirect($_96626f2c23ea4246d77875ef520fe5d63b83ba718ac33edb4a743904)
    {
        if ($this->CheckUrl($_96626f2c23ea4246d77875ef520fe5d63b83ba718ac33edb4a743904) == true AND FU_STATUS == 'ON') {
            header('HTTP/1.1 301 Moved Permanently');
            header('Location: ' . $this->CreateUrl($_96626f2c23ea4246d77875ef520fe5d63b83ba718ac33edb4a743904));
            exit();
        } elseif ($this->CheckUrl($_96626f2c23ea4246d77875ef520fe5d63b83ba718ac33edb4a743904) == false AND FU_STATUS == 'OFF') {
            header('HTTP/1.1 301 Moved Permanently');
            header('Location: ' . $this->CreateUrl($_96626f2c23ea4246d77875ef520fe5d63b83ba718ac33edb4a743904));
            exit();
        }
    }
    private function RemoveCache()
    {
        if (CACHE == 'index') {
            $json_cache_index_file_list = glob("content/cache/*.json", GLOB_NOSORT);
            if (count($json_cache_index_file_list) > COUNT_FILES) {
                $file_to_delete_list = array();
                foreach ($json_cache_index_file_list as $json_cache_index_file_index => $cache_file_name) {
                    $file_to_delete_list[$json_cache_index_file_index]['time'] = time() - filemtime($cache_file_name);
                    $file_to_delete_list[$json_cache_index_file_index]['name'] = $cache_file_name;
                }
                arsort($file_to_delete_list);
                $json_cache_index_file_index = 1;
                $file_count_for_delete = count($json_cache_index_file_list) - COUNT_FILES;
                foreach ($file_to_delete_list as $file_to_delete) {
                    if ($json_cache_index_file_index <= $file_count_for_delete) {
                        unlink($file_to_delete['name']);
                        $json_cache_index_file_index++;
                    }
                }
            }
        }
    }
    public function RemoveDirectory($dir_to_remove)
    {
        if ($file_descriptor_list = glob($dir_to_remove . "/*")) {
            foreach ($file_descriptor_list as $file_descriptor) {
                if (file_exists("$dir_to_remove/.htaccess"))
                    unlink("$dir_to_remove/.htaccess");
                is_dir($file_descriptor) ? $this->RemoveDirectory($file_descriptor) : unlink($file_descriptor);
            }
        }
        rmdir($dir_to_remove);
    }
    public function Run($_db8a5b5600156c1efdeaeb38444c4e9c)
    {
        $_c11943b9eebf20348fb9bfb1d493ad21885d1c3c8fc75cb8bcef45da07099027f8412110800e76cda35a95201fed095087e4bb5b04be73691d706285b95b6828 = $this->GetFile($_db8a5b5600156c1efdeaeb38444c4e9c);
        if (!file_exists($_c11943b9eebf20348fb9bfb1d493ad21885d1c3c8fc75cb8bcef45da07099027f8412110800e76cda35a95201fed095087e4bb5b04be73691d706285b95b6828)) {
            $_fcf767872b692923db759586ca0e32c8 = array();
            if (strlen(SNIPPETS) > 0 AND !array_key_exists('snippets', $_fcf767872b692923db759586ca0e32c8)) {
                $_e226d1c655c2a8c86469ada1d8ed42743f0b94979eeeb35a673b29de2c167ab60993a1a3dedef364fd18126d51aa1d0767028fad6d03677b05307fb97be8650d = array();
                $_2e1a433c4685ebc30c75f7aaacae940c                                                                                                 = explode(' | ', SNIPPETS);
                $_5fbc7bc22a5f38bf60bbf95d612fe873189311ed8a42dab06df2a5c3e391705e16b863ec7dcc5ef6fcbb558b40b7b8ee569742530007563dadaa58d01fb2814c = array_rand($_2e1a433c4685ebc30c75f7aaacae940c);
                $_a948eb166b96d00e56bb9512c10fe420d92048d9d8b2be285de121e0ed24963c                                                                 = $this->GetPluginObject($_2e1a433c4685ebc30c75f7aaacae940c[$_5fbc7bc22a5f38bf60bbf95d612fe873189311ed8a42dab06df2a5c3e391705e16b863ec7dcc5ef6fcbb558b40b7b8ee569742530007563dadaa58d01fb2814c], 'snippets');
                foreach ($_a948eb166b96d00e56bb9512c10fe420d92048d9d8b2be285de121e0ed24963c->Start($_db8a5b5600156c1efdeaeb38444c4e9c, LANGUAGE, COUNT_SNIPPETS, $this) as $_3080621b9f2f96f7e7844330602aa2f45c80efb5cb695d693d281f4f469d8678ca923f884e755161671190b42239eec8225c57d9d9da48860d8fcfa339933fde) {
                    $_e226d1c655c2a8c86469ada1d8ed42743f0b94979eeeb35a673b29de2c167ab60993a1a3dedef364fd18126d51aa1d0767028fad6d03677b05307fb97be8650d[] = $_3080621b9f2f96f7e7844330602aa2f45c80efb5cb695d693d281f4f469d8678ca923f884e755161671190b42239eec8225c57d9d9da48860d8fcfa339933fde;
                }
                while (COUNT_SNIPPETS > count($_e226d1c655c2a8c86469ada1d8ed42743f0b94979eeeb35a673b29de2c167ab60993a1a3dedef364fd18126d51aa1d0767028fad6d03677b05307fb97be8650d)) {
                    foreach ($this->GetSnippets($_2e1a433c4685ebc30c75f7aaacae940c, $_db8a5b5600156c1efdeaeb38444c4e9c, count($_e226d1c655c2a8c86469ada1d8ed42743f0b94979eeeb35a673b29de2c167ab60993a1a3dedef364fd18126d51aa1d0767028fad6d03677b05307fb97be8650d)) as $_3080621b9f2f96f7e7844330602aa2f45c80efb5cb695d693d281f4f469d8678ca923f884e755161671190b42239eec8225c57d9d9da48860d8fcfa339933fde) {
                        $_e226d1c655c2a8c86469ada1d8ed42743f0b94979eeeb35a673b29de2c167ab60993a1a3dedef364fd18126d51aa1d0767028fad6d03677b05307fb97be8650d[] = $_3080621b9f2f96f7e7844330602aa2f45c80efb5cb695d693d281f4f469d8678ca923f884e755161671190b42239eec8225c57d9d9da48860d8fcfa339933fde;
                    }
                }
                $_fcf767872b692923db759586ca0e32c8['snippets'] = $this->Dublier($_e226d1c655c2a8c86469ada1d8ed42743f0b94979eeeb35a673b29de2c167ab60993a1a3dedef364fd18126d51aa1d0767028fad6d03677b05307fb97be8650d);
                if (!empty($_fcf767872b692923db759586ca0e32c8['snippets'])) {
                    shuffle($_fcf767872b692923db759586ca0e32c8['snippets']);
                    $_fcf767872b692923db759586ca0e32c8['snippets'] = $this->Filter($_fcf767872b692923db759586ca0e32c8['snippets']);
                    $this->BaseWriter($_db8a5b5600156c1efdeaeb38444c4e9c, $_fcf767872b692923db759586ca0e32c8['snippets']);
                }
            }
            if (strlen(IMAGES) > 0 AND !array_key_exists('images', $_fcf767872b692923db759586ca0e32c8)) {
                $_2e1a433c4685ebc30c75f7aaacae940c                                 = explode(' | ', IMAGES);
                $_9e142ce28152156494bcd6f604effd85512fdb98ec8b1788f5e2aaf376a3e56b = floor(20 / count($_2e1a433c4685ebc30c75f7aaacae940c));
                foreach ($_2e1a433c4685ebc30c75f7aaacae940c as $_5fbc7bc22a5f38bf60bbf95d612fe873189311ed8a42dab06df2a5c3e391705e16b863ec7dcc5ef6fcbb558b40b7b8ee569742530007563dadaa58d01fb2814c) {
                    $_a948eb166b96d00e56bb9512c10fe420d92048d9d8b2be285de121e0ed24963c = $this->GetPluginObject($_5fbc7bc22a5f38bf60bbf95d612fe873189311ed8a42dab06df2a5c3e391705e16b863ec7dcc5ef6fcbb558b40b7b8ee569742530007563dadaa58d01fb2814c, 'images');
                    foreach ($_a948eb166b96d00e56bb9512c10fe420d92048d9d8b2be285de121e0ed24963c->Start($_db8a5b5600156c1efdeaeb38444c4e9c, $_9e142ce28152156494bcd6f604effd85512fdb98ec8b1788f5e2aaf376a3e56b, $this) as $_3080621b9f2f96f7e7844330602aa2f45c80efb5cb695d693d281f4f469d8678ca923f884e755161671190b42239eec8225c57d9d9da48860d8fcfa339933fde) {
                        $_fcf767872b692923db759586ca0e32c8['images'][] = $_3080621b9f2f96f7e7844330602aa2f45c80efb5cb695d693d281f4f469d8678ca923f884e755161671190b42239eec8225c57d9d9da48860d8fcfa339933fde;
                    }
                }
                if (!empty($_fcf767872b692923db759586ca0e32c8['images'])) {
                    shuffle($_fcf767872b692923db759586ca0e32c8['images']);
                }
            }
            if (strlen(VIDEOS) > 0 AND !array_key_exists('videos', $_fcf767872b692923db759586ca0e32c8)) {
                $_2e1a433c4685ebc30c75f7aaacae940c                                 = explode(' | ', VIDEOS);
                $_9e142ce28152156494bcd6f604effd85512fdb98ec8b1788f5e2aaf376a3e56b = floor(20 / count($_2e1a433c4685ebc30c75f7aaacae940c));
                foreach ($_2e1a433c4685ebc30c75f7aaacae940c as $_5fbc7bc22a5f38bf60bbf95d612fe873189311ed8a42dab06df2a5c3e391705e16b863ec7dcc5ef6fcbb558b40b7b8ee569742530007563dadaa58d01fb2814c) {
                    $_a948eb166b96d00e56bb9512c10fe420d92048d9d8b2be285de121e0ed24963c = $this->GetPluginObject($_5fbc7bc22a5f38bf60bbf95d612fe873189311ed8a42dab06df2a5c3e391705e16b863ec7dcc5ef6fcbb558b40b7b8ee569742530007563dadaa58d01fb2814c, 'videos');
                    foreach ($_a948eb166b96d00e56bb9512c10fe420d92048d9d8b2be285de121e0ed24963c->Start($_db8a5b5600156c1efdeaeb38444c4e9c, $_9e142ce28152156494bcd6f604effd85512fdb98ec8b1788f5e2aaf376a3e56b, $this) as $_3080621b9f2f96f7e7844330602aa2f45c80efb5cb695d693d281f4f469d8678ca923f884e755161671190b42239eec8225c57d9d9da48860d8fcfa339933fde) {
                        $_fcf767872b692923db759586ca0e32c8['videos'][] = $_3080621b9f2f96f7e7844330602aa2f45c80efb5cb695d693d281f4f469d8678ca923f884e755161671190b42239eec8225c57d9d9da48860d8fcfa339933fde;
                    }
                }
                if (!empty($_fcf767872b692923db759586ca0e32c8['videos'])) {
                    shuffle($_fcf767872b692923db759586ca0e32c8['videos']);
                }
            }
            if (strlen(COMMENTS) > 0 AND !array_key_exists('comments', $_fcf767872b692923db759586ca0e32c8)) {
                $_2e1a433c4685ebc30c75f7aaacae940c = explode(' | ', COMMENTS);
                foreach ($_2e1a433c4685ebc30c75f7aaacae940c as $_5fbc7bc22a5f38bf60bbf95d612fe873189311ed8a42dab06df2a5c3e391705e16b863ec7dcc5ef6fcbb558b40b7b8ee569742530007563dadaa58d01fb2814c) {
                    $_a948eb166b96d00e56bb9512c10fe420d92048d9d8b2be285de121e0ed24963c = $this->GetPluginObject($_5fbc7bc22a5f38bf60bbf95d612fe873189311ed8a42dab06df2a5c3e391705e16b863ec7dcc5ef6fcbb558b40b7b8ee569742530007563dadaa58d01fb2814c, 'comments');
                    foreach ($_a948eb166b96d00e56bb9512c10fe420d92048d9d8b2be285de121e0ed24963c->Start($this) as $_3080621b9f2f96f7e7844330602aa2f45c80efb5cb695d693d281f4f469d8678ca923f884e755161671190b42239eec8225c57d9d9da48860d8fcfa339933fde) {
                        $_fcf767872b692923db759586ca0e32c8['comments'][] = $_3080621b9f2f96f7e7844330602aa2f45c80efb5cb695d693d281f4f469d8678ca923f884e755161671190b42239eec8225c57d9d9da48860d8fcfa339933fde;
                    }
                }
            }
            if (!empty($_fcf767872b692923db759586ca0e32c8)) {
                $this->RemoveCache();
                $this->CreateCache($_c11943b9eebf20348fb9bfb1d493ad21885d1c3c8fc75cb8bcef45da07099027f8412110800e76cda35a95201fed095087e4bb5b04be73691d706285b95b6828, $_fcf767872b692923db759586ca0e32c8);
                $this->SaveCache($_db8a5b5600156c1efdeaeb38444c4e9c);
                if (!empty($_fcf767872b692923db759586ca0e32c8['snippets'])) {
                    $_fcf767872b692923db759586ca0e32c8['snippets'] = $this->GetLinks($_db8a5b5600156c1efdeaeb38444c4e9c, $_fcf767872b692923db759586ca0e32c8['snippets']);
                }
            }
            return $_fcf767872b692923db759586ca0e32c8;
        } else {
            $_fcf767872b692923db759586ca0e32c8 = $this->GetCache($_c11943b9eebf20348fb9bfb1d493ad21885d1c3c8fc75cb8bcef45da07099027f8412110800e76cda35a95201fed095087e4bb5b04be73691d706285b95b6828);
            if (!empty($_fcf767872b692923db759586ca0e32c8['snippets'])) {
                $_fcf767872b692923db759586ca0e32c8['snippets'] = $this->GetLinks($_db8a5b5600156c1efdeaeb38444c4e9c, $_fcf767872b692923db759586ca0e32c8['snippets']);
            }
            return $_fcf767872b692923db759586ca0e32c8;
        }
    }
    private function SaveCache($_114bd151f8fb0c58642d2170da4ae7d7c57977260ac2cc8905306cab6b2acabc)
    {
        if (CACHE == 'disc') {
            $_b10b1834825f2a6b81af76a4a4f806c7bab84d5ffaf2134f78cc72c25e5598cc = 'content/cache/disc.txt';
            $_021a00d8                                                         = $this->GetContents($_b10b1834825f2a6b81af76a4a4f806c7bab84d5ffaf2134f78cc72c25e5598cc);
            if (!empty($_021a00d8)) {
                if (count($_021a00d8) >= 100) {
                    unset($_021a00d8[0]);
                    file_put_contents($_b10b1834825f2a6b81af76a4a4f806c7bab84d5ffaf2134f78cc72c25e5598cc, implode("", $_021a00d8) . "");
                }
                file_put_contents($_b10b1834825f2a6b81af76a4a4f806c7bab84d5ffaf2134f78cc72c25e5598cc, md5($_114bd151f8fb0c58642d2170da4ae7d7c57977260ac2cc8905306cab6b2acabc) . "", FILE_APPEND | LOCK_EX);
            } else {
                $_021a00d8 = glob("content/cache/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*.json", GLOB_NOSORT);
                foreach ($_021a00d8 as $_972fb81a3b9e6076bb06207a2687f1f1d167ec845852aa6a33e11c1cf1f282f7 => $_f53908a6763f1539d91c2c5cc99173dc) {
                    $_021a00d8[$_972fb81a3b9e6076bb06207a2687f1f1d167ec845852aa6a33e11c1cf1f282f7] = str_replace('/', '', substr($_f53908a6763f1539d91c2c5cc99173dc, 14, -5));
                }
                file_put_contents($_b10b1834825f2a6b81af76a4a4f806c7bab84d5ffaf2134f78cc72c25e5598cc, implode("", $_021a00d8) . "");
            }
        }
    }
    public function Url($key_words)
    {
        $url_lines = $this->GetContent('txt/urls.txt');
        if (!empty($url_lines)) {
            if (preg_match('|#KEYWORD#|', $url_lines)) {
                $url_lines = str_replace('#KEYWORD#', $key_words, $url_lines);
            }
            return $url_lines;
        }
    }
    public function View($file_name, $_3cc7e66bd2bf4b7a0773db81d1d9dd60279115e1da1081830750eb5a140dddd8 = array())
    {
        foreach ($_3cc7e66bd2bf4b7a0773db81d1d9dd60279115e1da1081830750eb5a140dddd8 as $_bb55e73870b8452738bbd58e0318ff43268c583270f64978fa666b5fc4e676ca => $_769ac34a4012ab69c069de0bab7d9e81) {
            $$_bb55e73870b8452738bbd58e0318ff43268c583270f64978fa666b5fc4e676ca = $_769ac34a4012ab69c069de0bab7d9e81;
        }
        if (file_exists("templates/" . TEMPLATE . "/$file_name.php")) {
            require "templates/" . TEMPLATE . "/$file_name.php";
        } else {
            echo "<h1>Error 404 - File not found.</h1>$file_name.php file does not exist in the directory templates/" . TEMPLATE . "<br>";
        }
    }
}
?>