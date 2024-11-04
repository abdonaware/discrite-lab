
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

    static class Parser {

        public List<String> sampleExpression = new ArrayList<>();

    }

    static char[] expressionValidation(String representation) {
        boolean letterflag = false, operatorsFlag = false;
        List<Character> operators = new ArrayList<>();
        int count = 0;
        int bractCount = 0;
        for (char c : representation.toCharArray()) {
            switch (c) {
                case '~':
                    // if (!operatorsFlag) {
                    //     operatorsFlag = true;
                    //     letterflag = false;
                    //     count++;
                    // } else {
                    //     throw new Error("invald Expression");
                    // }
                    break;
                case '^':
                case 'v':
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
            
            for (int i = 0 ;i< operatorArray.length;i++) {
                System.out.print("plese Enter the boolen value of "+ operatorArray[i]);
                operatorsValue[i]= sc.nextBoolean();
            }

            sc.close();
        }catch(Error e){
            System.err.println(e.getMessage());
        } 
        catch (Exception e) {
        }
    }
}
