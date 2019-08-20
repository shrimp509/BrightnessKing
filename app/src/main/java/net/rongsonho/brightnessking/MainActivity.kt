package net.rongsonho.brightnessking

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    lateinit var btn : ImageButton
    var isActivate : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn = findViewById(R.id.main_btn)

        btn.setOnClickListener{
            isActivate = !isActivate
            if (isActivate){
                btn.setImageResource(R.drawable.btn_on)
            }else{
                btn.setImageResource(R.drawable.btn_off)
            }
        }
    }
}
