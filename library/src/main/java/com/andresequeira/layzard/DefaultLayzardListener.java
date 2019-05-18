package com.andresequeira.layzard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import org.jetbrains.annotations.NotNull;

public abstract class DefaultLayzardListener<L extends Layzard> implements LayzardListener<L> {

    @Override
    public boolean onEvent(@NotNull LayzardEvent<?> event, @NotNull L layzard, @NotNull Object[] params) {
        return LayzardListener.DefaultImpls.onEvent(this, event, layzard, params);
    }

    @Override
    public void onPreReCreate(@NonNull L layzard) {
        LayzardListener.DefaultImpls.onPreReCreate(this, layzard);
    }

    @Override
    public void onReCreate(@NonNull L layzard) {
        LayzardListener.DefaultImpls.onReCreate(this, layzard);
    }

    @Override
    public void onPreCreate(@NonNull L layzard) {
        LayzardListener.DefaultImpls.onPreCreate(this, layzard);
    }

    @Override
    public void onCreate(@NonNull L layzard) {
        LayzardListener.DefaultImpls.onCreate(this, layzard);
    }

    @Override
    public void onPreCreateView(@NonNull L layzard) {
        LayzardListener.DefaultImpls.onPreCreateView(this, layzard);
    }

    @Override
    public void onCreateView(@NonNull L layzard, @NonNull View view) {
        LayzardListener.DefaultImpls.onCreateView(this, layzard, view);
    }

    @Override
    public void onPreRebind(@NonNull L layzard) {
        LayzardListener.DefaultImpls.onPreRebind(this, layzard);
    }

    @Override
    public void onRebind(@NonNull L layzard) {
        LayzardListener.DefaultImpls.onRebind(this, layzard);
    }

    @Override
    public void onPreBind(@NonNull L layzard) {
        LayzardListener.DefaultImpls.onPreBind(this, layzard);
    }

    @Override
    public void onBind(@NonNull L layzard) {
        LayzardListener.DefaultImpls.onBind(this, layzard);
    }

    @Override
    public void onBound(@NonNull L layzard) {
        LayzardListener.DefaultImpls.onBound(this, layzard);
    }

    @Override
    public void onPreUnbind(@NonNull L layzard) {
        LayzardListener.DefaultImpls.onPreUnbind(this, layzard);
    }

    @Override
    public void onUnbind(@NonNull L layzard) {
        LayzardListener.DefaultImpls.onUnbind(this, layzard);
    }

    @Override
    public void onPreDestroyView(@NonNull L layzard, @NonNull View view) {
        LayzardListener.DefaultImpls.onPreDestroyView(this, layzard, view);
    }

    @Override
    public void onDestroyView(@NonNull L layzard, @NonNull View view) {
        LayzardListener.DefaultImpls.onDestroyView(this, layzard, view);
    }

    @Override
    public void onPreDestroy(@NonNull L layzard) {
        LayzardListener.DefaultImpls.onPreDestroy(this, layzard);
    }

    @Override
    public void onDestroy(@NonNull L layzard) {
        LayzardListener.DefaultImpls.onDestroy(this, layzard);
    }

    @Override
    public void onSaveState(@NonNull L layzard, @NonNull Bundle bundle) {
        LayzardListener.DefaultImpls.onSaveState(this, layzard, bundle);
    }

    @Override
    public void onRestoreState(@NonNull L layzard, @NonNull Bundle instanceState) {
        LayzardListener.DefaultImpls.onRestoreState(this, layzard, instanceState);
    }

    @Override
    public void onActivityResult(@NonNull L layzard, int requestCode, int resultCode, @Nullable Intent data) {
        LayzardListener.DefaultImpls.onActivityResult(this, layzard, requestCode, resultCode, data);
    }

    public boolean onHandleBack(@NonNull L layzard) {
        return LayzardListener.DefaultImpls.onHandleBack(this, layzard);
    }
}
