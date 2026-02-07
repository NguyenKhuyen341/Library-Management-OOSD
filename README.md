# Hệ thống Quản lý Thư viện (UniLib) - Đồ án OOSD

1. Thông tin sinh viên
- Họ tên các thành viên: Trần Anh Huy, Nguyễn Khuyến, Nguyễn Hữu Lợi, Nguyễn Đăng Khoa.
- Lớp: 010412201009 - XD phần mềm hướng đối tượng.

2. Công nghệ sử dụng
- Backend: Java Spring Boot 3.x
- Frontend: HTML5, CSS3, JavaScript (Fetch API), Bootstrap 5
- Database: MySQL

3. Hướng dẫn cài đặt & Chạy
1. Cấu hình Database:
   - Mở MySQL Workbench hoặc phpMyAdmin.
   - Tạo database mới tên là: `library_db` (CREATE DATABASE library_db;).
   - Lưu ý: Kiểm tra file `src/main/resources/application.properties` để chỉnh sửa username/password MySQL cho khớp với máy.

2. Chạy ứng dụng:
   - Mở project bằng IntelliJ IDEA hoặc Eclipse.
   - Chạy file: `src/main/java/com/library/project/LibraryApplication.java`

3. Tài khoản Demo:
   - Hệ thống sẽ tự động nạp dữ liệu mẫu khi chạy lần đầu.
   - Admin: username: `admin` / password: `123`
   - Sinh viên: username: `reader01` / password: `123`

4. Các tính năng chính
- Quản lý sách (Thêm, sửa, xóa, upload ảnh).
- Quản lý mượn trả (Tính phạt quá hạn theo Strategy Pattern).
- Trang dành cho sinh viên (Xem lịch sử mượn, xem hồ sơ).
