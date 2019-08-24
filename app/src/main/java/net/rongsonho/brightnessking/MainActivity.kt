package net.rongsonho.brightnessking

import android.animation.Animator
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    lateinit var btn : ImageButton
    var isActivate : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
    }

    private fun initView(){
        initBrightnessOnOffButton()

        setLogoAnimation()

        setTitleTypeFace()
    }

    private fun initBrightnessOnOffButton(){

        // TODO: use shared preference
        btn = findViewById(R.id.main_btn)
        btn.setOnClickListener{
            isActivate = !isActivate
            if (isActivate){
                btn.setImageResource(R.drawable.button_on_state)
            }else{
                btn.setImageResource(R.drawable.button_off_state)
            }
        }
    }

    private fun setLogoAnimation(){

        // set animation
        val backgroundWhite = findViewById<ImageView>(R.id.white_background)
        val logo = findViewById<ImageView>(R.id.main_icon)

        logo.animate().alpha(1f).setDuration(2000).setListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(p0: Animator?) {
                // Nothing to do
            }

            override fun onAnimationEnd(p0: Animator?) {

                // set Foreground icon and background to
                backgroundWhite.animate().alpha(0f).setDuration(300).start()
                logo.animate().alpha(0f).setDuration(300).start()
            }

            override fun onAnimationCancel(p0: Animator?) {
                // Nothing to do
            }

            override fun onAnimationStart(p0: Animator?) {
                // Nothing to do
            }
        }).start()
    }

    private fun setTitleTypeFace(){
        val title = findViewById<TextView>(R.id.main_title)
        setTypeFace(title)
    }

    private fun setTypeFace(view : TextView){
        view.typeface = Typeface.createFromAsset(assets, resources.getString(R.string.main_title_typeface))
    }
}
