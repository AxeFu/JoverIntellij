package ru.axefu.jover.intellij;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiAssignmentExpressionImpl;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JoverOperatorService {
    private JoverOperatorService() {
    }

    @Nullable
    public static PsiMethod resolveOperatorMethod(@NotNull PsiBinaryExpression expression) {
        JoverOperator operator = JoverOperator.fromToken(expression.getOperationTokenType());
        PsiExpression left = expression.getLOperand();
        PsiExpression right = expression.getROperand();
        if (operator == null || left == null || right == null) return null;

        PsiType leftType = getType(left);
        PsiType rightType = getType(right);

        return resolve(leftType, operator, rightType, expression);
    }

    public static PsiMethod resolveOperatorMethod(PsiAssignmentExpression expression) {
        JoverOperator operator = JoverOperator.fromToken(expression.getOperationTokenType());
        PsiExpression left = expression.getLExpression();
        PsiExpression right = expression.getRExpression();
        if (operator == null || left == null || right == null) return null;

        PsiType leftType = getType(left);
        PsiType rightType = getType(right);

        return resolve(leftType, operator, rightType, expression);
    }

    public static PsiMethod resolveOperatorMethod(PsiPolyadicExpression expression) {
        PsiExpression[] operands = expression.getOperands();
        if (operands.length < 2) return null;
        PsiExpression left, right = operands[0];
        PsiMethod result = null;
        for (int i = 1; i < operands.length; i++) {
            left = right;
            right = operands[i];

            PsiJavaToken token = expression.getTokenBeforeOperand(right);
            if (token == null) return null;

            JoverOperator operator = JoverOperator.fromToken(token.getTokenType());
            if (operator == null) return null;

            PsiType leftType = result == null ? getType(left) : result.getReturnType();
            PsiType rightType = getType(right);

            result = resolve(leftType, operator, rightType, expression);
            if (result == null) return null;
        }
        return result;
    }

    public static PsiType getType(PsiExpression expression) {
        if (expression instanceof PsiBinaryExpression binary) {
            PsiMethod method = resolveOperatorMethod(binary);
            if (method != null) {
                return method.getReturnType();
            }
        }
        if (expression instanceof PsiPolyadicExpression polyadic) {
            PsiMethod method = resolveOperatorMethod(polyadic);
            if (method != null) {
                return method.getReturnType();
            }
        }
        if (expression instanceof PsiAssignmentExpression assignment) {
            PsiMethod method = resolveOperatorMethod(assignment);
            if (method != null) {
                return method.getReturnType();
            }
        }
        return expression.getType();
    }

    private static PsiMethod resolve(PsiType leftType, JoverOperator operator, PsiType rightType, PsiExpression expression) {
        if (leftType == null
                || leftType instanceof PsiPrimitiveType
                || leftType.equalsToText("java.lang.String")
                || rightType == null)
            return null;

        PsiClass clazz = PsiUtil.resolveClassInClassTypeOnly(leftType);
        if (clazz == null) return null;

        for (PsiMethod method : clazz.findMethodsByName(operator.getMethodName(), true)) {
            PsiParameter[] parameters = method.getParameterList().getParameters();
            if (!isOperatorMethod(method)) continue;

            PsiType parameterType = parameters[0].getType();
            if (parameterType.isAssignableFrom(rightType) && PsiUtil.isAccessible(method, expression, clazz)) {
                return method;
            }
        }
        return null;
    }

    public static boolean isJoverOperator(@NotNull PsiPolyadicExpression expression) {
        return resolveOperatorMethod(expression) != null;
    }

    public static boolean isJoverOperator(@NotNull PsiBinaryExpression expression) {
        return resolveOperatorMethod(expression) != null;
    }

    public static boolean isJoverOperator(@NotNull PsiAssignmentExpression expression) {
        return resolveOperatorMethod(expression) != null;
    }

    public static boolean isOperatorMethod(@NotNull PsiMethod method) {
        return JoverOperator.fromMethodName(method.getName()) != null
                && !method.hasModifierProperty(PsiModifier.STATIC)
                && method.getParameterList().getParametersCount() == 1;
    }
}
