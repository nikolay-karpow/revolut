package com.revolut.accounts.core;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MoneyTest {
    @Test
    public void plus() {
        Money original = new Money(567);
        Money increased = original.plus(new Money(80));
        assertThat(increased).isEqualTo(new Money(647));
    }

    @Test
    public void minus() {
        Money original = new Money(800);
        Money decreased = original.minus(new Money(230));
        assertThat(decreased).isEqualTo(new Money(570));
    }

    @Test
    public void isLessThan() {
        Money a = new Money(504);
        Money b = new Money(300);
        Money c = new Money(504);

        assertThat(a.isLessThan(a)).isFalse();
        assertThat(a.isLessThan(b)).isFalse();
        assertThat(a.isLessThan(c)).isFalse();
        assertThat(b.isLessThan(a)).isTrue();
    }

    @Test
    public void isNegative() {
        assertThat(new Money(0).isNegative()).isFalse();
        assertThat(new Money(10).isNegative()).isFalse();
        assertThat(new Money(-10).isNegative()).isTrue();
    }
}
