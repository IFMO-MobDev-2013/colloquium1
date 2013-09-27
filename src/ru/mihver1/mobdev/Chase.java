package ru.mihver1.mobdev;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.*;

public class Chase extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.main);
    }

    public void onPress(View view) {
        switch (view.getId()) {
            case R.id.add:

                break;
            case R.id.remove:
                break;
            case R.id.reset:
                break;
        }
    }
}
