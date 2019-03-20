package com.example.asus.ocrreaderwithtxttospeech;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class SpeedFragment extends DialogFragment {
    SeekBar speedSeekbar;
    TextView okButton,cancelButton;

    public interface onInputSpeedListner{
        void sentSpeedInput(String input);
    }

    public onInputSpeedListner monInputListner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.speed_fragment,container,false);

        speedSeekbar = (SeekBar)view.findViewById(R.id.speedSeekBar);
        okButton = (TextView) view.findViewById(R.id.okButton);
        cancelButton = (TextView) view.findViewById(R.id.cancelButton);

        setCancelable(false);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("okButton","pressed");
                float speed = (float)speedSeekbar.getProgress()/50;
                if (speed < 0.1){
                    speed = 0.1f;
                }
                String speedstr = String.valueOf(speed);
                monInputListner.sentSpeedInput(speedstr);
                getDialog().dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        monInputListner = (onInputSpeedListner) getActivity();
    }
}
