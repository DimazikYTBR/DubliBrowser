package de.baumann.browser;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BrowserViewModel extends ViewModel {
    private final MutableLiveData<String> currentUrl = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LiveData<String> getCurrentUrl() {
        return currentUrl;
    }

    public void updateUrl(String url) {
        currentUrl.setValue(url);
    }
}