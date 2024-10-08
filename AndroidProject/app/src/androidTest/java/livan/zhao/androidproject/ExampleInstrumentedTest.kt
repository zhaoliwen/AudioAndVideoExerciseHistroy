package livan.zhao.androidproject

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    val TAG = "ExampleInstrumentedTest"
    var nStartBit: Int = 0
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("livan.zhao.androidproject", appContext.packageName)
    }

    fun get_H264File_bytes(BYTES:Int = 1000):ByteArray{
        /**
         * 获取src/main/assets目录中的资源文件
         *
         * Context ctx = InstrumentationRegistry.getTargetContext();
         * 获取src/androidTest/assets目录中的资源文件
         *
         * Context ctx = InstrumentationRegistry.getContext();
         * src/androidTest下创建assets目录可以通过new Disectory选择assets方式创建
         */
        val appContext = InstrumentationRegistry.getInstrumentation().context
        val assetManager = appContext.assets
        val fileName = "TwentyThousand_go_downstairs.h264"

        val buffer = ByteArray(BYTES)
        try {
            // 打开指定的文件
            assetManager.open(fileName).use { inputStream ->
                // 读取指定字节数

                val bytesRead = inputStream.read(buffer, 0, BYTES)

                // 处理读取到的字节
                if (bytesRead != -1) {
                    // 这里可以处理读取到的数据，例如：
                    Log.d(TAG,"Read $bytesRead bytes: ${buffer.joinToString(", ")}")
                } else {
                    Log.d(TAG,"End of file reached")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return buffer
    }

    private fun readBitsAsInt(readBitNum: Int, buffer: ByteArray): Int {
        var dwRet = 0
        for (i in 0 until readBitNum) {
            // 每次循环，dwRet 会左移 1 位（shl 1），为即将读取的新比特腾出空间。
            dwRet = dwRet shl 1
            /*
             * nStartBit / 8 用于确定当前要读取的字节位置。
             * nStartBit % 8 用来确定在这个字节内的具体比特位置。
             */
            if ((buffer[nStartBit / 8].toInt() and (0x80 shr (nStartBit % 8))) != 0) {
                dwRet += 1
            }
            nStartBit++
        }
        return dwRet
    }

    fun columbusDecode(pBuff: ByteArray): Int {
        //        5位  8位的表示
//统计0 的个数
        var nZeroNum = 0
        while (nStartBit < pBuff.size * 8) {
            if ((pBuff[nStartBit / 8].toInt() and (0x80 shr (nStartBit % 8))) != 0) {
                break
            }
            nZeroNum++
            nStartBit++
        }

        nStartBit++
        //跳出循环 到外面记录值  000 1  110      110
//                    0001
//        计算  101的十进制
        var dwRet = 0 //1  0
        for (i in 0 until nZeroNum) {
            dwRet = dwRet shl 1 //0 <<1   1*2=2 11  0   3*2=6
            if ((pBuff[nStartBit / 8].toInt() and (0x80 shr (nStartBit % 8))) != 0) {
                dwRet += 1 //6+0 dwRet=6
            }
            nStartBit++
        }
        val value = (1 shl nZeroNum) - 1 + dwRet
        return value
    }

//===========================================Test================================================
    @Test
    fun columbusDecode_Test(){
        nStartBit = 4*8
        val BYTES = 2000
        var h264Buffer = get_H264File_bytes(BYTES)
        val forbidden_zero_bit = readBitsAsInt(1, h264Buffer)
        assertEquals(forbidden_zero_bit,0)

        val nal_ref_idc = readBitsAsInt(2, h264Buffer)
        assertEquals(nal_ref_idc,3)

        val nal_unit_type = readBitsAsInt(5, h264Buffer)
        assertEquals(nal_unit_type,7)

        val profile_idc = readBitsAsInt(8, h264Buffer)
        assertEquals(profile_idc,100)

        val constraint_flag = readBitsAsInt(8, h264Buffer) // 约束标志
        assertEquals(constraint_flag,0)

        val level_idc = readBitsAsInt(8, h264Buffer) // 约束标志
        assertEquals(level_idc,50)

        val start_colubus_flag = columbusDecode(h264Buffer)
        assertEquals(0, start_colubus_flag)

        val chroma_format_idc = columbusDecode(h264Buffer)
        assertEquals(1, chroma_format_idc)

        println("forbidden_zero_bit: $forbidden_zero_bit, " +
                "nal_ref_idc: $nal_ref_idc, " +
                "nal_unit_type: $nal_unit_type,"+
                "profile_idc: $profile_idc"
        )
    }

    @Test
    fun readBitsAsInt_Test(){
        nStartBit = 4*8
        val BYTES = 2000
        var h264Buffer = get_H264File_bytes(BYTES)
        val forbidden_zero_bit = readBitsAsInt(1, h264Buffer)
        val nal_ref_idc = readBitsAsInt(2, h264Buffer)
        val nal_unit_type = readBitsAsInt(5, h264Buffer)
        val profile_idc = readBitsAsInt(8, h264Buffer)
        assertEquals(forbidden_zero_bit,0)
        assertEquals(nal_ref_idc,3)
        assertEquals(nal_unit_type,7)
        assertEquals(profile_idc,100)
        println("forbidden_zero_bit: $forbidden_zero_bit, " +
                "nal_ref_idc: $nal_ref_idc, " +
                "nal_unit_type: $nal_unit_type,"+
                "profile_idc: $profile_idc"
        )
    }

    @Test
    fun get_H264File_bytes_Test(){
        val BYTES = 2000
        var buffer = get_H264File_bytes(BYTES)
        assertEquals(buffer.size, BYTES)
    }



}