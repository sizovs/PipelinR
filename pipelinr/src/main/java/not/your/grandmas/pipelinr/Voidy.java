package not.your.grandmas.pipelinr;

public class Voidy {

    @Override
    public String toString() {
        return Voidy.class.getSimpleName();
    }

    @Override
    public int hashCode() {
        return Voidy.class.getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Voidy) {
            Voidy other = (Voidy) obj;
            return other.hashCode() == this.hashCode();
        }
        return false;
    }
}
