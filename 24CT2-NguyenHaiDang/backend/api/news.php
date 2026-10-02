<?php
// Lấy tin tức du lịch mới nhất từ RSS công khai của VnExpress (hợp pháp, miễn phí cho cá nhân/phi lợi nhuận
// theo điều khoản tại vnexpress.net/rss). Chỉ hiển thị tiêu đề + tóm tắt ngắn do chính VnExpress cung cấp
// trong RSS, luôn dẫn link về bài gốc và ghi rõ nguồn — không sao chép toàn bộ nội dung bài viết.
require_once __DIR__ . '/../config/cors.php';

$cacheFile = __DIR__ . '/../cache/travel_news_cache.json';
$cacheMinutes = 20; // đỡ gọi liên tục sang VnExpress, cũng nhanh hơn cho người dùng

if (file_exists($cacheFile) && (time() - filemtime($cacheFile)) < $cacheMinutes * 60) {
    echo file_get_contents($cacheFile);
    exit;
}

function fetchRss($url) {
    $ctx = stream_context_create(['http' => [
        'header' => "User-Agent: Mozilla/5.0 (DangBooking travel news reader)\r\n",
        'timeout' => 5
    ]]);
    return @file_get_contents($url, false, $ctx);
}

$xmlRaw = fetchRss('https://vnexpress.net/rss/du-lich.rss');
$items = [];

if ($xmlRaw !== false) {
    libxml_use_internal_errors(true);
    $xml = simplexml_load_string($xmlRaw);
    if ($xml && isset($xml->channel->item)) {
        $count = 0;
        foreach ($xml->channel->item as $item) {
            if ($count >= 6) break;
            $descRaw = (string) $item->description;
            // Tách ảnh minh họa (nếu có) và phần tóm tắt chữ ra khỏi HTML của RSS
            $image = null;
            if (preg_match('/<img[^>]+src="([^"]+)"/', $descRaw, $m)) {
                $image = $m[1];
            }
            $summary = trim(strip_tags($descRaw));

            $items[] = [
                'title'   => (string) $item->title,
                'link'    => (string) $item->link,
                'summary' => mb_substr($summary, 0, 160) . (mb_strlen($summary) > 160 ? '…' : ''),
                'image'   => $image,
                'pubDate' => (string) $item->pubDate,
            ];
            $count++;
        }
    }
}

$result = json_encode([
    'success' => count($items) > 0,
    'source' => 'VnExpress Du lịch',
    'source_url' => 'https://vnexpress.net/du-lich',
    'items' => $items,
    'fetched_at' => date('c')
]);

if (count($items) > 0) {
    @file_put_contents($cacheFile, $result);
}
echo $result;
