package ceneax.app.qrscan

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import ceneax.app.lib.qrscan.QRScan
import ceneax.app.lib.qrscan.arch.IScanCallback
import ceneax.app.lib.qrscan.bean.ParseResult
import ceneax.app.qrscan.databinding.ActivityCustomScanBinding

class CustomScanActivity : AppCompatActivity(), IScanCallback {
    private lateinit var binding: ActivityCustomScanBinding
    private lateinit var mQRScan: QRScan

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityCustomScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        bindEvent()
    }

    private fun initView() {
        // 上一个界面传过来的值，引擎类型
        val engine = intent?.extras?.getInt("engine", 0) ?: 0

        // 初始化扫描器
        mQRScan = QRScan.Builder(this)
            // 整个Builder方法中，只有这一个必须设置一个值，其他方法都是可选
            .setQRPreviewView(binding.qrPreviewView)
            .setEngine(if (engine == 0) QRScan.Engine.MLKIT else QRScan.Engine.ZXING)
            .setScanCallback(this)
            .build()
    }

    private fun bindEvent() {
    }

    override fun onResult(bitmap: Bitmap, vararg results: ParseResult) {
        val singleResult = results.first()
        Toast.makeText(
            this,
            "结果：${singleResult.content}，耗时：${singleResult.tookMs}，引擎：${mQRScan.engine.name}",
            Toast.LENGTH_SHORT
        ).show()

        // 连续扫描，延迟2s后继续扫描
        mQRScan.rescan(2000)
    }

    override fun onDestroy() {
        super.onDestroy()
        mQRScan.release()
    }
}