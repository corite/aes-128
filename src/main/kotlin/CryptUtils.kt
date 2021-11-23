import java.io.File
import java.util.*

class CryptUtils {

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

    fun padHex(number:String, amount:Int) :String {
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

    fun xTime(a:Int):Int {
        var t = a shl 1

        if ("80".toInt(16) and t != 0){
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

    fun matrixMultiply(vector:IntArray, matrix:Array<IntArray>):IntArray {
        val resultVector = IntArray(matrix.size)
        //todo: not sure if this works
        for(i in matrix.indices) {
            //resultVector[i] = (multiply(matrix[i][0],vector[0]) xor multiply(matrix[i][1],vector[1])) xor (multiply(matrix[i][2],vector[2]) xor multiply(matrix[i][3],vector[3]))
            for (j in matrix.indices) {
                resultVector[i] = resultVector[i] xor multiply(matrix[i][j], vector[i])
            }
        }
        return resultVector
    }

    fun multiply(a:Int, b:Int):Int {
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


    fun getMatrixAsHexString(matrix: Array<IntArray>):String {
        var hexString = ""
        for (row in matrix) {
            for (value in row) {
                hexString += value.toString(16)
            }
        }
        return hexString
    }

    fun printMatrix(matrix: Array<IntArray>) {
        println()
        for (row in matrix) {
            println(row.joinToString("") { padHex(it.toString(16), 2) })
        }
        println()
    }

    fun expandKeys(keys:IntArray, sBox:Array<IntArray>):IntArray {
        val roundKeys = IntArray(44)
        for (i in 0..43) {
            if (i<4) {
                roundKeys[i] = keys[i]
            } else if(i >= 4 && i % 4 == 0) {
                roundKeys[i] = keys[i-4] xor rcon(i/4,sBox) xor subWord(rotWord(roundKeys[i-1]),sBox)
            } else {
                roundKeys[i] = roundKeys[i-4] xor roundKeys[i-1]
            }
        }
        return roundKeys
    }

    fun rcon(i:Int,sBox: Array<IntArray>):Int {
        val iAsHex = padHex(i.toString(16),2)
        val firstByte = iAsHex.substring(0,1).toInt(16)
        val secondByte = iAsHex.substring(1).toInt(16)
        return (padHex(sBox[firstByte][secondByte].toString(16),2) + "000000").toInt(16)
    }

    fun subWord(word:Int,sBox: Array<IntArray>):Int {
        var wordString = ""
        for (byte in getBytes(word)) {
            val byteAsHex = padHex(byte.toString(16),2)
            val firstByte = byteAsHex.substring(0,1).toInt(16)
            val secondByte = byteAsHex.substring(1).toInt(16)
            wordString += padHex(sBox[firstByte][secondByte].toString(16),2)
        }
        return wordString.toInt(16)
    }

    fun getBytes(word: Int):IntArray {
        val bitMask = "FF000000".toInt(16)
        val bytes = IntArray(4)
        for (i in bytes.indices) {
            bytes[i] = (word and (bitMask ushr (2*i))) ushr 2*(3-i)
        }
        return bytes
    }

    fun getWord(bytes:IntArray):Int {
        return bytes.joinToString("") { padHex(it.toString(16), 2) }.toInt(16)
    }

    fun rotWord(word:Int):Int {
        val bytes = getBytes(word).toMutableList()
        Collections.rotate(bytes,1)
        return getWord(bytes.toIntArray())
    }



}