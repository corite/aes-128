@file:OptIn(ExperimentalUnsignedTypes::class)

import CryptUtils.Constants.to16UByteArray
import CryptUtils.Constants.xor
import java.math.BigInteger

class DecryptionHandler {
    private val cryptHandler = CryptUtils()
    private val reverseMixColumnMatrix:Array<UByteArray> = arrayOf(
        ubyteArrayOf("E".toUByte(16),"B".toUByte(16),"D".toUByte(16), 9u)
        ,ubyteArrayOf(9u,"E".toUByte(16),"B".toUByte(16),"D".toUByte(16))
        ,ubyteArrayOf("D".toUByte(16),9u,"E".toUByte(16),"B".toUByte(16))
        ,ubyteArrayOf("B".toUByte(16),"D".toUByte(16),9u,"E".toUByte(16)))
    private val sBoxInv = CryptUtils.sBoxInverse

    fun decryptChunk(textToEncrypt:UByteArray,keys:Array<UByteArray>):UByteArray {
        var textMatrix = cryptHandler.getAsMatrix(textToEncrypt)

        textMatrix = cryptHandler.addRoundKey(textMatrix,keys[10])
        textMatrix = cryptHandler.shiftRowsRight(textMatrix)
        textMatrix = cryptHandler.subBytes(textMatrix,sBoxInv)

        for (i in 9 downTo 1) {
            textMatrix = cryptHandler.addRoundKey(textMatrix,keys[i])
            textMatrix = cryptHandler.mixColumns(textMatrix,reverseMixColumnMatrix)
            textMatrix = cryptHandler.shiftRowsRight(textMatrix)
            textMatrix = cryptHandler.subBytes(textMatrix,sBoxInv)
        }

        textMatrix = cryptHandler.addRoundKey(textMatrix,keys[0])

        return cryptHandler.getMatrixAsIntArray(textMatrix)
    }

    fun decrypt(text: UByteArray, keyAsBytes: UByteArray, mode: CipherMode):UByteArray {
        val key = cryptHandler.getKeyAsWords(keyAsBytes)
        val keys =cryptHandler.expandKey(key)
        val chunkedTexts = cryptHandler.chunkText(text,16)

        return when(mode) {
            CipherMode.ECB -> decryptECB(chunkedTexts, keys)
            CipherMode.CBC -> decryptCBC(chunkedTexts, keys)
        }
    }

    private fun decryptCBC(chunkedTexts:Array<UByteArray>, keys:Array<UByteArray>):UByteArray {
        val decryptedText = UByteArray(chunkedTexts.size * 16)
        var lastDecBlock = UByteArray(16)

        for (i in chunkedTexts.indices) {
            val decryptedChunk = decryptChunk(chunkedTexts[i], keys) xor lastDecBlock
            //copying the result into the output array
            for (j in decryptedChunk.indices) {
                decryptedText[(i*16)+j] = decryptedChunk[j]
            }
            lastDecBlock = chunkedTexts[i]
        }
        return decryptedText
    }

    private fun decryptECB(chunkedTexts:Array<UByteArray>, keys:Array<UByteArray>):UByteArray {
        val decryptedText = UByteArray(chunkedTexts.size * 16)

        for (i in chunkedTexts.indices) {
            val decryptedChunk = decryptChunk(chunkedTexts[i],keys)
            for (j in decryptedChunk.indices) {
                decryptedText[(i*16)+j] = decryptedChunk[j]
            }
        }
        return decryptedText
    }
}