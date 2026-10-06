package ru.axefu.jover.intellij;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiBinaryExpression;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypes;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.util.TypeConversionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JoverOperatorService {
    private JoverOperatorService() {
    }

    @Nullable
    public static JoverOperator getOperator(@NotNull PsiBinaryExpression expression) {
        return JoverOperator.fromToken(expression.getOperationTokenType());
    }

    /**
     * Matches the behavior of Jover's annotation processor:
     * - only declared/reference-like left operand types are transformed;
     * - java.lang.String is excluded because Java owns String concatenation;
     * - only instance methods are candidates;
     * - exactly one argument is passed.
     */
    @Nullable
    public static PsiMethod resolveOperatorMethod(@NotNull PsiBinaryExpression expression) {
        JoverOperator operator = getOperator(expression);
        if (operator == null) {
            return null;
        }

        PsiExpression left = expression.getLOperand();
        PsiExpression right = expression.getROperand();

        if (left == null || right == null) {
            return null;
        }

        PsiType leftType = getJoverExpressionType(left);
        PsiType rightType = getJoverExpressionType(right);

        if (!(leftType instanceof PsiClassType)) {
            return null;
        }

        if (leftType.equalsToText("java.lang.String")) {
            return null;
        }

        if (rightType == null) {
            return null;
        }

        PsiClassType.ClassResolveResult classResult = ((PsiClassType) leftType).resolveGenerics();
        PsiClass leftClass = classResult.getElement();
        if (leftClass == null) {
            return null;
        }

        PsiSubstitutor classSubstitutor = classResult.getSubstitutor();

        PsiMethod best = null;
        int bestScore = Integer.MIN_VALUE;

        for (PsiMethod method : leftClass.getAllMethods()) {
            if (!operator.getMethodName().equals(method.getName())) {
                continue;
            }
            if (method.hasModifierProperty(PsiModifier.STATIC)) {
                continue;
            }
            if (!isAccessible(method, expression, leftClass)) {
                continue;
            }

            PsiParameter[] parameters = method.getParameterList().getParameters();
            if (parameters.length != 1) {
                if (!(method.isVarArgs() && parameters.length == 1)) {
                    continue;
                }
            }

            PsiType parameterType = classSubstitutor.substitute(parameters[0].getType());
            if (parameterType == null) {
                parameterType = parameters[0].getType();
            }

            if (method.isVarArgs() && parameterType instanceof com.intellij.psi.PsiEllipsisType) {
                parameterType = ((com.intellij.psi.PsiEllipsisType) parameterType).getComponentType();
            }

            if (!TypeConversionUtil.areTypesConvertible(rightType, parameterType)) {
                continue;
            }

            int score = score(rightType, parameterType);
            if (score > bestScore) {
                best = method;
                bestScore = score;
            }
        }

        return best;
    }

    public static PsiType getJoverExpressionType(@NotNull PsiExpression expression) {
        if (expression instanceof PsiBinaryExpression) {
            PsiBinaryExpression binary = (PsiBinaryExpression) expression;

            PsiMethod operatorMethod = resolveOperatorMethod(binary);
            if (operatorMethod != null) {
                return operatorMethod.getReturnType();
            }
        }

        return expression.getType();
    }

    public static boolean isJoverOperator(@NotNull PsiBinaryExpression expression) {
        return resolveOperatorMethod(expression) != null;
    }

    public static boolean isOperatorMethod(@NotNull PsiMethod method) {
        return JoverOperator.fromMethodName(method.getName()) != null
                && !method.hasModifierProperty(PsiModifier.STATIC)
                && method.getParameterList().getParametersCount() == 1;
    }

    private static boolean isAccessible(
            @NotNull PsiMethod method,
            @NotNull PsiBinaryExpression expression,
            @NotNull PsiClass receiverClass
    ) {
        return PsiUtil.isAccessible(method, expression, receiverClass);
    }

    private static int score(@NotNull PsiType argument, @NotNull PsiType parameter) {
        if (argument.equals(parameter)) {
            return 1000;
        }
        if (TypeConversionUtil.isAssignable(parameter, argument)) {
            return 500;
        }
        return 100;
    }
}
