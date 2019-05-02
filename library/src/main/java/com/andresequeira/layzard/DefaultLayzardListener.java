package com.andresequeira.layzard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

public abstract class DefaultLayzardListener<L extends Layzard> implements LayzardListener<L> {

    @Override
    public boolean onEvent(@NotNull LayzardEvent event, @NotNull L layout, @Nullable View view, @Nullable Bundle bundle) {
        return LayzardListener.DefaultImpls.onEvent(this, event, layout, view, bundle);
    }

    @Override
    public void onPreReInit(@NotNull L layout) {
        LayzardListener.DefaultImpls.onPreReInit(this, layout);
    }

    @Override
    public void onReInit(@NotNull L layout) {
        LayzardListener.DefaultImpls.onReInit(this, layout);
    }

    @Override
    public void onPreInit(@NotNull L layout) {
        LayzardListener.DefaultImpls.onPreInit(this, layout);
    }

    @Override
    public void onInit(@NotNull L layout) {
        LayzardListener.DefaultImpls.onInit(this, layout);
    }

    @Override
    public void onPreInitUi(@NotNull L layout) {
        LayzardListener.DefaultImpls.onPreInitUi(this, layout);
    }

    @Override
    public void onInitUi(@NotNull L layout, @NotNull View view) {
        LayzardListener.DefaultImpls.onInitUi(this, layout, view);
    }

    @Override
    public void onPreRebind(@NotNull L layout) {
        LayzardListener.DefaultImpls.onPreRebind(this, layout);
    }

    @Override
    public void onRebind(@NotNull L layout) {
        LayzardListener.DefaultImpls.onRebind(this, layout);
    }

    @Override
    public void onPreBind(@NotNull L layout) {
        LayzardListener.DefaultImpls.onPreBind(this, layout);
    }

    @Override
    public void onBind(@NotNull L layout) {
        LayzardListener.DefaultImpls.onBind(this, layout);
    }

    @Override
    public void onBound(@NotNull L layout) {
//        LayzardListener.DefaultImpls.onBound(this, layout);
    }

    @Override
    public void onPreUnbind(@NotNull L layout) {
        LayzardListener.DefaultImpls.onPreUnbind(this, layout);
    }

    @Override
    public void onUnbind(@NotNull L layout) {
        LayzardListener.DefaultImpls.onUnbind(this, layout);
    }

    @Override
    public void onPreDestroyUi(@NotNull L layout, @NotNull View view) {
        LayzardListener.DefaultImpls.onPreDestroyUi(this, layout, view);
    }

    @Override
    public void onDestroyUi(@NotNull L layout, @NotNull View view) {
        LayzardListener.DefaultImpls.onDestroyUi(this, layout, view);
    }

    @Override
    public void onPreDestroy(@NotNull L layout) {
        LayzardListener.DefaultImpls.onPreDestroy(this, layout);
    }

    @Override
    public void onDestroy(@NotNull L layout) {
        LayzardListener.DefaultImpls.onDestroy(this, layout);
    }

    @Override
    public void onSaveState(@NotNull L layout, @NotNull Bundle bundle) {
        LayzardListener.DefaultImpls.onSaveState(this, layout, bundle);
    }

    @Override
    public void onRestoreState(@NotNull L layout, @NotNull Bundle instanceState) {
        LayzardListener.DefaultImpls.onRestoreState(this, layout, instanceState);
    }

    @Override
    public void onActivityResult(@NotNull L layout, int requestCode, int resultCode, @Nullable Intent data) {
        LayzardListener.DefaultImpls.onActivityResult(this, layout, requestCode, resultCode, data);
    }

    public boolean onHandleBack(@NotNull L layout) {
        return LayzardListener.DefaultImpls.onHandleBack(this, layout);
    }
}
