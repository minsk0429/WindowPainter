import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Screen extends JPanel implements MouseListener, MouseMotionListener {

	public static final int SELECTION = 0;
	public static final int POINT = 1;
	public static final int LINE = 2;
	public static final int CIRCLE = 3;
	public static final int RECTANGLE = 4;
	public static final int ERASER = 5;
	public static final int TEXT = 6;
	public static final int FILL_BUCKET = 7;
	
    private static abstract class AbstractDrawingObject { 
        int drawMode;
        Point startPoint;
        Point endPoint;
        Color color; 
        int stroke; 
        boolean isFilled;
        Color fillColor;
        
        public AbstractDrawingObject(int drawMode, Point startPoint, Point endPoint, Color color, int stroke, boolean isFilled, Color fillColor) {
            this.drawMode = drawMode;
            this.startPoint = new Point(startPoint.x, startPoint.y); 
            this.endPoint = new Point(endPoint.x, endPoint.y);     
            this.color = color;
            this.stroke = stroke;
            this.isFilled = isFilled;
            this.fillColor = fillColor;
        }
        
        public abstract void draw(Graphics2D g);
        public abstract String toString();
        public abstract void saveToStream(DataOutputStream dos) throws IOException;
        
        public abstract boolean contains(Rectangle selectionRect);
        
        public abstract boolean isValid();
        
        public String getDetailedState() {
            String state = "Object Mode: " + this.drawMode + "\n";
            state += "  Start: (" + this.startPoint.x + ", " + this.startPoint.y + ")\n";
            state += "  End: (" + this.endPoint.x + ", " + this.endPoint.y + ")\n";
            state += "  Color (Line/Text): RGB " + this.color.getRGB() + "\n";
            state += "  Color (Fill): RGB " + this.fillColor.getRGB() + "\n";
            state += "  Stroke: " + this.stroke + " | Filled: " + this.isFilled;
            return state;
        }
        
        public void translate(int dx, int dy) {
            this.startPoint.translate(dx, dy);
            this.endPoint.translate(dx, dy);
        }
    }
    
    private static class DrawLine extends AbstractDrawingObject {
        public DrawLine(Point startPoint, Point endPoint, Color color, int stroke) {
            super(Screen.LINE, startPoint, endPoint, color, stroke, false, Color.WHITE);
        }
        
        @Override
        public void draw(Graphics2D g) {
            g.setStroke(new BasicStroke(this.stroke));
            g.setColor(this.color);
            g.drawLine(this.startPoint.x, this.startPoint.y, this.endPoint.x, this.endPoint.y);
        }
        
        @Override
        public String toString() {
            return String.format("LINE: Start(%d,%d), End(%d,%d), Color(%d), Stroke(%d)",
                startPoint.x, startPoint.y, endPoint.x, endPoint.y, color.getRGB(), stroke);
        }
        
        @Override
        public void saveToStream(DataOutputStream dos) throws IOException {
            int mode = this.drawMode;
            int startX = this.startPoint.x;
            int startY = this.startPoint.y;
            int endX = this.endPoint.x;
            int endY = this.endPoint.y;
            int lineColorRGB = this.color.getRGB();
            int lineStroke = this.stroke;
            boolean isLineFilled = this.isFilled;
            int fillColorValue = this.fillColor.getRGB();

            dos.writeInt(mode);
            dos.writeInt(startX);
            dos.writeInt(startY);
            dos.writeInt(endX);
            dos.writeInt(endY);
            dos.writeInt(lineColorRGB);
            dos.writeInt(lineStroke);
            dos.writeBoolean(isLineFilled); 
            dos.writeInt(fillColorValue);
        }
        
        public static AbstractDrawingObject loadFromStream(DataInputStream dis) throws IOException {
             int readMode = dis.readInt();
             int readStartX = dis.readInt();
             int readStartY = dis.readInt();
             int readEndX = dis.readInt();
             int readEndY = dis.readInt();
             Color readColor = new Color(dis.readInt());
             int readStroke = dis.readInt();
             boolean readIsFilled = dis.readBoolean();
             Color readFillColor = new Color(dis.readInt());
             
             Point startP = new Point(readStartX, readStartY);
             Point endP = new Point(readEndX, readEndY);
             
             if (readMode != Screen.LINE) {
                 return null;
             }
             
             return new DrawLine(startP, endP, readColor, readStroke);
        }
        
        @Override
        public boolean contains(Rectangle selectionRect) {
            return selectionRect.contains(this.startPoint) || selectionRect.contains(this.endPoint) || selectionRect.intersects(
                new Rectangle(
                    Math.min(this.startPoint.x, this.endPoint.x), 
                    Math.min(this.startPoint.y, this.endPoint.y), 
                    Math.abs(this.startPoint.x - this.endPoint.x), 
                    Math.abs(this.startPoint.y - this.endPoint.y)
                )
            );
        }

        @Override
        public boolean isValid() {
            if (this.startPoint == null || this.endPoint == null) {
                return false;
            }
            if (this.stroke <= 0 || this.stroke > 100) {
                return false;
            }
            return true;
        }
    }
    
    private static class DrawCircle extends AbstractDrawingObject {
        public DrawCircle(Point startPoint, Point endPoint, Color color, int stroke, boolean isFilled, Color fillColor) {
            super(Screen.CIRCLE, startPoint, endPoint, color, stroke, isFilled, fillColor);
        }
        
        private int getDrawX() { return Math.min(this.startPoint.x, this.endPoint.x); }
        private int getDrawY() { return Math.min(this.startPoint.y, this.endPoint.y); }
        private int getDrawW() { return Math.abs(this.startPoint.x - this.endPoint.x); }
        private int getDrawH() { return Math.abs(this.startPoint.y - this.endPoint.y); }

        @Override
        public void draw(Graphics2D g) {
            int x = this.getDrawX();
            int y = this.getDrawY();
            int w = this.getDrawW();
            int h = this.getDrawH();
            
            if (this.isFilled) {
                g.setColor(this.fillColor);
                g.fillOval(x, y, w, h);
            }
            g.setStroke(new BasicStroke(this.stroke));
            g.setColor(this.color);
            g.drawOval(x, y, w, h);
        }

        @Override
        public String toString() {
            return String.format("CIRCLE: Start(%d,%d), End(%d,%d), Filled(%s), FColor(%d)",
                startPoint.x, startPoint.y, endPoint.x, endPoint.y, isFilled, fillColor.getRGB());
        }

        @Override
        public void saveToStream(DataOutputStream dos) throws IOException {
            int mode = this.drawMode;
            int startX = this.startPoint.x;
            int startY = this.startPoint.y;
            int endX = this.endPoint.x;
            int endY = this.endPoint.y;
            int lineColorRGB = this.color.getRGB();
            int lineStroke = this.stroke;
            boolean isCircleFilled = this.isFilled;
            int fillColorValue = this.fillColor.getRGB();

            dos.writeInt(mode);
            dos.writeInt(startX);
            dos.writeInt(startY);
            dos.writeInt(endX);
            dos.writeInt(endY);
            dos.writeInt(lineColorRGB);
            dos.writeInt(lineStroke);
            dos.writeBoolean(isCircleFilled);
            dos.writeInt(fillColorValue);
        }
        
        public static AbstractDrawingObject loadFromStream(DataInputStream dis) throws IOException {
             int readMode = dis.readInt();
             int sx = dis.readInt();
             int sy = dis.readInt();
             int ex = dis.readInt();
             int ey = dis.readInt();
             Color color = new Color(dis.readInt());
             int stroke = dis.readInt();
             boolean isFilled = dis.readBoolean();
             Color fillColor = new Color(dis.readInt());
             return new DrawCircle(new Point(sx, sy), new Point(ex, ey), color, stroke, isFilled, fillColor);
        }

        @Override
        public boolean contains(Rectangle selectionRect) {
            int x = this.getDrawX();
            int y = this.getDrawY();
            int w = this.getDrawW();
            int h = this.getDrawH();
            return selectionRect.intersects(new Rectangle(x, y, w, h));
        }

        @Override
        public boolean isValid() {
            if (this.stroke <= 0 || this.stroke > 100) {
                return false;
            }
            if (this.startPoint.equals(this.endPoint)) {
                return false;
            }
            return true;
        }
    }
    
    private static class DrawRectangle extends AbstractDrawingObject {
        public DrawRectangle(Point startPoint, Point endPoint, Color color, int stroke, boolean isFilled, Color fillColor) {
            super(Screen.RECTANGLE, startPoint, endPoint, color, stroke, isFilled, fillColor);
        }
        
        private int getDrawX() { return Math.min(this.startPoint.x, this.endPoint.x); }
        private int getDrawY() { return Math.min(this.startPoint.y, this.endPoint.y); }
        private int getDrawW() { return Math.abs(this.startPoint.x - this.endPoint.x); }
        private int getDrawH() { return Math.abs(this.startPoint.y - this.endPoint.y); }
        
        @Override
        public void draw(Graphics2D g) {
            int x = this.getDrawX();
            int y = this.getDrawY();
            int w = this.getDrawW();
            int h = this.getDrawH();
            
            if (this.isFilled) {
                g.setColor(this.fillColor);
                g.fillRect(x, y, w, h);
            }
            g.setStroke(new BasicStroke(this.stroke));
            g.setColor(this.color);
            g.drawRect(x, y, w, h);
        }

        @Override
        public void saveToStream(DataOutputStream dos) throws IOException {
            int mode = this.drawMode;
            int startX = this.startPoint.x;
            int startY = this.startPoint.y;
            int endX = this.endPoint.x;
            int endY = this.endPoint.y;
            int lineColorRGB = this.color.getRGB();
            int lineStroke = this.stroke;
            boolean isRectFilled = this.isFilled;
            int fillColorValue = this.fillColor.getRGB();
            
            dos.writeInt(mode);
            dos.writeInt(startX);
            dos.writeInt(startY);
            dos.writeInt(endX);
            dos.writeInt(endY);
            dos.writeInt(lineColorRGB);
            dos.writeInt(lineStroke);
            dos.writeBoolean(isRectFilled);
            dos.writeInt(fillColorValue);
        }
        
        public static AbstractDrawingObject loadFromStream(DataInputStream dis) throws IOException {
             int readMode = dis.readInt();
             int sx = dis.readInt();
             int sy = dis.readInt();
             int ex = dis.readInt();
             int ey = dis.readInt();
             Color color = new Color(dis.readInt());
             int stroke = dis.readInt();
             boolean isFilled = dis.readBoolean();
             Color fillColor = new Color(dis.readInt());
             return new DrawRectangle(new Point(sx, sy), new Point(ex, ey), color, stroke, isFilled, fillColor);
        }

        @Override
        public String toString() {
            return String.format("RECT: Start(%d,%d), End(%d,%d), Filled(%s), FColor(%d)",
                startPoint.x, startPoint.y, endPoint.x, endPoint.y, isFilled, fillColor.getRGB());
        }

        @Override
        public boolean contains(Rectangle selectionRect) {
            int x = this.getDrawX();
            int y = this.getDrawY();
            int w = this.getDrawW();
            int h = this.getDrawH();
            return selectionRect.intersects(new Rectangle(x, y, w, h));
        }

        @Override
        public boolean isValid() {
            if (this.stroke < 0 || this.stroke > 100) {
                return false;
            }
            return true;
        }
    }
    
    private static class DrawText extends AbstractDrawingObject {
        String text;
        int fontSize;
        
        public DrawText(Point location, Color color, String text, int fontSize) {
            super(Screen.TEXT, location, location, color, 0, false, Color.WHITE);
            this.text = text;
            this.fontSize = fontSize;
        }
        
        @Override
        public void draw(Graphics2D g) {
            g.setColor(this.color);
            g.setFont(new Font("맑은 고딕", Font.PLAIN, this.fontSize));
            g.drawString(this.text, this.startPoint.x, this.startPoint.y);
        }
        
        @Override
        public void saveToStream(DataOutputStream dos) throws IOException {
            int mode = this.drawMode;
            int startX = this.startPoint.x;
            int startY = this.startPoint.y;
            int lineColorRGB = this.color.getRGB();
            String textContent = this.text;
            int textFontSize = this.fontSize;
            boolean isTextFilled = this.isFilled;
            int fillColorValue = this.fillColor.getRGB();
            
            dos.writeInt(mode);
            dos.writeInt(startX);
            dos.writeInt(startY);
            dos.writeInt(lineColorRGB);
            dos.writeUTF(textContent);
            dos.writeInt(textFontSize);
            dos.writeBoolean(isTextFilled); 
            dos.writeInt(fillColorValue);
        }

        public static AbstractDrawingObject loadFromStream(DataInputStream dis) throws IOException {
             int readMode = dis.readInt();
             int sx = dis.readInt();
             int sy = dis.readInt();
             Color color = new Color(dis.readInt());
             String text = dis.readUTF();
             int fontSize = dis.readInt();
             dis.readBoolean();
             Color fillColor = new Color(dis.readInt());
             return new DrawText(new Point(sx, sy), color, text, fontSize);
        }

        @Override
        public String toString() {
            return String.format("TEXT: Loc(%d,%d), Text('%s'), Size(%d)",
                startPoint.x, startPoint.y, text, fontSize);
        }
        
        @Override
        public boolean contains(Rectangle selectionRect) {
            return selectionRect.contains(this.startPoint);
        }
        
        @Override
        public void translate(int dx, int dy) {
            this.startPoint.translate(dx, dy);
        }

        @Override
        public boolean isValid() {
            if (this.fontSize < 8 || this.fontSize > 72) {
                return false;
            }
            if (this.text == null || this.text.trim().isEmpty()) {
                return false;
            }
            return true;
        }
    }

	private Graphics bufferGraphics;
	private Image offscreen;	
	private Dimension dim;
	
	private LinkedList<Point> mouseList = new LinkedList<>(); 
	private LinkedList<Color> mouseColorList = new LinkedList<>(); 
	private LinkedList<AbstractDrawingObject> drawingList = new LinkedList<>(); 
	
	private int drawMode;
	private Point startPoint = new Point();
	private Point endPoint = new Point();
	private Point oldPoint = new Point();
	private Color currentColor = Color.BLACK;
	private Color currentFillColor = Color.WHITE;
	private int currentStroke = 1; 
	private boolean currentFill = false;
	private int currentEraserSize = 10;
	private int currentFontSize = 16;
    private JTextField textField = null;
    private Point currentTextLocation;
    
    private Rectangle currentSelectionRect = null;
    private Point selectionStartPoint = null;
    private LinkedList<AbstractDrawingObject> selectedObjects = new LinkedList<>();
    private boolean isDraggingSelection = false;
    
    private BufferedImage currentImage = null;
    private float zoomLevel = 1.0f;
    private boolean isDebugMode = false;

	public Screen() {
        setLayout(null);
        setPreferredSize(new Dimension(800, 600));
        setOpaque(true);

		addMouseListener(this);
		addMouseMotionListener(this);
		setDrawMode(Screen.LINE);
	}
	
	public void initBufferd() {	
		 dim = getSize();	
		 setBackground(Color.white);	
		 offscreen = createImage(dim.width,dim.height);
		 bufferGraphics = offscreen.getGraphics(); 
         
         bufferGraphics.setColor(Color.white);
         bufferGraphics.fillRect(0, 0, dim.width, dim.height);
         
         if (dim.width > 0 && dim.height > 0) {
             if (currentImage == null || currentImage.getWidth() != dim.width || currentImage.getHeight() != dim.height) {
                currentImage = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
             }
         }
	}
    
    private void logDebugMessage(String message) {
        if (isDebugMode) {
            String fullMessage = "[DEBUG] [Mode: " + this.drawMode + "] " + message;
            System.out.println(fullMessage);
        }
    }
    
    private Point getActualPoint(int screenX, int screenY) {
        int actualX = (int)(screenX / zoomLevel);
        int actualY = (int)(screenY / zoomLevel);
        Point resultPoint = new Point(actualX, actualY);
        return resultPoint;
    }
    
    private Point getScreenPoint(int actualX, int actualY) {
        int screenX = (int)(actualX * zoomLevel);
        int screenY = (int)(actualY * zoomLevel);
        Point resultPoint = new Point(screenX, screenY);
        return resultPoint;
    }

	@Override
	public void paintComponent(Graphics g) {
        super.paintComponent(g); 
        
        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight()); 
        
        if (bufferGraphics == null) {
            initBufferd();
        }
		
		Graphics2D g2dBuffer = (Graphics2D)bufferGraphics; 
		
		g2dBuffer.setTransform(AffineTransform.getScaleInstance(zoomLevel, zoomLevel));
		
		logDebugMessage("--- Starting Paint Cycle (Objects: " + drawingList.size() + ") ---");
        
		for(int i=0; i < mouseList.size(); i++) {
			Point currentPoint = mouseList.get(i);
            int pointX = currentPoint.x;
            int pointY = currentPoint.y;

            Color drawingColor = Color.BLACK;

			if(i < mouseColorList.size()) {
				drawingColor = mouseColorList.get(i);
			} 
            
            Color currentColorForG = drawingColor;
            Color currentColorForBuffer = drawingColor;

            g.setColor(currentColorForG);
            bufferGraphics.setColor(currentColorForBuffer);
			
			bufferGraphics.fillOval(pointX, pointY, 10, 10);
            logDebugMessage(String.format("Painting Point at (%d, %d)", pointX, pointY));
		}
        
		for(AbstractDrawingObject obj : drawingList) {
            if (obj.isValid()) {
                obj.draw(g2dBuffer);
                logDebugMessage("Drawing Object Type: " + obj.drawMode);
            } else {
                logDebugMessage("Skipping invalid object: " + obj.drawMode + " | State: " + obj.getDetailedState());
            }
		}

		g.drawImage(offscreen,0,0,(int)(dim.width * zoomLevel), (int)(dim.height * zoomLevel), this);
        
        g2dBuffer.setTransform(new AffineTransform());
        
        if (offscreen != null && currentImage != null) {
            Graphics gImage = currentImage.getGraphics();
            gImage.drawImage(offscreen, 0, 0, null);
            gImage.dispose();
        }
        
        if (currentSelectionRect != null) {
            g.setColor(Color.BLACK);
            Graphics2D g2dMain = (Graphics2D)g;
            
            float[] dash = {5.0f};
            Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
            g2dMain.setStroke(dashed);
            
            int rectX = (int)(currentSelectionRect.x * zoomLevel);
            int rectY = (int)(currentSelectionRect.y * zoomLevel);
            int rectW = (int)(currentSelectionRect.width * zoomLevel);
            int rectH = (int)(currentSelectionRect.height * zoomLevel);

            logDebugMessage(String.format("Drawing Selection Box at (%d, %d) size %d x %d", rectX, rectY, rectW, rectH));

            g2dMain.drawRect(rectX, rectY, rectW, rectH);
            
            g2dMain.setStroke(new BasicStroke(1)); 
        }
        logDebugMessage("--- Paint Cycle Finished ---");
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if( e.getButton() == MouseEvent.BUTTON1) {
            
            Point actualPoint = getActualPoint(e.getX(), e.getY());
            
			if (textField != null) {
                saveTextField();
                return;
            }
            
			if(this.drawMode == Screen.SELECTION) {
			    selectionStartPoint = actualPoint;
                currentSelectionRect = new Rectangle(selectionStartPoint.x, selectionStartPoint.y, 0, 0);
                
                if (!selectedObjects.isEmpty() && currentSelectionRect.contains(selectedObjects.get(0).startPoint)) {
                     isDraggingSelection = true;
                     logDebugMessage("Entering Selection Drag Mode.");
                } else {
                     selectedObjects.clear();
                     isDraggingSelection = false;
                     logDebugMessage("Starting New Selection.");
                }
                oldPoint = actualPoint;
                repaint();
                return;
			}
			else if(this.drawMode == Screen.FILL_BUCKET) {
			    if (currentImage != null) {
			        int x = actualPoint.x;
			        int y = actualPoint.y;
			        
			        if (isValid(x, y)) {
			            int targetColorRGB = currentImage.getRGB(x, y);
			            int replacementRGB = currentFillColor.getRGB();
			            
			            if (targetColorRGB != replacementRGB) {
			                logDebugMessage(String.format("Starting Flood Fill at (%d, %d). Target Color: %d", x, y, targetColorRGB));
			                floodFill(x, y, targetColorRGB, replacementRGB);
			                offscreen = currentImage; 
			                repaint();
			            } else {
                            logDebugMessage("Fill target color already matches replacement color.");
                        }
			        } else {
                        logDebugMessage("Fill location is outside image bounds.");
                    }
			    }
			    return;
			}
			else if(this.drawMode == Screen.POINT) {
			    logDebugMessage(String.format("Point Mode: Initializing point at (%d, %d)", actualPoint.x, actualPoint.y));
			}
			else if(this.drawMode == Screen.LINE || this.drawMode == Screen.CIRCLE || this.drawMode == Screen.RECTANGLE) {
				startPoint = actualPoint;
				endPoint = actualPoint;
				oldPoint = actualPoint;
                logDebugMessage(String.format("Starting draw mode %d at (%d, %d)", drawMode, actualPoint.x, actualPoint.y));
			}
			else if(this.drawMode == Screen.TEXT) {
                currentTextLocation = actualPoint;
                logDebugMessage(String.format("Starting Text Mode at (%d, %d)", actualPoint.x, actualPoint.y));
                
                textField = new JTextField();
                textField.setFont(new Font("맑은 고딕", Font.PLAIN, currentFontSize));
                textField.setBounds((int)(currentTextLocation.x * zoomLevel), (int)(currentTextLocation.y * zoomLevel) - currentFontSize, 100, currentFontSize + 5); 
                
                textField.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        saveTextField();
                    }
                });
                
                add(textField);
                textField.requestFocusInWindow();
                revalidate();
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if( e.getButton() == MouseEvent.BUTTON1) {
			if(this.drawMode == Screen.SELECTION) {
                
                Point actualPoint = getActualPoint(e.getX(), e.getY());
                
                if (!isDraggingSelection) {
                    currentSelectionRect.setBounds(
                        Math.min(selectionStartPoint.x, actualPoint.x),
                        Math.min(selectionStartPoint.y, actualPoint.y),
                        Math.abs(selectionStartPoint.x - actualPoint.x),
                        Math.abs(selectionStartPoint.y - actualPoint.y)
                    );
                    
                    selectedObjects.clear();
                    Rectangle checkRect = currentSelectionRect;
                    
                    for(AbstractDrawingObject obj : drawingList) {
                        if (obj.contains(checkRect)) {
                            selectedObjects.add(obj);
                        }
                    }
                    
                    if (selectedObjects.isEmpty()) {
                        currentSelectionRect = null;
                        logDebugMessage("Selection Ended: No objects selected.");
                    } else {
                        logDebugMessage("Selection Ended: " + selectedObjects.size() + " objects selected.");
                    }
                }
                
                isDraggingSelection = false;
                repaint();
                return;
            }
			else if(this.drawMode == Screen.POINT || this.drawMode == Screen.ERASER || this.drawMode == Screen.TEXT || this.drawMode == Screen.FILL_BUCKET) {
			}
			else if(this.drawMode == Screen.LINE) {
				endPoint.setLocation(getActualPoint(e.getX(), e.getY()));
				
                Point finalStartPoint = startPoint;
                Point finalEndPoint = endPoint;
                Color lineColor = currentColor;
                int lineStroke = currentStroke;

				AbstractDrawingObject obj = new DrawLine(finalStartPoint, finalEndPoint, lineColor, lineStroke); 
                
                if (obj.isValid()) {
                    drawingList.add(obj); 
                    logDebugMessage("LINE object created and validated.");
                } else {
                    logDebugMessage("LINE object creation failed validation: " + obj.getDetailedState());
                }
				repaint();
			}
			else if(this.drawMode == Screen.CIRCLE) {
				endPoint.setLocation(getActualPoint(e.getX(), e.getY()));
                
                Point finalStartPoint = startPoint;
                Point finalEndPoint = endPoint;
                Color circleColor = currentColor;
                int circleStroke = currentStroke;
                boolean isCircleFilled = currentFill;
                Color circleFillColor = currentFillColor;
                
				AbstractDrawingObject obj = new DrawCircle(finalStartPoint, finalEndPoint, circleColor, circleStroke, isCircleFilled, circleFillColor);
                
                if (obj.isValid()) {
                    drawingList.add(obj); 
                    logDebugMessage("CIRCLE object created and validated.");
                } else {
                    logDebugMessage("CIRCLE object creation failed validation: " + obj.getDetailedState());
                }
				repaint();
			}
			else if(this.drawMode == Screen.RECTANGLE) {
				endPoint.setLocation(getActualPoint(e.getX(), e.getY()));
                
                Point finalStartPoint = startPoint;
                Point finalEndPoint = endPoint;
                Color rectColor = currentColor;
                int rectStroke = currentStroke;
                boolean isRectFilled = currentFill;
                Color rectFillColor = currentFillColor;
                
				AbstractDrawingObject obj = new DrawRectangle(finalStartPoint, finalEndPoint, rectColor, rectStroke, isRectFilled, rectFillColor);
                
                if (obj.isValid()) {
                    drawingList.add(obj); 
                    logDebugMessage("RECTANGLE object created and validated.");
                } else {
                    logDebugMessage("RECTANGLE object creation failed validation: " + obj.getDetailedState());
                }
				repaint();
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {	
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
        
        Point actualPoint = getActualPoint(e.getX(), e.getY());
        
		if (this.drawMode == Screen.SELECTION) {
            int dx = actualPoint.x - oldPoint.x;
            int dy = actualPoint.y - oldPoint.y;
            
            if (isDraggingSelection) {
                for (AbstractDrawingObject obj : selectedObjects) {
                    obj.translate(dx, dy);
                }
                currentSelectionRect.translate(dx, dy);
                oldPoint = actualPoint;
                repaint();
                logDebugMessage(String.format("Selection dragged by (%d, %d)", dx, dy));
                return;
            } else {
                currentSelectionRect.setBounds(
                    Math.min(selectionStartPoint.x, actualPoint.x),
                    Math.min(selectionStartPoint.y, actualPoint.y),
                    Math.abs(selectionStartPoint.x - actualPoint.x),
                    Math.abs(selectionStartPoint.y - actualPoint.y)
                );
                repaint();
                return;
            }
        }
		else if(this.drawMode == Screen.POINT) {
			int x = e.getX();
			int y = e.getY();
			mouseList.add(new Point(x, y));
			mouseColorList.add(currentColor);
			repaint();
		}
		else if(this.drawMode == Screen.ERASER) {
			int x = e.getX();
			int y = e.getY();
			
			Graphics g = getGraphics();
			Color oldColor = g.getColor();
			
			g.setColor(getBackground()); 
			int size = currentEraserSize;
			g.fillRect(x - size/2, y - size/2, size, size); 
			
			g.setColor(oldColor);
		}
		else if(this.drawMode == Screen.LINE) {
            Graphics g = getGraphics();
            Color oldColor = g.getColor();
            endPoint = actualPoint;
            
            g.setXORMode(getBackground());
            
            Point oldScreenStart = getScreenPoint(startPoint.x, startPoint.y);
            Point oldScreenEnd = getScreenPoint(oldPoint.x, oldPoint.y);
            g.drawLine(oldScreenStart.x, oldScreenStart.y, oldScreenEnd.x, oldScreenEnd.y);
            
            Point newScreenEnd = getScreenPoint(endPoint.x, endPoint.y);
            g.drawLine(oldScreenStart.x, oldScreenStart.y, newScreenEnd.x, newScreenEnd.y);
            
            oldPoint = actualPoint;
            g.setColor(oldColor);
		}
		else if(this.drawMode == Screen.CIRCLE) {
            Graphics g = getGraphics();
            Color oldColor = g.getColor();
            endPoint = actualPoint;
            g.setXORMode(getBackground());
            
            Point oldScreenStart = getScreenPoint(startPoint.x, startPoint.y);
            Point oldScreenEnd = getScreenPoint(oldPoint.x, oldPoint.y);
            
            int oldW = oldScreenEnd.x - oldScreenStart.x;
            int oldH = oldScreenEnd.y - oldScreenStart.y;
            int oldX = oldScreenStart.x;
            int oldY = oldScreenStart.y;
            
            g.drawOval(oldX, oldY, oldW, oldH);
            
            Point newScreenEnd = getScreenPoint(endPoint.x, endPoint.y);
            
            int newW = newScreenEnd.x - oldScreenStart.x;
            int newH = newScreenEnd.y - oldScreenStart.y;
            int newX = oldScreenStart.x;
            int newY = oldScreenStart.y;
            
            g.drawOval(newX, newY, newW, newH);
            
            oldPoint = actualPoint;
            g.setColor(oldColor);
		}
		else if(this.drawMode == Screen.RECTANGLE) {
            Graphics g = getGraphics();
            Color oldColor = g.getColor();
            endPoint = actualPoint;
            g.setXORMode(getBackground());
            
            Point oldScreenStart = getScreenPoint(startPoint.x, startPoint.y);
            Point oldScreenEnd = getScreenPoint(oldPoint.x, oldPoint.y);
            
            int oldW = oldScreenEnd.x - oldScreenStart.x;
            int oldH = oldScreenEnd.y - oldScreenStart.y;
            int oldX = oldScreenStart.x;
            int oldY = oldScreenStart.y;
            
            g.drawRect(oldX, oldY, oldW, oldH);
            
            Point newScreenEnd = getScreenPoint(endPoint.x, endPoint.y);
            
            int newW = newScreenEnd.x - oldScreenStart.x;
            int newH = newScreenEnd.y - oldScreenStart.y;
            int newX = oldScreenStart.x;
            int newY = oldScreenStart.y;
            
            g.drawRect(newX, newY, newW, newH);
            
            oldPoint = actualPoint;
            g.setColor(oldColor);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}
	
	private void saveTextField() {
        if (textField != null && !textField.getText().trim().isEmpty()) {
            String text = textField.getText();
            
            AbstractDrawingObject obj = new DrawText(
                currentTextLocation, 
                currentColor, 
                text, 
                currentFontSize
            );
            
            if (obj.isValid()) {
                drawingList.add(obj);
                logDebugMessage("TEXT object finalized and added to list.");
            } else {
                logDebugMessage("TEXT object failed validation on finalize.");
            }
        }
        
        remove(textField);
        revalidate();
        repaint();
        textField = null;
    }
    
    private boolean isValid(int x, int y) {
	    return x >= 0 && y >= 0 && currentImage != null && x < currentImage.getWidth() && y < currentImage.getHeight();
	}
	
	private void floodFill(int x, int y, int targetRGB, int replacementRGB) {
	    if (currentImage == null || targetRGB == replacementRGB) return;
	    
	    logDebugMessage("FloodFill Started.");
	    LinkedList<Point> stack = new LinkedList<>();
	    stack.push(new Point(x, y));
	    
	    while (!stack.isEmpty()) {
	        Point p = stack.pop();
	        int px = p.x;
	        int py = p.y;
	        
            String checkMsg = String.format("Checking pixel (%d, %d)", px, py);
	        if (!isValid(px, py)) { logDebugMessage(checkMsg + ": Invalid bounds."); continue; }
	        
	        int currentRGB = currentImage.getRGB(px, py);
	        
	        if (currentRGB != targetRGB) { logDebugMessage(checkMsg + ": Color mismatch."); continue; }
	        
	        currentImage.setRGB(px, py, replacementRGB);
	        logDebugMessage(String.format("Pixel (%d, %d) recolored.", px, py));
	        
	        stack.push(new Point(px + 1, py));
	        stack.push(new Point(px - 1, py));
	        stack.push(new Point(px, py + 1));
	        stack.push(new Point(px, py - 1));
	    }
        logDebugMessage("FloodFill Finished.");
	}
    
    private BufferedImage getDrawingAsImage() {
        if (dim.width <= 0 || dim.height <= 0) return null;
        logDebugMessage("Generating final BufferedImage for save.");
        
        BufferedImage outputImage = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = outputImage.createGraphics();
        
        g2d.drawImage(offscreen, 0, 0, dim.width, dim.height, null);
        
        Graphics2D g2dBuffer = (Graphics2D)bufferGraphics;
        g2dBuffer.setTransform(AffineTransform.getScaleInstance(1.0, 1.0));
        for(AbstractDrawingObject obj : drawingList) {
            obj.draw(g2dBuffer);
        }
        g2dBuffer.setTransform(AffineTransform.getScaleInstance(zoomLevel, zoomLevel));
        
        g2d.drawImage(offscreen, 0, 0, dim.width, dim.height, null);
        g2d.dispose();
        
        return outputImage;
    }
    
    private void performImageTransform(BufferedImage newImage) {
        if (newImage == null) return;
        
        logDebugMessage("Starting Image Transform Update.");
        
        offscreen = newImage;
        dim = new Dimension(newImage.getWidth(), newImage.getHeight());
        currentImage = newImage;
        
        bufferGraphics = offscreen.getGraphics();
        
        if (getParent() instanceof JFrame) {
            JFrame parent = (JFrame)getParent();
            parent.setSize(dim.width + 50, dim.height + 150);
        }
        
        setSize(dim.width, dim.height);
        setPreferredSize(dim);
        
        if (getParent() != null) {
            getParent().revalidate();
        }
        repaint();
        logDebugMessage("Image transformation complete. New dimensions: " + dim.width + "x" + dim.height);
    }
    
    public void applyFlipHorizontal() {
        if (currentImage == null) return;
        logDebugMessage("Applying Flip Horizontal transformation.");
        
        int width = currentImage.getWidth();
        int height = currentImage.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = currentImage.getRGB(x, y);
                newImage.setRGB(width - 1 - x, y, rgb);
            }
        }
        performImageTransform(newImage);
    }
    
    public void applyFlipVertical() {
        if (currentImage == null) return;
        logDebugMessage("Applying Flip Vertical transformation.");
        
        int width = currentImage.getWidth();
        int height = currentImage.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = currentImage.getRGB(x, y);
                newImage.setRGB(x, height - 1 - y, rgb);
            }
        }
        performImageTransform(newImage);
    }
    
    public void applyRotate90Degrees(boolean clockwise) {
        if (currentImage == null) return;
        logDebugMessage("Applying Rotate 90 Degrees (Clockwise: " + clockwise + ") transformation.");
        
        int width = currentImage.getWidth();
        int height = currentImage.getHeight();
        
        BufferedImage newImage = new BufferedImage(height, width, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = currentImage.getRGB(x, y);
                
                if (clockwise) {
                    newImage.setRGB(height - 1 - y, x, rgb);
                } else {
                    newImage.setRGB(y, width - 1 - x, rgb);
                }
            }
        }
        performImageTransform(newImage);
    }
	
	public void setDrawMode(int drawMode) {
		this.drawMode = drawMode;
        logDebugMessage("Draw Mode set to: " + drawMode);
	}
	
	public void setCurrentColor(Color color) {
		this.currentColor = color;
        logDebugMessage("Line Color set to: " + color.getRGB());
	}
	
	public void setCurrentFillColor(Color color) {
		this.currentFillColor = color;
        logDebugMessage("Fill Color set to: " + color.getRGB());
	}
	
	public void setCurrentStroke(int stroke) {
		this.currentStroke = stroke;
        logDebugMessage("Stroke set to: " + stroke);
	}
	
	public void setCurrentFill(boolean isFilled) {
        this.currentFill = isFilled;
        logDebugMessage("Fill Toggled to: " + isFilled);
    }
    
    public void setEraserSize(int size) {
        this.currentEraserSize = size;
        logDebugMessage("Eraser Size set to: " + size);
    }
    
    public void setCurrentFontSize(int size) {
        this.currentFontSize = size;
        logDebugMessage("Font Size set to: " + size);
    }
    
    public void setZoomLevel(float zoomLevel) {
        this.zoomLevel = zoomLevel;
        logDebugMessage("Zoom Level set to: " + zoomLevel);
        repaint();
    }
    
    public void setDebugMode(boolean isDebug) {
        this.isDebugMode = isDebug;
        logDebugMessage("Debug Mode toggled to: " + isDebug);
    }
	
	public void clearAll() {
		mouseList.clear(); 
		mouseColorList.clear(); 
		initBufferd(); 
		drawingList.clear();
        selectedObjects.clear();
        currentSelectionRect = null;
		repaint();
        logDebugMessage("All drawing lists cleared.");
	}
	
	public void deleteSelection() {
	    drawingList.removeAll(selectedObjects); 
	    selectedObjects.clear();
	    currentSelectionRect = null;
	    repaint();
        logDebugMessage("Deleted selected objects. Repaint requested.");
	}
	
	public void fillSelectedArea(Color color) {
	    if (currentSelectionRect == null || currentImage == null) {
	        logDebugMessage("Fill Selection Failed: No area selected or image is null.");
	        return;
	    }
	    
	    int fillRGB = color.getRGB();
        
        int startX = currentSelectionRect.x;
        int startY = currentSelectionRect.y;
        int endX = currentSelectionRect.x + currentSelectionRect.width;
        int endY = currentSelectionRect.y + currentSelectionRect.height;
        
        logDebugMessage(String.format("Starting Fill Selection (%d, %d) to (%d, %d)", startX, startY, endX, endY));

	    for (int y = startY; y < endY; y++) {
	        for (int x = startX; x < endX; x++) {
	            if (isValid(x, y)) {
	                currentImage.setRGB(x, y, fillRGB);
	            }
	        }
	    }
	    
	    offscreen = currentImage;
	    repaint();
	    logDebugMessage("Fill Selection Completed.");
	}
	
	public void save(String filename) {
		File file = new File(filename);
        logDebugMessage("Starting SAVE operation to: " + filename);
        
		try (
			FileOutputStream fos = new FileOutputStream(file); 
			DataOutputStream dos = new DataOutputStream(fos);
		) {
			
            int mouseCount = mouseList.size();
			dos.writeInt(mouseCount);
            logDebugMessage("Writing " + mouseCount + " Point objects.");
			for(Point point : mouseList) {
                int px = point.x;
                int py = point.y;
				dos.writeInt(px);
				dos.writeInt(py);
                logDebugMessage(String.format("Writing point (%d, %d)", px, py));
			}
			
            int drawCount = drawingList.size();
			dos.writeInt(drawCount);
            logDebugMessage("Writing " + drawCount + " DrawingObjects.");
			for(AbstractDrawingObject obj : drawingList) {
				obj.saveToStream(dos);
                logDebugMessage("Saved object mode: " + obj.drawMode);
			}
            
		} catch (IOException e) {
			e.printStackTrace();
		}
        logDebugMessage("SAVE operation completed.");
	}
    
	public void open(String filename) {
		File file = new File(filename);
        logDebugMessage("Starting OPEN operation from: " + filename);

		try (
			FileInputStream fis = new FileInputStream(file);
			DataInputStream dis = new DataInputStream(fis);
		) {
			
			mouseList.clear();
			mouseColorList.clear(); 
			int pointSize = dis.readInt();
            logDebugMessage("Reading " + pointSize + " Point objects.");

			for(int i=0; i < pointSize; i++) {
				int x = dis.readInt();
				int y = dis.readInt();
				mouseList.add(new Point(x, y));
                
                mouseColorList.add(Color.BLACK); 
			}
			
			drawingList.clear();
			int drawingSize = dis.readInt();
            logDebugMessage("Reading " + drawingSize + " DrawingObjects.");

			for(int i=0; i < drawingSize; i++) {
				int drawMode = dis.readInt();
                AbstractDrawingObject obj = null;
                
                switch(drawMode) {
                    case Screen.LINE:
                        obj = DrawLine.loadFromStream(dis);
                        break;
                    case Screen.CIRCLE:
                        obj = DrawCircle.loadFromStream(dis);
                        break;
                    case Screen.RECTANGLE:
                        obj = DrawRectangle.loadFromStream(dis);
                        break;
                    case Screen.TEXT:
                        obj = DrawText.loadFromStream(dis);
                        break;
                }
                
                if(obj != null) {
                    if (obj.isValid()) {
				        drawingList.add(obj);
                        logDebugMessage("Loaded object mode: " + drawMode + " and validated.");
                    } else {
                        logDebugMessage("Warning: Loaded object mode " + drawMode + " failed validation.");
                    }
                } else {
                    logDebugMessage("Warning: Unknown drawMode encountered: " + drawMode);
                }
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
        logDebugMessage("OPEN operation completed.");
	}
    
    public boolean saveImage(File file, String format) {
        BufferedImage imageToSave = getDrawingAsImage();
        if (imageToSave == null) return false;
        
        logDebugMessage("Saving image file to: " + file.getAbsolutePath());
        
        try {
            ImageIO.write(imageToSave, format, file);
            logDebugMessage("Image save SUCCESS.");
            return true;
        } catch (IOException e) {
            logDebugMessage("Image save FAILED: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean loadImage(File file) {
        logDebugMessage("Loading image file from: " + file.getAbsolutePath());
        try (FileInputStream fis = new FileInputStream(file)) {
            BufferedImage loadedImage = ImageIO.read(fis);
            
            if (loadedImage == null) {
                logDebugMessage("Image read FAILED: Loaded image is null.");
                return false;
            }
            	
            drawingList.clear();
            mouseList.clear();
            mouseColorList.clear();
            
            offscreen = loadedImage;
            dim = new Dimension(loadedImage.getWidth(), loadedImage.getHeight());
            currentImage = loadedImage;
            
            bufferGraphics = offscreen.getGraphics();
            
            if (getParent() instanceof JFrame) {
                JFrame parent = (JFrame)getParent();
                parent.setSize(dim.width + 50, dim.height + 150);
            }
            
            setSize(dim.width, dim.height);
            setPreferredSize(dim);
            
            if (getParent() != null) {
                getParent().revalidate();
            }
            
            logDebugMessage("Image load SUCCESS. New dimensions: " + dim.width + "x" + dim.height);
            return true;
        } catch (IOException e) {
            logDebugMessage("Image load FAILED: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}