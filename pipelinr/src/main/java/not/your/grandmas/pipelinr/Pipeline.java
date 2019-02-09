package not.your.grandmas.pipelinr;

public interface Pipeline {

    <R, C extends Command<R>> R send(C command);


}
