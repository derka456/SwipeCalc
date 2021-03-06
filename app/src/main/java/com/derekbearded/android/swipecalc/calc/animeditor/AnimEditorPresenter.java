package com.derekbearded.android.swipecalc.calc.animeditor;

import com.derekbearded.android.swipecalc.data.Expression;
import com.derekbearded.android.swipecalc.data.MathSymbol;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Sputnik on 1/31/2018.
 */

class AnimEditorPresenter implements Observer, AnimEditorContract.Presenter {
    private static final String CAKE = "84302253047020543"; // "the cake is a lie"
    private StringBuilder builder = new StringBuilder();
    private Expression expression;
    private AnimEditorContract.View view;
    private boolean prevEquals;

    public AnimEditorPresenter(AnimEditorContract.View view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void clear() {
        expression.clear();
        view.updateDisplay("");
        view.updatePreview("");
    }

    @Override
    public void start() {
        expression = new Expression();
        expression.addObserver(this);
    }

    // Updates model when a button is pressed
    public void addNewValue(String symbol){
        switch(symbol){
            case "C":
                clear();
                clearCake();
                break;
            case "=":
                if (!expression.getValue().isEmpty()) {
                    expression.clear(true, false);
                    view.updateDisplay(expression.getValue());
                    view.updatePreview("");
                }
                clearCake();
                prevEquals = true;
                break;
            case "\u00b1":
                expression.add(MathSymbol.fromString("\u00af"));
                clearCake();
            case "( )":
                expression.add(MathSymbol.fromString("("));
                clearCake();
                break;
            case "+":
            case "\u2212":
                // minus
            case "\u00d7":
                // times
            case "\u00f7":
                // divide
            case "%":
                // catching all operators
                expression.add(MathSymbol.fromString(symbol));
                break;
            default:
                // numerals only here
                if (prevEquals){
                    expression.clear(false, false);
                    prevEquals = false;
                }
                expression.add(MathSymbol.fromString(symbol));
                checkCake(symbol);
                break;
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        view.updateDisplay(expression.toStringGroupingAsInputted());
        view.updatePreview(expression.getValue());
    }

    private void checkCake(String symbol){
        builder.append(symbol);
        int comparison = builder.length() - CAKE.length();
        if (comparison == 0){
            if (builder.toString().equals(CAKE)){
                view.showDevOpts();
            } else {
                clearCake();
            }
        }
    }

    private void clearCake(){
        builder.setLength(0);
    }
}