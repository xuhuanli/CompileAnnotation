package com.lollitech.compileannotation

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.lollitech.annotations.BindClass
import com.lollitech.annotations.BindView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@BindClass
class MainActivity : AppCompatActivity() {
    @BindView(R.id.tv1)
    var tv1: TextView? = null

    @BindView(R.id.tv2)
    var tv2: TextView? = null

    @BindView(R.id.btn1)
    var btn1: Button? = null

    @BindView(R.id.tv2)
    var btn2: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        MainActivity_bindView.bindView(this)
        setContentView(R.layout.activity_main)
        tv1
    }
}