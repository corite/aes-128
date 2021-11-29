import java.io.File

class DecryptionHandler {
    private val cryptHandler = CryptUtils()
    private val filePrefix = "src"+ File.separator+"main"+ File.separator+"resources"+File.separator
    private val reverseMixColumnMatrix:Array<IntArray> = arrayOf(
        intArrayOf("E".toInt(16),"B".toInt(16),"D".toInt(16),9)
        ,intArrayOf(9,"E".toInt(16),"B".toInt(16),"D".toInt(16))
        ,intArrayOf("D".toInt(16),9,"E".toInt(16),"B".toInt(16))
        ,intArrayOf("B".toInt(16),"D".toInt(16),9,"E".toInt(16)))
    private val sBox = cryptHandler.readSBox(filePrefix+"SBoxInvers.txt")

    fun decryptChunk(textToEncrypt:IntArray,keys:Array<IntArray>):IntArray {
        var textMatrix = cryptHandler.getAsMatrix(textToEncrypt)

        textMatrix = cryptHandler.addRoundKey(textMatrix,keys[10])
        textMatrix = cryptHandler.shiftRowsRight(textMatrix)
        textMatrix = cryptHandler.subBytes(textMatrix,sBox)

        for (i in 9 downTo 1) {
            textMatrix = cryptHandler.addRoundKey(textMatrix,keys[i])
            textMatrix = cryptHandler.mixColumns(textMatrix,reverseMixColumnMatrix)
            textMatrix = cryptHandler.shiftRowsRight(textMatrix)
            textMatrix = cryptHandler.subBytes(textMatrix,sBox)
        }

        textMatrix = cryptHandler.addRoundKey(textMatrix,keys[0])

        return cryptHandler.getMatrixAsIntArray(textMatrix)
    }

    fun decrypt(text: IntArray, keyAsBytes: IntArray):IntArray {
        val key = cryptHandler.getKeyAsWords(keyAsBytes)
        val keys =cryptHandler.expandKey(key)
        val chunkedTexts = cryptHandler.chunkText(text,16)
        val decryptedText = IntArray(chunkedTexts.size * 16)

        for (i in chunkedTexts.indices) {
            val decryptedChunk = decryptChunk(chunkedTexts[i],keys)
            for (j in decryptedChunk.indices) {
                decryptedText[(i*16)+j] = decryptedChunk[j]
            }
        }
        return decryptedText
    }
}