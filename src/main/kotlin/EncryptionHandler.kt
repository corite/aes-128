@file:OptIn(ExperimentalUnsignedTypes::class, ExperimentalUnsignedTypes::class)

import CryptUtils.Constants.xor

class EncryptionHandler {
    private val cryptHandler = CryptUtils()
    private val mixColumnMatrix:Array<UByteArray> = arrayOf(ubyteArrayOf(2u,3u,1u,1u), ubyteArrayOf(1u,2u,3u,1u), ubyteArrayOf(1u,1u,2u,3u), ubyteArrayOf(3u,1u,1u,2u))

    private val sBox = CryptUtils.sBox


    fun encryptChunk(textToEncrypt:UByteArray, keys:Array<UByteArray>):UByteArray {

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

    fun encrypt(text: UByteArray, keyAsBytes: UByteArray, mode: CipherMode):UByteArray {
        val key = cryptHandler.getKeyAsWords(keyAsBytes)
        val keys = cryptHandler.expandKey(key)
        val chunkedTexts = cryptHandler.chunkText(text,16)
        return when(mode) {
            CipherMode.ECB -> encryptECB(chunkedTexts, keys)
            CipherMode.CBC -> encryptCBC(chunkedTexts, keys)
        }
    }

    private fun encryptCBC(chunkedTexts:Array<UByteArray>, keys:Array<UByteArray>):UByteArray {
        val encryptedText = UByteArray(chunkedTexts.size * 16)
        var lastEncBlock = UByteArray(16)

        for (i in chunkedTexts.indices) {
            val encryptedChunk = encryptChunk(chunkedTexts[i] xor lastEncBlock, keys)
            //copying the result into the output array
            for (j in encryptedChunk.indices) {
                encryptedText[(i*16)+j] = encryptedChunk[j]
            }
            lastEncBlock = encryptedChunk
        }
        return encryptedText
    }

    private fun encryptECB(chunkedTexts:Array<UByteArray>, keys:Array<UByteArray>):UByteArray {
        val encryptedText = UByteArray(chunkedTexts.size * 16)

        for (i in chunkedTexts.indices) {
            val encryptedChunk = encryptChunk(chunkedTexts[i],keys)
            for (j in encryptedChunk.indices) {
                encryptedText[(i*16)+j] = encryptedChunk[j]
            }
        }
        return encryptedText
    }
}