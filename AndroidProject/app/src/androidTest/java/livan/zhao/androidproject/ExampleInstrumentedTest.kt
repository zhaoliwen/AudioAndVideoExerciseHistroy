package livan.zhao.androidproject

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.io.IOException

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    val TAG = "ExampleInstrumentedTest"
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

    @Test
    fun get_H264File_bytes_Test(){
        val BYTES = 1000
        var buffer = get_H264File_bytes(BYTES)
        assertEquals(buffer.size, BYTES)
    }



}