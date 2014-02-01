/*
 *  Copyright (C) 2012-2014 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.editor;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.filechooser.*;

import com.snap2d.input.*;

/**
 * @author Brian Groenke
 *
 */
public class EditPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4623244904065662486L;

	public static final Color BACK_FILL = new Color(235, 235, 245);
	public static final int SAVED = 0, CHANGED = 1;

	SpriteData data;
	BufferedImage bi;
	Rectangle imgBounds;
	LinkedList<Point> vertices = new LinkedList<Point>();
	int wt, ht;
	boolean finish;

	private SpriteEditor parent;
	private String loadLoc, dataFileName;
	
	int fileStatus = SAVED;

	public EditPanel(SpriteEditor parent) {
		this.parent = parent;
		InputListener input = new InputListener();
		addMouseListener(input);
		new InputDispatch(false).registerKeyClient(input);
	}

	@Override
	public void paintComponent(Graphics g) {
		g.setColor(BACK_FILL);
		g.fillRect(0, 0, getWidth(), getHeight());

		if(wt == 0 || ht == 0) {
			wt = getWidth(); ht = getHeight();
		}

		if(bi != null) {
			Rectangle pbounds = getBounds();
			int x = (int) Math.max(0, pbounds.getCenterX() - (bi.getWidth() / 2));
			int y = (int) Math.max(0, pbounds.getCenterY() - (bi.getHeight() / 2));
			if(imgBounds == null || x != imgBounds.getX() || y != imgBounds.getY())
				imgBounds = new Rectangle(x, y, bi.getWidth(), bi.getHeight());
			g.drawImage(bi, x, y, null);

			int tx = (int)imgBounds.getX(); int ty = (int)imgBounds.getY();
			Point last = null;
			g.setColor(Color.RED);
			for(Point p:vertices) {
				g.fillOval(p.x + tx - 5/2, p.y + ty - 5/2, 5, 5);
				if(last != null) {
					g.drawLine(last.x + tx, last.y + ty, p.x + tx, p.y + ty);
				}

				last = p;
			}
			
			if(finish) {
				g.drawLine(last.x + tx, last.y + ty, vertices.getFirst().x + tx, vertices.getFirst().y + ty);
			}
		}
	}

	public void load() {
		if(bi != null) {
			int reply = JOptionPane.showConfirmDialog(parent, "Save current sprite data?");
			if(reply == JOptionPane.CANCEL_OPTION)
				return;
			else if(reply == JOptionPane.YES_OPTION)
				save();
		}
		
		clear();
		final JFileChooser jfc = new JFileChooser();
		jfc.setFileFilter(new FileNameExtensionFilter("Images/Sprite-Data", "png", "jpg", "gif", SpriteData.FILE_SUFFIX));
		int resp = jfc.showOpenDialog(this);
		if(resp != JFileChooser.APPROVE_OPTION)
			return;
		File f = jfc.getSelectedFile();
		if(f == null) {
			JOptionPane.showMessageDialog(parent, "No file selected");
			return;
		}

		if(f.getName().endsWith("." + SpriteData.FILE_SUFFIX))
			loadData(f);
		else
			loadImage(f);
		repaint();
	}

	private void loadData(File f) {
		try {
			ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(f));
			data = (SpriteData) objIn.readObject();
			objIn.close();
			String name = f.getName();
			dataFileName = name.substring(0, name.lastIndexOf("."));
			for(Point p:data.vertices)
				vertices.add(p);
			finish = true;
			File img = new File(f.getParent() + File.separator + data.imgName);
			loadImage(img);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void loadImage(File f) {
		try {
			bi = ImageIO.read(f);
			loadLoc = f.getParent();
			if(data == null) {
				data = new SpriteData();
				data.imgName = f.getName();
			}
			Dimension size = new Dimension(Math.max(bi.getWidth(), wt), Math.max(bi.getHeight(), ht));
			setPreferredSize(size);
			parent.updateScrollPane();
			parent.validate();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(parent, "Error occurred when attempting to laod image data:"
					+ e.getMessage(), "I/O Exception", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * @return true on safe execution, false on error.  true does not necessarily mean data was saved.
	 */
	public boolean save() {
		if(!finish) {
			JOptionPane.showMessageDialog(parent, "Missing or incomplete data: unable to save");
			return false;
		}
		
		// if there is an exisiting file, ask to overwrite it - if 'no', show dialog to save
		// new file.  Otherwise, overwrite loaded file using stored file location strings.

		File f = null;
		int resp = JOptionPane.NO_OPTION;
		if(dataFileName != null)
			resp = JOptionPane.showConfirmDialog(parent, "Save and overwrite existing file?");
		if(resp == JOptionPane.CANCEL_OPTION)
			return true;
		else if(resp == JOptionPane.NO_OPTION) {
			final JFileChooser jfc = new JFileChooser(loadLoc);
			jfc.setFileFilter(new FileNameExtensionFilter("Sprite-Data (*.sdat)", SpriteData.FILE_SUFFIX));
			resp = jfc.showSaveDialog(this);
			if(resp != JFileChooser.APPROVE_OPTION)
				return true;
			f = jfc.getSelectedFile();
			f = new File(f + "." + SpriteData.FILE_SUFFIX);
		} else if(resp == JOptionPane.YES_OPTION) {
			f = new File(loadLoc + File.separator + dataFileName + SpriteData.FILE_SUFFIX);
		}
		Point[] parr = new Point[vertices.size()];
		data.vertices = vertices.toArray(parr);
		data.wt = (int) imgBounds.getWidth();
		data.ht = (int) imgBounds.getHeight();
		System.out.println("saving for image " + data.imgName);
		try {
			ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(f));
			objOut.writeObject(data);
			objOut.close();
			JOptionPane.showMessageDialog(parent, "Successfully saved sprite data file");
			fileStatus = SAVED;
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void clear() {
		vertices.clear();
		if(bi != null)
			bi.flush();
		bi = null;
		imgBounds = null;
		dataFileName = null;
		finish = false;
		repaint();
	}

	public class InputListener extends MouseAdapter implements KeyEventClient {

		/**
		 *
		 */
		@Override
		public void mousePressed(MouseEvent arg0) {
			int mx = arg0.getX();
			int my = arg0.getY();
			if(imgBounds != null && imgBounds.contains(mx, my) && !finish) {
				vertices.add(new Point(mx - (int)imgBounds.getX(), (my - (int)imgBounds.getY())));
			}
			repaint();
			fileStatus = CHANGED;
		}
		

		/**
		 *
		 */
		@Override
		public void processKeyEvent(KeyEvent e) {
			if(e.getID() == KeyEvent.KEY_PRESSED && parent.hasFocus()) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_O:
					if(e.isControlDown()) {
						load();
					}
					break;
				case KeyEvent.VK_S:
					if(e.isControlDown()) {
						save();
					}
					break;
				case KeyEvent.VK_Z:
					if(e.isControlDown()) {
						if(finish)
							finish = false;
						else
							vertices.removeLast();
					}
					break;
				case KeyEvent.VK_ENTER:
					if(vertices.size() > 2)
						finish = true;
					else
						JOptionPane.showMessageDialog(parent, "Not enough vertices");
					fileStatus = CHANGED;
					break;
				case KeyEvent.VK_BACK_SPACE:
					if(vertices.size() > 0) {
						int resp = JOptionPane.showConfirmDialog(parent, "Clear all drawn bounds?", "Confirm Clear", 
								JOptionPane.OK_CANCEL_OPTION);
						if(resp == JOptionPane.OK_OPTION) {
							finish = false;
							vertices.clear();
						}
					} else {
						int resp = JOptionPane.showConfirmDialog(parent, "Clear canvas of loaded data?", "Confirm Clear", 
								JOptionPane.OK_CANCEL_OPTION);
						if(resp == JOptionPane.OK_OPTION)
							clear();
					}
					fileStatus = CHANGED;
				}
				repaint();
			}

		}

	}

}
