package stunningJourney;

import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.image.BufferStrategy;

/*
 * This class manages the window in which the program will be drawn.
 */
public class Window {

	private Frame fullscreenFrame;	// The fullscreen window which will only be used when the program is in fullscreen mode
	private Frame windowedFrame;	// The window which will only be used when the game is not in fullscreen mode
	private boolean type;			// Set to true if the window is a fullscreen window, otherwise it is set to false
	private DisplayMode mode;		// The display mode of the fullscreen window which consists of width, height, colour bit depth and refresh rate
	private int width, height;		// The width and height of the window in pixels (including the border, if there is one)
	private String name;			// The window title/name which will be shown in several places depending on the operating system
	private int bufferCount;		// The number of buffers used for rendering on the screen (use 2 for double buffering and 3 for triple buffering)
	private GraphicsDevice screen;	// The screen on which the fullscreen mode will be activated
	private boolean ready;			// Will be set to true if the window is able to draw, otherwise (for example when changing screens) it is set to false
	private boolean drawing;		// Will be set to true if the window is currently being drawn on, so that it doesn't change it properties
	private BufferStrategy buffer;	// The buffer which will be used for drawing
	
	/*
	 * The constructor takes the window type, display mode, width, height and name and saves them in the variables.
	 * Afterwards it creates the actual windows with these parameters, and makes the correct window visible.
	 */ 
	public Window(boolean type, DisplayMode mode, int width, int height, String name, int bufferCount, GraphicsDevice screen) {
		setReady(false);
		setDrawing(false);
		setType(type);
		setMode(mode);
		setWidth(width);
		setHeight(height);
		setName(name);
		setBufferCount(bufferCount);
		setScreen(screen);
		setFullscreenFrame(createWindow(getMode(), getName()));
		setWindowedFrame(createWindow(getWidth(), getHeight(), getName()));
		showWindow();	// The frame has to be visible when creating a buffer for it
		if (getType()) {
			setBuffer(createBuffer(getFullscreenFrame()));
		} else {
			setBuffer(createBuffer(getWindowedFrame()));
		}
		setReady(true);
	}
	
	/*
	 * This method draws the stuff it gets on the window.
	 */
	public void draw(Test test) {
		if (getReady()) {												// Checks if the frames are ready to be drawn
			setDrawing(true);											// Sets drawing to true, so that the window will not be changed during drawing
			Graphics graphics = null;									// Reserves memory for a Graphics object that will be used
	        do {
	            do {
	            	try {
		                graphics = getBuffer().getDrawGraphics();		// Gets the graphics object from the bufferStrategy
		                test.draw(graphics);							// Draws our content in the graphics object
	            	} finally {	
	            		graphics.dispose();								// Deletes the graphics object from the memory
	            	}
	            } while (getReady() && getBuffer().contentsRestored());	// If the contents lost and later restored, the drawing gets repeated
	            getBuffer().show();										// Shows the drawn graphics on the screen
	        } while (getReady() && getBuffer().contentsLost());			// If the contents were lost after trying to show it on the screen, the whole process gets repeated
	        setDrawing(false);											// Sets drawing to false, so that the window properties can be changed again
		}
	}
	
	/*
	 * This method stops the drawing of the screen, makes the window invisible and then disposes the frames.
	 */
	public void dispose() {
		boolean done = false;
		while (!done) {
			if (!getDrawing()) {
				setReady(false);
				if (getType()) {
					toggleFullscreen(getFullscreenFrame(), false);
				} else {
					getWindowedFrame().setVisible(false);
				}
				getFullscreenFrame().dispose();
				getWindowedFrame().dispose();
				done = true;
			}
		}
	}
	
	/*
	 * This method changes the type of the window to fullscreen or not fullscreen.
	 * If it already has the target type, it does nothing.
	 */
	public void changeType(boolean type) {
		if (getType() != type) {											// Checks if the target type is actually different from the current type
			boolean done = false;
			while (!done) {
				if (!getDrawing()) {
					setReady(false);										// Temporarily disables drawing to the screen
					if (type) {												// Checks if the window should be fullscreen or not
						if (toggleFullscreen(getFullscreenFrame(), true)) {	// Toggles fullscreen and checks if it was successful
							getWindowedFrame().setVisible(false);			// If it was successful, it makes the old window invisible
							setType(type);									// and adjusts the type variable accordingly
							getBuffer().dispose();							// Removes the previous buffer
							setBuffer(createBuffer(getFullscreenFrame()));	// Creates a new buffer for the fullscreen frame
						} 
					} else {
						toggleFullscreen(getFullscreenFrame(), false);		// Disables the fullscreen window,
						getWindowedFrame().setVisible(true);				// enables the normal window,
						setType(type);										// and adjusts the type variable accordingly
						getBuffer().dispose();								// Removes the previous buffer
						setBuffer(createBuffer(getWindowedFrame()));		// Creates a new buffer for the normal frame
					}
					done = true;
				}
			}
			setReady(true);													// Reenables drawing to the screen
		}
	}
	
	/*
	 * This method changes the display mode of the fullscreen window.
	 * If it already has the target mode, it does nothing.
	 * If it is currently in fullscreen mode, it checks if display mode change is supported
	 * by the screen device, and then changes it.
	 * If it is not in fullscreen mode, it just saves the new display mode in the mode variable.
	 */
	public void changeDisplayMode(DisplayMode mode) {
		if (!getMode().equals(mode)) {									// Checks if the target mode is actually different from the current mode
			if (getType()) {											// Checks if the window is currently in fullscreen mode
				boolean done = false;
				while (!done) {
					if (!getDrawing()) {
						setReady(false);								// Temporarily disables drawing to the screen
						if (getScreen().isDisplayChangeSupported()) {	// If it is, it checks if the screen device supports display mode change
							getScreen().setDisplayMode(mode);			// If it does, it changes the display mode to the new mode
							setMode(mode);								// and saves it in the mode variable
						} else {										// If display mode change is not supported, it prints an error message
							System.out.println("Display mode change is not supported on this device!");
						}
						done = true;
					}
				}
				setReady(true);											// Reenables drawing to the screen
			} else {													// If the window is not in fullscreen mode
				setMode(mode);											// It just saves the new display mode in the mode variable
			}
		}
	}
	
	/*
	 * This method changes the size of the normal window to the width and height it was given.
	 */
	public void changeSize(int width, int height) {
		setWidth(width);
		setHeight(height);
		getWindowedFrame().setSize(getWidth(), getHeight());	// Changes the size of the normal window to the new values
	}
	
	/*
	 * This method changes the window title/name of both frames to the name it was given.
	 */
	public void changeName(String name) {
		setName(name);
		getWindowedFrame().setTitle(getName());	
		getFullscreenFrame().setTitle(getName());
	}
	
	/*
	 * This method changes the number of buffers which will be used for drawing.
	 * If the numbers of buffers is already the same as the given number, it does nothing.
	 */
	public void changeBufferCount(int bufferCount) {
		if (getBufferCount() != bufferCount) {							// Checks if the new number is actually different from the old one
			boolean done = false;
			while (!done) {
				if (!getDrawing()) {
					setReady(false);									// Temporarily disables drawing to the screen
					getBuffer().dispose();								// Removes the previous buffer
					setBufferCount(bufferCount);						// Adjusts the bufferCount variable to the new value
					if (getType()) {									// Checks if the current window is a fullscreen window
						setBuffer(createBuffer(getFullscreenFrame()));	// If it is, it creates a new bufferStrategy for the fullscreen frame
					} else {
						setBuffer(createBuffer(getWindowedFrame()));	// If it is not, it creates a new bufferStrategy for the normal frame
					}
					done = true;
				}
			}
			setReady(true);												// Reenables drawing to the screen
		}
	}
	
	/*
	 * This method changes the screen device on which the fullscreen window will be shown.
	 * If it will already be shown on the screen it was given, it does nothing.
	 */
	public void changeScreen(GraphicsDevice screen) {
		if (!getScreen().equals(screen)) {											// Checks if the target screen is actually different from the current screen
			if (getType()) {														// Checks if the window is currently in fullscreen
				boolean done = false;
				while (!done) {
					if (!getDrawing()) {
						setReady(false);											// Temporarily disables drawing to the screen
						if (screen.isFullScreenSupported()) {						// Checks if the target screen supports fullscreen exclusive mode
							getScreen().setFullScreenWindow(null);					// If yes, it disables fullscreen on the old screen
							getFullscreenFrame().setVisible(false);					// and makes the fullscreen window invisible
							setScreen(screen);										// Sets the screen variable to the new screen 
							getScreen().setFullScreenWindow(getFullscreenFrame());	// And activates fullscreen on the new screen
							if (getScreen().isDisplayChangeSupported()) {			// Checks if display mode change is supported
								getScreen().setDisplayMode(getMode());				// If it is, it enters the display mode saved in the mode variable
							} else {												// If it is not, it prints an error message
								System.out.println("Display mode change is not supported on this device!");
							}
						} else {													// If fullscreen is not supported, it prints an error message
							System.out.println("Fullscreen is not supported on this device!");
						}
						done = true;
					}
				}
				setReady(true);														// Reenables drawing to the screen
			} else {																// If the window is not in fullscreen, it just adjusts the screen variable
				setScreen(screen);
			}
		}
	}
	
	/*
	 * This method makes either the fullscreen or the normal frame visible depending on the type variable.
	 */
	private void showWindow() {
		if (getType()) {											// Checks if the fullscreen window should become visible
			if (!toggleFullscreen(getFullscreenFrame(), true)) {	// Tries to toggle the fullscreen mode and checks if it was successful
				getWindowedFrame().setVisible(true);				// If it was not, it shows the normal frame instead
				setType(false);										// and changes the type variable accordingly
			}
		} else {
			getWindowedFrame().setVisible(true);					// If the normal window should become visible, it does exactly that
		}
	}
	
	/*
	 * This method takes the frame it was given and checks if it is possible to toggle fullscreen.
	 * It then either toggles fullscreen on or off, and returns the new window type.
	 * It returns true if the window is now a fullscreen window, and otherwise it returns false.
	 */
	private boolean toggleFullscreen(Frame frame, boolean fullscreen) {
		if (fullscreen) {										// Checks if fullscreen should be toggled on or off
			if (getScreen().isFullScreenSupported()) {			// Checks if fullscreen is supported on the target screen
				getScreen().setFullScreenWindow(frame);			// If it is supported, it enters fullscreen exclusive mode
				if (getScreen().isDisplayChangeSupported()) {	// Checks if display mode change is supported
					getScreen().setDisplayMode(getMode());		// If it is, it enters the display mode saved in the mode variable
				} else {										// If it is not, it prints an error message
					System.out.println("Display mode change is not supported on this device!");
				}
				return true;									// Then it returns true because it entered fullscreen
			} else {											// If fullscreen is not supported, it prints an error message and returns false
				System.out.println("Fullscreen is not supported on this device!");
				return false;
			}
		} else {												// If fullscreen should be toggled off
			getScreen().setFullScreenWindow(null);				// it exits the fullscreen exclusive mode
			getFullscreenFrame().setVisible(false);				// and makes the fullscreen window invisible
			return false;										// and returns false, because it is not fullscreen anymore
		}
	}
	
	/*
	 * This method creates a bufferStrategy for the given frame in order to use double or triple buffering.
	 * The choice of double or triple buffering is made according to the bufferCount variable.
	 */
	private BufferStrategy createBuffer(Frame frame) {
		frame.createBufferStrategy(getBufferCount());	// Creates bufferStrategy with number of buffers according to bufferCount
		return frame.getBufferStrategy();				// Returns the bufferStrategy it created
	}
	
	/*
	 * This method creates a window with the width, height and name it was given and then returns it.
	 */
	private Frame createWindow(int width, int height, String name) {
		Frame newFrame = new Frame(name);
		newFrame.setSize(width, height);
		newFrame.setLocationRelativeTo(null);	// Places the window exactly in the middle of the screen
		newFrame.setIgnoreRepaint(true);		// Ignores the repaint calls from outside of the program
		return newFrame;
	}
	
	/*
	 * This method creates a fullscreen window with the mode and name it was given and then returns it.
	 */
	private Frame createWindow(DisplayMode mode, String name) {
		Frame newFrame = new Frame(name);
		newFrame.setUndecorated(true);		// Removes the border and title bar of the window for fullscreen mode
		newFrame.setResizable(false);
		newFrame.setIgnoreRepaint(true);	// Ignores the repaint calls from outside of the program
		return newFrame;
	}
	
	/*
	 * This method returns the content of the fullscreenFrame variable.
	 */
	private Frame getFullscreenFrame() {
		return fullscreenFrame;
	}
	
	/*
	 * This method sets the fullscreenFrame variable to the passed frame.
	 */
	private void setFullscreenFrame(Frame frame) {
		this.fullscreenFrame = frame;
	}
	
	/*
	 * This method returns the content of the windowedFrame variable.
	 */
	private Frame getWindowedFrame() {
		return windowedFrame;
	}
	
	/*
	 * This method sets the windowedFrame variable to the passed frame.
	 */
	private void setWindowedFrame(Frame frame) {
		this.windowedFrame = frame;
	}
	
	/*
	 * This method returns the content of the type variable.
	 */
	private boolean getType() {
		return type;
	}
	
	/*
	 * This method sets the type variable to the passed boolean value.
	 */
	private void setType(boolean type) {
		this.type = type;
	}
	
	/*
	 * This method returns the content of the mode variable.
	 */
	private DisplayMode getMode() {
		return mode;
	}
	
	/*
	 * This method sets the mode variable to the passed display mode.
	 */
	private void setMode(DisplayMode mode) {
		this.mode = mode;
	}
	
	/*
	 * This method returns the content of the width variable.
	 */
	private int getWidth() {
		return width;
	}
	
	/*
	 * This method sets the width variable to the passed number.
	 */
	private void setWidth(int width) {
		this.width = width;
	}
	
	/*
	 * This method returns the content of the height variable.
	 */
	private int getHeight() {
		return height;
	}
	
	/*
	 * This method sets the height variable to the passed number.
	 */
	private void setHeight(int height) {
		this.height = height;
	}
	
	/*
	 * This method returns the content of the name variable.
	 */
	private String getName() {
		return name;
	}
	
	/*
	 * This method sets the name variable to the passed string.
	 */
	private void setName(String name) {
		this.name = name;
	}
	
	/*
	 * This method returns the content of the bufferCount variable.
	 */
	private int getBufferCount() {
		return bufferCount;
	}
	
	/*
	 * This method sets the bufferCount variable to the passed number.
	 */
	private void setBufferCount(int bufferCount) {
		this.bufferCount = bufferCount;
	}
	
	/*
	 * This method returns the content of the screen variable.
	 */
	private GraphicsDevice getScreen() {
		return screen;
	}
	
	/*
	 * This method sets the screen variable to the passed graphics device.
	 */
	private void setScreen(GraphicsDevice screen) {
		this.screen = screen;
	}
	
	/*
	 * This method returns the content of the ready variable.
	 */
	private boolean getReady() {
		return ready;
	}
	
	/*
	 * This method sets the ready variable to the passed boolean.
	 */
	private void setReady(boolean ready) {
		this.ready = ready;
	}
	
	/*
	 * This method returns the content of the drawing variable.
	 */
	private boolean getDrawing() {
		return drawing;
	}
	
	/*
	 * This method sets the drawing variable to the passed boolean.
	 */
	private void setDrawing(boolean drawing) {
		this.drawing = drawing;
	}
	
	/*
	 * This method returns the content of the buffer variable.
	 */
	private BufferStrategy getBuffer() {
		return buffer;
	}
	
	/*
	 * This method sets the buffer variable to the passed buffer strategy.
	 */
	private void setBuffer(BufferStrategy buffer) {
		this.buffer = buffer;
	}
}
