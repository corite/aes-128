import java.io.File

class EncryptionHandler {
    private val cryptHandler = CryptUtils()
    private val mixColumnMatrix:Array<IntArray> = arrayOf(intArrayOf(2,3,1,1), intArrayOf(1,2,3,1), intArrayOf(1,1,2,3), intArrayOf(3,1,1,2))
    private val filePrefix = "src"+ File.separator+"main"+ File.separator+"resources"+File.separator

    private val sBox = cryptHandler.readSBox(filePrefix+"SBox.txt")


    fun encryptChunk(textToEncrypt:IntArray,keys:Array<IntArray>):String {
        val vec = intArrayOf("d4".toInt(16),"bf".toInt(16),"5d".toInt(16),"30".toInt(16))
        println(cryptHandler.multiply(2,"d4".toInt(16)))

        println()
        cryptHandler.matrixMultiply(vec,mixColumnMatrix).forEach { print("$it ") }
        println()

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
        return cryptHandler.getMatrixAsHexString(cryptHandler.addRoundKey(textMatrix,keys[10]))
    }

}