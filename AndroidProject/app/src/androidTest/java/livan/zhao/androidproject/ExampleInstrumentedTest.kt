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

    private fun readBitsAsInt(readBitNum: Int, h264: ByteArray): Int {
        var dwRet = 0
        for (i in 0 until readBitNum) {
            dwRet = dwRet shl 1
            /**
             * nStartBit / 8 用于确定当前要读取的字节位置。
             * nStartBit % 8 用来确定在这个字节内的具体比特位置。
             */
            if ((h264[nStartBit / 8].toInt() and (0x80 shr (nStartBit % 8))) != 0) {
                dwRet += 1
            }
            nStartBit++
        }
        return dwRet
    }

    @Test
    fun readBitsAsInt_Test(){
        nStartBit = 4*8
        val BYTES = 2000
        var h264Buffer = get_H264File_bytes(BYTES)
        val forbidden_zero_bit = readBitsAsInt(1, h264Buffer)
        val nal_ref_idc = readBitsAsInt(2, h264Buffer)
        val nal_unit_type = readBitsAsInt(5, h264Buffer)
        assertEquals(forbidden_zero_bit,0)
        assertEquals(nal_ref_idc,3)
        assertEquals(nal_unit_type,7)
        println("forbidden_zero_bit: $forbidden_zero_bit, nal_ref_idc: $nal_ref_idc, nal_unit_type: $nal_unit_type")
    }

    @Test
    fun get_H264File_bytes_Test(){
        val BYTES = 2000
        var buffer = get_H264File_bytes(BYTES)
        assertEquals(buffer.size, BYTES)
    }



}