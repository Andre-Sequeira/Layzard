package com.andresequeira.layzard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.*;

import java.util.*;

import static com.andresequeira.layzard.Layzard.Dispatchers.*;

/**
 * TODO: set result mechanism
 * idea -> receiveResult(Bundle) : Bundle(Int requestData, Bundle result)
 * -> navigator toRequester(Bundle result)
 */
public abstract class Layzard implements LayzardListener<Layzard> {

    private static boolean debugEnabled;
    private boolean logCycle;
    private static final String debugCyclePrefix = "Cycle: ";

    public static void enabledLogs(boolean enable) {
        debugEnabled = enable;
    }

    private static final LinkedHashSet<LayzardListener<Layzard>> commonListeners = new LinkedHashSet<>();

    @SuppressWarnings({"ManualArrayToCollectionCopy", "UseBulkOperation"})
    @SafeVarargs
    public static void addCommonListeners(LayzardListener<Layzard>... listeners) {
        for (LayzardListener<Layzard> listener : listeners) {
            commonListeners.add(listener);
        }
    }

    @NonNull
    public static <LAYOUT extends Layzard> Initializer<LAYOUT> newInitializer(
            Class<LAYOUT> layoutClass) {
        return new Initializer<>(layoutClass);
    }

    protected final void logCycle(String cycle) {
        if (isDebugCycleEnabled()) {
            Log.d(getClass().getSimpleName(), debugCyclePrefix + " " + cycle);
        }
    }

    public final void setLifecycleLogsEnabled(boolean enable) {
        logCycle = enable;
    }

    public final boolean isDebugCycleEnabled() {
        return debugEnabled && logCycle;
    }

    private static final String KEY_BUNDLE = "Layzard.args";
    private static final String KEY_ID = "Layzard.id";
    private static final String KEY_LAYOUT_ARGS = "Layzard.args";
    private static final String KEY_SAVED_STATE = "Layzard.savedState";
    private static final String KEY_FIRST_BIND = "Layzard.firstBind";

    private LinkedList<String> requiredArgKeys = new LinkedList<>();

    private Object host;

    private Context context;
    private String id;

    private Bundle instanceState;

    private View view;

    private boolean isTop;
    private boolean isRoot;

    private boolean created;
    private boolean restore;

    private boolean bound;
    private boolean firstBind = true;

    private boolean argsRequired;

    private boolean recreating;
    private boolean creating;
    private boolean binding;

    private Bundle args;

    private LinkedHashSet<LayzardListener<Layzard>> pendingPreLifecycleListeners =
            new LinkedHashSet<>();
    private LinkedHashSet<LayzardListener<Layzard>> pendingLifecycleListeners =
            new LinkedHashSet<>();

    private ListenerDispatcher<Layzard> dispatcher = new ListenerDispatcher<>();
    private ChildrenHandler childrenHandler = new ChildrenHandler();

    public Layzard() {
        this(null);
    }

    public Layzard(@Nullable Bundle args) {
        if (args != null) {
            newArgs(args);
        }
    }

    public Context getContext() {
        return getView() != null ? getView().getContext() : context;
    }

    public Activity getActivity() {
        return (Activity) context;
    }

    public String getInstanceId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    public final <L extends Layzard> L addLifecycleListener(
            LayzardListener<L> listener) {
        if (listener == null || listener == this) {
            return (L) this;
        }
        if (dispatcher.isEmpty()) {
            pendingLifecycleListeners.add((LayzardListener<Layzard>) listener);
            return (L) this;
        }
        this.dispatcher.add((LayzardListener<Layzard>) listener);
        return (L) this;
    }

    protected final void addPreLifecycleListener(LayzardListener listener) {
        if (isCreated()) {
            return;
        }
        pendingPreLifecycleListeners.add(listener);
    }

    public final void removeLifecycleListener(LayzardListener listener) {
        if (listener == this) {
            return;
        }
        this.dispatcher.remove(listener);
    }

    public boolean isTop() {
        return isTop;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public Layzard setIsTop(boolean top) {
        if (created) {
            return this;
        }
        isTop = top;
        return this;
    }

    public Layzard setIsRoot(boolean root) {
        if (created) {
            return this;
        }
        isRoot = root;
        return this;
    }

    void setHost(Object host) {
        if (isCreated() || isCreating()) {
            throw new RuntimeException("This should just be a test but it's here just in case");
        }
        if (host == null) {
            throw new RuntimeException("No host provided");
        }
        this.host = host;
    }

    public <H> H getHost() {
        //noinspection unchecked
        return (H) host;
    }

    public <H> H getTopHost() {
        if (host instanceof Layzard) {
            //noinspection unchecked
            return (H) ((Layzard) host).getTopHost();
        }
        return getHost();
    }

    @SuppressWarnings({"UseBulkOperation", "ManualArrayToCollectionCopy"})
    protected final void requireArgs(String... keys) {
        for (String key : keys) {
            requiredArgKeys.add(key);
        }
        argsRequired = true;
    }

    public boolean isArgsRequired() {
        return argsRequired;
    }

    public boolean isCreated() {
        return created;
    }

    public boolean isRestore() {
        return restore;
    }

    public boolean isFirstBind() {
        return firstBind;
    }

    public boolean isBound() {
        return bound;
    }

    public boolean isCreating() {
        return creating;
    }

    public boolean isBinding() {
        return binding;
    }

    public View getView() {
        return view;
    }

    //region Args
    public final <L extends Layzard> L newArgs(@NonNull Bundle args) {
        if (!created && !creating) {
            this.args = args;
            //noinspection unchecked
            return (L) this;
        }

        for (String key : requiredArgKeys) {
            if (!args.containsKey(key)) {
                throw new IllegalArgumentException("Invalid args for layout: "
                        + getClass().getName() + ", missing key: " + key);
            }

            final Object arg = args.get(key);
            if (key.endsWith("ID") && arg instanceof Long) {
                long id = (long) arg;
                if (id <= 0) {
                    throw new IllegalArgumentException("Invalid args for layout: "
                            + getClass().getName() + ", invalid id: " + id + " ,for key: " + key);
                }
            }
        }

        final boolean b = onNewArgs(args);
        if (!b) {
            throw new IllegalArgumentException("Invalid args for layout: " + getClass().getName());
        }
        this.args = args;

        reCreate(args);

        //noinspection unchecked
        return (L) this;
    }

    public final Bundle getArgs() {
        return (Bundle) args.clone();
    }

    @SuppressWarnings("unchecked")
    public final <T> T getArg(String key) {
        return (T) args.get(key);
    }

    private void processArgs(@Nullable Bundle args) {
        if (args != null) {
            newArgs(args);
        } else if (this.args != null) {
            newArgs(this.args);
        }
        if (isArgsRequired() && this.args == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " args required");
        }
    }

    @NonNull
    private Bundle getCheckLayoutArgs(Bundle bundle) {
        final Bundle layoutBundle = getLayoutArgs(bundle);
        if (layoutBundle == null) {
            throw new RuntimeException("No layout args inside this args.");
        }
        return layoutBundle;
    }

    public final Bundle getLayoutArgs(Bundle bundle) {
        return bundle.getBundle(KEY_LAYOUT_ARGS);
    }

    public final boolean checkLayoutArgs(Bundle bundle) {
        return getLayoutArgs(bundle) != null;
    }
    //endregion

    private String getInstanceStateBundleKey(String id) {
        return String.format("%s_%s", KEY_BUNDLE, id);
    }

    //region ReCreate
    private void reCreate(Bundle bundle) {
        dispatch(onPreReInit);

        recreating = true;
        reCreate1(bundle);

        dispatch(onReInit);
    }

    @CallSuper
    protected void reCreate1(Bundle bundle) {
        boolean wasBound = bound;
        unbind();
        View view = getView();
        destroyUi();
        destroy();

        boolean wasInit = created;

        if (wasInit) {
            create(host, context, bundle);

            if (view != null) {
                createView(view);
            }

            if (wasBound) {
                bind();
            }
        }
        recreating = false;
    }
    //endregion

    //region Create
    public final void create(@Nullable Bundle outerBundle, @NonNull Object host, @NonNull Context context) {
        create(host, context, getCheckLayoutArgs(outerBundle));
    }

    public final void create(@NonNull Object host, @NonNull Context context, @Nullable Bundle args) {
        if (created) {
            throw new RuntimeException("Already created");
        }

        setHost(host);

        if (!recreating) {
            dispatcher.addAll(pendingPreLifecycleListeners);
            dispatcher.add(this);
            dispatcher.add(childrenHandler);
            dispatcher.addAll(pendingLifecycleListeners);
            dispatcher.addAll(commonListeners);
        }
        childrenHandler.init();
        dispatcher.init();

        this.context = context.getApplicationContext();

        //new instance, assign a new id
        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        dispatch(onPreInit);
        creating = true;

        logCycle("create");

        processArgs(args);
        create();
        dispatch(onInit);

        restoreInstanceState(instanceState);
        instanceState = null;
    }

    @CallSuper
    protected void create() {
        creating = false;
        created = true;
    }
    //endregion

    //region Save
    public final void save(Bundle outerBundle, String id) {
        outerBundle.putBundle(getInstanceStateBundleKey(id), save());
    }

    public final Bundle save() {
        logCycle("save");

        final Bundle layoutBundle = new Bundle();
        final Bundle instanceState = new Bundle();
        saveState(instanceState);

        dispatch(onSaveState, instanceState);
        if (instanceState.size() > 0) {
            layoutBundle.putBundle(KEY_SAVED_STATE, instanceState);
        }

        layoutBundle.putBoolean(KEY_FIRST_BIND, firstBind);

        layoutBundle.putBundle(KEY_LAYOUT_ARGS, args);
        layoutBundle.putString(KEY_ID, id);

        restore = false;

        return layoutBundle;
    }

    @CallSuper
    protected void saveState(Bundle instanceState) {

    }
    //endregion

    //region Restore
    public final void restore(Bundle outerBundle, Object host, Context context, Bundle outerInstanceState, String id) {
        restore(outerInstanceState, id);
        create(outerBundle, host, context);
    }

    public final void restore(Object host, Context context, Bundle layoutArgs, Bundle outerInstanceState, String id) {
        if (outerInstanceState != null) {
            restore(outerInstanceState, id);
        }
        create(host, context, layoutArgs);
    }

    public final void restore(Bundle outerInstanceState, String id) {
        final Bundle bundle = outerInstanceState.getBundle(getInstanceStateBundleKey(id));
        if (bundle == null) {
            throw new RuntimeException();
        }
        restore(bundle);
    }

    public final void restore(@NonNull Bundle layoutBundle) {
        if (restore) {
            return;
        }
        logCycle("restore");
        id = layoutBundle.getString(KEY_ID);
        firstBind = layoutBundle.getBoolean(KEY_FIRST_BIND);

        instanceState = layoutBundle.getBundle(KEY_SAVED_STATE);
        restore = true;

        if (!isCreated()) {
            return;
        }

        restoreInstanceState(instanceState);
    }

    private void restoreInstanceState(Bundle instanceState) {
        if (instanceState != null) {
            restoreState(instanceState);
            dispatch(onRestoreState, instanceState);
        }
    }

    @CallSuper
    protected void restoreState(Bundle instanceState) {

    }
    //endregion

    //region CreateView
    public final View createView(ViewGroup parent) {
        return createView(parent, true);
    }

    public final View createView(ViewGroup parent, boolean bind) {
//        if (this.context == null) {
//            this.context = parent.getContext();
//        }

        return createView(null, LayoutInflater.from(parent.getContext()), parent, bind);
    }

    public final View createView(LayoutInflater inflater, ViewGroup parent) {
        return createView(null, inflater, parent, true);
    }

    public final View createView(Object host, LayoutInflater inflater, ViewGroup parent, boolean performBind) {
        if (view != null) {
            return view;
        }

        if (!created) {
            create(host, inflater.getContext(), null);
        }

        logCycle("createView");

        dispatch(onPreInitUi);
        createView(inflater.inflate(getLayoutResId(), parent, false));
        dispatch(onInitUi, view);
        if (performBind) {
            bind();
        }
        return view;
    }

    @CallSuper
    protected void createView(View v) {
        this.view = v;
    }
    //endregion

    //region Bind
    public final void bind() {
        logCycle("bind");

        if (bound) {
            return;
        }
        dispatch(onPreBind);

        binding = true;
        bind(firstBind);

        dispatch(onBind);

        bound(firstBind);
        dispatch(onBound);
        firstBind = false;
    }

    @CallSuper
    protected void bind(@SuppressWarnings("unused") boolean firstBind) {
        binding = false;
        bound = true;
    }

    @CallSuper
    protected void bound(boolean firstBind) {

    }
    //endregion

    //region Unbind
    public final void unbind() {
        if (!bound) {
            return;
        }

        logCycle("unbind");

        dispatch(onPreUnbind);
        unbind1();

        dispatch(onUnbind);
    }

    @CallSuper
    protected void unbind1() {
        bound = false;
    }
    //endregion

    //region DestroyUi
    public final void destroyUi() {
        unbind();

        if (view == null) {
            return;
        }

        dispatch(onPreDestroyUi, view);
        View view1 = view;
        destroyUi(view);
        logCycle("destroyUi");
        dispatch(onDestroyUi, view1);
    }

    @CallSuper
    protected void destroyUi(View view) {
        this.view = null;
    }
    //endregion

    //region Destroy
    public final void destroy() {
        destroyUi();

        if (!created) {
            return;
        }

        dispatch(onPreDestroy);
        destroy1();
        logCycle("destroy");
        dispatch(onDestroy);

        if (!recreating) {
            this.dispatcher.clear();
        }
    }

    protected void destroy1() {
        created = false;
        context = null;
        if (!recreating) {
            host = null;
        }
    }
    //endregion

    public abstract int getLayoutResId();

    protected boolean onNewArgs(@NonNull Bundle args) {
        return true;
    }

    public boolean handleBack() {
        if (dispatcher.onHandleBack(this)) {
            return true;
        }
        return false;
    }

    //region RESOURCE HELPER
    public Resources getResources() {
        return getContext().getResources();
    }

    @NonNull
    public String getString(@StringRes int resId) {
        return getContext().getString(resId);
    }

    @NonNull
    public final String getString(@StringRes int resId, Object... formatArgs) {
        return getContext().getString(resId, formatArgs);
    }
    //endregion

    protected void initInside(Initializer<?> initializer, ViewGroup parent) {
        initInside(initializer, parent, null);
    }

    protected <L extends Layzard> void initInside(Initializer<L> initializer, ViewGroup parent, LayzardListener<L> onCreateListener) {
        final ChildHandler childHandler = new ChildHandler<>(initializer, 0, parent, onCreateListener);
        addChildHandler(childHandler);
    }

    protected <L extends Layzard> void initInside(Initializer<L> initializer, @IdRes int parentViewId) {
        initInside(initializer, parentViewId, null);
    }

    protected <L extends Layzard> void initInside(Initializer<L> initializer, @IdRes int parentViewId, LayzardListener<L> onCreateListener) {
        final ChildHandler childHandler = new ChildHandler<>(initializer, parentViewId, null, onCreateListener);
        addChildHandler(childHandler);
    }

    private void addChildHandler(ChildHandler childHandler) {
        //noinspection unchecked
        childrenHandler.add(childHandler);
    }

    public Map<String, Layzard> getChildren() {
        return childrenHandler.getLayoutsMap();
    }

    public Layzard getChild(String tag) {
        return childrenHandler.get(tag).layout;
    }

    public Layzard getChild(int index) {
        return childrenHandler.get(index).layout;
    }

    public <L extends Layzard> L getChild(Class<L> layoutClass) {
        final ChildHandler<L> childHandler = childrenHandler.get(layoutClass);
        if (childHandler == null) {
            return null;
        }
        return childHandler.layout;
    }

    private void dispatch(
            Dispatchers.EventDispatcher<Dispatchers.LayoutDispatcher<Layzard>> dispatcher) {
        this.dispatcher.dispatch(dispatcher, this);
    }

    private void dispatch(
            Dispatchers.EventDispatcher<Dispatchers.BundleDispatcher<Layzard>> dispatcher,
            Bundle bundle) {
        this.dispatcher.dispatch(dispatcher, this, bundle);

    }

    private void dispatch(
            Dispatchers.EventDispatcher<Dispatchers.ViewDispatcher<Layzard>> dispatcher,
            View view) {
        this.dispatcher.dispatch(dispatcher, this, view);
    }

    public interface Dispatchers {

        EventDispatcher<LayoutDispatcher<Layzard>> onPreReInit = new EventDispatcher<>(LayzardEvent.PRE_RE_CREATE, LayzardListener::onPreReInit);
        EventDispatcher<LayoutDispatcher<Layzard>> onReInit = new EventDispatcher<>(LayzardEvent.RE_CREATE, LayzardListener::onReInit);
        EventDispatcher<LayoutDispatcher<Layzard>> onPreInit = new EventDispatcher<>(LayzardEvent.PRE_CREATE, LayzardListener::onPreInit);
        EventDispatcher<LayoutDispatcher<Layzard>> onInit = new EventDispatcher<>(LayzardEvent.CREATE, LayzardListener::onInit);
        EventDispatcher<BundleDispatcher<Layzard>> onSaveState = new EventDispatcher<>(LayzardEvent.SAVE, LayzardListener::onSaveState);
        EventDispatcher<BundleDispatcher<Layzard>> onRestoreState = new EventDispatcher<>(LayzardEvent.RESTORE, LayzardListener::onRestoreState);
        EventDispatcher<LayoutDispatcher<Layzard>> onPreInitUi = new EventDispatcher<>(LayzardEvent.PRE_CREATE_VIEW, LayzardListener::onPreInitUi);
        EventDispatcher<ViewDispatcher<Layzard>> onInitUi = new EventDispatcher<>(LayzardEvent.CREATE_VIEW, LayzardListener::onInitUi);
        EventDispatcher<LayoutDispatcher<Layzard>> onPreBind = new EventDispatcher<>(LayzardEvent.PRE_BIND, LayzardListener::onPreBind);
        EventDispatcher<LayoutDispatcher<Layzard>> onBind = new EventDispatcher<>(LayzardEvent.BIND, LayzardListener::onBind);
        EventDispatcher<LayoutDispatcher<Layzard>> onBound = new EventDispatcher<>(LayzardEvent.BOUND, LayzardListener::onBound);
        EventDispatcher<LayoutDispatcher<Layzard>> onPreUnbind = new EventDispatcher<>(LayzardEvent.PRE_UNBIND, LayzardListener::onPreUnbind);
        EventDispatcher<LayoutDispatcher<Layzard>> onUnbind = new EventDispatcher<>(LayzardEvent.UNBIND, LayzardListener::onUnbind);
        EventDispatcher<LayoutDispatcher<Layzard>> onPreRebind = new EventDispatcher<>(LayzardEvent.PRE_RE_BIND, LayzardListener::onPreRebind);
        EventDispatcher<LayoutDispatcher<Layzard>> onRebind = new EventDispatcher<>(LayzardEvent.RE_BIND, LayzardListener::onRebind);
        EventDispatcher<ViewDispatcher<Layzard>> onPreDestroyUi = new EventDispatcher<>(LayzardEvent.PRE_DESTROY_VIEW, LayzardListener::onPreDestroyUi);
        EventDispatcher<ViewDispatcher<Layzard>> onDestroyUi = new EventDispatcher<>(LayzardEvent.DESTROY_VIEW, LayzardListener::onDestroyUi);
        EventDispatcher<LayoutDispatcher<Layzard>> onPreDestroy = new EventDispatcher<>(LayzardEvent.PRE_DESTROY, LayzardListener::onPreDestroy);
        EventDispatcher<LayoutDispatcher<Layzard>> onDestroy = new EventDispatcher<>(LayzardEvent.DESTROY, LayzardListener::onDestroy);


        class EventDispatcher<D extends Dispatcher> {

            LayzardEvent event;
            D d;

            public EventDispatcher(LayzardEvent event, D d) {
                this.event = event;
                this.d = d;
            }
        }

        interface Dispatcher<L extends Layzard> {
            void call(LayzardListener<L> listener, L layout, View view, Bundle bundle);
        }

        interface LayoutDispatcher<L extends Layzard> extends Dispatcher<L> {

            default void call(LayzardListener<L> listener, L layout, View view, Bundle bundle) {
                call(listener, layout);
            }

            void call(LayzardListener<L> listener, L layout);
        }

        interface ViewDispatcher<L extends Layzard> extends Dispatcher<L> {

            default void call(LayzardListener<L> listener, L layout, View view, Bundle bundle) {
                call(listener, layout, view);
            }

            void call(LayzardListener<L> listener, L layout, View bundle);
        }

        interface BundleDispatcher<L extends Layzard> extends Dispatcher<L> {

            default void call(LayzardListener<L> listener, L layout, View view, Bundle bundle) {
                call(listener, layout, bundle);
            }

            void call(LayzardListener<L> listener, L layout, Bundle bundle);
        }
    }

    public static class ListenerDispatcher<LAYOUT extends Layzard> implements
            LayzardListener<LAYOUT> {

        LinkedHashSet<LayzardListener<LAYOUT>> lifecycleListeners = new LinkedHashSet<>();
        ArrayList<LayzardListener<LAYOUT>> listeners;

        public ListenerDispatcher() {

        }

        public void add(LayzardListener<LAYOUT> listener) {
            lifecycleListeners.add(listener);
            if (listeners != null) {
                listeners.add(listener);
            }
        }

        public void addAll(Collection<LayzardListener<LAYOUT>> items) {
            for (LayzardListener<LAYOUT> item : items) {
                add(item);
            }
        }

        public void remove(LayzardListener<LAYOUT> listener) {
            lifecycleListeners.remove(listener);
            if (listeners != null) {
                listeners.remove(listener);
            }
        }

        public void clear() {
            lifecycleListeners.clear();
            listeners = null;
        }

        public boolean isEmpty() {
            return lifecycleListeners.isEmpty();
        }

        public void init() {
            listeners = new ArrayList<>(lifecycleListeners);
        }

        void dispatch(Dispatchers.EventDispatcher<Dispatchers.Dispatcher<LAYOUT>> dispatcher, LAYOUT layout, @Nullable View view, @Nullable Bundle bundle) {
            final int size = listeners.size();
            for (int i = 0; i < size; i++) {
                LayzardListener<LAYOUT> listener = listeners.get(i);
                if (!listener.onEvent(dispatcher.event, layout, view, bundle)) {
                    dispatcher.d.call(listener, layout, view, bundle);
                }
            }
//            for (LayzardListener<LAYOUT> listener : lifecycleListeners) {
//                try {
//                } catch (ClassCastException e) {
//                    throwLayoutCastException(e, listener);
//                }
//            }
        }

        @SuppressWarnings("unchecked")
        void dispatch(Dispatchers.EventDispatcher<Dispatchers.LayoutDispatcher<Layzard>> dispatcher, LAYOUT layout) {
            dispatch((Dispatchers.EventDispatcher) dispatcher, layout, null, null);
        }

        @SuppressWarnings("unchecked")
        void dispatch(Dispatchers.EventDispatcher<Dispatchers.BundleDispatcher<Layzard>> dispatcher, LAYOUT layout, Bundle bundle) {
            dispatch((Dispatchers.EventDispatcher) dispatcher, layout, null, bundle);
        }

        @SuppressWarnings("unchecked")
        void dispatch(Dispatchers.EventDispatcher<Dispatchers.ViewDispatcher<Layzard>> dispatcher, LAYOUT layout, View view) {
            dispatch((Dispatchers.EventDispatcher) dispatcher, layout, view, null);
        }

        private void throwLayoutCastException(ClassCastException e, LayzardListener l) {
            throw new RuntimeException(
                    "Listener: " + l + "does not match layout",
                    e
            );
        }


        @Override
        public boolean onEvent(LayzardEvent event, LAYOUT layout, @Nullable View view, @Nullable Bundle savedState) {
            return false;
        }

        @Override
        public void onPreReInit(LAYOUT layout) {
            dispatch(onPreReInit, layout);
        }

        @Override
        public void onReInit(LAYOUT layout) {
            dispatch(onReInit, layout);
        }

        @Override
        public void onPreInit(LAYOUT layout) {
            dispatch(onPreInit, layout);
        }

        @Override
        public void onInit(LAYOUT layout) {
            dispatch(onInit, layout);
        }

        @Override
        public void onPreInitUi(LAYOUT layout) {
            dispatch(onPreInitUi, layout);
        }

        @Override
        public void onInitUi(LAYOUT layout, View view) {
            dispatch(onInitUi, layout, view);
        }

        @Override
        public void onPreRebind(LAYOUT layout) {
            dispatch(onPreRebind, layout);
        }

        @Override
        public void onRebind(LAYOUT layout) {
            dispatch(onRebind, layout);
        }

        @Override
        public void onPreBind(LAYOUT layout) {
            dispatch(onPreBind, layout);
        }

        @Override
        public void onBind(LAYOUT layout) {
            dispatch(onBind, layout);
        }

        @Override
        public void onBound(LAYOUT layout) {
            dispatch(onBound, layout);
        }

        @Override
        public void onPreUnbind(LAYOUT layout) {
            dispatch(onPreUnbind, layout);
        }

        @Override
        public void onUnbind(LAYOUT layout) {
            dispatch(onUnbind, layout);
        }

        @Override
        public void onPreDestroyUi(LAYOUT layout, View view) {
            dispatch(onPreDestroyUi, layout, view);
        }

        @Override
        public void onDestroyUi(LAYOUT layout, View view) {
            dispatch(onDestroyUi, layout, view);
        }

        @Override
        public void onPreDestroy(LAYOUT layout) {
            dispatch(onPreDestroy, layout);
        }

        @Override
        public void onDestroy(LAYOUT layout) {
            dispatch(onDestroy, layout);
        }

        @Override
        public void onSaveState(LAYOUT layout, Bundle bundle) {
            dispatch(onSaveState, layout, bundle);
        }

        @Override
        public void onRestoreState(LAYOUT layout, Bundle instanceState) {
            dispatch(onRestoreState, layout, instanceState);
        }

        @Override
        public void onActivityResult(LAYOUT layout, int requestCode, int resultCode, Intent data) {
            //TODO
        }

        @Override
        public boolean onHandleBack(LAYOUT layout) {
            for (LayzardListener<LAYOUT> listener : lifecycleListeners) {
                final boolean b = listener.onEvent(LayzardEvent.BACK, layout, null, null);
                if (!b && listener.onHandleBack(layout)) {
                    return true;
                }
            }
            return false;
        }
    }

    public class ChildrenHandler extends ListenerDispatcher<Layzard> {

        LinkedHashMap<String, ChildHandler<Layzard>> childrenMap = new LinkedHashMap<>();

        @Override
        public void add(LayzardListener<Layzard> listener) {

        }

        @Override
        public void remove(LayzardListener<Layzard> listener) {

        }

        @Override
        public void clear() {
            super.clear();
            childrenMap.clear();
        }

        public void add(ChildHandler<Layzard> listener) {
            final String tag = listener.initializer.tag;
            final ChildHandler<Layzard> childHandler = childrenMap.get(tag);
            if (childHandler != null) {
                childHandler.layout.reCreate(childHandler.initializer.getArgs());
                return;
            }
            super.add(listener);
            listener.initializer.index = childrenMap.size();
            childrenMap.put(tag, listener);
        }

//        public void remove(ChildHandler listener) {
//            super.remove(listener);
//            childrenMap.remove(listener.initializer.tag);
//        }

        public ChildHandler get(String tag) {
            return childrenMap.get(tag);
        }

        public ChildHandler get(int index) {
            return new ArrayList<>(childrenMap.values()).get(index);
        }

        public <L extends Layzard> ChildHandler<L> get(Class<L> aClass) {
            for (ChildHandler childHandler : childrenMap.values()) {
                if (childHandler.initializer.layoutClass == aClass) {
                    //noinspection unchecked
                    return childHandler;
                }
            }
            return null;
        }


        public Map<String, Layzard> getLayoutsMap() {
            Map<String, Layzard> result = new HashMap<>();
            for (Map.Entry<String, ChildHandler<Layzard>> entry : childrenMap.entrySet()) {
                result.put(entry.getKey(), entry.getValue().layout);
            }
            return result;
        }
    }

    public class ChildHandler<L extends Layzard> extends DefaultLayzardListener<L> {

        private final L layout;
        private int parentViewId;
        private Initializer<L> initializer;
        private ViewGroup parentView;

        private ChildHandler(Initializer<L> initializer, LayzardListener<L> listener) {
            this.initializer = initializer;
            layout = initializer.newLayoutInstance(Layzard.this);
            layout.addLifecycleListener(listener);
        }

        public ChildHandler(Initializer<L> initializer, @IdRes int parentViewId, @Nullable ViewGroup parentView, LayzardListener<L> listener) {
            this(initializer, listener);
            this.parentViewId = parentViewId;
            this.parentView = parentView;
            syncState();
        }

        private void syncState() {
            if (Layzard.this.isCreated()) {
                onInit(Layzard.this);
            }
            final View view = Layzard.this.getView();
            if (view != null) {
                onInitUi(Layzard.this, view);
            }
            if (isBound() || isBinding()) {
                onBind(Layzard.this);
            }
        }

        @Override
        public void onInit(Layzard layout) {
            if (this.layout.isCreated()) {
                return;
            }
            this.layout.create(layout, layout.getContext(), initializer.getArgs());
        }

        @Override
        public void onInitUi(Layzard layout, View view) {
            if (parentView == null) {
                parentView = view.findViewById(parentViewId);
            }
            final View childView = this.layout.createView(parentView, false);
            parentView.addView(childView);
        }

        @Override
        public void onBind(Layzard layout) {
            this.layout.bind();
        }

        @Override
        public void onPreUnbind(Layzard layout) {
            this.layout.unbind();
        }

        @Override
        public void onPreDestroyUi(Layzard layout, View view) {
            parentView.removeView(this.layout.getView());
            this.layout.destroyUi();
            parentView = null;
        }

        @Override
        public void onPreDestroy(L layout) {
            this.layout.destroy();
        }

        @Override
        public void onSaveState(Layzard layout, Bundle bundle) {
            this.layout.save(bundle, initializer.getTag());
        }

        @Override
        public void onRestoreState(Layzard layout, Bundle instanceState) {
            this.layout.restore(instanceState, initializer.getTag());
        }

        @Override
        public boolean onHandleBack(Layzard layout) {
            return this.layout.handleBack();
        }
    }

    public static class Initializer<LAYOUT extends Layzard> {

        @SuppressWarnings({"UseBulkOperation", "ManualArrayToCollectionCopy"})
        public static ArrayList<Initializer> newList(Initializer... items) {
            ArrayList<Initializer> list = new ArrayList<>(items.length);
            for (Initializer item : items) {
                list.add(item);
            }
            return newList(list);
        }

        public static ArrayList<Initializer> newList(Collection<Initializer> items) {
            ArrayList<Initializer> list = new ArrayList<>(items);
            for (int i = 0; i < list.size(); i++) {
                list.get(i).index = i;
            }
            return list;
        }

        protected void setIndex(Integer index) {
            this.index = index;
        }

        @Nullable
        private Initializer<?> stackWIP;
        //remove previous stack
        private boolean isRoot;
        //drop this stack and merge with previous one if it exists
        private boolean merge;

        private final Class<LAYOUT> layoutClass;
        private String title;
        @Nullable
        private Bundle args;
        private String tag;
        private Integer index;

        private LAYOUT cache;

        protected Initializer(Class<LAYOUT> layoutClass) {
            this.layoutClass = layoutClass;
        }

        public final void newLayoutInstanceCache(Object host) {
            cache = newLayoutInstance(host);
        }

        public final LAYOUT newLayoutInstance(Object host) {
            final LAYOUT layout = newLayout(host);
            if (layout == null) {
                throw new RuntimeException();
            }
            layout.setHost(host);
            return layout;//wth
        }

        protected LAYOUT newLayout(Object host) {
            try {
                return layoutClass.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        //region Parcelable

        public Initializer(Parcel in, ClassLoader loader) {
            final Parcelable parcelable = in.readParcelable(loader);
            if (parcelable != null) {
                stackWIP = unwrap(parcelable);
            }
            isRoot = in.readByte() != 0;
            merge = in.readByte() != 0;

            title = in.readString();
            args = in.readBundle(loader);
            tag = in.readString();
            if (in.readByte() == 0) {
                index = null;
            } else {
                index = in.readInt();
            }
            String className = in.readString();
//            if (loader == null) {
//                return;
//            }
            try {
                //noinspection unchecked
                layoutClass = (Class<LAYOUT>)
                        getClass().getClassLoader()
                                .loadClass(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(stackWIP != null ? stackWIP.parcelable() : null, flags);
            dest.writeByte((byte) (isRoot ? 1 : 0));
            dest.writeByte((byte) (merge ? 1 : 0));

            dest.writeString(title);
            dest.writeBundle(args);
            dest.writeString(tag);
            if (index == null) {
                dest.writeByte((byte) 0);
            } else {
                dest.writeByte((byte) 1);
                dest.writeInt(index);
            }
            dest.writeString(layoutClass.getName());
        }

        public LAYOUT getCachedLayout() {
            return cache;
        }

        public LAYOUT popCache() {
            LAYOUT tmp = cache;
            cache = null;
            return tmp;
        }

        private static class P implements Parcelable {

            Initializer<?> initializer;

            public P(Initializer<?> initializer) {
                this.initializer = initializer;
            }

            public P(Parcel in, ClassLoader loader) {
                initializer = new Initializer<>(in, loader);
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                initializer.writeToParcel(dest, flags);
            }

            public static final Creator<P> CREATOR = new ClassLoaderCreator<P>() {
                @Override
                public P createFromParcel(Parcel source, ClassLoader loader) {
                    return new P(source, loader);
                }

                @Override
                public P createFromParcel(Parcel in) {
                    return new P(in, null);
                }

                @Override
                public P[] newArray(int size) {
                    return new P[size];
                }
            };
        }
        //endregion

        public Initializer<LAYOUT> title(String title) {
            this.title = title;
            return this;
        }

        public Initializer<LAYOUT> tag(String tag) {
            this.tag = tag;
            return this;
        }

        public Integer getIndex() {
            return index;
        }

        public Initializer<LAYOUT> args(Bundle args) {
            this.args = args;
            return this;
        }

        public Initializer<LAYOUT> stack(Initializer<?> initializer) {
            this.stackWIP = initializer;
            return this;
        }

        public Initializer<LAYOUT> root() {
            this.isRoot = true;
            return this;
        }

        public Initializer<LAYOUT> merge() {
            this.merge = true;
            return this;
        }


        public Class<LAYOUT> getLayoutClass() {
            return layoutClass;
        }

        @Nullable
        public Initializer<?> getStack() {
            return stackWIP;
        }

        public boolean isRoot() {
            return isRoot;
        }

        public boolean isMerge() {
            return merge;
        }

        public LinkedList<Initializer<?>> getStackList() {

            final LinkedList<Initializer<?>> initializers = new LinkedList<>();

            initializers.add(this);

            Initializer stack = this.stackWIP;
            while (stack != null) {
                initializers.addFirst(stack);
                stack = stack.getStack();
            }

            return initializers;
        }

        public String getTitle() {
            return title;
        }

        public String getTag() {
            if (tag != null) {
                return tag;
            }
            StringBuilder builder = new StringBuilder();
            if (index != null) {
                builder.append(index);
                builder.append(":");
            }
            if (title != null) {
                builder.append(title);
                builder.append(":");
            }

            builder.append(layoutClass.getName());
            return builder.toString();
        }

        @Nullable
        public Bundle getArgs() {
            return args;
        }

        public String toString() {
            return "Initializer(layoutClass=" + this.getLayoutClass() + ", title=" + this.getTitle() + ", args=" + this.getArgs() + ")";
        }

        public Parcelable parcelable() {
            return new P(this);
        }

        private static Unwrapper unwrapper = new Unwrapper();

        static {
            unwrapper.add(P.class, p -> ((P) p).initializer);
        }

        public static void addUnwrapper(Class<?> instanceOfClass, UnwrapFunction<?> function) {
            unwrapper.add(instanceOfClass, function);
        }

        public static <LAYOUT extends Layzard> Initializer<LAYOUT> unwrap(Parcelable parcelable) {
            return unwrapper.unwrap(parcelable);
        }

        public static <LAYOUT extends Layzard> ArrayList<Initializer<LAYOUT>> unwrap(
                @Nullable Collection<Parcelable> parcelables) {
            if (parcelables == null) {
                return null;
            }
            ArrayList<Initializer<LAYOUT>> initializers = new ArrayList<>(parcelables.size());
            for (Parcelable parcelable : parcelables) {
                initializers.add(unwrap(parcelable));
            }
            return initializers;
        }

        public static ArrayList<Parcelable> wrap(Collection<Initializer> initializers) {
            ArrayList<Parcelable> parcelables = new ArrayList<>(initializers.size());
            for (Initializer<?> initializer : initializers) {
                parcelables.add(initializer.parcelable());
            }
            return parcelables;
        }

        public interface UnwrapFunction<LAYOUT extends Layzard> {
            Layzard.Initializer<LAYOUT> unwrap(Parcelable parcelable);
        }

        private static class Unwrapper {

            private Map<Class<?>, UnwrapFunction> map = new HashMap<>();

            void add(Class<?> aClass, UnwrapFunction function) {
                map.put(aClass, function);
            }

            @SuppressWarnings("unchecked")
            <LAYOUT extends Layzard> Initializer<LAYOUT> unwrap(Parcelable parcelable) {
                for (Map.Entry<Class<?>, UnwrapFunction> entry : map.entrySet()) {
                    if (entry.getKey().isAssignableFrom(parcelable.getClass())) {
                        return (Initializer<LAYOUT>) entry.getValue().unwrap(parcelable);
                    }
                }
                return (Initializer<LAYOUT>) parcelable;
            }

        }
    }
}
