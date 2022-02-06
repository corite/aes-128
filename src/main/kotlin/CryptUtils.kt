import java.io.File
import java.util.*

class CryptUtils {
    private val filePrefix = "src"+File.separator+"main"+File.separator+"resources"+File.separator
    private val sBox = readSBox(filePrefix+"SBox.txt")

    fun addRoundKey(matrix:Array<IntArray>, key:IntArray):Array<IntArray> {
        for (i in 0..15) {
            matrix[i%4][i/4] = matrix[i%4][i/4] xor key[i]
        }
        return matrix
    }

    fun getAsMatrix(text:IntArray):Array<IntArray> {
        val matrix:Array<IntArray> = Array(4){ IntArray(4) }
        for (i in text.indices) {
            matrix[i%4][i/4] = text[i]
            //matrix column by column
        }
        return matrix
    }

    fun readSBox(filename:String):Array<IntArray>{
        val matrix:Array<IntArray> = Array(16){ IntArray(16) }
        val lines = File(filename).readLines(Charsets.US_ASCII)
        for (i in lines.indices) {
            val hexValues = lines[i].split(", ").map { it.substring(2) }.map { it.toInt(16) }
            matrix[i] = hexValues.toIntArray()
        }
        return matrix
    }

    fun subBytes(matrix:Array<IntArray>, sBox:Array<IntArray>):Array<IntArray> {
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

    fun shiftRowsLeft(matrix:Array<IntArray>):Array<IntArray> {
        for (i in 0..3) {
            val line = matrix[i]
            matrix[i] = line.mapIndexed { index, _ -> line[ (index+i+line.size) % line.size ] }.toIntArray()
        }
        return matrix
    }

    fun shiftRowsRight(matrix:Array<IntArray>):Array<IntArray> {
        for (i in 0..3) {
            val line = matrix[i]
            matrix[i] = line.mapIndexed { index, _ -> line[ (index-i+line.size) % line.size ] }.toIntArray()
        }
        return matrix
    }

    private fun xTime(a:Int):Int {
        var t = a shl 1

        if ("80".toInt(16) and a != 0){
            //check if the highest order bit is set
            t = t xor "1b".toInt(16)
        }

        return t and "FF".toInt(16)
        //this removes all bits other than the first 8
    }

    fun mixColumns(matrix:Array<IntArray>, mcMatrix:Array<IntArray>):Array<IntArray> {
        for (i in 0..3) {
            var column = matrix.map { it[i] }.toIntArray()
            column = matrixMultiply(column, mcMatrix)
            matrix.forEachIndexed { index, it -> it[i] = column[index] }
        }
        return matrix
    }

    private fun matrixMultiply(vector:IntArray, matrix:Array<IntArray>):IntArray {
        val resultVector = IntArray(matrix.size)
        for(i in matrix.indices) {
            for (j in matrix.indices) {
                resultVector[i] = resultVector[i] xor multiply(matrix[i][j], vector[j])
            }
        }
        return resultVector
    }

    private fun multiply(a:Int, b:Int):Int {
        var aNum = a
        var bNum = b
        var sum = 0

        while (aNum>0) {
            if (aNum % 2 != 0) {
                sum = sum xor bNum
            }
            bNum = xTime(bNum)
            aNum = aNum shr 1

        }
        return sum
    }

    fun getMatrixAsIntArray(matrix: Array<IntArray>):IntArray {
        val array = IntArray(16)
        for (i in 0 until 16) {
            array[i] = matrix[i%4][i/4]
        }
        return array
    }

    fun printMatrix(matrix: Array<IntArray>) {
        println()
        for (row in matrix) {
            println(row.joinToString("") { padHex(it.toString(16), 2) })
        }
        println()
    }

    fun expandKey(k:IntArray):Array<IntArray> {
        val w = IntArray(44)
        for (i in 0..43) {
            if (i<4) {
                w[i] = k[i]
            } else if(i >= 4 && i % 4 == 0) {
                w[i] = w[i-4] xor rcon(i/4) xor subWord(rotWord(w[i-1]))
            } else {
                w[i] = w[i-4] xor w[i-1]
            }
        }


        val wordArrays = chunkText(w,4)
        for (i in wordArrays.indices) {
            //convert words to bytes
            val keyAsBytes = mutableListOf<Int>()
            for (word in wordArrays[i]) {
                keyAsBytes.addAll(getBytes(word).toMutableList())
            }
            wordArrays[i]= keyAsBytes.toIntArray()
        }
        return wordArrays
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
        }
        return wordString.toUInt(16).toInt()
    }

    private fun getBytes(word: Int):IntArray {
        val mask:UInt = "FF000000".toUInt(16)
        val bytes = IntArray(4)
        for (i in 0..3) {
            bytes[i] = ((word.toUInt() and (mask shr (8*i))) shr ((3-i)*8)).toInt()
        }
        return bytes
    }

    private fun getWord(bytes:IntArray):Int {
        return bytes.map { padHex(it.toString(16),2) }.joinToString("").toUInt(16).toInt()
    }
    fun getKeyAsWords(keyAsBytes:IntArray):IntArray {
        val words = IntArray(keyAsBytes.size/4)
        for (i in 0 until (keyAsBytes.size/4)) {
            words[i] = getWord(keyAsBytes.copyOfRange(i*4,(i+1)*4))
        }
        return words
    }

    private fun rotWord(word:Int):Int {
        val bytes = getBytes(word).toMutableList()
        Collections.rotate(bytes,3)
        return getWord(bytes.toIntArray())
    }

    fun chunkText(text:IntArray,size:Int):Array<IntArray> {
        val list: MutableList<IntArray> = mutableListOf()
        for (i in 1..(text.size/size)) {
            list += text.copyOfRange((i-1)*size,i*size)
        }
        val remainder = text.size%size

        if (remainder != 0) {
            val lastList: MutableList<Int> = text.copyOfRange(text.size-remainder,text.size).toMutableList()
            for (i in remainder until size) {
                lastList +=0
            }
            list += lastList.toIntArray()
        }

        return list.toTypedArray()
    }
}