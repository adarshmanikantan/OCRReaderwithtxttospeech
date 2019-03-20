package com.example.asus.ocrreaderwithtxttospeech;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class PitchFragment extends DialogFragment{

    SeekBar pitchSeekbar;
    TextView okButton,cancelButton;

    public interface onInputPitchListner{
        void sentPitchInput(String input);
    }

    public onInputPitchListner monInputListner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pitch_fragment,container,false);

        pitchSeekbar = (SeekBar)view.findViewById(R.id.pitchSeekBar);
        okButton = (TextView) view.findViewById(R.id.okButton);
        cancelButton = (TextView) view.findViewById(R.id.cancelButton);

        setCancelable(false);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("okButton","pressed");
                float pitch = (float)pitchSeekbar.getProgress()/50;
                if (pitch < 0.1){
                    pitch = 0.1f;
                }
                String pitchstr = String.valueOf(pitch);
                monInputListner.sentPitchInput(pitchstr);
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
        monInputListner = (onInputPitchListner)getActivity();
    }
}
