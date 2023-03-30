package ed.inf.adbs.minibase.base;

import java.util.Objects;

public class StringConstant extends Constant implements Comparable<StringConstant>{
    private String value;

    public StringConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "'" + value + "'";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof StringConstant)) return false;
        return (this.value).equals(((StringConstant) obj).getValue());
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }

    @Override
    public int compareTo(StringConstant stringConstant) {
        return this.value.compareTo(stringConstant.getValue());
    }
}