package livan.zhao.androidproject

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer


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
         * Context ctx = InstrumentationRegistry.getTargetContext();
         *
         * 获取src/androidTest/assets目录中的资源文件
         * Context ctx = InstrumentationRegistry.getContext();
         *
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

    @Throws(Exception::class)
    fun decodeToPCM(musicPath: String?, outPath: String?, startTime: Int, endTime: Int) {
        if (endTime < startTime) {
            return
        }
        //    MP3  （zip  rar    ） ----> aac   封装个事 1   编码格式
//        jie  MediaExtractor = 360 解压 工具
        val mediaExtractor = MediaExtractor()

        mediaExtractor.setDataSource(musicPath!!)
        val audioTrack = selectTrack(mediaExtractor)

        mediaExtractor.selectTrack(audioTrack)
        // 视频 和音频
        mediaExtractor.seekTo(startTime.toLong(), MediaExtractor.SEEK_TO_CLOSEST_SYNC)
        // 轨道信息  都记录 编码器
        val oriAudioFormat = mediaExtractor.getTrackFormat(audioTrack)
        var maxBufferSize = 100 * 1000
        maxBufferSize = if (oriAudioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
            oriAudioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
        } else {
            100 * 1000
        }
        val buffer = ByteBuffer.allocateDirect(maxBufferSize)
        //        h264   H265  音频
        val mediaCodec = MediaCodec.createDecoderByType(oriAudioFormat.getString((MediaFormat.KEY_MIME))!!)
        //        设置解码器信息    直接从 音频文件
        mediaCodec.configure(oriAudioFormat, null, null, 0)
        val pcmFile = File(outPath)
        val writeChannel = FileOutputStream(pcmFile).channel
        mediaCodec.start()
        val info = MediaCodec.BufferInfo()
        var outputBufferIndex = -1
        while (true) {
            val decodeInputIndex = mediaCodec.dequeueInputBuffer(100000)
            if (decodeInputIndex >= 0) {
                val sampleTimeUs = mediaExtractor.sampleTime

                if (sampleTimeUs == -1L) {
                    break
                } else if (sampleTimeUs < startTime) {
//                    丢掉 不用了
                    mediaExtractor.advance()
                    continue
                } else if (sampleTimeUs > endTime) {
                    break
                }
                //                获取到压缩数据
                info.size = mediaExtractor.readSampleData(buffer, 0)
                info.presentationTimeUs = sampleTimeUs
                info.flags = mediaExtractor.sampleFlags

                //                下面放数据  到dsp解码
                val content = ByteArray(buffer.remaining())
                buffer[content]
                //                输出文件  方便查看
//                FileUtils.writeContent(content);
//                解码
                val inputBuffer = mediaCodec.getInputBuffer(decodeInputIndex)
                inputBuffer!!.put(content)
                mediaCodec.queueInputBuffer(
                    decodeInputIndex,
                    0,
                    info.size,
                    info.presentationTimeUs,
                    info.flags
                )
                //                释放上一帧的压缩数据
                mediaExtractor.advance()
            }

            outputBufferIndex = mediaCodec.dequeueOutputBuffer(info, 100000)
            while (outputBufferIndex >= 0) {
                val decodeOutputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex)
                writeChannel.write(decodeOutputBuffer) //MP3  1   pcm2
                mediaCodec.releaseOutputBuffer(outputBufferIndex, false)
                outputBufferIndex = mediaCodec.dequeueOutputBuffer(info, 100000)
            }
        }
        writeChannel.close()
        mediaExtractor.release()
        mediaCodec.stop()
        mediaCodec.release()
        //        转换MP3    pcm数据转换成mp3封装格式
//
//        File wavFile = new File(Environment.getExternalStorageDirectory(),"output.mp3" );
//        new PcmToWavUtil(44100,  AudioFormat.CHANNEL_IN_STEREO,
//                2, AudioFormat.ENCODING_PCM_16BIT).pcmToWav(pcmFile.getAbsolutePath()
//                , wavFile.getAbsolutePath());
        Log.i(TAG, "mixAudioTrack: 转换完毕")
    }

    private fun selectTrack(mediaExtractor: MediaExtractor): Int {
//获取每条轨道
        val numTracks = mediaExtractor.trackCount
        for (i in 0 until numTracks) {
//            数据      MediaFormat
            val format = mediaExtractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime!!.startsWith("audio/")) {
                return i
            }
        }
        return -1
    }

//===========================================Test================================================
    @Test
    fun decodeToPCM_Test(){
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val context = InstrumentationRegistry.getInstrumentation().context
        val fileName = "TwentyThousand_go_downstairs.aac"
        val inputStream = context.assets.open(fileName)
        val tempFile = File(appContext.cacheDir, "temp_aac.aac")

        FileOutputStream(tempFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        println("${appContext.cacheDir.path}")

        decodeToPCM(tempFile.absolutePath,"${appContext.filesDir.path}/TwentyThousand_go_downstairs_2_5.pcm",2000*1000,5000*1000)
    }

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

        val level_idc = readBitsAsInt(8, h264Buffer) // 编码等级
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