package ru.axefu.jover.intellij;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.search.GlobalSearchScope;

public class Jover {
    private static final String REQUIRED_CLASS = "ru.axefu.jover.annotation.processor.OperatorProcessor";

    private Jover() {}

    public static boolean isAvailable(Project project) {
        return JavaPsiFacade.getInstance(project).findClass(REQUIRED_CLASS, GlobalSearchScope.allScope(project)) != null;
    }
}
