package com.absinthe.anywhere_.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.absinthe.anywhere_.model.AnywhereEntity;
import com.absinthe.anywhere_.model.AnywhereRepository;

import java.util.List;

public class AnywhereViewModel extends AndroidViewModel {

    private AnywhereRepository mRepository;
    private LiveData<List<AnywhereEntity>> mAllAnywhereEntities;

    private MutableLiveData<String> mPackageName = null;
    private MutableLiveData<String> mClassName = null;
    private MutableLiveData<String> mClassNameType = null;

    public AnywhereViewModel(Application application) {
        super(application);
        mRepository = new AnywhereRepository(application);
        mAllAnywhereEntities = mRepository.getAllAnywhereEntities();
    }

    public LiveData<List<AnywhereEntity>> getAllAnywhereEntities() {
        return mAllAnywhereEntities;
    }

    public void insert(AnywhereEntity ae) {
        mRepository.insert(ae);
    }

    public MutableLiveData<String> getPackageName() {
        if (mPackageName == null) {
            mPackageName = new MutableLiveData<>();
        }
        return mPackageName;
    }

    public MutableLiveData<String> getClassName() {
        if (mClassName == null) {
            mClassName = new MutableLiveData<>();
        }
        return mClassName;
    }

    public MutableLiveData<String> getClassNameType() {
        if (mClassNameType == null) {
            mClassNameType = new MutableLiveData<>();
        }
        return mClassNameType;
    }
}
