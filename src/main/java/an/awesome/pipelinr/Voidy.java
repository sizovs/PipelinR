package an.awesome.pipelinr;

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
        return obj instanceof Voidy;
    }
}
