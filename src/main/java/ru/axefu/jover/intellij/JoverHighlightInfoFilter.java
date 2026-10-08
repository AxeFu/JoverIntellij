package ru.axefu.jover.intellij;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoFilter;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiAssignmentExpressionImpl;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JoverHighlightInfoFilter implements HighlightInfoFilter {

    @Override
    public boolean accept(@NotNull HighlightInfo info, @Nullable PsiFile file) {
        if (file == null || info.getSeverity() != HighlightSeverity.ERROR) {
            return true;
        }

        PsiElement element = file.findElementAt(info.getStartOffset());
        PsiExpression expression;
        if ((expression = PsiTreeUtil.getParentOfType(element, PsiBinaryExpression.class, false)) != null) {
            PsiBinaryExpression binary = (PsiBinaryExpression) expression;
            return !JoverOperatorService.isJoverOperator(binary);
        }

        if ((expression = PsiTreeUtil.getParentOfType(element, PsiPolyadicExpression.class, false)) != null) {
            PsiPolyadicExpression polyadic = (PsiPolyadicExpression) expression;
            return !JoverOperatorService.isJoverOperator(polyadic);
        }

        if ((expression = PsiTreeUtil.getParentOfType(element, PsiAssignmentExpressionImpl.class, false)) != null) {
            PsiAssignmentExpressionImpl assignment = (PsiAssignmentExpressionImpl) expression;
            return !JoverOperatorService.isJoverOperator(assignment);
        }

        return true;
    }
}
