package com.derekbearded.android.swipecalc.util;

import com.derekbearded.android.swipecalc.anim.Animator;

/**
 * Created by Sputnik on 2/16/2018.
 */

public interface ButtonGrid {
    interface ButtonListener {
        void buttonPressed(String input);
    }

    void setPathAnimator(Animator animator);

    void setPathActivator(final PathActivator activator);

    void registerButtonListener(ButtonListener listener);

    void unregisterButtonListener(ButtonListener listener);
}