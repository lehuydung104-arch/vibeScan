# VibeScan - Ứng dụng Quét Tài Liệu Sang PDF Cho Android (Tối ưu Samsung S20 FE)

**VibeScan** là ứng dụng quét tài liệu sang định dạng PDF thông minh, gọn nhẹ và chuẩn nét, được xây dựng bằng **Kotlin**, **Jetpack Compose (Material 3)**, tích hợp trực tiếp **Google ML Kit Document Scanner API** và **Google ML Kit Text Recognition** (AI OCR trích xuất tiêu đề tự động).

Ứng dụng hỗ trợ kết nối và tự động đồng bộ tài liệu lên **Google Drive** và **Microsoft OneDrive** hoàn toàn **MIỄN PHÍ 100%**.

---

## ⚡ Quy Trình 2 Lần Bấm: Quét & Lưu

1. **Lần bấm 1:** Tại màn hình chính, bấm nút **"Quét tài liệu"**.
2. **Camera:** Đưa camera vào tài liệu cần quét -> Chụp xong.
3. **Màn hình tùy chọn sau khi quét:**
   - **Phần giữa:** Bản xem trước tài liệu cực nét, chiếm trọn không gian để bạn dễ dàng kiểm tra.
   - **Phần dưới cùng:**
     - **Thanh công cụ bộ lọc:** `📷 Gốc` (Mặc định), `✨ Nâng cao` (Làm đẹp), `📄 Trắng đen (B&W)`, `🌫️ Thang xám`, `🔄 Xoay 90°`.
     - **Ô nhập tên bản scan:** Mặc định theo định dạng: **`Scan_Ngày_Tháng_Năm`** lấy từ đồng hồ trên máy (ví dụ: `Scan_03_09_2026.pdf`) hoặc tên do AI OCR trích xuất từ văn bản. Bàn phím chỉ hiện khi chạm tay vào ô.
     - **Dải nhãn tùy chọn nhanh 1 chạm:** `🧾 Hóa đơn`, `📑 Hợp đồng`, `🪪 Giấy tờ`, `📄 Tài liệu`, `📋 Biên bản`, `📨 Công văn`, `📚 Sách vở`...
     - **Nút "LƯU" to nổi bật:** 1 chạm để lưu vào máy và đồng bộ lên mây.
4. **Lần bấm 2:** Bấm **"LƯU"** -> Hệ thống xuất PDF chất lượng cao, tự động phân vào đúng thư mục riêng -> Xong!

---

## 🌟 Tính Năng Mới Nổi Bật

### 1. 🗂️ Tự động phân loại Thư mục riêng biệt (Cả trên máy & Google Drive / OneDrive)
Ứng dụng tự động nhận diện tên file bạn lưu để đưa vào đúng thư mục riêng:
- `🧾 Hóa đơn/`: Chứa các file `HoaDon_...`, `BienLai_...`
- `📑 Hợp đồng/`: Chứa các file `HopDong_...`
- `📋 Biên bản/`: Chứa các file `BienBan_...`
- `📨 Công văn/`: Chứa các file `CongVan_...`
- `🪪 Giấy tờ/`: Chứa các file `GiayTo_...`, `CCCD_...`
- `📄 Tài liệu/`: Chứa các file `TaiLieu_...`
- `📚 Sách vở/`: Chứa các file `Sach_...`
- `📁 Bản scan/`: Chứa các file scan mặc định `Scan_...`

👉 **Đồng bộ mây thông minh:** Khi lưu lên Google Drive hoặc OneDrive, hệ thống cũng **tự động tạo đúng các thư mục con này trên đám mây**, giúp tài liệu của bạn luôn ngăn nắp và khoa học!

👉 **Thanh Tab lọc thư mục trên màn hình chính:** Có dải tab cuộn ngang (`Tất cả`, `🧾 Hóa đơn (3)`, `📑 Hợp đồng (2)`...) để lọc và xem nhanh theo từng nhóm.

---

### 2. 👆 Bấm giữ để Chọn nhiều & Xóa hàng loạt (Multi-select & Batch Delete)
- **Kích hoạt:** Bấm giữ (Long press) vào bất kỳ thẻ tài liệu nào trong danh sách.
- **Thao tác:**
  - Chạm vào các file khác để chọn thêm hoặc bỏ chọn (có Checkbox và viền xanh nổi bật).
  - Nút **"Tất cả" / "Bỏ chọn"** trên thanh tiêu đề để chọn nhanh toàn bộ file.
  - Nút **"Chia sẻ"** (biểu tượng share): Gửi cùng lúc nhiều file PDF qua Zalo, Gmail, v.v.
  - Nút **"Xóa" (thùng rác đỏ)**: Hiển thị hộp thoại xác nhận an toàn trước khi xóa sạch các file đã chọn.

---

### 3. 🛡️ Cơ chế chống trùng tên thông minh (Auto-Increment)
Nếu trong cùng 1 ngày bạn quét nhiều tài liệu và không đổi tên, app sẽ tự động đánh số thứ tự tiếp theo:
- File thứ 1: `Scan_03_09_2026.pdf`
- File thứ 2: `Scan_03_09_2026 (1).pdf`
- File thứ 3: `Scan_03_09_2026 (2).pdf`...
- Đảm bảo **KHÔNG BAO GIỜ bị ghi đè hay mất file cũ**.

---

## 📱 Hướng Dẫn Cập Nhật Lên Samsung Galaxy S20 FE

1. Mở cửa sổ **Android Studio** trên máy tính.
2. Cắm cáp USB nối điện thoại với máy tính.
3. Bấm nút **Run (tam giác xanh 🟢)** (hoặc phím tắt `Shift + F10`) để cài đặt bản cập nhật lên điện thoại!
