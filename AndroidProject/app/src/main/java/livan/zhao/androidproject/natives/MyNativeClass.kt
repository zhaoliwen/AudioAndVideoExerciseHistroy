package livan.zhao.androidproject.natives

class MyNativeClass {
    external fun myNativeMethod(param: Int): Int
    external fun ffmpegInfo():String

    companion object {
        init {
            System.loadLibrary("my_native_lib") // 加载 native 库
        }
    }
}