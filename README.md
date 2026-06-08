# human-logistic

## Giới thiệu

Dự án này thu thập dữ liệu liên quan đến thiên tai từ mạng xã hội và web, bao gồm:
- Crawler thu thập bài đăng, bình luận, lượt thích và lượt chia sẻ
- Tiền xử lý dữ liệu để làm sạch và chuyển đổi sang định dạng phân tích
- Phân tích cảm xúc để tách yếu tố tích cực và tiêu cực
- Dự đoán mức độ thiệt hại và nhu cầu cần thiết về hàng hóa thiết yếu

## Chức năng chính

- Thu thập dữ liệu thiên tai và sự kiện khẩn cấp
- Đọc bình luận và đánh giá thái độ người dùng
- Phân tích mức độ lan truyền thông tin qua like/share
- Ước lượng thiệt hại và nhu cầu thực phẩm, nước, y tế, chỗ ở

## Yêu cầu

- Java 21
- Maven 3.9+
- Python 3.x (dùng cho mô hình phân tích Python)

## Chạy nhanh

1. Build project:
   ```bash
   mvn clean package
   ```

2. Chạy giao diện JavaFX:
   ```bash
   mvn clean javafx:run
   ```

## Kiểm tra

```bash
mvn clean test
```

## Ghi chú

- Dữ liệu mặc định có thể là `results_2025-12-13_11-45-27.csv`.
- Class chính nếu chạy bằng mã lệnh là `com.demo.App`.
- Dự án gồm cả phần Java crawler và mô hình Python để phân tích.
