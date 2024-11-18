package com.connectsdk.sampler.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.connectsdk.sampler.R;
import com.connectsdk.service.capability.Launcher;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.sessions.LaunchSession;
import com.connectsdk.service.capability.listeners.ResponseListener;

public class SearchFragment extends BaseFragment {

    private static final String TAG = "SearchFragment";

    private RadioButton selectGoogleButton;
    private RadioButton selectNaverButton;
    private EditText inputText;
    private Button searchButton;
    private TextView responseMessageTextView;

    private LaunchSession browserSession;

    public SearchFragment() {
    }

    public SearchFragment(Context context) {
        super(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        // UI 요소 초기화
        selectGoogleButton = rootView.findViewById(R.id.radioGoogle);
        selectNaverButton = rootView.findViewById(R.id.radioNaver);
        inputText = rootView.findViewById(R.id.inputText);
        searchButton = rootView.findViewById(R.id.goButton);
        responseMessageTextView = rootView.findViewById(R.id.responseMessageTextView);

        // 검색 버튼 클릭 리스너 설정
        searchButton.setOnClickListener(launchBrowser);

        return rootView;
    }

    private final View.OnClickListener launchBrowser = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (browserSession != null) {
                closeBrowserSession();
            } else {
                executeSearch();
            }
        }
    };

    private void closeBrowserSession() {
        browserSession.close(new ResponseListener<Object>() {
            @Override
            public void onError(ServiceCommandError error) {
                Log.d(TAG, "브라우저 세션 종료 오류: " + error.getMessage());
            }

            @Override
            public void onSuccess(Object object) {
                Log.d(TAG, "브라우저 세션 종료 성공");
            }
        });

        browserSession = null;
        searchButton.setSelected(false);
    }

    private void executeSearch() {
        String query = inputText.getText().toString().trim();
        if (query.isEmpty()) {
            Log.e(TAG, "검색어가 입력되지 않았습니다.");
            responseMessageTextView.setText("검색어를 입력하세요.");
            return;
        }

        String url;
        if (selectGoogleButton.isChecked()) {
            url = "https://www.google.com/search?q=" + query;
        } else if (selectNaverButton.isChecked()) {
            url = "https://search.naver.com/search.naver?query=" + query;
        } else {
            Log.e(TAG, "검색 엔진이 선택되지 않았습니다.");
            responseMessageTextView.setText("검색 엔진을 선택하세요.");
            return;
        }

        if (getTv() != null && getTv().hasCapability(Launcher.Browser)) {
            getTv().getLauncher().launchBrowser(url, new Launcher.AppLaunchListener() {
                @Override
                public void onError(ServiceCommandError error) {
                    Log.e(TAG, "브라우저 실행 오류: " + error.getMessage());
                    responseMessageTextView.setText("브라우저 실행 오류: " + error.getMessage());
                }

                @Override
                public void onSuccess(LaunchSession session) {
                    Log.d(TAG, "브라우저 실행 성공\nURL: " + url);
                    responseMessageTextView.setText("브라우저 실행 성공");
                }
            });
        } else {
            Log.e(TAG, "TV가 브라우저 실행 기능을 지원하지 않습니다.");
            responseMessageTextView.setText("TV가 브라우저 실행 기능을 지원하지 않습니다.");
        }
    }
}
