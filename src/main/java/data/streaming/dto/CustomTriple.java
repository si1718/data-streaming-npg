package data.streaming.dto;

public class CustomTriple<T, U, Z> {

    private T left;
    private U middle;
    private Z right;

    public CustomTriple(T left, U middle, Z right) {
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    public T getLeft() {
        return left;
    }

    public void setLeft(T left) {
        this.left = left;
    }

    public U getMiddle() {
        return middle;
    }

    public void setMiddle(U middle) {
        this.middle = middle;
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

        CustomTriple<?, ?, ?> customTriple = (CustomTriple<?, ?, ?>) o;

        if (left != null ? !left.equals(customTriple.left) : customTriple.left != null) return false;
        if (middle != null ? !middle.equals(customTriple.middle) : customTriple.middle != null) return false;
        return right != null ? right.equals(customTriple.right) : customTriple.right == null;
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (middle != null ? middle.hashCode() : 0);
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }
}
