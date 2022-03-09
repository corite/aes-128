@file:OptIn(ExperimentalUnsignedTypes::class)

import java.lang.IllegalArgumentException
import java.math.BigInteger
import java.util.*

class CryptUtils {
    companion object Constants {
        private const val sBoxString = "0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b, 0x6f, 0xc5, 0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76\n" +
                "0xca, 0x82, 0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0, 0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4, 0x72, 0xc0\n" +
                "0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc, 0x34, 0xa5, 0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15\n" +
                "0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96, 0x05, 0x9a, 0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75\n" +
                "0x09, 0x83, 0x2c, 0x1a, 0x1b, 0x6e, 0x5a, 0xa0, 0x52, 0x3b, 0xd6, 0xb3, 0x29, 0xe3, 0x2f, 0x84\n" +
                "0x53, 0xd1, 0x00, 0xed, 0x20, 0xfc, 0xb1, 0x5b, 0x6a, 0xcb, 0xbe, 0x39, 0x4a, 0x4c, 0x58, 0xcf\n" +
                "0xd0, 0xef, 0xaa, 0xfb, 0x43, 0x4d, 0x33, 0x85, 0x45, 0xf9, 0x02, 0x7f, 0x50, 0x3c, 0x9f, 0xa8\n" +
                "0x51, 0xa3, 0x40, 0x8f, 0x92, 0x9d, 0x38, 0xf5, 0xbc, 0xb6, 0xda, 0x21, 0x10, 0xff, 0xf3, 0xd2\n" +
                "0xcd, 0x0c, 0x13, 0xec, 0x5f, 0x97, 0x44, 0x17, 0xc4, 0xa7, 0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73\n" +
                "0x60, 0x81, 0x4f, 0xdc, 0x22, 0x2a, 0x90, 0x88, 0x46, 0xee, 0xb8, 0x14, 0xde, 0x5e, 0x0b, 0xdb\n" +
                "0xe0, 0x32, 0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c, 0xc2, 0xd3, 0xac, 0x62, 0x91, 0x95, 0xe4, 0x79\n" +
                "0xe7, 0xc8, 0x37, 0x6d, 0x8d, 0xd5, 0x4e, 0xa9, 0x6c, 0x56, 0xf4, 0xea, 0x65, 0x7a, 0xae, 0x08\n" +
                "0xba, 0x78, 0x25, 0x2e, 0x1c, 0xa6, 0xb4, 0xc6, 0xe8, 0xdd, 0x74, 0x1f, 0x4b, 0xbd, 0x8b, 0x8a\n" +
                "0x70, 0x3e, 0xb5, 0x66, 0x48, 0x03, 0xf6, 0x0e, 0x61, 0x35, 0x57, 0xb9, 0x86, 0xc1, 0x1d, 0x9e\n" +
                "0xe1, 0xf8, 0x98, 0x11, 0x69, 0xd9, 0x8e, 0x94, 0x9b, 0x1e, 0x87, 0xe9, 0xce, 0x55, 0x28, 0xdf\n" +
                "0x8c, 0xa1, 0x89, 0x0d, 0xbf, 0xe6, 0x42, 0x68, 0x41, 0x99, 0x2d, 0x0f, 0xb0, 0x54, 0xbb, 0x16"
        private const val sBoxInverseString = "0x52, 0x09, 0x6a, 0xd5, 0x30, 0x36, 0xa5, 0x38, 0xbf, 0x40, 0xa3, 0x9e, 0x81, 0xf3, 0xd7, 0xfb\n" +
                "0x7c, 0xe3, 0x39, 0x82, 0x9b, 0x2f, 0xff, 0x87, 0x34, 0x8e, 0x43, 0x44, 0xc4, 0xde, 0xe9, 0xcb\n" +
                "0x54, 0x7b, 0x94, 0x32, 0xa6, 0xc2, 0x23, 0x3d, 0xee, 0x4c, 0x95, 0x0b, 0x42, 0xfa, 0xc3, 0x4e\n" +
                "0x08, 0x2e, 0xa1, 0x66, 0x28, 0xd9, 0x24, 0xb2, 0x76, 0x5b, 0xa2, 0x49, 0x6d, 0x8b, 0xd1, 0x25\n" +
                "0x72, 0xf8, 0xf6, 0x64, 0x86, 0x68, 0x98, 0x16, 0xd4, 0xa4, 0x5c, 0xcc, 0x5d, 0x65, 0xb6, 0x92\n" +
                "0x6c, 0x70, 0x48, 0x50, 0xfd, 0xed, 0xb9, 0xda, 0x5e, 0x15, 0x46, 0x57, 0xa7, 0x8d, 0x9d, 0x84\n" +
                "0x90, 0xd8, 0xab, 0x00, 0x8c, 0xbc, 0xd3, 0x0a, 0xf7, 0xe4, 0x58, 0x05, 0xb8, 0xb3, 0x45, 0x06\n" +
                "0xd0, 0x2c, 0x1e, 0x8f, 0xca, 0x3f, 0x0f, 0x02, 0xc1, 0xaf, 0xbd, 0x03, 0x01, 0x13, 0x8a, 0x6b\n" +
                "0x3a, 0x91, 0x11, 0x41, 0x4f, 0x67, 0xdc, 0xea, 0x97, 0xf2, 0xcf, 0xce, 0xf0, 0xb4, 0xe6, 0x73\n" +
                "0x96, 0xac, 0x74, 0x22, 0xe7, 0xad, 0x35, 0x85, 0xe2, 0xf9, 0x37, 0xe8, 0x1c, 0x75, 0xdf, 0x6e\n" +
                "0x47, 0xf1, 0x1a, 0x71, 0x1d, 0x29, 0xc5, 0x89, 0x6f, 0xb7, 0x62, 0x0e, 0xaa, 0x18, 0xbe, 0x1b\n" +
                "0xfc, 0x56, 0x3e, 0x4b, 0xc6, 0xd2, 0x79, 0x20, 0x9a, 0xdb, 0xc0, 0xfe, 0x78, 0xcd, 0x5a, 0xf4\n" +
                "0x1f, 0xdd, 0xa8, 0x33, 0x88, 0x07, 0xc7, 0x31, 0xb1, 0x12, 0x10, 0x59, 0x27, 0x80, 0xec, 0x5f\n" +
                "0x60, 0x51, 0x7f, 0xa9, 0x19, 0xb5, 0x4a, 0x0d, 0x2d, 0xe5, 0x7a, 0x9f, 0x93, 0xc9, 0x9c, 0xef\n" +
                "0xa0, 0xe0, 0x3b, 0x4d, 0xae, 0x2a, 0xf5, 0xb0, 0xc8, 0xeb, 0xbb, 0x3c, 0x83, 0x53, 0x99, 0x61\n" +
                "0x17, 0x2b, 0x04, 0x7e, 0xba, 0x77, 0xd6, 0x26, 0xe1, 0x69, 0x14, 0x63, 0x55, 0x21, 0x0c, 0x7d"

        val sBox = readSBox(sBoxString)
        val sBoxInverse = readSBox(sBoxInverseString)

        private fun readSBox(matrixAsString:String):Array<UByteArray>{
            val matrix:Array<UByteArray> = Array(16){ UByteArray(16) }
            val lines = matrixAsString.split("\n")
            for (i in lines.indices) {
                val hexValues = lines[i].split(", ").map { it.substring(2) }.map { it.toUByte(16) }
                //              split into single hex numbers       remove '0x' prefix                  interpret as Hex-Number
                matrix[i] = hexValues.toUByteArray()// save this row
            }
            return matrix
        }
        infix fun UByteArray.xor(other:UByteArray):UByteArray {
            if (this.size == other.size) {
                val output = UByteArray(this.size)
                for (i in output.indices) {
                    output[i] = this[i] xor other[i]
                }
                return output
            } else throw IllegalArgumentException()
        }
        fun BigInteger.to16UByteArray():UByteArray {
            val filledBytes = this.toByteArray().toUByteArray()
            return UByteArray(16-filledBytes.size) +filledBytes
        }
    }

    fun addRoundKey(matrix:Array<UByteArray>, key:UByteArray):Array<UByteArray> {
        for (i in 0..15) {
            matrix[i%4][i/4] = matrix[i%4][i/4] xor key[i]
            // going through the matrix column by column
        }
        return matrix
    }

    fun getAsMatrix(text:UByteArray):Array<UByteArray> {
        val matrix:Array<UByteArray> = Array(4){ UByteArray(4) }
        for (i in text.indices) {
            matrix[i%4][i/4] = text[i]
            //matrix column by column
        }
        return matrix
    }

    fun subBytes(matrix:Array<UByteArray>, sBox:Array<UByteArray>):Array<UByteArray> {
        for (i in 0..3) {
            for (j in 0..3) {
                val currentValue = padHex(matrix[j][i].toString(radix = 16),2)
                val firstBytes = currentValue.substring(0,1).toInt(radix = 16)
                val secondBytes = currentValue.substring(1).toInt(radix = 16)
                matrix[j][i] = sBox[firstBytes][secondBytes]
            }
        }
        return matrix
    }

    private fun padHex(number:String, amount:Int) :String {
        return "0".repeat(amount-number.length)+number
    }

    fun shiftRowsLeft(matrix:Array<UByteArray>):Array<UByteArray> {
        for (i in 0..3) {
            val line = matrix[i]
            matrix[i] = List(matrix[i].size) { index -> line[ (index+i+line.size) % line.size ] }.toUByteArray()
        }
        return matrix
    }

    fun shiftRowsRight(matrix:Array<UByteArray>):Array<UByteArray> {
        for (i in 0..3) {
            val line = matrix[i]
            matrix[i] = List(matrix[i].size) { index -> line[ (index-i+line.size) % line.size ] }.toUByteArray()
        }
        return matrix
    }

    private fun xTime(a:UByte):UByte {
        var t = a.toInt() shl 1

        if ("80".toInt(16) and a.toInt() != 0){
            //check if the highest order bit is set
            t = t xor "1b".toInt(16)
        }

        return (t and "FF".toInt(16)).toUByte()
        //this removes all bits other than the first 8
    }

    fun mixColumns(matrix:Array<UByteArray>, mcMatrix:Array<UByteArray>):Array<UByteArray> {
        for (i in 0..3) {
            var column = matrix.map { it[i] }.toUByteArray() //extract column
            column = matrixMultiply(column, mcMatrix)
            matrix.forEachIndexed { index, it -> it[i] = column[index] } //re-insert column
        }
        return matrix
    }

    private fun matrixMultiply(vector:UByteArray, matrix:Array<UByteArray>):UByteArray {
        val resultVector = UByteArray(matrix.size)
        for(i in matrix.indices) {
            for (j in matrix.indices) {
                resultVector[i] = resultVector[i] xor multiply(matrix[i][j], vector[j])
            }
        }
        return resultVector
    }

    private fun multiply(a:UByte, b:UByte):UByte {
        var aNum = a
        var bNum = b
        var sum:UByte = 0u

        while (aNum > 0u) {
            if (aNum % 2u != 0u) {
                sum = sum xor bNum
            }
            bNum = xTime(bNum)
            aNum = (aNum.toInt() shr 1).toUByte()

        }
        return sum
    }

    fun getMatrixAsUByteArray(matrix: Array<UByteArray>):UByteArray {
        val array = UByteArray(16)
        for (i in 0 until 16) {
            array[i] = matrix[i%4][i/4]
        }
        return array
    }

    fun printMatrix(matrix: Array<UByteArray>) {
        println()
        for (row in matrix) {
            println(row.joinToString("") { padHex(it.toString(16), 2) })
        }
        println()
    }

    fun expandKey(k:IntArray):Array<UByteArray> {
        val w = IntArray(44)
        for (i in 0..43) {
            if (i<4) {
                w[i] = k[i]
            } else if(i % 4 == 0) {
                w[i] = w[i-4] xor rcon(i/4) xor subWord(rotWord(w[i-1]))
            } else {
                w[i] = w[i-4] xor w[i-1]
            }
        }


        val wordArrays = chunkText(w,4) // group into words
        val uByteArrays = Array(11) { UByteArray(16) }
        for (i in wordArrays.indices) {
            //convert words to bytes
            val keyAsBytes = mutableListOf<UByte>()
            for (word in wordArrays[i]) {
                keyAsBytes.addAll(getBytes(word).toMutableList())
            }
            uByteArrays[i]= keyAsBytes.toUByteArray()
        }
        return uByteArrays
    }

    private fun rcon(i:Int):Int {
        val rci = intArrayOf("01".toInt(16),"02".toInt(16),"04".toInt(16),"08".toInt(16),"10".toInt(16),"20".toInt(16),"40".toInt(16),"80".toInt(16),"1b".toInt(16),"36".toInt(16))
        return rci[i-1] shl 24
    }

    private fun subWord(word:Int):Int {
        var wordString = ""
        for (byte in getBytes(word)) {
            val byteAsHex = padHex(byte.toString(16),2)
            val firstByte = byteAsHex.substring(0,1).toInt(16)
            val secondByte = byteAsHex.substring(1).toInt(16)
            wordString += padHex(sBox[firstByte][secondByte].toString(16),2)
            //sub evey byte and concat as hex string
        }
        return wordString.toUInt(16).toInt()
    }

    private fun getBytes(word: Int):UByteArray {
        val mask:UInt = "FF000000".toUInt(16)
        val bytes = UByteArray(4)
        for (i in 0..3) {
            bytes[i] = ((word.toUInt() and (mask shr (8*i))) shr ((3-i)*8)).toUByte()
        }
        return bytes
    }

    private fun getWord(bytes:UByteArray):Int {
        return bytes.joinToString("") { padHex(it.toString(16), 2) }.toUInt(16).toInt()
    }

    fun getKeyAsWords(keyAsBytes:UByteArray):IntArray {
        val words = IntArray(keyAsBytes.size/4)
        for (i in 0 until (keyAsBytes.size/4)) {
            words[i] = getWord(keyAsBytes.copyOfRange(i*4,(i+1)*4))
        }
        return words
    }

    private fun rotWord(word:Int):Int {
        val bytes = getBytes(word).toMutableList()
        Collections.rotate(bytes,3)
        //rotate 1 to the left
        return getWord(bytes.toUByteArray())
    }

    private fun chunkText(text:IntArray, size:Int):Array<IntArray> {
        val list: MutableList<IntArray> = mutableListOf()
        for (i in 1..(text.size/size)) {
            list += text.copyOfRange((i-1)*size,i*size)
        }
        val remainder = text.size % size

        if (remainder != 0) {
            //pad with zeros if necessary
            val lastList: MutableList<Int> = text.copyOfRange(text.size-remainder,text.size).toMutableList()
            for (i in remainder until size) {
                lastList += 0
            }
            list += lastList.toIntArray()
        }

        return list.toTypedArray()
    }

    fun chunkText(text:UByteArray,size:Int):Array<UByteArray> {
        val list: MutableList<UByteArray> = mutableListOf()
        for (i in 1..(text.size/size)) {
            list += text.copyOfRange((i-1)*size,i*size)
        }
        val remainder = text.size % size

        if (remainder != 0) {
            //pad with zeros if necessary
            val lastList: MutableList<UByte> = text.copyOfRange(text.size-remainder,text.size).toMutableList()
            for (i in remainder until size) {
                lastList += 0u
            }
            list += lastList.toUByteArray()
        }

        return list.toTypedArray()
    }
    fun getCtr(nonce:ULong):ULong {
        val leadingZeros = nonce.countLeadingZeroBits()
        return nonce shl leadingZeros
        //shifts the number so that the first bit is always 1 (as long as the number doesn't equal 0)
    }
}