package com.siddharthsinghbaghel.canvakids

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_info.*

class InfoActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "👦🏽 Info"
        setContentView(R.layout.activity_info)

        git.setOnClickListener(this)
        linkedin.setOnClickListener(this)
        btn_back.setOnClickListener(this)

    }

    override fun onClick(v: View?) {

        when(v?.id){

            R.id.git->{
                val intent = Intent("android.intent.action.VIEW", Uri.parse("https://github.com/Siddharth-sing"));
                startActivity(intent)
            }
            R.id.linkedin->{
                val intent = Intent("android.intent.action.VIEW", Uri.parse("https://www.linkedin.com/in/siddharth-singh-baghel-912866190/"));
                startActivity(intent)
            }
            R.id.btn_back ->{
                val intent = Intent(this,MainActivity::class.java);
                startActivity(intent)
                finish()
            }
        }
    }
}