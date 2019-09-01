package com.absinthe.anywhere_.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "anywhere_table")
public class AnywhereEntity {

    @NonNull
    @ColumnInfo(name = "package_name")
    private String mPackageName;

    @NonNull
    @ColumnInfo(name = "class_name")
    private String mClassName;

    @NonNull
    @ColumnInfo(name = "app_name")
    private String mAppName;

    @NonNull
    @ColumnInfo(name = "custom_texture")
    private String mCustomTexture;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "hash_code")
    private String mHashCode;

    public AnywhereEntity(@NonNull String packageName, @NonNull String className, @NonNull String appName, @NonNull String customTexture, @NonNull String hashCode) {
        mPackageName = packageName;
        mClassName = className;
        mAppName = appName;
        mCustomTexture = customTexture;
        mHashCode = hashCode;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String getClassName() {
        return this.mClassName;
    }

    public String getAppName() {
        return this.mAppName;
    }

    public String getCustomTexture() {
        return this.mCustomTexture;
    }

    public String getHashCode() {
        return this.mHashCode;
    }
}
