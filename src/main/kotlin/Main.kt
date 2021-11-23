import java.io.File
val filePrefix = "src"+File.separator+"main"+File.separator+"resources"+File.separator
fun main() {


    //encrypt test example 1
    val encHandler = EncryptionHandler()

    val texts = getExample(filePrefix+"example1texts")
    val keys = getExample(filePrefix+"example1keys")
/*
    for (key in keys) {
        println(key.joinToString("") { it.toString(16) })
    }
*/

    for (text in texts) {
        println(text.joinToString("") { it.toString(16) })
        println(encHandler.encryptChunk(text,keys))
        println()
        break
    }

}

fun getExample(file:String):Array<IntArray>{
    val lines = File(file).readLines(Charsets.US_ASCII)
    var lineArray:Array<IntArray> = Array(lines.size){ IntArray(16) }
    for (i in lines.indices) {
        lineArray[i] = lines[i].split(" ").map { it.toInt(16) }.toIntArray()
    }
    return lineArray
}