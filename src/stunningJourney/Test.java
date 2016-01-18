package stunningJourney;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

public class Test {
	
	public Test () {
		test();
	}

	public void draw(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.drawLine(50, 50, 200, 50);
	}
	
	private void test() {
		GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		Window window = new Window(false, device.getDisplayMode(), 800, 600, "Hier isn titel", 3, device);
		window.changeBufferCount(2);
		while (true) {
			window.draw(this);
		}
		
	}
	
	public static void main(String[] args) {
		new Test();
	}
}

