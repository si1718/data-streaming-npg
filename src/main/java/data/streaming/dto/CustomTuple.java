package data.streaming.dto;

public class CustomTuple<T, Z> {

    private T left;
    private Z right;

    public CustomTuple(T left, Z right) {
        this.left = left;
        this.right = right;
    }

    public T getLeft() {
        return left;
    }

    public void setLeft(T left) {
        this.left = left;
    }

    public Z getRight() {
        return right;
    }

    public void setRight(Z right) {
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomTuple<?, ?> customTriple = (CustomTuple<?, ?>) o;

        if (left != null ? !left.equals(customTriple.left) : customTriple.left != null) return false;
        return right != null ? right.equals(customTriple.right) : customTriple.right == null;
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }
}
