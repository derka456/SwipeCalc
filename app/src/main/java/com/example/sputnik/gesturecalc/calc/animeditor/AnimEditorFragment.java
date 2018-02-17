package com.example.sputnik.gesturecalc.calc.animeditor;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.example.sputnik.gesturecalc.R;
import com.example.sputnik.gesturecalc.anim.CircleAnimator;
import com.example.sputnik.gesturecalc.anim.FactoryAnimator;
import com.example.sputnik.gesturecalc.anim.PathAnimator;
import com.example.sputnik.gesturecalc.util.ButtonGrid;
import com.example.sputnik.gesturecalc.util.PathActivator;

/**
 * Created by Sputnik on 2/16/2018.
 */

public class AnimEditorFragment extends Fragment implements AnimEditorContract.View {

    private AnimEditorContract.Presenter presenter;
    private PathAnimator animator;
    private PathActivator activator;
    ButtonGrid buttonLayout;
    SeekBar sizeBar, spacingBar, durationBar, opacityBar, pathBar;
    EditText sizeEdit, spacingEdit, durationEdit, opacityEdit;
    TextView display, preview;
    Switch shapeSwitch;
    Spinner spinner;
    SpinnerAdapter spinnerAdapter;

    abstract class MySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
    class MyTextWatcher implements TextWatcher {
        private SeekBar seekBar;

        MyTextWatcher(SeekBar seekBar){
            this.seekBar = seekBar;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            int val = Integer.valueOf(s.toString());
            val = val < 0 ? 0 : val;
            val = val > seekBar.getMax() ? seekBar.getMax() : val;
            seekBar.setProgress(val);
        }
    }

    @Override
    public void setPresenter(AnimEditorContract.Presenter presenter) {
        this.presenter = presenter;
    }

    public void onResume() {
        super.onResume();
        presenter.start();
        buttonLayout.registerButtonListener(new ButtonGrid.ButtonListener() {
            @Override
            public void buttonPressed(String input) {
                presenter.addNewValue(input);
                pathBar.setProgress(pathBar.getMax());
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frag_anim_edit, container, false);

        buttonLayout = root.findViewById(R.id.gridLayout);
        activator = new PathActivator();
        animator = FactoryAnimator.makeAnimator(FactoryAnimator.Type.Circle);
        ViewTreeObserver viewTreeObserver = ((ViewGroup) buttonLayout).getViewTreeObserver();
        if (viewTreeObserver.isAlive()){
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
                        ((ViewGroup) buttonLayout).getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        ((ViewGroup) buttonLayout).getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    buttonLayout.setupSize();
                    animator.setCanvasSize(((ViewGroup) buttonLayout).getWidth(), ((ViewGroup) buttonLayout).getHeight());
                }
            });
        }
        buttonLayout.setPathActivator(activator);
        buttonLayout.setPathAnimator(animator);

        display = root.findViewById(R.id.display);
        preview = root.findViewById(R.id.preview);

        sizeBar = root.findViewById(R.id.seekBarSize);
        spacingBar = root.findViewById(R.id.seekBarSpacing);
        durationBar = root.findViewById(R.id.seekBarDuration);
        opacityBar = root.findViewById(R.id.seekBarOpacity);
        pathBar = root.findViewById(R.id.seekBarPath);

        sizeEdit = root.findViewById(R.id.editNumberSize);
        spacingEdit = root.findViewById(R.id.editNumberSpacing);
        durationEdit = root.findViewById(R.id.editNumberDuration);
        opacityEdit = root.findViewById(R.id.editNumberOpacity);

        shapeSwitch = root.findViewById(R.id.switchCircleLine);
        spinner = root.findViewById(R.id.spinnerSetting);


        sizeBar.setOnSeekBarChangeListener(new MySeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    sizeEdit.setText(Integer.toString(progress));
                    animator.setStartSize(progress);
                }
            }
        });
        spacingBar.setOnSeekBarChangeListener(new MySeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    spacingEdit.setText(Integer.toString(progress));
                    if (animator instanceof CircleAnimator){
                        ((CircleAnimator) animator).setCircleCenterSpacing(progress);
                    }
                }
            }
        });
        durationBar.setOnSeekBarChangeListener(new MySeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    durationEdit.setText(Integer.toString(progress));
                    animator.setAnimationDuration(progress);
                }
            }
        });
        opacityBar.setOnSeekBarChangeListener(new MySeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    opacityEdit.setText(Integer.toString(progress));
                    animator.setOpacity(progress);
                }
            }
        });
        pathBar.setOnSeekBarChangeListener(new MySeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    animator.reDrawTo(progress);
                    ((ViewGroup) buttonLayout).invalidate();
                }
            }
        });

        sizeEdit.addTextChangedListener(new MyTextWatcher(sizeBar));
        spacingEdit.addTextChangedListener(new MyTextWatcher(spacingBar));
        durationEdit.addTextChangedListener(new MyTextWatcher(durationBar));
        opacityEdit.addTextChangedListener(new MyTextWatcher(opacityBar));

        shapeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                animator.recycle();
                if (isChecked){
                    animator = FactoryAnimator.makeAnimator(FactoryAnimator.Type.Line);
                } else {
                    animator = FactoryAnimator.makeAnimator(FactoryAnimator.Type.Circle);
                }
                ((ViewGroup) buttonLayout).invalidate();
            }
        });

        return root;
    }

    public void updateDisplay(String expression) {
        display.setText(expression);
    }

    @Override
    public void updatePreview(String expression) {
        preview.setText(expression);
    }
}