package com.andresequeira.layzard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;

public abstract class SimpleLayoutListener<L extends BaseLayout> implements LayoutListener<L> {

    @Override
    public boolean onEvent(Event event, L layout, @Nullable View view, @Nullable Bundle bundle) {
        return false;
    }

    @Override
    public void onPreReInit(L layout) {
    }

    @Override
    public void onReInit(L layout) {
    }

    @Override
    public void onPreInit(L layout) {
    }

    @Override
    public void onInit(L layout) {
    }

    @Override
    public void onPreInitUi(L layout) {
    }

    @Override
    public void onInitUi(L layout, View view) {
    }

    @Override
    public void onPreRebind(L layout) {
    }

    @Override
    public void onRebind(L layout) {
    }

    @Override
    public void onPreBind(L layout) {
    }

    @Override
    public void onBind(L layout) {
    }

    @Override
    public void onBound(L layout) {
    }

    @Override
    public void onPreUnbind(L layout) {
    }

    @Override
    public void onUnbind(L layout) {
    }

    @Override
    public void onPreDestroyUi(L layout, View view) {
    }

    @Override
    public void onDestroyUi(L layout, View view) {
    }

    @Override
    public void onPreDestroy(L layout) {
    }

    @Override
    public void onDestroy(L layout) {
    }

    @Override
    public void onSaveState(L layout, Bundle bundle) {
    }

    @Override
    public void onRestoreState(L layout, Bundle instanceState) {
    }

    @Override
    public void onActivityResult(L layout, int requestCode, int resultCode, Intent data) {
    }

    public boolean onHandleBack(L layout) {
        return false;
    }
}
