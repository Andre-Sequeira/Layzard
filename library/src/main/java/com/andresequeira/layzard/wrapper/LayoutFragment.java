package com.andresequeira.layzard.wrapper;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.andresequeira.layzard.Layzard;

import java.util.List;

public class LayoutFragment<LAYOUT extends Layzard> extends AppCompatDialogFragment {

    private static final String KEY_LAYOUT_INITIALIZER = "LayoutFragment.layoutInitializer";
//    private static final String KEY_AS_DIALOG = "LayoutFragment.showAsDialog";

    private LAYOUT layout;

    public static <L extends Layzard> LayoutFragment<L> newFragment(Layzard.Initializer<L> initializer) {
        Bundle args = new Bundle();
        if (initializer != null) {
            args.putParcelable(KEY_LAYOUT_INITIALIZER, initializer.parcelable());
        }
        final LayoutFragment<L> fragment = new LayoutFragment<>();
        fragment.setArguments(args);
        return fragment;
    }

    private static <LAYOUT extends Layzard> Layzard.Initializer<LAYOUT> getLayoutInitializer(
            LayoutFragment<LAYOUT> instance) {
        final Bundle arguments = instance.getArguments();
        if (arguments == null) {
            throw new RuntimeException();
        }
        return getLayoutInitializer(arguments);
    }

    private static <LAYOUT extends Layzard> Layzard.Initializer<LAYOUT> getLayoutInitializer(
            Bundle bundle) {
        return Layzard.Initializer.unwrap(
                (Parcelable) bundle.getParcelable(KEY_LAYOUT_INITIALIZER)
        );
    }


    private static <LAYOUT extends Layzard> Class<LAYOUT> getLayoutClass(
            LayoutFragment<LAYOUT> instance) {
        return getLayoutInitializer(instance).getLayoutClass();
    }

    private static <LAYOUT extends Layzard> Bundle getLayoutArgs(
            LayoutFragment<LAYOUT> instance) {
        return getLayoutInitializer(instance).getArgs();
    }

    private static Bundle checkArgs(Bundle args) {
        if (getLayoutInitializer(args) == null) {
            throw new RuntimeException("constructor(Bundle) should only be called on restore " +
                    "instances. The first constructor to be called should always be one one with " +
                    "Initializer<LAYOUT>");
        }
        return args;
    }

//    @SuppressWarnings("ConstantConditions")
//    public LayoutFragment asDialog() {
//        getArguments().putBoolean(KEY_AS_DIALOG, true);
//        return this;
//    }
//
//    @SuppressWarnings("ConstantConditions")
//    public LayoutFragment asBottomSheet() {
//        getArguments().putBoolean(KEY_AS_DIALOG, false);
//        return this;
//    }

//    @SuppressWarnings("ConstantConditions")
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        if (getArguments().getBoolean(KEY_AS_DIALOG)) {
//            return new AppCompatDialog(getContext(), getTheme());
//        }
//        return super.onCreateDialog(savedInstanceState);
//    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isLayoutCreated()) {
            final LAYOUT layout = getLayout();
            setLayoutConfigs(layout);
            layout.restore(this, getContext(), getLayoutArgs(), savedInstanceState, null);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getLayout().createView(inflater, container);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        final LAYOUT layout = getLayout();
        if (layout != null) {
            layout.save(outState, null);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        getLayout().onActivityResult(getLayout(), requestCode, resultCode, data);
    }

    @Override
    public void onDestroyView() {
        getLayout().destroyUi();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        final LAYOUT layout = getLayout();
        if (layout != null) {
            layout.destroy();
        }
        super.onDestroy();
    }


    public boolean isLayoutCreated() {
        LAYOUT layout = getLayout();
        if (layout == null) {
            return false;
        }
        return layout.isCreated();
    }

    public LAYOUT getLayout() {
        if (layout == null) {
            layout = getLayoutInitializer().newLayoutInstance(this);
        }
        return layout;
    }


    private void setLayoutConfigs(LAYOUT layout) {
        layout.setIsTop(getParentFragment() == null);
        final FragmentManager fragmentManager = getFragmentManager();

        if (fragmentManager == null) {
            return;
        }
        boolean isRoot = false;
        final List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments.size() > 0) {
            isRoot = fragments.get(0) == this;
        } else {
            if (fragmentManager.getBackStackEntryCount() > 0) {
                final FragmentManager.BackStackEntry rootEntry = fragmentManager.getBackStackEntryAt(0);
                final Fragment fragmentById = fragmentManager.findFragmentById(rootEntry.getId());
                isRoot = fragmentById == this;
            }
        }
        layout.setIsRoot(isRoot);
    }

    private Layzard.Initializer<LAYOUT> getLayoutInitializer() {
        return getLayoutInitializer(this);
    }

    private Bundle getLayoutArgs() {
        return getLayoutArgs(this);
    }

}
