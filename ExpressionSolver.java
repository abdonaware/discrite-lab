
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

        public String getRepresentation() {
            return representation;
        }

        public void setRepresentation(String representation) {
            this.representation = representation;
        }

    }

    static class ExpressionEvaloter implements LogicalExpressionSolver {

        public char[] operatorArray;
        public boolean[] operatorsValue;

        // public boolean evaluateExpression(Expression expression) {

        // }

    }

    static char[] expressionValidation(String representation) {
        boolean letterflag = false, operatorsFlag = false;
        List<Character> operators = new ArrayList<>();
        Stack<Character> opeartions = new Stack<>();
        String postFixExpression;
        int count = 0;
        int bractCount = 0;
        for (char c : representation.toCharArray()) {
            switch (c) {
                case '~':
                    opeartions.push(c);
                    break;
                case '^':
                    if (!operatorsFlag || count != 0) {
                        operatorsFlag = true;
                        letterflag = false;
                        count++;
                        if (!operators.isEmpty() && opeartions.peek() == '~') {
                            postFixExpression.append(string(opeartions.pop()));
                        }
                        opeartions.push(c);
                    } else {
                        throw new Error("invald Expression");
                    }
                    break;
                case 'v':
                    if (!operatorsFlag || count != 0) {
                        operatorsFlag = true;
                        letterflag = false;
                        count++;
                    } else {
                        throw new Error("invald Expression");
                    }
                    break;
                case '>':
                    if (!operatorsFlag || count != 0) {
                        operatorsFlag = true;
                        letterflag = false;
                        count++;
                    } else {
                        throw new Error("invald Expression");
                    }
                    break;

                case '(':
                    count++;
                    bractCount++;
                    break;
                case ')':
                    count++;
                    bractCount--;
                    if (bractCount < 0) {
                        throw new Error("invald Expression");
                    }
                    break;

                default:
                    if (Character.isLetter(c)) {
                        if (!letterflag) {

                            operators.add(c);
                            letterflag = true;
                            operatorsFlag = false;
                            count++;
                        } else {
                            throw new Error("invald Expression");
                        }
                    } else {
                        throw new Error("invald Char ......");
                    }
            }
        }
        if (bractCount != 0) {
            throw new Error("invald Expression");
        }
        char[] operatorArray = new char[operators.size()];
        for (int i = 0; i < operators.size(); i++) {
            operatorArray[i] = operators.get(i);
        }

        return operatorArray;

    }

    public static void main(String[] args) {
        try {
            Scanner sc = new Scanner(System.in);
            LogicExpression lc = new LogicExpression();
            lc.setRepresentation(sc.nextLine());
            char[] operatorArray = expressionValidation(lc.getRepresentation());
            boolean operatorsValue[] = new boolean[operatorArray.length];

            for (int i = 0; i < operatorArray.length; i++) {
                System.out.print("plese Enter the boolen value of " + operatorArray[i]);
                while (!sc.hasNextBoolean()) {
                    System.out.println("Invalid input. Please enter true or false.");
                    sc.next(); // Consume the invalid input
                }
                operatorsValue[i] = sc.nextBoolean();
            }

            sc.close();
        } catch (Error e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
        }
    }
}
