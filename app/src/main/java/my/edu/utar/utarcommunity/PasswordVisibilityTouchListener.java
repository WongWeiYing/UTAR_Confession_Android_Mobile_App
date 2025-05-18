package my.edu.utar.utarcommunity;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class PasswordVisibilityTouchListener implements View.OnTouchListener{

    private final EditText password;
    private boolean passwordVisible;
    private static final int DRAWABLE_RIGHT = 2 ;

    public PasswordVisibilityTouchListener(EditText password) {
        this.password = password;

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (event.getRawX() >= password.getRight() - password.getPaddingRight() - password.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width()) {
                int selection = password.getSelectionEnd();

                if (passwordVisible) {
                    Log.d("onTouch", "Touch detected on drawable. Toggling password visibility.");
                    password.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.visibility_off, 0);
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passwordVisible = false;
                } else {
                    password.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.visibility_on, 0);
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passwordVisible = true;
                }
                password.setSelection(selection);
                return true;
            } else {
                Log.d("onTouch", "Touch not detected on drawable. Performing click action.");
                v.performClick();
            }
        }
        return false;
    }
}
