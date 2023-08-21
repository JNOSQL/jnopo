package br.org.soujava.coffewithjava.hello;

public class Hello {

	private final String name;
	
	public Hello(String name) {
        this.name = name;
	}

	public String getHello(){
		return name;
	}
}