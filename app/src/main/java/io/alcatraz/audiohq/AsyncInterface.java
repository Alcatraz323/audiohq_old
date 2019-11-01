package io.alcatraz.audiohq;

import android.support.annotation.Nullable;

public interface AsyncInterface<T>{
    void onAyncDone(@Nullable T val);
    void onFailure(String reason);
}

