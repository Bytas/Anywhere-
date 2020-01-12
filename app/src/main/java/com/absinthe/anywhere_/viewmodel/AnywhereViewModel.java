package com.absinthe.anywhere_.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.absinthe.anywhere_.AnywhereApplication;
import com.absinthe.anywhere_.R;
import com.absinthe.anywhere_.adapter.page.PageNode;
import com.absinthe.anywhere_.adapter.page.PageTitleNode;
import com.absinthe.anywhere_.model.AnywhereEntity;
import com.absinthe.anywhere_.database.AnywhereRepository;
import com.absinthe.anywhere_.model.AnywhereType;
import com.absinthe.anywhere_.model.Const;
import com.absinthe.anywhere_.model.GlobalValues;
import com.absinthe.anywhere_.services.CollectorService;
import com.absinthe.anywhere_.ui.main.MainActivity;
import com.absinthe.anywhere_.utils.manager.Logger;
import com.absinthe.anywhere_.utils.PermissionUtils;
import com.absinthe.anywhere_.utils.ToastUtil;
import com.absinthe.anywhere_.view.Editor;
import com.absinthe.anywhere_.view.SchemeEditor;
import com.chad.library.adapter.base.entity.node.BaseNode;

import java.util.ArrayList;
import java.util.List;

public class AnywhereViewModel extends AndroidViewModel {

    private AnywhereRepository mRepository;
    private LiveData<List<AnywhereEntity>> mAllAnywhereEntities;

    private MutableLiveData<String> mCommand = null;
    private MutableLiveData<String> mWorkingMode = null;
    private MutableLiveData<String> mBackground = null;
    private MutableLiveData<String> mCardMode = null;

    public boolean refreshLock = false;

    public AnywhereViewModel(Application application) {
        super(application);
        mRepository = AnywhereApplication.sRepository;
        mAllAnywhereEntities = mRepository.getAllAnywhereEntities();
    }

    public LiveData<List<AnywhereEntity>> getAllAnywhereEntities() {
        return mAllAnywhereEntities;
    }

    public void insert(AnywhereEntity ae) {
        mRepository.insert(ae);
    }

    public void update(AnywhereEntity ae) {
        mRepository.update(ae);
    }

    public void delete(AnywhereEntity ae) {
        mRepository.delete(ae);
    }

    public MutableLiveData<String> getCommand() {
        if (mCommand == null) {
            mCommand = new MutableLiveData<>();
        }
        return mCommand;
    }

    public MutableLiveData<String> getWorkingMode() {
        if (mWorkingMode == null) {
            mWorkingMode = new MutableLiveData<>();
        }
        return mWorkingMode;
    }

    public MutableLiveData<String> getBackground() {
        if (mBackground == null) {
            mBackground = new MutableLiveData<>();
        }
        return mBackground;
    }

    public MutableLiveData<String> getCardMode() {
        if (mCardMode == null) {
            mCardMode = new MutableLiveData<>();
        }
        return mCardMode;
    }

    public void refreshDB() {
        switch (GlobalValues.sSortMode) {
            case Const.SORT_MODE_TIME_DESC:
            default:
                mAllAnywhereEntities = mRepository.getSortedByTimeDesc();
                break;
            case Const.SORT_MODE_TIME_ASC:
                mAllAnywhereEntities = mRepository.getSortedByTimeAsc();
                break;
            case Const.SORT_MODE_NAME_DESC:
                mAllAnywhereEntities = mRepository.getSortedByNameDesc();
                break;
            case Const.SORT_MODE_NAME_ASC:
                mAllAnywhereEntities = mRepository.getSortedByNameAsc();
                break;
        }
    }

    public PageTitleNode getEntity(String title) {
        List<BaseNode> pageNodeList = new ArrayList<>();
        PageNode pageNode = new PageNode();
        pageNode.setTitle(title);
        pageNodeList.add(pageNode);
        PageTitleNode pageTitle = new PageTitleNode(pageNodeList, title);
        pageTitle.setExpanded(title.equals(GlobalValues.sCategory));
        return pageTitle;
    }

    public void setUpUrlScheme(String url) {
        AnywhereEntity ae = AnywhereEntity.Builder();
        ae.setAppName(MainActivity.getInstance().getString(R.string.bsd_new_url_scheme_name));
        ae.setParam1(url);
        ae.setType(AnywhereType.URL_SCHEME);

        Editor editor = new SchemeEditor(MainActivity.getInstance())
                .item(ae)
                .isEditorMode(false)
                .isShortcut(false)
                .build();
        editor.show();
    }

    public void startCollector(Activity activity) {
        Intent intent = new Intent(activity, CollectorService.class);
        intent.putExtra(CollectorService.COMMAND, CollectorService.COMMAND_OPEN);
        activity.startService(intent);
        ToastUtil.makeText(R.string.toast_collector_opened);

        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        activity.startActivity(homeIntent);
    }

    public void checkWorkingPermission(Activity activity) {
        Logger.d("workingMode =", GlobalValues.sWorkingMode);
        if (GlobalValues.sWorkingMode != null) {
            switch (GlobalValues.sWorkingMode) {
                case Const.WORKING_MODE_URL_SCHEME:
                    setUpUrlScheme("");
                    break;
                case Const.WORKING_MODE_SHIZUKU:
                    if (!PermissionUtils.checkOverlayPermission(MainActivity.getInstance(), Const.REQUEST_CODE_ACTION_MANAGE_OVERLAY_PERMISSION)) {
                        return;
                    }
                    if (PermissionUtils.checkShizukuOnWorking(activity) && PermissionUtils.shizukuPermissionCheck(activity)) {
                        startCollector(activity);
                    }
                    break;
                case Const.WORKING_MODE_ROOT:
                    if (!PermissionUtils.checkOverlayPermission(MainActivity.getInstance(), Const.REQUEST_CODE_ACTION_MANAGE_OVERLAY_PERMISSION)) {
                        return;
                    }
                    if (PermissionUtils.upgradeRootPermission(activity.getPackageCodePath())) {
                        startCollector(activity);
                    } else {
                        Logger.d("ROOT permission denied.");
                        ToastUtil.makeText(R.string.toast_root_permission_denied);
                    }
                    break;
            }
        }

    }
}
