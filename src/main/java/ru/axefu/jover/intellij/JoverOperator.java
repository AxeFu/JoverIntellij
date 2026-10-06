package ru.axefu.jover.intellij;

import com.intellij.psi.JavaTokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Nullable;

public enum JoverOperator {
    ADD(JavaTokenType.PLUS, "add"),
    SUBTRACT(JavaTokenType.MINUS, "subtract"),
    MULTIPLY(JavaTokenType.ASTERISK, "multiply"),
    DIVIDE(JavaTokenType.DIV, "divide");

    private final IElementType tokenType;
    private final String methodName;

    JoverOperator(IElementType tokenType, String methodName) {
        this.tokenType = tokenType;
        this.methodName = methodName;
    }

    public IElementType getTokenType() {
        return tokenType;
    }

    public String getMethodName() {
        return methodName;
    }

    @Nullable
    public static JoverOperator fromToken(IElementType tokenType) {
        for (JoverOperator operator : values()) {
            if (operator.tokenType == tokenType) {
                return operator;
            }
        }
        return null;
    }

    @Nullable
    public static JoverOperator fromMethodName(String name) {
        for (JoverOperator operator : values()) {
            if (operator.methodName.equals(name)) {
                return operator;
            }
        }
        return null;
    }
}
