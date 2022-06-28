package com.bonita.filemanager

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bonita.filemanager.constants.FileManagerConstants
import com.bonita.filemanager.databinding.ActivityMainBinding
import com.bonita.filemanager.factory.FileManagerFactory
import com.bonita.filemanager.factory.RootDiskFragment

/**
 * File Manager Activity
 *
 * @author bonita
 * @date 2021-11-17
 */
class MainActivity : AppCompatActivity(), FileManagerListener.FragmentListener {

    private lateinit var binding: ActivityMainBinding

    // 파일매니저 프래그먼트
    private lateinit var fileManagerFragment: FileManagerFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // picker 에서는 dialog theme. inflate 보다 먼저 설정해야 함
        if (intent.action != Intent.ACTION_MAIN) {
            setTheme(R.style.CustomDialogTheme)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // action 에 따라 fragment 시작
        val targetPath = FileManagerConstants.STORAGE.FLASHDISK
        val fragmentTag = RootDiskFragment.FRAGMENT_TAG

        onReplaceFragment(fragmentTag, targetPath, true)
    }

    override fun onReplaceFragment(a_fragmentTag: String, a_targetPath: String, a_isFirst: Boolean) {
        fileManagerFragment = FileManagerFactory.getInstance(a_fragmentTag, a_targetPath, a_isFirst)
        fileManagerFragment.replaceFragmentListener = this

        // Fragment 교체
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_main, fileManagerFragment, a_fragmentTag)
            .commit()
    }
}