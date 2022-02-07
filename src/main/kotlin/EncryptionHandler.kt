import java.io.File

class EncryptionHandler {
    private val cryptHandler = CryptUtils()
    private val mixColumnMatrix:Array<IntArray> = arrayOf(intArrayOf(2,3,1,1), intArrayOf(1,2,3,1), intArrayOf(1,1,2,3), intArrayOf(3,1,1,2))

    private val sBox = CryptUtils.sBox


    fun encryptChunk(textToEncrypt:IntArray,keys:Array<IntArray>):IntArray {

        var textMatrix = cryptHandler.getAsMatrix(textToEncrypt)

        textMatrix = cryptHandler.addRoundKey(textMatrix,keys[0])

        for (i in 1..9) {
            textMatrix = cryptHandler.subBytes(textMatrix,sBox)

            textMatrix = cryptHandler.shiftRowsLeft(textMatrix)

            textMatrix = cryptHandler.mixColumns(textMatrix,mixColumnMatrix)

            textMatrix = cryptHandler.addRoundKey(textMatrix,keys[i])
        }

        textMatrix = cryptHandler.subBytes(textMatrix,sBox)
        textMatrix = cryptHandler.shiftRowsLeft(textMatrix)
        textMatrix = cryptHandler.addRoundKey(textMatrix,keys[10])

        return cryptHandler.getMatrixAsIntArray(textMatrix)
    }

    fun encrypt(text: IntArray, keyAsBytes: IntArray):IntArray {
        val key = cryptHandler.getKeyAsWords(keyAsBytes)
        val keys =cryptHandler.expandKey(key)
        val chunkedTexts = cryptHandler.chunkText(text,16)
        val encryptedText = IntArray(chunkedTexts.size * 16)

        for (i in chunkedTexts.indices) {
            val encryptedChunk = encryptChunk(chunkedTexts[i],keys)
            for (j in encryptedChunk.indices) {
                encryptedText[(i*16)+j] = encryptedChunk[j]
            }
        }
        return encryptedText
    }
}