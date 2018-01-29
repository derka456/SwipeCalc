package com.example.sputnik.gesturecalc.Engine;

/**
 * Created by Sputnik on 1/24/2018.
 */

enum ExpressionPrecedence {
    /*
    LOWEST ->   Addition / subtraction
    LOW ->      Multiplication / Division
    MEDIUM ->   Unary multiplication (%, negation)
    HIGH ->     Exponent
    HIGHEST ->  TBD
     */
    LOWEST, LOW, MEDIUM, HIGH, HIGHEST, NUMBER
}