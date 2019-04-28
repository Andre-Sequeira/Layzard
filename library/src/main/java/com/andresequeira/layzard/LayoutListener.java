package com.andresequeira.layzard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;

public interface LayoutListener<L extends BaseLayout> {

    class Event {

        public static final Event PRE_RE_INIT = new Event("LayoutListener#onPreReInit");
        public static final Event RE_INIT = new Event("LayoutListener#onReInit");
        public static final Event PRE_INIT = new Event("LayoutListener#onPreInit");
        public static final Event INIT = new Event("LayoutListener#onInit");
        public static final Event RESTORE = new Event("LayoutListener#onRestoreState");
        public static final Event PRE_INIT_UI = new Event("LayoutListener#onPreInitUi");
        public static final Event INIT_UI = new Event("LayoutListener#onInitUi");
        public static final Event PRE_RE_BIND = new Event("LayoutListener#onPreReBind");
        public static final Event RE_BIND = new Event("LayoutListener#onReBind");
        public static final Event PRE_BIND = new Event("LayoutListener#onPreBind");
        public static final Event BIND = new Event("LayoutListener#onBind");
        public static final Event BOUND = new Event("LayoutListener#onBound");
        public static final Event PRE_UNBIND = new Event("LayoutListener#onPreUnbind");
        public static final Event UNBIND = new Event("LayoutListener#onUnbind");
        public static final Event PRE_DESTROY_UI = new Event("LayoutListener#onPreDestroyUi");
        public static final Event DESTROY_UI = new Event("LayoutListener#onDestroyUi");
        public static final Event SAVE = new Event("LayoutListener#onSaveState");
        public static final Event PRE_DESTROY = new Event("LayoutListener#onPreDestroy");
        public static final Event DESTROY = new Event("LayoutListener#onDestroy");
        public static final Event BACK = new Event("LayoutListener#onBack");

        private final String name;

        public Event(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public final boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public final int hashCode() {
            return super.hashCode();
        }
    }



    boolean onEvent(Event event, L layout, @Nullable View view, @Nullable Bundle bundle);

    void onPreReInit(L layout);

    void onReInit(L layout);

    void onPreInit(L layout);

    void onInit(L layout);

    void onRestoreState(L layout, Bundle instanceState);

    void onPreInitUi(L layout);

    void onInitUi(L layout, View view);

    void onPreRebind(L layout);

    void onRebind(L layout);

    void onPreBind(L layout);

    void onBind(L layout);

    void onBound(L layout);

    void onPreUnbind(L layout);

    void onUnbind(L layout);

    void onPreDestroyUi(L layout, View view);

    void onDestroyUi(L layout, View view);

    void onSaveState(L layout, Bundle bundle);

    void onPreDestroy(L layout);

    void onDestroy(L layout);

    void onActivityResult(L layout, int requestCode, int resultCode, Intent data);

    boolean onHandleBack(L layout);
}
