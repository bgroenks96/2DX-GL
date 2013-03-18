/*
 * Copyright © 2011-2013 Brian Groenke
 * All rights reserved.
 * 
 * This file is not part of the 2DX library.
 */

package bg.x2d.physics;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import javax.swing.*;

import bg.x2d.geo.*;
import bg.x2d.math.*;
import bg.x2d.physics.PhysicsNode.Collision;

/**
 * Test application for the 2DX Physics Engine.
 * 
 * @author Brian Groenke
 * 
 */
public class PhysicsTest extends JApplet {

	private static final long serialVersionUID = -291519786066585781L;

	PhysicsPanel panel;
	JPanel btns;

	volatile boolean running, shouldRender;

	Friction friction;
	Force[] forces = new Force[1];
	StandardPhysics sp;
	Entity rect;
	volatile long sleepTime = 10;
	double mass = 1, collFrac = 0.5;
	int size = 60, ppm = 7;

	@Override
	public void init() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		Vector2f.setDefaultPrecision(8);
		FloatMath.setPrecision(8);

		friction = new Friction(0.0f, 0.0f, new Gravity());
		forces[0] = friction;
	}

	@Override
	public void start() {
		panel = new PhysicsPanel();
		btns = new JPanel();

		add(BorderLayout.CENTER, panel);
		JButton toggle = new JButton("Run");
		toggle.addActionListener(new RunListener());
		JButton settings = new JButton("Settings");
		settings.addActionListener(new SettingsDialog());
		btns.add(toggle);
		btns.add(settings);
		btns.setBackground(Color.GRAY);
		add(BorderLayout.SOUTH, btns);
		panel.init((-getWidth() / 2.0f), (-getHeight() / 2.0f), getWidth(),
				(getHeight() - btns.getPreferredSize().height));
		updateComponents();

		shouldRender = true;
		new Thread(new Runnable() {

			@Override
			public void run() {
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							// force Java to use high-res timer
							Thread.sleep(Long.MAX_VALUE);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

				}).start();
				while (shouldRender) {
					panel.repaint();
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		}).start();
	}

	@Override
	public void stop() {
		shouldRender = false;
		running = false;
		remove(panel);
		remove(btns);
	}

	public void updateComponents() {
		if (rect != null) {
			panel.detach(rect);
		}
		sp = new StandardPhysics(new Vector2f(0, 0), mass);
		PointLD p = (rect != null) ? rect.worldLoc : new PointLD(0, 0);
		rect = new Entity(sp, p, panel.view, size);
		panel.attach(rect);
	}

	class PhysicsPanel extends JPanel {

		private static final long serialVersionUID = -2502921145279012663L;

		WorldView view;
		Set<Entity> entities;

		PhysicsPanel() {
			PanelMouseListener pml = new PanelMouseListener();
			addMouseListener(pml);
			addMouseMotionListener(pml);
		}

		public void init(float minx, float miny, int width, int height) {
			view = new WorldView(minx, miny, minx + width, miny + height);
			entities = new HashSet<Entity>();
		}

		public void attach(final Entity e) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					entities.add(e);
				}

			});
		}

		public void detach(final Entity e) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					entities.remove(e);
				}

			});
		}

		@Override
		public void paintComponent(Graphics g) {
			if (running) {
				updatePhysics();
			} else {
				last = 0;
			}
			Graphics2D g2 = (Graphics2D) g;
			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(0, 0, getWidth(), getHeight());
			for (Entity e : entities) {
				e.draw(g2);
			}
			g2.dispose();
		}

		long last = 0;

		private void updatePhysics() {
			double now = System.nanoTime();
			if (last == 0) {
				last = (long) now;
			}
			if (now - last > 20000000) {
				float secs = (float) ((now - last) / 1.0E9);
				for (Entity e : entities) {
					if (e.ignorePhysicsUpdates) {
						continue;
					}
					PhysicsNode node = e.pnode;
					node.applyForces(secs * ppm, forces);
					Point2D.Float np = node.getVelocity2f().applyTo(
							e.worldLoc.getFloatPoint(), ppm * secs);
					e.setWorldLoc(np.getX(), np.getY());
					checkCollision(e, secs);
				}
				last = (long) now;
			}
		}

		private void checkCollision(Entity e, float time) {
			/*
			 * if(!view.worldContains(e.getWorldBounds())) { e.pnode.collide((float)collFrac);
			 * Rectangle2D rect = view.moveInBounds(e.getWorldBounds()); e.setWorldLoc(rect.getX(),
			 * rect.getY()); }
			 */
			Rectangle2D worldBounds = e.getWorldBounds();
			Rectangle2D inBounds = view.moveInBounds(worldBounds);
			if (inBounds.getX() == e.worldLoc.getX()
					&& inBounds.getY() == e.worldLoc.getY()) {
				return;
			}
			Collision ctype;
			if (worldBounds.getX() != inBounds.getX()) {
				ctype = Collision.Y;
			} else if (worldBounds.getY() != inBounds.getY()) {
				ctype = Collision.X;
			} else {
				ctype = Collision.XY;
			}
			e.pnode.collide((float) collFrac, 0.0f, ctype);
			e.setWorldLoc(inBounds.getX(), inBounds.getY());
		}

		class PanelMouseListener extends MouseAdapter {

			Entity proc;
			int prOffsX, prOffsY, lx, ly;
			boolean drag;
			double mark;

			@Override
			public void mousePressed(MouseEvent arg0) {
				int x = arg0.getX();
				int y = arg0.getY();
				for (Entity e : entities) {
					if (e.getBounds().contains(x, y)) {
						proc = e;
						break;
					}
				}
				if (proc == null) {
					return;
				}
				proc.pnode.setVelocity(new Vector2f(0, 0));
				proc.ignorePhysicsUpdates = true;
				prOffsX = arg0.getX() - proc.getBounds().x;
				prOffsY = arg0.getY() - proc.getBounds().y;
				drag = true;
				mark = System.nanoTime();
				lx = x;
				ly = y;
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				drag = false;
				if (proc == null) {
					return;
				}
				if (running) {
					float xvel = (float) ((arg0.getX() - lx) / ppm / ((System
							.nanoTime() - mark) / 1.0E9));
					float yvel = (float) ((ly - arg0.getY()) / ppm / ((System
							.nanoTime() - mark) / 1.0E9));
					proc.pnode.setVelocity(new Vector2f(xvel, yvel));
				}
				proc.ignorePhysicsUpdates = false;
			}

			@Override
			public void mouseDragged(MouseEvent arg0) {
				if (proc != null) {
					proc.setScreenLoc(arg0.getX() - prOffsX, arg0.getY()
							- prOffsY);
				}
			}

		}
	}

	private class Entity {
		PhysicsNode pnode;
		PointLD worldLoc;
		Point screenLoc;
		WorldView view;

		int size;
		boolean ignorePhysicsUpdates;

		Entity(PhysicsNode node, PointLD worldLoc, WorldView viewport, int size) {
			this.pnode = node;
			this.view = viewport;
			this.size = size;

			this.worldLoc = worldLoc;
			setWorldLoc(worldLoc.getX(), worldLoc.getY());
		}

		public void setWorldLoc(double x, double y) {
			worldLoc.setLocation(x, y);
			screenLoc = view.worldToScreen(worldLoc);
		}

		public void setScreenLoc(int x, int y) {
			screenLoc.setLocation(x, y);
			worldLoc = view.screenToWorld(screenLoc);
		}

		public Rectangle2D.Double getWorldBounds() {
			return new Rectangle2D.Double(worldLoc.getX(), worldLoc.getY(),
					size, size);
		}

		public Rectangle getBounds() {
			return new Rectangle(screenLoc.x, screenLoc.y, size, size);
		}

		public void draw(Graphics2D g) {
			g.setColor(Color.red);
			g.fill(getBounds());
		}
	}

	class WorldView {

		Rectangle screenView;
		float x1, y1, x2, y2;

		WorldView(float xmin, float ymin, float xmax, float ymax) {
			x1 = xmin;
			y1 = ymin;
			x2 = xmax;
			y2 = ymax;
			screenView = new Rectangle(0, 0, Math.round(x2 - x1), Math.round(y2
					- y1));
		}

		public void reposition(float xmin, float ymin, float xmax, float ymax) {
			x1 = xmin;
			y1 = ymin;
			x2 = xmax;
			y2 = ymax;
			screenView.setSize(Math.round(x2 - x1), Math.round(y2 - y1));
		}

		public Point worldToScreen(Point p) {
			float x = (float) p.getX();
			float y = (float) p.getY();
			return new Point(Math.round(x - x1), Math.round((y2 - y1)
					- (y - y1)));
		}

		public PointLD screenToWorld(Point p) {
			float x = (float) p.getX();
			float y = (float) p.getY();
			return new PointLD((x + x1), (y2 - y));
		}

		public boolean worldContains(Rectangle2D r) {
			Rectangle2D.Float worldBounds = new Rectangle2D.Float(x1, y1,
					(x2 - x1), (y2 - y1));
			double xsig = Math.signum(Math.abs(r.getX()) - Math.abs(x1)), ysig = Math
					.signum(Math.abs(r.getY()) - Math.abs(y1));
			return worldBounds.contains(r.getX(), r.getY())
					&& worldBounds.contains(r.getX(), r.getY() + r.getHeight()
							* ysig)
					&& worldBounds.contains(r.getX() + r.getWidth() * xsig,
							r.getY() + r.getHeight() * ysig)
					&& worldBounds.contains(r.getX() + r.getWidth() * xsig,
							r.getY());
		}

		public Rectangle2D.Double moveInBounds(Rectangle2D r) {
			double x = r.getX();
			double y = r.getY();

			Rectangle2D.Double inBounds = new Rectangle2D.Double();
			boolean minSet = false;
			if (x < x1) {
				inBounds.x = x1;
				minSet = true;
			} else {
				inBounds.x = x;
			}
			if (y - r.getHeight() < y1) {
				inBounds.y = y1 + r.getHeight();
				minSet = true;
			} else {
				inBounds.y = y;
			}
			if (x + r.getWidth() > x2) {
				inBounds.x = x2 - r.getWidth();
			} else if (!minSet) {
				inBounds.x = x;
			}
			if (y > y2) {
				inBounds.y = y2;
			} else if (!minSet) {
				inBounds.y = y;
			}
			inBounds.width = r.getWidth();
			inBounds.height = r.getHeight();
			return inBounds;
		}

		public boolean screenContains(Rectangle r) {
			return screenView.contains(r.getX(), r.getY())
					&& screenView.contains(r.getX(), r.getY() + r.getHeight())
					&& screenView.contains(r.getX() + r.getWidth(), r.getY()
							+ r.getHeight())
					&& screenView.contains(r.getX(), r.getY() + r.getHeight());
		}
	}

	class RunListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (running) {
				running = false;
				((JButton) e.getSource()).setText("Run");
			} else {
				running = true;
				((JButton) e.getSource()).setText("Stop");
			}
		}

	}

	class SettingsDialog extends JDialog implements ActionListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5918993610373606958L;

		private static final int TEXT_FIELDS = 10;

		String[] textFieldNames = new String[] { "mass", "ppm", "size",
				"collFrac", "frictionStatic", "frictionKinetic", "sleepTime" };
		JTextField[] textFields = new JTextField[textFieldNames.length];
		JCheckBox antiGrav = new JCheckBox("Anti-gravity");

		SettingsDialog() {
			setTitle("Physics Control");
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

			Box main = Box.createVerticalBox();

			for (int i = 0; i < textFieldNames.length; i++) {
				JPanel p = new JPanel();
				p.setOpaque(false);
				JTextField jtf = new JTextField(TEXT_FIELDS);
				jtf.setName(textFieldNames[i]);
				textFields[i] = jtf;
				p.add(new JLabel(jtf.getName()));
				p.add(jtf);
				main.add(p);
				main.add(Box.createVerticalStrut(10));
			}
			antiGrav.setOpaque(false);
			main.add(antiGrav);

			setContentPane(main);
			addWindowListener(new CloseListener());

			pack();
			setLocationRelativeTo(null);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			for (int i = 0; i < textFields.length; i++) {
				switch (i) {
				case 0:
					textFields[i].setText(String.valueOf(mass));
					break;
				case 1:
					textFields[i].setText(String.valueOf(ppm));
					break;
				case 2:
					textFields[i].setText(String.valueOf(size));
					break;
				case 3:
					textFields[i].setText(String.valueOf(collFrac));
					break;
				case 4:
					textFields[i].setText(String.valueOf(friction.fsc));
					break;
				case 5:
					textFields[i].setText(String.valueOf(friction.fkc));
					break;
				case 6:
					textFields[i].setText(String.valueOf(sleepTime));
				}
			}
			antiGrav.setSelected(forces.length > 1);
			setVisible(true);
		}

		class CloseListener extends WindowAdapter {

			@Override
			public void windowClosing(WindowEvent e) {
				float fsx = 0.0f, fsy = 0.0f;
				for (int i = 0; i < textFields.length; i++) {
					try {
						switch (i) {
						case 0:
							mass = Double.parseDouble(textFields[i].getText());
							break;
						case 1:
							ppm = Integer.parseInt(textFields[i].getText());
							break;
						case 2:
							size = Integer.parseInt(textFields[i].getText());
							break;
						case 3:
							collFrac = Double.parseDouble(textFields[i]
									.getText());
							break;
						case 4:
							fsx = Float.parseFloat(textFields[i].getText());
							break;
						case 5:
							fsy = Float.parseFloat(textFields[i].getText());
							break;
						case 6:
							sleepTime = Long.parseLong(textFields[i].getText());
							break;
						}
					} catch (NumberFormatException nfe) {
						JOptionPane.showMessageDialog(null,
								"Variable setting did not complete properly:\n"
										+ nfe.toString(), "Parsing Error",
								JOptionPane.WARNING_MESSAGE);
					}
				}

				updateComponents();

				if (antiGrav.isSelected()) {
					forces = new Force[2];
					forces[0] = new GeneralForce(new Vector2f(0.0f,
							-Gravity.STANDARD * (float) mass));
				} else {
					forces = new Force[1];
				}

				friction = new Friction(fsx, fsy, new Gravity());
				forces[forces.length - 1] = friction;
			}
		}

	}
}
