package io.alcatraz.audiohq;

import android.support.annotation.Nullable;

public interface AsyncInterface<T>{
    boolean onAyncDone(@Nullable T val);
    void onFailure(String reason);
}

