package me.anon.grow3.util

public operator fun StringBuilder.plusAssign(string: String) { this.append(string) }
public operator fun StringBuilder.plusAssign(char: Char) { this.append(char) }
public operator fun StringBuilder.plusAssign(long: Long) { this.append(long) }
public operator fun StringBuilder.plusAssign(int: Int) { this.append(int) }
public operator fun StringBuilder.plusAssign(double: Double) { this.append(double) }
