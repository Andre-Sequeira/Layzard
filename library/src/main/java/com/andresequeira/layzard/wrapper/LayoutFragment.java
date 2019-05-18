package com.andresequeira.layzard.wrapper;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.andresequeira.layzard.Layzard;
import com.andresequeira.layzard.LayzardInitializer;

import java.util.List;
import java.util.Objects;

public class LayoutFragment<L extends Layzard> extends AppCompatDialogFragment {

    private static final String KEY_LAYOUT_INITIALIZER = "LayoutFragment.layoutInitializer";
//    private static final String KEY_AS_DIALOG = "LayoutFragment.showAsDialog";

    private L layzard;

    public static <L extends Layzard> LayoutFragment<L> newFragment(LayzardInitializer<L> initializer) {
        Bundle args = new Bundle();
        if (initializer != null) {
            args.putParcelable(KEY_LAYOUT_INITIALIZER, initializer.parcelable());
        }
        final LayoutFragment<L> fragment = new LayoutFragment<>();
        fragment.setArguments(args);
        return fragment;
    }

    private static <L extends Layzard> LayzardInitializer<L> getLayoutInitializer(
            LayoutFragment<L> instance) {
        final Bundle arguments = instance.getArguments();
        if (arguments == null) {
            throw new RuntimeException();
        }
        return getLayoutInitializer(arguments);
    }

    private static <L extends Layzard> LayzardInitializer<L> getLayoutInitializer(Bundle bundle) {
        return LayzardInitializer.unwrap(
                Objects.requireNonNull(bundle.getParcelable(KEY_LAYOUT_INITIALIZER))
        );
    }


    private static <L extends Layzard> Class<L> getLayoutClass(
            LayoutFragment<L> instance) {
        return getLayoutInitializer(instance).getLayzardClass();
    }

    private static <L extends Layzard> Bundle getLayoutArgs(
            LayoutFragment<L> instance) {
        return getLayoutInitializer(instance).getArgs();
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

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isLayoutCreated()) {
            final L layout = getLayzard();
            setLayoutConfigs(layout);
            layout.restoreWith(getContext(), getLayoutArgs(), savedInstanceState, null);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getLayzard().createView(inflater, container);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        final L layout = getLayzard();
        if (layout != null) {
            layout.save(outState, null);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        getLayzard().onActivityResult(getLayzard(), requestCode, resultCode, data);
    }

    @Override
    public void onDestroyView() {
        getLayzard().destroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        final L layout = getLayzard();
        if (layout != null) {
            layout.destroy();
        }
        super.onDestroy();
    }


    public boolean isLayoutCreated() {
        L layout = getLayzard();
        if (layout == null) {
            return false;
        }
        return layout.isCreated();
    }

    public L getLayzard() {
        if (layzard == null) {
            layzard = getLayoutInitializer().newLayzard(this);
        }
        return layzard;
    }


    private void setLayoutConfigs(L layout) {
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

    private LayzardInitializer<L> getLayoutInitializer() {
        return getLayoutInitializer(this);
    }

    private Bundle getLayoutArgs() {
        return getLayoutArgs(this);
    }

}
