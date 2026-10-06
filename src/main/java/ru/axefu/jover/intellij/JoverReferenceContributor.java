package ru.axefu.jover.intellij;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiBinaryExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JoverReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(PsiBinaryExpression.class),
                new PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(
                            @NotNull PsiElement element,
                            @NotNull ProcessingContext context
                    ) {
                        if (!(element instanceof PsiBinaryExpression)) {
                            return PsiReference.EMPTY_ARRAY;
                        }

                        PsiBinaryExpression expression = (PsiBinaryExpression) element;
                        if (JoverOperatorService.resolveOperatorMethod(expression) == null) {
                            return PsiReference.EMPTY_ARRAY;
                        }

                        return new PsiReference[]{
                                new JoverOperatorReference(expression)
                        };
                    }
                }
        );
    }

    private static final class JoverOperatorReference
            extends PsiReferenceBase<PsiBinaryExpression> {

        JoverOperatorReference(@NotNull PsiBinaryExpression expression) {
            super(expression, expression.getOperationSign().getTextRangeInParent(), false);
        }

        @Override
        public @Nullable PsiElement resolve() {
            return JoverOperatorService.resolveOperatorMethod(getElement());
        }

        @Override
        public boolean isReferenceTo(@NotNull PsiElement element) {
            PsiElement resolved = resolve();
            return resolved != null && resolved.isEquivalentTo(element);
        }

        @Override
        public @NotNull Object[] getVariants() {
            return new Object[0];
        }
    }
}
