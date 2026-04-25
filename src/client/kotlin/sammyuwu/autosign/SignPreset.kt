package sammyuwu.autosign

data class SignPreset(
    var name: String = "preset",
    var line1: String = "",
    var line2: String = "",
    var line3: String = "",
    var line4: String = ""
) {
    fun lines(): Array<String> = arrayOf(line1, line2, line3, line4)

    fun setLine(idx: Int, value: String) {
        when (idx) {
            0 -> line1 = value
            1 -> line2 = value
            2 -> line3 = value
            3 -> line4 = value
        }
    }
}
