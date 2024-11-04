
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

public class ExpressionSolver {

    interface Expression {

        String getRepresentation();

        void setRepresentation(String representation);
    }

    interface LogicalExpressionSolver {

        boolean evaluateExpression(Expression expression);
    }

    static class LogicExpression implements Expression {

        private String representation;

        @Override
        public String getRepresentation() {
            return representation;
        }

        @Override
        public void setRepresentation(String representation) {
            this.representation = representation;
        }

    }

    static class ExpressionEvaluator implements LogicalExpressionSolver {

        public char[] operatorArray;
        public boolean[] operatorsValue;

        @Override
        public boolean evaluateExpression(Expression expression) {
            char result;
            Stack<Character> opeartions = new Stack<>();
            for (char elem : expression.getRepresentation().toCharArray()) {
                switch (elem) {
                    case '~':
                        if (opeartions.isEmpty()) {
                            throw new Error("invald Expression");
                        }
                        char oper = opeartions.pop();
                        if (oper == '1') {
                            opeartions.push('0');
                        } else {
                            opeartions.push('1');
                        }

                        break;
                    case '^':
                        if (opeartions.isEmpty()) {
                            throw new Error("invald Expression");
                        }
                        char oper1 = opeartions.pop();
                        if (opeartions.isEmpty()) {
                            throw new Error("invald Expression");
                        }
                        char oper2 = opeartions.pop();
                        if (oper1 == '1' && oper2 == '1') {
                            opeartions.push('1');
                        } else {
                            opeartions.push('0');
                        }
                        break;
                    case 'v':
                        if (opeartions.isEmpty()) {
                            throw new Error("invald Expression");
                        }
                        oper1 = opeartions.pop();
                        if (opeartions.isEmpty()) {
                            throw new Error("invald Expression");
                        }
                        oper2 = opeartions.pop();
                        if (oper1 == '0' && oper2 == '0') {
                            opeartions.push('0');
                        } else {
                            opeartions.push('1');
                        }

                        break;
                    case '>':
                        if (opeartions.isEmpty()) {
                            throw new Error("invald Expression");
                        }
                        oper2 = opeartions.pop();
                        if (opeartions.isEmpty()) {
                            throw new Error("invald Expression");
                        }
                        oper1 = opeartions.pop();
                        if (oper1 == '1' && oper2 == '0') {
                            opeartions.push('0');
                        } else {
                            opeartions.push('1');
                        }

                        break;
                    case '0':
                    case '1':
                        opeartions.push(elem);
                        break;
                    default:
                        throw new AssertionError();
                }

            }
            if(opeartions.isEmpty()){
                throw new Error("invald Expression");
                }
            result = opeartions.pop();
            if (result == '0') {
                return false;
            } else {
                return true;
            }
        }

    }

    static class ValidationResult {

        private String postfix;
        private char[] operatorArray;

        public ValidationResult(String postfix, char[] operatorArray) {
            this.postfix = postfix;
            this.operatorArray = operatorArray;
        }

        public String getPostfix() {
            return postfix;
        }

        public char[] getOperatorArray() {
            return operatorArray;
        }
    }

    static ValidationResult expressionValidation(String representation) {
        boolean letterflag = false, operatorsFlag = false;
        List<Character> operators = new ArrayList<>();
        Stack<Character> opeartions = new Stack<>();
        StringBuilder postFixExpression = new StringBuilder();
        int count = 0;
        for (char c : representation.toCharArray()) {
            switch (c) {
                case '~':
                    opeartions.push(c);
                    break;
                case '^':
                    if (!operatorsFlag && count != 0) {
                        operatorsFlag = true;
                        letterflag = false;
                        count++;
                        while (!opeartions.isEmpty() && opeartions.peek() == '~') {
                            postFixExpression.append(opeartions.pop());
                        }
                        opeartions.push(c);
                    } else {
                        throw new Error("invald Expression");
                    }
                    break;
                case 'v':
                    if (!operatorsFlag && count != 0) {
                        operatorsFlag = true;
                        letterflag = false;
                        count++;
                        while (!opeartions.isEmpty() && (opeartions.peek() == '~' || opeartions.peek() == '^')) {
                            postFixExpression.append(opeartions.pop());
                        }
                        opeartions.push(c);
                    } else {
                        throw new Error("invald Expression");
                    }
                    break;
                case '>':
                    if (!operatorsFlag && count != 0) {
                        operatorsFlag = true;
                        letterflag = false;
                        count++;
                        while (!opeartions.isEmpty() && (opeartions.peek() == '~' || opeartions.peek() == '^' || opeartions.peek() == 'v')) {
                            postFixExpression.append(opeartions.pop());
                        }
                        opeartions.push(c);
                    } else {
                        throw new Error("invald Expression");
                    }
                    break;

                case '(':

                    opeartions.push(c);
                    break;
                case ' ':
                    break;
                case ')':
                    count++;

                    while (!opeartions.isEmpty() && !(opeartions.peek() == '(')) {
                        postFixExpression.append(opeartions.pop());
                    }
                    if (opeartions.peek() == '(') {
                        opeartions.pop();
                    } else {
                        throw new Error("invald Expression");
                    }
                    break;
                default:
                    if (Character.isLetter(c)) {
                        if (!letterflag) {
                            if (!operators.contains(c)) {
                                operators.add(c);
                            }
                            letterflag = true;
                            operatorsFlag = false;
                            count++;
                            postFixExpression.append(c);
                        } else {
                            throw new Error("invald Expression");
                        }
                    } else {
                        throw new Error("invald Char ......");
                    }
            }
        }
        while (!opeartions.isEmpty() && (opeartions.peek() == '~' || opeartions.peek() == '^' || opeartions.peek() == 'v' || opeartions.peek() == '>')) {
            postFixExpression.append(opeartions.pop());
        }
        if (!opeartions.isEmpty()) {
            throw new Error("invald Expression");
        }

        char[] operatorArray = new char[operators.size()];
        for (int i = 0; i < operators.size(); i++) {
            operatorArray[i] = operators.get(i);
        }

        return new ValidationResult(postFixExpression.toString(), operatorArray);

    }

    public static void main(String[] args) {
        try {
            Scanner sc = new Scanner(System.in);
            LogicExpression lc = new LogicExpression();
            System.out.println(" ");
            System.out.print("Enter a logical expression: ");
            lc.setRepresentation(sc.nextLine());

            ValidationResult validationResult = expressionValidation(lc.getRepresentation());
            boolean[] operatorsValue = new boolean[validationResult.operatorArray.length];
            ExpressionEvaluator evaluator = new ExpressionEvaluator();

            for (int i = 0; i < validationResult.operatorArray.length; i++) {
                System.out.print("Please enter the boolean value of " + validationResult.operatorArray[i] + " (true/false): ");
                while (!sc.hasNextBoolean()) {
                    System.out.println("Invalid input. Please enter true or false.");
                    sc.next();
                }
                operatorsValue[i] = sc.nextBoolean();
            }

            StringBuilder modifiedPostfix = new StringBuilder();
            char[] postfixArray = validationResult.getPostfix().toCharArray();
            boolean flag = false;
            int indexOfUsedNegation = 0;
            for (int i = 0; i < postfixArray.length; i++) {
                char elem = postfixArray[i];
                if (indexOfUsedNegation != i && flag) {
                    flag = false;
                }
                if (Character.isLetter(elem) && elem != 'v') {
                    int index = new String(validationResult.getOperatorArray()).indexOf(elem);
                    boolean value = operatorsValue[index];
                    modifiedPostfix.append(value ? '1' : '0');
                } else {
                    modifiedPostfix.append(elem);
                }
            }
            System.out.println("postfix" + " " + modifiedPostfix.toString() + " " + validationResult.getPostfix());
            lc.setRepresentation(modifiedPostfix.toString());
            boolean answer = evaluator.evaluateExpression(lc);
            System.out.println("The answer is: " + answer);
            sc.close();

        } catch (Error e) {
            System.err.println(e.getMessage());
            // Print stack trace for additional debugging
        } catch (Exception e) {
            System.err.println("Unexpected exception: " + e.getMessage());
            e.printStackTrace();  // Print stack trace for additional debugging
        }
    }
}
