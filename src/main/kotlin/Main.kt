import java.io.File
val filePrefix = "src"+File.separator+"main"+File.separator+"resources"+File.separator


fun main() {
    println("#".repeat(16)+" AES PART I "+"#".repeat(16))

    val texts1 = getExample(filePrefix+"example1texts")
    val keys1 = getExample(filePrefix+"example1keys")
    prettyPrintI("Example 1", texts1, keys1)

    val texts2 = getExample(filePrefix+"example2texts")
    val keys2 = getExample(filePrefix+"example2keys")
    prettyPrintI("Example 2", texts2, keys2)

    println("#".repeat(16)+" AES PART II "+"#".repeat(16))

    prettyPrintII("Example 1",getAsLongArray(texts1),keys1[0])
    prettyPrintII("Example 1 only first 20 bytes",getAsLongArray(texts1).copyOfRange(0,20),keys1[0])
}

fun prettyPrintI(title:String, texts:Array<IntArray>, keys:Array<IntArray>) {
    val encHandler = EncryptionHandler()
    val decHandler = DecryptionHandler()
    println()
    println("-".repeat(16)+" $title "+"-".repeat(16))
    println()
    for (text in texts) {
        println("plain text    : ${text.joinToString("") { it.toString(16) }}")
        val encryptedText = encHandler.encryptChunk(text,keys);
        println("encrypted text: ${encryptedText.joinToString("") { it.toString(16) }}")
        val decryptedText = decHandler.decryptChunk(encryptedText,keys);
        println("decrypted text: ${decryptedText.joinToString("") { it.toString(16) }}")
        println()
    }
}

fun prettyPrintII(title:String, text:IntArray, key:IntArray) {
    val encHandler = EncryptionHandler()
    val decHandler = DecryptionHandler()
    println()
    println("-".repeat(16)+" $title "+"-".repeat(16))
    println()

    val encrypted = encHandler.encrypt(text,key)
    println("clear-text: ${text.map { it.toString(16) }.joinToString(" ")}")
    println("encrypted : ${encrypted.map { it.toString(16) }.joinToString(" ")}")
    val decrypted = decHandler.decrypt(encrypted,key)
    println("decrypted : ${decrypted.map { it.toString(16) }.joinToString(" ")}")
}

fun getExample(file:String):Array<IntArray>{
    val lines = File(file).readLines(Charsets.US_ASCII)
    var lineArray:Array<IntArray> = Array(lines.size){ IntArray(16) }
    for (i in lines.indices) {
        lineArray[i] = lines[i].split(" ").map { it.toInt(16) }.toIntArray()
    }
    return lineArray
}

fun getAsLongArray(matrix: Array<IntArray>):IntArray {
    var arr = intArrayOf()

    for (row in matrix) {
        arr += row
    }
    return arr
}