package ru.axefu.jover.intellij;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoFilter;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiBinaryExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JoverHighlightInfoFilter implements HighlightInfoFilter {

    @Override
    public boolean accept(@NotNull HighlightInfo info, @Nullable PsiFile file) {
        if (file == null || info.getSeverity() != HighlightSeverity.ERROR) {
            return true;
        }

        int start = info.getStartOffset();
        int end = info.getEndOffset();

        if (start < 0 || end < start || end > file.getTextLength()) {
            return true;
        }

        PsiElement element = file.findElementAt(start);
        if (element == null) {
            return true;
        }

        PsiBinaryExpression expression = PsiTreeUtil.getParentOfType(element, PsiBinaryExpression.class, false);

        if (expression == null) {
            return true;
        }

        PsiJavaToken operationSign = expression.getOperationSign();
        TextRange highlightedRange = TextRange.create(start, end);
        if (!highlightedRange.intersects(operationSign.getTextRange())) {
            return true;
        }

        /*
         * Suppress only the Java operator error when Jover can resolve
         * the expression to a real instance method.
         */
        return !JoverOperatorService.isJoverOperator(expression);
    }
}
