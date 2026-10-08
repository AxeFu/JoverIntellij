package ru.axefu.jover.intellij;

import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

public final class JoverImplicitUsageProvider implements ImplicitUsageProvider {

    @Override
    public boolean isImplicitUsage(@NotNull PsiElement element) {
        return Jover.isAvailable(element.getProject()) && isReferencedByAlternativeNames(element);
    }

    @Override
    public boolean isImplicitRead(@NotNull PsiElement element) {
        return false;
    }

    @Override
    public boolean isImplicitWrite(@NotNull PsiElement element) {
        return false;
    }

    public boolean isReferencedByAlternativeNames(@NotNull PsiElement element) {
        return element instanceof PsiMethod
                && JoverOperatorService.isOperatorMethod((PsiMethod) element);
    }
}
