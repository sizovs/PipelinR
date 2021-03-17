package an.awesome.pipelinr;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class VoidyTest {

  @Test
  public void equalsToAnyOtherVoidyButNotDifferentObjects() {
    // given
    Voidy anObject = new Voidy();
    Voidy otherVoidy = new Voidy();

    Object differentObject = new Object();

    // then
    assertThat(anObject).isEqualTo(otherVoidy);
    assertThat(anObject.hashCode()).isEqualTo(otherVoidy.hashCode());

    // and
    assertThat(anObject).isNotEqualTo(differentObject);
    assertThat(anObject.hashCode()).isNotEqualTo(differentObject.hashCode());
  }

  @Test
  public void hasANiceToString() {
    assertThat(new Voidy()).hasToString("Voidy");
  }
}
