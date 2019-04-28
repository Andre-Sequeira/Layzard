package com.andresequeira.layzard.wrapper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.andresequeira.layzard.BaseLayout;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LayoutActivity<L extends BaseLayout> extends AppCompatActivity {

    private static final String EXTRA_LAYOUT_INITIALIZER = "LayoutActivity.layoutInitializer";

    private L layout;

    public static void startActivity(Context context, BaseLayout.Initializer<?> initializer) {
        startActivity(LayoutActivity.class, context, initializer);
    }

    protected static void startActivity(Class<? extends LayoutActivity> layoutActivityClass,
                                        Context context,
                                        BaseLayout.Initializer<?> initializer) {

        final Intent intent = new Intent(context, layoutActivityClass)
                .putExtra(EXTRA_LAYOUT_INITIALIZER, initializer.parcelable());

        context.startActivity(intent);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Parcelable parcelable = getIntent().getParcelableExtra(EXTRA_LAYOUT_INITIALIZER);
        if (parcelable == null) {
            throw new RuntimeException("This activity must be started with LayoutActivity.startActivity()");
        }
        BaseLayout.Initializer<L> initializer = BaseLayout.Initializer.unwrap(parcelable);

        Object last = getLastNonConfigurationInstance();
        if (last instanceof BaseLayout) {
            layout = (L) last;
        } else {
            layout = initializer.newLayoutInstance(this);
        }

        layout.setIsRoot(isTaskRoot())
                .setIsTop(true)
                .restore(this, this, initializer.getArgs(), savedInstanceState, null);

        setContentView(getView());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        layout.restore(savedInstanceState, null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        layout.save(outState, null);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        layout.onActivityResult(layout, requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        layout.destroyUi();
        if (!isChangingConfigurations()) {
            layout.destroy();
        }
        layout = null;
        super.onDestroy();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return layout;
    }

    @Override
    public void onBackPressed() {
        if (layout.handleBack()) {
            return;
        }
        super.onBackPressed();
    }

    protected View getView() {
        return layout.initUi(findViewById(android.R.id.content));
    }
}