package ru.wert.bazapik_mobile.viewer;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import ru.wert.bazapik_mobile.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ProgressIndicatorFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_progress_indicator, container, false);

//        ((Activity) inflater.getContext()).getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark, null));

        return v;
    }
}