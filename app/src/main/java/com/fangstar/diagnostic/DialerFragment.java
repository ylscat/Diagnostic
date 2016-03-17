package com.fangstar.diagnostic;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fangstar.diagnostic.ui.PageScrollView;

/**
 * Created at 2016/3/14.
 *
 * @author YinLanShan
 */
public class DialerFragment extends Fragment implements View.OnClickListener{
    private TextView mNumber;
    private long dialTime;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialer, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.findViewById(R.id.bt0).setOnClickListener(this);
        view.findViewById(R.id.bt1).setOnClickListener(this);
        view.findViewById(R.id.bt2).setOnClickListener(this);
        view.findViewById(R.id.bt3).setOnClickListener(this);
        view.findViewById(R.id.bt4).setOnClickListener(this);
        view.findViewById(R.id.bt5).setOnClickListener(this);
        view.findViewById(R.id.bt6).setOnClickListener(this);
        view.findViewById(R.id.bt7).setOnClickListener(this);
        view.findViewById(R.id.bt8).setOnClickListener(this);
        view.findViewById(R.id.bt9).setOnClickListener(this);
        view.findViewById(R.id.back).setOnClickListener(this);
        view.findViewById(R.id.call).setOnClickListener(this);
        mNumber = (TextView)view.findViewById(R.id.number);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt0:
                append('0');
                break;
            case R.id.bt1:
                append('1');
                break;
            case R.id.bt2:
                append('2');
                break;
            case R.id.bt3:
                append('3');
                break;
            case R.id.bt4:
                append('4');
                break;
            case R.id.bt5:
                append('5');
                break;
            case R.id.bt6:
                append('6');
                break;
            case R.id.bt7:
                append('7');
                break;
            case R.id.bt8:
                append('8');
                break;
            case R.id.bt9:
                append('9');
                break;
            case R.id.back:
                removeLast();
                break;
            case R.id.call:
                String num = mNumber.getText().toString();
                if(num.length() == 11) {
                    Intent intent = new Intent(Intent.ACTION_CALL,
                            Uri.parse("tel:" + num));
                    startActivity(intent);
                    dialTime = System.currentTimeMillis();
                }
                break;
        }
    }

    private void append(char c) {
        int len = mNumber.length();
        if(len == 11)
            return;
        mNumber.append(String.valueOf(c));
    }

    private void removeLast() {
        int len = mNumber.length();
        if(len == 0)
            return;
        Editable e = mNumber.getEditableText().delete(len - 1, len);
        mNumber.setText(e);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(dialTime != 0 && System.currentTimeMillis() - dialTime> 2000) {
            PageScrollView pager = (PageScrollView)getActivity().findViewById(R.id.pager);
            pager.scrollToPage(0);
            dialTime = 0;
        }
    }
}
