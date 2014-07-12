package com.netx.ut.lib.external;
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;
import com.netx.generics.R1.util.UnitTester;
import com.netx.basic.R1.io.FileSystemException;


public class NTJai extends UnitTester {

	public static void main(String[] args) throws Throwable {
		NTJai nt = new NTJai();
		nt.testSimpleImage();
		//nt.testImageOperationsApplet();
	}

	public void testSimpleImage() throws FileSystemException {
		Window window = new Window();
		window.setTitle("Image");
		JPanel cPane = new JPanel();
		ImageIcon i = new ImageIcon(_getPath("flower.jpg"));
		cPane.add(new JLabel(i));
		window.setContentPane(cPane);
		window.pack();
		window.setVisible(true);
	}

	// TODO this needs to run on a browser
	public void testImageOperationsApplet() throws FileSystemException {
		JFrame f = new JFrame("ImageOps");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {System.exit(0);}
		});
		//String s[] = { "bld.jpg", "bld.jpg", "boat.gif", "boat.gif"};
		String images[] = { _getPath("flower.jpg"), _getPath("flower.jpg"), _getPath("flower.jpg"), _getPath("flower.jpg")};
		JApplet applet = new ImageOperationsApplet(images);
		f.getContentPane().add("Center", applet);
		applet.init();
		f.setSize(new Dimension(550,550));
		f.pack();
		f.setVisible(true);
	}

	private String _getPath(String filename) throws FileSystemException {
		return getTestResourceLocation().getDirectory("lib.external").getAbsolutePath()+"/"+filename;
	}

	private static class Window extends JFrame {

		public Window() {
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
		}
	}

}