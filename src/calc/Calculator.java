package calc;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Double.NaN;
import static java.lang.Math.pow;


/*
 *   A calculator for rather simple arithmetic expressions
 *
 *   This is not the program, it's a class declaration (with methods) in it's
 *   own file (which must be named Calculator.java)
 *
 *   NOTE:
 *   - No negative numbers implemented
 */
class Calculator {

    // Here are the only allowed instance variables!
    // Error messages (more on static later)
    final static String MISSING_OPERAND = "Missing or bad operand";
    final static String DIV_BY_ZERO = "Division with 0";
    final static String MISSING_OPERATOR = "Missing operator or parenthesis";
    final static String OP_NOT_FOUND = "Operator not found";

    // Definition of operators
    final static String OPERATORS = "+-*/^";

    // Method used in REPL
    double eval(String expr) {
        if (expr.length() == 0) {
            return NaN;
        }
        List<String> tokens = tokenize(expr);
        List<String> postfix = infix2Postfix(tokens);
        double result = evalPostfix(postfix);
        return result;
    }

    // ------  Evaluate RPN expression -------------------

    public double evalPostfix(List<String> postfix) {
        Stack<String> result = new Stack<>();
        int i = 0;
        while (postfix.size() > 0) {
            String currToken = postfix.get(i);
            if (isNumber(currToken)) {
                result.push(currToken);
            }
            if (OPERATORS.contains(currToken)) {
                double sum = applyOperator(currToken, Double.parseDouble(result.pop()), Double.parseDouble(result.pop()));
                result.push(Double.toString(sum));
            }
            postfix.remove(currToken);
        }

        return Double.parseDouble(result.pop());
    }


    double applyOperator(String op, double d1, double d2) {
        switch (op) {
            case "+":
                return d1 + d2;
            case "-":
                return d2 - d1;
            case "*":
                return d1 * d2;
            case "/":
                if (d1 == 0) {
                    throw new IllegalArgumentException(DIV_BY_ZERO);
                }
                return d2 / d1;
            case "^":
                return pow(d2, d1);
        }
        throw new RuntimeException(OP_NOT_FOUND);
    }

    // ------- Infix 2 Postfix ------------------------

    public List<String> infix2Postfix(List<String> tokens) {
        //https://en.wikipedia.org/wiki/Shunting-yard_algorithm
        Stack<String> output = new Stack<>();
        Stack<String> stack = new Stack<>();

        while (tokens.size() > 0) {
            String currToken = tokens.get(0);
            String topOperator = "";
            if(!stack.empty()) {topOperator = stack.peek();}
            if (isNumber(currToken)) {
                output.push(currToken); //token is a number add to output stack
            } else if (OPERATORS.contains(currToken)) {
                //TODO kan läggas i egen metod för att snygga till koden lite
                while (!topOperator.equals("(") && !stack.empty() && getPrecedence(topOperator) > getPrecedence(currToken)
                        || !topOperator.equals("(") && !stack.empty() && getPrecedence(topOperator) == getPrecedence(currToken) && getAssociativity(topOperator) == Assoc.LEFT) {
                    output.push(stack.pop()); //token is a operator add other operators from operatorstack to outputstack
                    topOperator = getTopOperator(stack);
                }
                stack.push(currToken); //store current token in operator stack
            } else if (currToken.equals("(")) {
                stack.push(currToken);
            } else if (currToken.equals(")")) { // if currToken is right paranthesis output all operators until left paranthesis
                while (!topOperator.equals("(")) {
                    //TODO kan läggas i egen metod för att snygga till koden lite
                    output.push(stack.pop());
                    topOperator = getTopOperator(stack);
                }
                if (topOperator.equals("(")) {
                    stack.pop();
                }
            }
            tokens.remove(0); //remove currentToken
        }
        while (stack.size() > 0) {
            output.push(stack.pop());
        }

        return output;
    }


    int getPrecedence(String op) {
        if ("+-".contains(op)) {
            return 2;
        } else if ("*/".contains(op)) {
            return 3;
        } else if ("^".contains(op)) {
            return 4;
        } else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }

    Assoc getAssociativity(String op) {
        if ("^".contains(op)) {
            return Assoc.RIGHT;
        } else if (OPERATORS.contains(op)) {
            return Assoc.LEFT;
        } else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }

    enum Assoc {
        LEFT,
        RIGHT
    }

    // ---------- Tokenize -----------------------
    //12+2 -> [12, +, 2]
    public List<String> tokenize(String expr) {
        expr = expr.replaceAll("\\s+",""); // Ta bort alla whitespaces
        ArrayList<String> tokens = new ArrayList<>();
        char[] exprChar = expr.toCharArray();
        int numbersInSequence = 0;
        for (int i = 0; i < exprChar.length; i++) {
            if (OPERATORS.indexOf(exprChar[i]) >= 0 || exprChar[i] == '(' || exprChar[i] == ')') {
                if (numbersInSequence > 0) { tokens.add(expr.substring(i - numbersInSequence, i));} // lägg till nummer innan operator
                tokens.add(expr.substring(i, i + 1));
                numbersInSequence = 0;
            }else if (i == exprChar.length - 1) {
                tokens.add(expr.substring(i - numbersInSequence)); //slutet av list lägg till resten av nummer om det finns nummer kvar
            } else if(isNumber(exprChar[i])) {
                //char is a number
                numbersInSequence++;
            }

        }
        return tokens;
    }

    // TODO Possibly more methods

    public boolean isNumber(String numb) {
        if(numb.matches("-?\\d+(\\.\\d+)?")) {
            return true;
        } else {
            return false;
        }
    }
    public boolean isNumber(char numb) {
        return Character.isDigit(numb);
    }

    public String getTopOperator(Stack<String> stack) {
        if (!stack.empty()) {return stack.peek();}
        else { return "";}
    }

}
