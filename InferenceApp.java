import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

/**
 * Run the command "java -ea <class_file> --demo" to run demo
 */

// Interfaces and Excpetions
class InvalidPattern extends RuntimeException {
  public InvalidPattern(String input) {
    super("Invalid pattern encountered: Expected something like '(A > B)' or '(~A)' but found '" + input + "'");
  }
}

interface InferenceRule {
  boolean matches(Expression exp1, Expression exp2);

  Expression apply(Expression exp1, Expression exp2);

  // Extra method
  String getName();
}

interface InferenceEngine {
  void addRule(InferenceRule rule);

  void addExpression(Expression exp);

  Expression applyRules();
}

// Expression DTO and Parser

class Operand {
  boolean negated;
  String operand;

  Operand(boolean negated, String operand) {
    this.negated = negated;
    this.operand = operand.trim();
  }

  Operand(String input) {
    input = input.trim();

    negated = input.charAt(0) == '~';
    operand = (negated ? input.substring(1) : input).trim();
  }

  public String toString() {
    return (negated ? "~" : "") + operand;
  }

  public boolean complementaryTo(Operand op) {
    return isNegated() != op.isNegated();
  }

  public boolean isNegated() {
    return negated;
  }

  public boolean equals(Operand op2) {
    return toString().equals(op2.toString());
  }

  public Operand complement() {
    return new Operand(!negated, operand);
  }
}

class Expression {
  private static String OPERAND_REGEX = "([~]*[A-Za-z]+)";
  private static String OPERATOR_REGEX = "([>v])";

  private static Pattern BINARY_REGEX = Pattern
      .compile(OPERAND_REGEX + "\\s*" + OPERATOR_REGEX + "\\s*" + OPERAND_REGEX);
  private static Pattern ONE_OPERAND_REGEX = Pattern.compile("\\s*" + OPERAND_REGEX + "\\s*");

  private static Scanner scan;

  Operand left;
  Operand right;
  String operator;

  Expression(Operand operand) {
    left = operand;
    right = null;
    operator = null;
  }

  Expression(String input) {
    Matcher binary = BINARY_REGEX.matcher(input);
    Matcher one_operand = ONE_OPERAND_REGEX.matcher(input);
    if (binary.matches()) {
      this.left = new Operand(binary.group(1));
      this.operator = binary.group(2).trim();
      this.right = new Operand(binary.group(3));

      // System.out.println(binary.group(1));
      // System.out.println(binary.group(2));
      // System.out.println(binary.group(3));
    }

    else if (one_operand.matches()) {
      this.left = new Operand(one_operand.group(1));
      this.operator = null;
      this.right = null;

      // System.out.println(unary.group(1));
      // System.out.println(unary.group(2));
    }

    else
      throw new InvalidPattern(input);
  }

  static Expression fromInput() {
    if (scan == null)
      scan = new Scanner(System.in);

    System.out.print("Enter Expression: ");
    return new Expression(scan.nextLine());
  }

  public boolean isOperator(String op) {
    return operator != null && operator.equals(op);
  }

  public boolean noOperator() {
    return operator == null;
  }

  public boolean includes(Operand op) {
    return left.equals(op) || right != null && right.equals(op);
  }

  public Operand theOther(Operand op) {
    if (!left.equals(op))
      return left;

    return right;
  }

  public String toString() {
    return (left + " " + (operator == null ? "" : operator) + " " + (right == null ? "" : right)).trim();
  }
}

// Inference Rules Strategies
class ModusPonens implements InferenceRule {
  Expression premise;

  public boolean matches(Expression exp1, Expression exp2) {
    return modusPonensPattern(exp1, exp2) || modusPonensPattern(exp2, exp1);
  }

  public Expression apply(Expression exp1, Expression exp2) {
    return new Expression(this.premise.right);
  }

  private boolean modusPonensPattern(Expression premise1, Expression premise2) {
    boolean result = premise1.isOperator(">") && premise2.noOperator() && premise1.left.equals(premise2.left);

    if (result)
      this.premise = premise1;

    return result;
  }

  public String getName() {
    return "Modus Ponens";
  }
}

class ModusTollens implements InferenceRule {
  Expression premise;

  public boolean matches(Expression exp1, Expression exp2) {
    return modusTollensPattern(exp1, exp2) || modusTollensPattern(exp2, exp1);
  }

  public Expression apply(Expression exp1, Expression exp2) {
    return new Expression(this.premise.left.complement());
  }

  private boolean modusTollensPattern(Expression premise1, Expression premise2) {
    boolean result = premise1.isOperator(">") && premise2.noOperator() && premise2.left.complementaryTo(premise1.right);

    if (result)
      this.premise = premise1;

    return result;
  }

  public String getName() {
    return "Modus Tollens";
  }
}

class HypotheticalSyllogism implements InferenceRule {
  Expression premise1;
  Expression premise2;

  public boolean matches(Expression exp1, Expression exp2) {
    return hypotheticalSyllogismPattern(exp1, exp2) || hypotheticalSyllogismPattern(exp2, exp1);
  }

  public Expression apply(Expression exp1, Expression exp2) {
    return new Expression(premise1.left.toString() + ">" + premise2.right.toString());
  }

  private boolean hypotheticalSyllogismPattern(Expression premise1, Expression premise2) {
    boolean result = premise1.isOperator(">") && premise2.isOperator(">") &&
        premise1.right.equals(premise2.left);

    if (result) {
      this.premise1 = premise1;
      this.premise2 = premise2;
    }

    return result;
  }

  public String getName() {
    return "Hypothetical Syllogism";
  }
}

class DisjunctiveSyllogism implements InferenceRule {
  Expression premise;

  public boolean matches(Expression exp1, Expression exp2) {
    return disjunctiveSyllogismPattern(exp1, exp2) || disjunctiveSyllogismPattern(exp2, exp1);
  }

  public Expression apply(Expression exp1, Expression exp2) {
    return premise;
  }

  private boolean disjunctiveSyllogismPattern(Expression premise1, Expression premise2) {
    boolean result = premise1.isOperator("v") && premise2.noOperator()
        && (premise1.left.complementaryTo(premise2.left) || premise1.right.complementaryTo(premise2.left));

    if (result) {
      if (premise1.left.complementaryTo(premise2.left))
        this.premise = new Expression(premise1.right);
      else
        this.premise = new Expression(premise1.left);
    }

    return result;
  }

  public String getName() {
    return "Disjunctive Syllogism";
  }
}

class Resolution implements InferenceRule {
  Expression premise;

  public boolean matches(Expression exp1, Expression exp2) {
    return resolutionPattern(exp1, exp2) || resolutionPattern(exp2, exp1);
  }

  public Expression apply(Expression exp1, Expression exp2) {
    return premise;
  }

  private boolean resolutionPattern(Expression premise1, Expression premise2) {
    boolean result = premise1.isOperator("v") && premise2.isOperator("v")
        && (premise2.includes(premise1.left.complement()) || premise2.includes(premise1.right.complement()));

    if (result) {
      if (premise2.includes(premise1.left.complement()))
        this.premise = new Expression(
            premise1.right.toString() + "v" + premise2.theOther(premise1.left.complement()).toString());
      else
        this.premise = new Expression(
            premise1.left.toString() + "v" + premise2.theOther(premise1.right.complement()).toString());
    }

    return result;
  }

  public String getName() {
    return "Disjunctive Syllogism";
  }
}

// Brute force inference engine
class Engine implements InferenceEngine {
  Expression exp1;
  Expression exp2;
  InferenceRule applied_rule;
  ArrayList<InferenceRule> rules = new ArrayList<InferenceRule>();

  Engine() {
    // Silence
  }

  public void addExpression(Expression exp) {
    if (exp1 == null)
      exp1 = exp;
    else
      exp2 = exp;
  }

  public Engine setExpressions(Expression exp1, Expression exp2) {
    this.exp1 = exp1;
    this.exp2 = exp2;
    return this;
  }

  public void addRule(InferenceRule rule) {
    rules.add(rule);
  }

  public Expression applyRules() {
    for (InferenceRule rule : rules) {
      if (rule.matches(exp1, exp2)) {
        applied_rule = rule;
        return rule.apply(exp1, exp2);
      }
    }

    return null;
  }

  public InferenceRule getAppliedRule() {
    return applied_rule;
  }
}

public class InferenceApp {
  public static void main(String[] args) {
    for (String arg : args) {
      if ("--demo".equals(arg)) {
        InferenceDemo.run();
        return;
      }
    }

    InferenceRule[] rules = {
        new ModusPonens(),
        new ModusTollens(),
        new HypotheticalSyllogism(),
        new DisjunctiveSyllogism(),
        new Resolution()
    };

    while (true) {
      Engine engine = new Engine();
      try{
        engine.addExpression(Expression.fromInput());
        engine.addExpression(Expression.fromInput());
      }
      catch(InvalidPattern e) {
        System.out.println(e.getMessage());
        continue;
      }

      for (InferenceRule rule : rules)
        engine.addRule(rule);

      Expression result = engine.applyRules();

      if (result != null)
        System.out.println(result.toString() + " (" + engine.getAppliedRule().getName() + ")");
      else
        System.out.println("The input expression cannot be inferred");
    }
  }
}

class Testrun {
  public Expression exp1;
  public Expression exp2;
  public Expression result;

  Testrun(Expression exp1, Expression exp2, Expression result) {
    this.exp1 = exp1;
    this.exp2 = exp2;
    this.result = result;
  }

  Testrun(String exp1, String exp2, String result) {
    this.exp1 = new Expression(exp1);
    this.exp2 = new Expression(exp2);
    this.result = new Expression(result);
  }
}

class InferenceDemo {
  public static void run() {
    InferenceRule[] rules = {
        new ModusPonens(),
        new ModusTollens(),
        new HypotheticalSyllogism(),
        new DisjunctiveSyllogism(),
        new Resolution()
    };

    Engine engine = new Engine();
    for (InferenceRule rule : rules)
      engine.addRule(rule);

    /**
     * Switching expressions order won't matter and doesn't need to be tested
     * becuase every rule tries matching in both orders. We don't test syntactical
     * errors also because it is verified during development. We care more here
     * about logical errors
     */

    Testrun[] testRuns = {
        // Modus Ponens
        new Testrun("P > Q", "P", "Q"),
        new Testrun("P > ~Q", "P", "~Q"),
        new Testrun("~P > Q", "~P", "Q"),
        new Testrun("~P > ~Q", "~P", "~Q"),

        // Modus Tollens
        new Testrun("P > Q", "~Q", "~P"),
        new Testrun("P > ~Q", "Q", "~P"),
        new Testrun("~P > Q", "~Q", "P"),
        new Testrun("~P > ~Q", "Q", "P"),

        // Hypothetical Syllogism
        new Testrun("P > Q", "Q > R", "P > R"),
        new Testrun("P > ~Q", "~Q > R", "P > R"),
        new Testrun("~P > Q", "Q > R", "~P > R"),
        new Testrun("P > Q", "Q > ~R", "P > ~R"),
        new Testrun("~P > Q", "Q > ~R", "~P > ~R"),

        // Disjunctive Syllogism
        new Testrun("P v Q", "~P", "Q"),
        new Testrun("~P v Q", "P", "Q"),
        new Testrun("~P v ~Q", "P", "~Q"),
        new Testrun("P v ~Q", "~P", "~Q"),

        // Resolution
        new Testrun("P v Q", "~P v R", "Q v R"),
        new Testrun("~P v Q", "P v R", "Q v R"),
        new Testrun("P v ~Q", "~P v R", "~Q v R"),
        new Testrun("P v Q", "~P v ~R", "Q v ~R"),
        new Testrun("P v ~Q", "~P v ~R", "~Q v ~R")
    };

    for (Testrun run : testRuns) {
      assert engine.setExpressions(run.exp1, run.exp2).applyRules().toString().equals(run.result.toString())
          : "Error in run: " + run.exp1.toString() + " and " + run.exp2.toString();

      System.out.println("Asserted that infering expressions '" + run.exp1 + "' and '" + run.exp2 + "' results in '"
          + run.result + "'");
    }
  }
}