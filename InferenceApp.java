import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

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
class Expression {
  private static Pattern BINARY_REGEX = Pattern.compile("(.+)\\s*([>v&])\\s*(.+)");
  private static Pattern UNARY_REGEX = Pattern.compile("([~])(.+)");
  private static Pattern NO_OP_REGEX = Pattern.compile("([A-Za-z]+)");

  private static Scanner scan;

  String left;
  String right;
  String operator;

  Expression(String input) {
    Matcher binary = BINARY_REGEX.matcher(input);
    Matcher unary = UNARY_REGEX.matcher(input);
    Matcher no_operator = NO_OP_REGEX.matcher(input);
    if (binary.matches()) {
      this.left = binary.group(1).trim();
      this.operator = binary.group(2).trim();
      this.right = binary.group(3).trim();

      // System.out.println(binary.group(1));
      // System.out.println(binary.group(2));
      // System.out.println(binary.group(3));
    }

    else if (unary.matches()) {
      this.left = "";
      this.operator = unary.group(1).trim();
      this.right = unary.group(2).trim();

      // System.out.println(unary.group(1));
      // System.out.println(unary.group(2));
    }

    else if (no_operator.matches()) {
      this.left = input.trim();
      this.operator = "";
      this.right = "";
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

  public String toString() {
    return (left + " " + operator + " " + right).trim();
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
    boolean result = premise1.operator.equals(">") && premise2.operator == "" && premise1.left.equals(premise2.left);

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
    return new Expression("~" + this.premise.left);
  }

  private boolean modusTollensPattern(Expression premise1, Expression premise2) {
    boolean result = premise1.operator.equals(">") && premise2.operator.equals("~")
        && premise1.right.equals(premise2.right);

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
    return new Expression(premise1.left + ">" + premise2.right);
  }

  private boolean hypotheticalSyllogismPattern(Expression premise1, Expression premise2) {
    boolean result = premise1.operator.equals(">") && premise2.operator.equals(">")
        && (premise1.right.equals(premise2.left) || premise2.left.equals(premise1.right));

    if (result) {
      if (premise1.right.equals(premise2.left)) {
        this.premise1 = premise1;
        this.premise2 = premise2;
      } else {
        this.premise1 = premise2;
        this.premise2 = premise1;
      }
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
    boolean result = premise1.operator.equals("v") && premise2.operator.equals("~")
        && (premise1.left.equals(premise2.right) || premise1.right.equals(premise2.right));

    if (result) {
      if (premise1.left.equals(premise2.right))
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

class Resulution implements InferenceRule {
  Expression premise;

  public boolean matches(Expression exp1, Expression exp2) {
    return disjunctiveSyllogismPattern(exp1, exp2) || disjunctiveSyllogismPattern(exp2, exp1);
  }

  public Expression apply(Expression exp1, Expression exp2) {
    return premise;
  }

  private boolean resoltuionPattern(Expression premise1, Expression premise2) {
    boolean result = premise1.operator.equals("v") && premise2.operator.equals("~")
        && (premise1.left.equals(premise2.right) || premise1.right.equals(premise2.right));

    if (result) {
      if (premise1.left.equals(premise2.right))
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
    InferenceRule[] rules = {
        new ModusPonens(),
        new ModusTollens(),
        new HypotheticalSyllogism(),
        new DisjunctiveSyllogism()
    };

    Engine engine = new Engine();
    engine.addExpression(Expression.fromInput());
    engine.addExpression(Expression.fromInput());

    for (InferenceRule rule : rules)
      engine.addRule(rule);

    Expression result = engine.applyRules();

    if (result != null)
      System.out.println(result.toString() + " (" + engine.getAppliedRule().getName() + ")");
    else
      System.out.println("The input expression cannot be inferred");
  }
}