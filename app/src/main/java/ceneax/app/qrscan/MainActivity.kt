package ceneax.app.qrscan

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import ceneax.app.lib.qrscan.QRScanActivity
import ceneax.app.qrscan.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), ActivityResultCallback<ActivityResult> {
    private lateinit var binding: ActivityMainBinding

    private val mLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        this
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // 权限申请
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                initView()
                bindEvent()
            } else {
                finish()
            }
        }.launch(Manifest.permission.CAMERA)
    }

    private fun initView() {
    }

    private fun bindEvent() {
        binding.btDefaultScanner.setOnClickListener {
            mLauncher.launch(Intent(this, QRScanActivity::class.java))
        }

        binding.btCustomScanner.setOnClickListener {
            mLauncher.launch(Intent(this, CustomScanActivity::class.java).putExtras(bundleOf(
                "engine" to 0
            )))
        }

        binding.btCustomScannerZXing.setOnClickListener {
            mLauncher.launch(Intent(this, CustomScanActivity::class.java).putExtras(bundleOf(
                "engine" to 1
            )))
        }
    }

    override fun onActivityResult(result: ActivityResult?) {
        result?.data?.extras?.apply {
            Toast.makeText(this@MainActivity, getString("data", ""), Toast.LENGTH_SHORT).show()
        }
    }
}