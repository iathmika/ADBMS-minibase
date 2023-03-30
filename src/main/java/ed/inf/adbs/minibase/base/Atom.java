package ed.inf.adbs.minibase.base;

public class Atom {
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        return object != null && getClass() == object.getClass();
    }

}
