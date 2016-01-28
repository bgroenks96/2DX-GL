package bg.x2d.utils;


public final class Arguments {

    public static void isNotNull(Object obj, String argName) {
        if (obj == null) throw new IllegalArgumentException("null argument: " + argName);
    }

    public static void isNull(Object obj, String argName) {
        if (obj != null) throw new IllegalArgumentException("null argument: " + argName);
    }

    public static <T> void areEqual(T t0, T t1, String arg0Name, String arg1Name) {
        if (!t0.equals(t1)) throw new IllegalArgumentException("argument '"+arg0Name+"' does not match '"+arg1Name+"'");
    }

    public static <T> void areNotEqual(T t0, T t1, String arg0Name, String arg1Name) {
        if (t0.equals(t1)) throw new IllegalArgumentException("argument '"+arg0Name+"' does not match '"+arg1Name+"'");
    }

    private Arguments() {
    }
}
