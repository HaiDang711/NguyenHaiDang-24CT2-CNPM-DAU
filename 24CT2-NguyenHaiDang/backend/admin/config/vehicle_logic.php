<?php
/**
 * Gợi ý phương tiện di chuyển theo số lượng khách — dựa theo quy tắc thực tế công ty đưa ra:
 *  - 1-4 khách  -> xe 4 chỗ
 *  - 5-7 khách  -> xe 7 chỗ (chở 5-6 người nếu nhiều hành lý, 7 người nếu ít hành lý)
 *  - 8-11 khách -> xe Limousine
 *  - >=12 khách -> xe buýt: nếu 1 xe 40 chỗ chở vừa đủ thì đi 1 xe, nếu không thì chia khoảng 20 người/xe cho thoải mái
 *
 * Đây chỉ là GỢI Ý tự động để nhân viên tham khảo — mọi trường hợp từ 12 khách trở lên hoặc có yêu cầu riêng
 * đều cần nhân viên xác nhận lại với người phụ trách/tài xế trước khi duyệt đơn (đúng như quy trình thực tế).
 */
function suggestVehicle($guestCount, $customRequest = null) {
    $guestCount = max(1, intval($guestCount));

    // Khách yêu cầu phương tiện riêng (cá nhân/gia đình/doanh nghiệp đặt riêng) -> ưu tiên tuyệt đối
    if ($customRequest) {
        return "Theo yêu cầu riêng của khách: \"$customRequest\" (đã trả đủ chi phí phát sinh nếu có)";
    }

    if ($guestCount <= 4) {
        return "1 xe 4 chỗ";
    }
    if ($guestCount <= 7) {
        return $guestCount == 7
            ? "1 xe 7 chỗ (đủ 7 người, phù hợp nếu hành lý gọn nhẹ)"
            : "1 xe 7 chỗ (chở {$guestCount} người, còn dư chỗ để hành lý)";
    }
    if ($guestCount <= 11) {
        return "1 xe Limousine (9-11 chỗ)";
    }

    // Từ 12 khách trở lên: xe buýt — cần nhân viên xác nhận thực tế
    $busesOf40 = ceil($guestCount / 40);
    $busesOf20 = ceil($guestCount / 20);
    if ($busesOf40 == 1) {
        return "Gợi ý: 1 xe buýt 40 chỗ (chở vừa {$guestCount} khách) — CẦN xác nhận với tài xế/người phụ trách";
    }
    return "Gợi ý: khoảng {$busesOf20} xe buýt (~20 khách/xe) hoặc {$busesOf40} xe buýt 40 chỗ — linh hoạt tùy tình huống, CẦN nhân viên xác nhận thực tế trước khi duyệt";
}
