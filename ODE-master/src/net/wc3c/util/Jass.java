package net.wc3c.util;

public abstract class Jass {
    private Jass() {
    }
    
    private static final String        LS          = System.getProperty("line.separator");
    
    private static final String        INDENT      = "    ";
    private static final StringBuilder indentation = new StringBuilder();
    
    private static final void increaseIndentation() {
        indentation.append(INDENT);
    }
    
    private static final void decreaseIndentation() {
        indentation.delete(indentation.length() - INDENT.length(), indentation.length());
    }
    
    private static final String indentation() {
        return indentation.toString();
    }
    
    public static final String function(final String name, final Type returnType, final Parameter... params) {
        return function(Visibility.DEFAULT, name, returnType, params);
    }
    
    public static final String function(final Visibility vis, final String name, final Type returnType,
            final Parameter... params) {
        final StringBuilder result = new StringBuilder();
        result.append(indentation() + vis.getValue() + " function " + name + " takes ");
        
        if (params.length > 0) {
            for (final Parameter param : params) {
                result.append(param.type.getValue() + " " + param.name + ",");
            }
            result.deleteCharAt(result.length() - 1);
        } else {
            result.append("nothing");
        }
        
        result.append(" returns " + returnType.getValue());
        result.append(LS);
        
        increaseIndentation();
        
        return result.toString();
    }
    
    public static final String endfunction() {
        decreaseIndentation();
        return indentation() + "endfunction" + LS;
    }
    
    public static final String library(final String name) {
        final String result = indentation() + "library " + name + LS;
        increaseIndentation();
        return result;
    }
    
    public static final String library(final String name, final String initializerName) {
        final String result = indentation() + "library " + name + " initializer " + initializerName + LS;
        increaseIndentation();
        return result;
    }
    
    public static final String endlibrary() {
        decreaseIndentation();
        return indentation() + "endlibrary" + LS;
    }
    
    public static final String ifBranch(final String condition) {
        final String result = indentation() + "if " + condition + " then" + LS;
        increaseIndentation();
        return result;
    }
    
    public static final String elseBranch() {
        decreaseIndentation();
        final String result = indentation() + "else" + LS;
        increaseIndentation();
        return result;
    }
    
    public static final String elseifBranch(final String condition) {
        decreaseIndentation();
        final String result = indentation() + "elseif " + condition + " then" + LS;
        increaseIndentation();
        return result;
    }
    
    public static final String endBranch() {
        decreaseIndentation();
        return indentation() + "endif" + LS;
    }
    
    public static final String call(final String functionName, final String... params) {
        final StringBuilder result = new StringBuilder();
        
        result.append(indentation() + "call " + functionName + "(");
        
        if (params.length > 0) {
            for (final String param : params) {
                result.append(param + ",");
            }
            result.setCharAt(result.length() - 1, ')');
        } else {
            result.append(")");
        }
        
        result.append(LS);
        
        return result.toString();
    }
    
    public static final String execute(final String functionName) {
        return Jass.call("ExecuteFunc", functionName);
    }
    
    public static final String ret() {
        return indentation() + "return" + LS;
    }
    
    public static final String ret(final String value) {
        return indentation() + "return " + value + LS;
    }
    
    public static final String set(final String varName, final String value, final String... dimensions) {
        final StringBuilder result = new StringBuilder();
        
        result.append(indentation() + "set ");
        result.append(varName);
        
        for (final String dimension : dimensions) {
            result.append("[" + dimension + "]");
        }
        
        result.append("=" + value);
        
        result.append(LS);
        return result.toString();
    }
    
    public static final String globals() {
        final String result = indentation() + "globals" + LS;
        increaseIndentation();
        return result;
    }
    
    public static final String globalVar(final Visibility vis, final Type type, final boolean array, final String name) {
        return indentation() + vis.getValue() + " " + type.getValue() + " " + (array ? "array " : "") + name + LS;
    }
    
    public static final String globalVar(final Visibility vis, final Type type, final String name,
            final String defaultValue) {
        return indentation() + vis.getValue() + " " + type.getValue() + " " + name + "=" + defaultValue + LS;
    }
    
    public static final String endglobals() {
        decreaseIndentation();
        return indentation() + "endglobals" + LS;
    }
    
    public static final class Parameter {
        Type   type;
        String name;
        
        public Parameter(final Type type, final String name) {
            this.type = type;
            this.name = name;
        }
    }
    
    public static enum Type {
        INTEGER("integer"),
        REAL("real"),
        BOOLEAN("boolean"),
        STRING("string"),
        HANDLE("handle"),
        NOTHING("nothing"),
        CODE("code"),
        HASHTABLE("hashtable");
        
        String value;
        
        private Type(final String value) {
            this.value = value;
        }
        
        public final String getValue() {
            return value;
        }
    }
    
    public static enum Visibility {
        PRIVATE("private"),
        PUBLIC("public"),
        DEFAULT(""),
        READONLY("readonly");
        
        String value;
        
        private Visibility(final String value) {
            this.value = value;
        }
        
        public final String getValue() {
            return value;
        }
    }
}
