package com.andresequeira.layzard.wrapper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.andresequeira.layzard.Layzard;
import com.andresequeira.layzard.LayzardInitializer;
import com.andresequeira.layzard.LayzardKt;

public class LayoutActivity<L extends Layzard> extends AppCompatActivity {

    private static final String EXTRA_LAYOUT_INITIALIZER = "LayoutActivity.layoutInitializer";

    private L layzard;

    public static void startActivity(Context context, LayzardInitializer<?> initializer) {
        startActivity(LayoutActivity.class, context, initializer);
    }

    protected static void startActivity(Class<? extends LayoutActivity> layoutActivityClass,
                                        Context context,
                                        LayzardInitializer<?> initializer) {

        final Intent intent = new Intent(context, layoutActivityClass)
                .putExtra(EXTRA_LAYOUT_INITIALIZER, initializer.parcelable());

        context.startActivity(intent);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Parcelable parcelable = getIntent().getParcelableExtra(EXTRA_LAYOUT_INITIALIZER);
        final LayzardInitializer<L> initializer;
        if (parcelable == null) {
            initializer = getInitializer();
            if (initializer == null) {
                throw new RuntimeException("This activity must be started with LayoutActivity.startActivity() or override getInitializer()");
            }
        } else {
            initializer = LayzardInitializer.Companion.unwrap(parcelable);
        }

        Object last = getLastNonConfigurationInstance();
        if (last instanceof Layzard) {
            layzard = (L) last;
        } else {
            layzard = initializer.newLayzard(this);
        }

        layzard.setIsRoot(isTaskRoot())
                .setIsTop(true)
                .restoreWith(this, initializer.getArgs(), savedInstanceState, null);

        setContentView(
                layzard.createView(findViewById(android.R.id.content))
        );

        layzard = layzard.newArgs(new Bundle());
        LayzardKt.newArgs()
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        layzard.restoreWith(savedInstanceState, null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        layzard.save(outState, null);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        layzard.onActivityResult(layzard, requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        layzard.destroyView();
        if (!isChangingConfigurations()) {
            layzard.destroy();
        }
        layzard = null;
        super.onDestroy();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return layzard;
    }

    @Override
    public void onBackPressed() {
        if (layzard.handleBack()) {
            return;
        }
        super.onBackPressed();
    }

    protected LayzardInitializer<L> getInitializer() {
        return null;
    }

    @Nullable
    public L getLayzard() {
        return layzard;
    }
}
