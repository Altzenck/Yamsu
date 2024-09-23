package me.altzenck;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Void {

    public static final Void VALUE = new Void();
}
