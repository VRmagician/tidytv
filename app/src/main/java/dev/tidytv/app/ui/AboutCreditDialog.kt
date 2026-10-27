@Composable
fun AboutCreditDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "เกี่ยวกับ TidyTV Pro", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "เวอร์ชัน: 2.0.0-Optimized")
                Divider()
                Text(
                    text = "👏 เครดิตผู้พัฒนาต้นฉบับ:",
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "โปรเจกต์นี้พัฒนาต่อยอดมาจาก 'TidyTV' ผลงานของ jolan94 (GitHub: github.com/jolan94/tidytv)"
                )
                Text(
                    text = "📜 สัญญาอนุญาต: MIT License (ใช้งานและดัดแปลงได้อิสระ)"
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("ปิด") }
        }
    )
}
