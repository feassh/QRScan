# QRScan

> 一个简单好用的 Android 条码、二维码扫描识别库

------------

[![](https://jitpack.io/v/ceneax/QRScan.svg)](https://jitpack.io/#ceneax/QRScan)

------------

### 引入依赖

##### 第一步：
项目根目录的 **build.gradle** 文件中加入以下代码：

```Groovy
allprojects {
    repositories {
        ...
        // 加入这行代码
        maven { url 'https://jitpack.io' }
    }
}
```

##### 第二步：
在项目模块中的 **build.gradle** 文件中加入以下代码：

```Groovy
dependencies {
    ...
    implementation 'com.github.ceneax:QRScan:需要引入的版本号'
}
```

### 开始使用

为了方便使用，该库内置了一个符合大众场景使用的扫描Activity，无需自己画界面，开箱即用。支持识别多个条码、二维码，支持选择相册图片进行解析。

无需进行任何初始化操作，无需提前申请拍照权限，该Activity会自动尝试申请权限。直接启动 **QRScanActivity**

```Kotlin
startActivityForResult(Intent(this, QRScanActivity::class.java), REQUEST_CODE)
```

然后接收Activity返回的结果，以 **Bundle** 的形式传递的 **String** 类型值，**key** 为 **"data"**

```Kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    data?.extras?.apply {
        Toast.makeText(this@MainActivity, getString("data", ""), Toast.LENGTH_SHORT).show()
    }
}
```

实际运行效果截图：

![](https://pic.imgdb.cn/item/629cca39094754312944b821.jpg)

多个二维码的情况下，会弹出选择界面，每一个二维码都会被绿色的圆角矩形框选出来，点击你想要解析的某一个二维码即可：

![](https://pic.imgdb.cn/item/629ccbe40947543129473432.jpg)

### 自定义扫描界面样式

如果预置的扫描界面不能满足使用场景，可以使用自定义方式，自己设计界面和扫描逻辑。

首先创建一个布局文件，布局中添加一个 **QRPreviewView** ：

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <ceneax.app.lib.qrscan.widget.QRPreviewView
        android:id="@+id/qrPreviewView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

然后在Activity中初始化扫描类 **（记得先申请拍照权限）** ：

```Kotlin
private lateinit var mQRScan: QRScan

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)

    // 初始化扫描器
    mQRScan = QRScan.Builder(this)
        // 整个Builder方法中，只有这一个必须设置一个值，其他方法都是可选
        .setQRPreviewView(binding.qrPreviewView)
        // 设置扫描结果回调
        .setScanCallback { bitmap, results ->
            // bitmap 为相机中得到的图像，包含二维码
            // results 为扫描结果数组，如果存在多个二维码，会返回多个结果
            // 取第一个结果
            taost(results[0].content)
        }
        .build()
}
```

默认只支持一次扫描，当扫描到结果并回调后，将不会处理后续的图像数据。如果想继续执行扫描的话，只需调用 **QRScan.rescan()** 方法即可：

```Kotlin
// 立即执行继续扫描
mQRScan.rescan()

// 延迟2s后继续扫描
mQRScan.rescan(2000)
```

退出扫描界面的时候记得在 **onDestroy** 中释放下资源：

```Kotlin
override fun onDestroy() {
    super.onDestroy()
    mQRScan.release()
}
```

这样就完成了自定义扫描功能，可以自己设计想要的任何界面以及具体扫描逻辑。

### QRScan.Builder

在自定义扫描功能的时候，要用到 **QRScan.Builder** 类来进行初始化相机并执行扫描识别任务，下面对 **Builder** 类中的方法进行说明。

```Kotlin
// 该方法必须执行，传入 QRPreviewView 用于预览相机画面
setQRPreviewView(QRPreviewView qrPreviewView)
```

```Kotlin
// 可选，默认：Engine.MLKIT
// 指定一个识别解析引擎，目前内置了两种：
// Engine.MLKIT：基于 Google MLKit BarcodeScan，识别速度快，支持多个条码、二维码，也是默认引擎
// Engine.ZXING：基于 Google ZXing，不支持多个条码、二维码
setEngine(Engine engine)
```

```Kotlin
// 可选，默认：CameraSelector.LENS_FACING_BACK
// 指定一个摄像头
// CameraSelector.LENS_FACING_FRONT：前置摄像头
// CameraSelector.LENS_FACING_BACK： 后置摄像头
setCameraId(int cameraId)
```

```Kotlin
// 可选，默认为空，不进行任何回调
// 回调参数：Bitmap bitmap：返回包含条码、二维码的图像
// 回调参数：ParseResult[] results：返回识别结果，数组形式
setScanCallback(IScanCallback scanCallback)
```

```Kotlin
// 可选，默认：DefaultQRAnimationView
// 指定一个扫描线动画，默认使用白色边缘透明的椭圆进行从上到下循环平移的非线性动画
// 传入 null ，则表示不使用扫描动画
// 如果需要自定义动画，创建一个类继承 QRAnimationView ，覆写 onDraw() 方法即可
// 具体可参考内置 DefaultQRAnimationView 的实现方式
setAnimationView(QRAnimationView animationView)
```