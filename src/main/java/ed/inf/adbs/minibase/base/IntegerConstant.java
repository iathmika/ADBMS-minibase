package ed.inf.adbs.minibase.base;

import java.util.Objects;

public class IntegerConstant extends Constant implements Comparable<IntegerConstant>{
    private Integer value;

    public IntegerConstant(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof IntegerConstant)) return false;
        return (this.value).equals(((IntegerConstant) obj).getValue());
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }

    @Override
    public int compareTo(IntegerConstant integerConstant) {
        return this.getValue().compareTo(integerConstant.getValue());
    }
}
