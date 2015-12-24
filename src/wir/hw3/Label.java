package wir.hw3;

public class Label implements Comparable<Label> {
    public String name;
    public int count;

    public Label(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Label other) {
        return other.count - this.count;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && name.equals(((Label) o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
